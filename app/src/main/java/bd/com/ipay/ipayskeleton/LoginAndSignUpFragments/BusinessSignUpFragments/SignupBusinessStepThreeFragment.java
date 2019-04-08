package bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.BusinessSignUpFragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import bd.com.ipay.ipayskeleton.Activities.SignupOrLoginActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.AddressInputSignUpView;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.OTPRequestBusinessSignup;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.OTPResponseBusinessSignup;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DeviceInfoFactory;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.InvalidInputResponse;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;


public class SignupBusinessStepThreeFragment extends BaseFragment implements HttpResponseListener, DatePickerDialog.OnDateSetListener {
	private HttpRequestPostAsyncTask mRequestOTPTask = null;

	private EditText mBusinessHolderFullNameView;

	private EditText mBirthdayEditText;
	private EditText mGenderEditText;
	private CheckBox mMaleCheckBox;
	private CheckBox mFemaleCheckBox;
	private AddressInputSignUpView mPersonalAddressView;
	private CustomProgressDialog mProgressDialog;

	private String mDeviceID;
	private String mDOB;
	private DatePickerDialog mDatePickerDialog;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_signup_business_step_three, container, false);

		mProgressDialog = new CustomProgressDialog(getActivity());

		mBusinessHolderFullNameView = v.findViewById(R.id.full_name);

		Button mSignupBusinessButton = v.findViewById(R.id.business_sign_in_button);
		mBirthdayEditText = v.findViewById(R.id.birthdayEditText);
		mGenderEditText = v.findViewById(R.id.genderEditText);
		mMaleCheckBox = v.findViewById(R.id.checkBoxMale);
		mFemaleCheckBox = v.findViewById(R.id.checkBoxFemale);
		CheckBox mAddressCheckbox = v.findViewById(R.id.checkboxBusinessAddress);
		mPersonalAddressView = v.findViewById(R.id.personal_address);

		mPersonalAddressView.setHintAddressInput(getString(R.string.address_line_1),
				getString(R.string.address_line_2));
		if (getActivity() != null)
			mDeviceID = DeviceInfoFactory.getDeviceId(getActivity());

		mDatePickerDialog = Utilities.getDatePickerDialog(getActivity(), null, this, true);

		final TextView termsAndPrivacyTextView = v.findViewById(R.id.terms_and_privacy_text_view);

		termsAndPrivacyTextView.setMovementMethod(LinkMovementMethod.getInstance());

		mBirthdayEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDatePickerDialog.show();
			}
		});
		mSignupBusinessButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Utilities.isConnectionAvailable(getActivity())) attemptRequestOTP();
				else if (getActivity() != null)
					Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
			}
		});

		mAddressCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					setAccountHolderAddress();
				} else {
					resetAccountHolderAddress();
				}
			}
		});

		mMaleCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mGenderEditText.setError(null);
				mMaleCheckBox.setChecked(true);
				mFemaleCheckBox.setChecked(false);

				setGenderCheckBoxTextColor(mMaleCheckBox.isChecked(), mFemaleCheckBox.isChecked());
			}
		});

		mFemaleCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mGenderEditText.setError(null);
				mFemaleCheckBox.setChecked(true);
				mMaleCheckBox.setChecked(false);

				setGenderCheckBoxTextColor(mMaleCheckBox.isChecked(), mFemaleCheckBox.isChecked());
			}
		});

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		setGenderCheckBoxTextColor(mMaleCheckBox.isChecked(), mFemaleCheckBox.isChecked());
		Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_business_signup_step_3));
	}

	private void setGenderCheckBoxTextColor(boolean maleCheckBoxChecked, boolean femaleCheckBoxChecked) {
		if (getContext() == null)
			return;
		if (maleCheckBoxChecked)
			mMaleCheckBox.setTextColor((Color.WHITE));
		else
			mMaleCheckBox.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));

		if (femaleCheckBoxChecked)
			mFemaleCheckBox.setTextColor((Color.WHITE));
		else
			mFemaleCheckBox.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
	}

	private void attemptRequestOTP() {
		if (mRequestOTPTask != null) {
			return;
		}

		String businessHoldersName = mBusinessHolderFullNameView.getText().toString().trim();

		// Store values at the time of the login attempt.
		SignupOrLoginActivity.mMobileNumberPersonal = SignupOrLoginActivity.mMobileNumberBusiness;
		SignupOrLoginActivity.mAccountType = Constants.BUSINESS_ACCOUNT_TYPE;
		SignupOrLoginActivity.mBirthdayBusinessHolder = mDOB;
		SignupOrLoginActivity.mNameBusiness = businessHoldersName;

		boolean cancel = false;
		View focusView = null;

		mBirthdayEditText.setError(null);
		mBusinessHolderFullNameView.setError(null);

		if (businessHoldersName.length() == 0) {
			mBusinessHolderFullNameView.setError(getString(R.string.error_invalid_name));
			focusView = mBusinessHolderFullNameView;
			cancel = true;

		} else if (!InputValidator.isValidNameWithRequiredLength(businessHoldersName)) {
			mBusinessHolderFullNameView.setError(getString(R.string.error_invalid_name_with_required_length));
			focusView = mBusinessHolderFullNameView;
			cancel = true;

		} else if (SignupOrLoginActivity.mBirthdayBusinessHolder == null || SignupOrLoginActivity.mBirthdayBusinessHolder.length() == 0) {
			mBirthdayEditText.setError(getString(R.string.error_invalid_birthday));
			focusView = mBirthdayEditText;
			cancel = true;

		} else if (mDOB == null) {
			mBirthdayEditText.setError(getString(R.string.error_invalid_birthday));
			focusView = mBirthdayEditText;
			cancel = true;

		} else if (!mPersonalAddressView.verifyUserInputs()) {
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			if (focusView != null) focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			SignupOrLoginActivity.mAddressBusinessHolder = mPersonalAddressView.getInformation();
			mProgressDialog.show();
			OTPRequestBusinessSignup mOtpRequestBusinessSignup = new OTPRequestBusinessSignup(SignupOrLoginActivity.mMobileNumberBusiness,
					Constants.MOBILE_ANDROID + mDeviceID, Constants.BUSINESS_ACCOUNT_TYPE);
			Gson gson = new Gson();
			String json = gson.toJson(mOtpRequestBusinessSignup);
			mRequestOTPTask = new HttpRequestPostAsyncTask(Constants.COMMAND_OTP_VERIFICATION,
					Constants.BASE_URL_MM + Constants.URL_OTP_REQUEST_BUSINESS, json, getActivity(), false);
			mRequestOTPTask.mHttpResponseListener = this;
			mRequestOTPTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private void setAccountHolderAddress() {
		mPersonalAddressView.setInformation(SignupOrLoginActivity.mAddressBusiness);
	}

	private void resetAccountHolderAddress() {
		mPersonalAddressView.resetInformation();
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {

		if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
			mProgressDialog.dismissDialogue();
			mRequestOTPTask = null;
			return;
		}


		Gson gson = new Gson();

		if (result.getApiCommand().equals(Constants.COMMAND_OTP_VERIFICATION)) {

			String message;
			OTPResponseBusinessSignup mOtpResponseBusinessSignup;
			try {
				mOtpResponseBusinessSignup = gson.fromJson(result.getJsonString(), OTPResponseBusinessSignup.class);
				message = mOtpResponseBusinessSignup.getMessage();
			} catch (Exception e) {
				e.printStackTrace();
				if (getActivity() != null)
					Toast.makeText(getActivity(), R.string.server_down, Toast.LENGTH_LONG).show();
				//Google Analytic event
				Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage());
				return;
			}


			if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
				if (getActivity() != null)
					Toast.makeText(getActivity(), R.string.otp_going_to_send, Toast.LENGTH_LONG).show();

				SignupOrLoginActivity.otpDuration = mOtpResponseBusinessSignup.getOtpValidFor();
				((SignupOrLoginActivity) getActivity()).switchToOTPVerificationBusinessFragment();

				//Google Analytic event
				Utilities.sendSuccessEventTracker(mTracker, "Business Signup to OTP", ProfileInfoCacheManager.getAccountId());

			} else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED) {
				if (getActivity() != null)
					Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
				SignupOrLoginActivity.otpDuration = mOtpResponseBusinessSignup.getOtpValidFor();
				((SignupOrLoginActivity) getActivity()).switchToOTPVerificationBusinessFragment();
			} else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BAD_REQUEST) {
				InvalidInputResponse invalidInputResponse = gson.fromJson(result.getJsonString(), InvalidInputResponse.class);
				String[] errorFields = invalidInputResponse.getErrorFieldNames();
				String errorMessage = invalidInputResponse.getMessage();
				if (errorFields != null) {
					Toast.makeText(getActivity(),
							Utilities.getErrorMessageForInvalidInput(errorFields, errorMessage), Toast.LENGTH_LONG).show();
					Utilities.sendFailedEventTracker(mTracker, "Business Signup to OTP", ProfileInfoCacheManager.getAccountId(), Utilities.getErrorMessageForInvalidInput(errorFields, errorMessage));
				} else {
					Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
					Utilities.sendFailedEventTracker(mTracker, "Business Signup to OTP", ProfileInfoCacheManager.getAccountId(), errorMessage);
				}
			} else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_ACCEPTABLE) {
				if (getActivity() != null)
					Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
			} else {
				if (getActivity() != null)
					Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

				//Google Analytic event
				Utilities.sendFailedEventTracker(mTracker, "Business Signup to OTP", ProfileInfoCacheManager.getAccountId(), message);

			}

			mProgressDialog.dismissDialogue();
			mRequestOTPTask = null;
		}
	}

	@Override
	public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

		final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
		numberFormat.setMinimumIntegerDigits(2);
		numberFormat.setMaximumIntegerDigits(2);
		mDOB = String.format(Locale.US, "%s/%s/%s", numberFormat.format(dayOfMonth),
				numberFormat.format(month+1), Integer.toString(year));
		final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE, dd MMMM, yyyy", Locale.getDefault());
		mBirthdayEditText.setError(null);
		mBirthdayEditText.setText(simpleDateFormat.format(calendar.getTime()));
	}
}


