package bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.PersonalSignUpFragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.OTPRequestPersonalSignup;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.OTPResponsePersonalSignup;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.DeepLinkAction;
import bd.com.ipay.ipayskeleton.Utilities.DeviceInfoFactory;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.InvalidInputResponse;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class SignupPersonalStepOneFragment extends BaseFragment implements HttpResponseListener, DatePickerDialog.OnDateSetListener {
	private HttpRequestPostAsyncTask mRequestOTPTask = null;
	private OTPResponsePersonalSignup mOtpResponsePersonalSignup;

	private EditText mPasswordView;
	private EditText mConfirmPasswordView;
	private EditText mMobileNumberView;
	private EditText mPromoCodeEditText;

	private EditText mNameView;
	private CheckBox mMaleCheckBox;
	private CheckBox mFemaleCheckBox;
	private EditText mBirthdayEditText;
	private EditText mGenderEditText;

	private CustomProgressDialog mProgressDialog;

	private String mDeviceID;

	private DatePickerDialog mDatePickerDialog;

	private String mDOB;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getActivity() != null)
			mTracker = Utilities.getTracker(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mMaleCheckBox.isChecked())
			mMaleCheckBox.setTextColor((Color.WHITE));
		if (mFemaleCheckBox.isChecked())
			mFemaleCheckBox.setTextColor((Color.WHITE));
		if (mTracker != null) {
			Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_personal_signup_step_1));
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_signup_personal_step_one, container, false);

		if (getActivity() == null)
			return v;
		DeepLinkAction mDeepLinkAction = getActivity().getIntent().getParcelableExtra(Constants.DEEP_LINK_ACTION);
		mNameView = v.findViewById(R.id.user_name);
		mPasswordView = v.findViewById(R.id.password);
		mConfirmPasswordView = v.findViewById(R.id.confirm_password);
		mMobileNumberView = v.findViewById(R.id.mobile_number);
		final TextView termsAndPrivacyTextView = v.findViewById(R.id.terms_and_privacy_text_view);

		termsAndPrivacyTextView.setMovementMethod(LinkMovementMethod.getInstance());
		mMobileNumberView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean b) {

				if (!b) {
					if (mMobileNumberView.getText() != null) {
						if (mMobileNumberView.getText() != null) {
							String number = mMobileNumberView.getText().toString();
							if (number.length() == 10 && number.startsWith("1")) {
								String firstPart = number.substring(2, 6);
								String secondPart = number.substring(6, 10);

								mMobileNumberView.setText(String
										.format(Locale.US, "%s-%s-%s",
												number.substring(0, 2), firstPart, secondPart));
							} else if (number.length() == 11 && number.startsWith("0")) {
								String firstPart = number.substring(3, 7);
								String secondPart = number.substring(7, 11);
								mMobileNumberView.setText(String
										.format(Locale.US, "%s-%s-%s",
												number.substring(0, 3), firstPart, secondPart));
							}
						}
					}
				} else {
					if (mMobileNumberView.getText() != null) {
						if (mMobileNumberView.getText() != null) {
							String number = mMobileNumberView.getText().toString();
							number = number.replaceAll("[^0-9]", "");
							mMobileNumberView.setText(number);
						}
					}

				}
			}
		});
		Button mNextButton = v.findViewById(R.id.personal_sign_in_button);
		mMaleCheckBox = v.findViewById(R.id.checkBoxMale);
		mFemaleCheckBox = v.findViewById(R.id.checkBoxFemale);
		mBirthdayEditText = v.findViewById(R.id.birthdayEditText);
		mPromoCodeEditText = v.findViewById(R.id.promoCodeEditText);
		mGenderEditText = v.findViewById(R.id.genderEditText);
		ImageView mCrossButton = v.findViewById(R.id.button_cross);
		Button mLoginButton = v.findViewById(R.id.button_log_in);
		mProgressDialog = new CustomProgressDialog(getActivity());

		mNameView.requestFocus();
		// Enable hyperlinked
		setGenderCheckBoxTextColor(mMaleCheckBox.isChecked(), mFemaleCheckBox.isChecked());

		mDatePickerDialog = Utilities.getDatePickerDialog(getActivity(), null, this, true);
		mBirthdayEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDatePickerDialog.show();
			}
		});

		if (mDeepLinkAction != null && !TextUtils.isEmpty(mDeepLinkAction.getQueryParameter())) {
			mPromoCodeEditText.setText(mDeepLinkAction.getQueryParameter());
			mPromoCodeEditText.setEnabled(false);
		}

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
		mDeviceID = DeviceInfoFactory.getDeviceId(getActivity());

		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Utilities.isConnectionAvailable(getActivity())) attemptRequestOTP();
				else if (getActivity() != null)
					Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
			}
		});

		mCrossButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getActivity() instanceof SignupOrLoginActivity) {
					((SignupOrLoginActivity) getActivity()).switchToTourActivity();
				}
			}
		});

		mLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getActivity() instanceof SignupOrLoginActivity) {
					((SignupOrLoginActivity) getActivity()).switchToLoginFragment();
				}
			}
		});

		return v;
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
		// Reset errors.
		mNameView.setError(null);
		mMobileNumberView.setError(null);
		mPasswordView.setError(null);

		String name = mNameView.getText().toString().trim();

		// Store values at the time of the login attempt.
		SignupOrLoginActivity.mName = name;
		SignupOrLoginActivity.mBirthday = mDOB;

		// Store values at the time of the login attempt.
		SignupOrLoginActivity.mPassword = mPasswordView.getText().toString().trim();
		SignupOrLoginActivity.mPromoCode = mPromoCodeEditText.getText().toString().trim();
		SignupOrLoginActivity.mMobileNumber = ContactEngine.formatMobileNumberBD(mMobileNumberView.getText().toString().trim());
		SignupOrLoginActivity.mAccountType = Constants.PERSONAL_ACCOUNT_TYPE;
		// Check for a valid password, if the user entered one.
		String passwordValidationMsg = InputValidator.isPasswordValid(SignupOrLoginActivity.mPassword);

		boolean cancel = false;
		View focusView = null;

		if (name.length() == 0) {
			mNameView.setError(getString(R.string.error_invalid_first_name));
			focusView = mNameView;
			cancel = true;

		} else if (!InputValidator.isValidNameWithRequiredLength(name)) {
			mNameView.setError(getString(R.string.error_invalid_name_with_required_length));
			focusView = mNameView;
			cancel = true;

		} else if (!InputValidator.isValidMobileNumberBD(mMobileNumberView.getText().toString())) {
			mMobileNumberView.setError(getString(R.string.error_invalid_mobile_number));
			focusView = mMobileNumberView;
			cancel = true;

		} else if (passwordValidationMsg.length() > 0) {
			mPasswordView.setError(passwordValidationMsg);
			focusView = mPasswordView;
			cancel = true;

		} else if (!mConfirmPasswordView.getText().toString().trim().equals(SignupOrLoginActivity.mPassword) && mConfirmPasswordView.getVisibility() == View.VISIBLE) {
			mConfirmPasswordView.setError(getString(R.string.confirm_password_not_matched));
			focusView = mConfirmPasswordView;
			cancel = true;

		} else if (SignupOrLoginActivity.mBirthday == null || SignupOrLoginActivity.mBirthday.length() == 0) {
			mBirthdayEditText.setError(getString(R.string.error_invalid_birthday));
			focusView = mBirthdayEditText;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			if (focusView != null) focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mProgressDialog.show();
			OTPRequestPersonalSignup mOtpRequestPersonalSignup = new OTPRequestPersonalSignup(SignupOrLoginActivity.mMobileNumber,
					Constants.MOBILE_ANDROID + mDeviceID, Constants.PERSONAL_ACCOUNT_TYPE);
			Gson gson = new Gson();
			String json = gson.toJson(mOtpRequestPersonalSignup);
			mRequestOTPTask = new HttpRequestPostAsyncTask(Constants.COMMAND_OTP_VERIFICATION,
					Constants.BASE_URL_MM + Constants.URL_OTP_REQUEST, json, getActivity(), false);
			mRequestOTPTask.mHttpResponseListener = this;
			mRequestOTPTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {

		if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
			mProgressDialog.dismiss();
			mRequestOTPTask = null;
			return;
		}

		Gson gson = new Gson();

		if (result.getApiCommand().equals(Constants.COMMAND_OTP_VERIFICATION)) {

			String message;
			try {
				mOtpResponsePersonalSignup = gson.fromJson(result.getJsonString(), OTPResponsePersonalSignup.class);
				message = mOtpResponsePersonalSignup.getMessage();
			} catch (Exception e) {
				e.printStackTrace();
				message = getString(R.string.server_down);
			}

			if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
				if (getActivity() != null)
					Toast.makeText(getActivity(), R.string.otp_going_to_send, Toast.LENGTH_LONG).show();

				SignupOrLoginActivity.otpDuration = mOtpResponsePersonalSignup.getOtpValidFor();
				((SignupOrLoginActivity) getActivity()).switchToOTPVerificationPersonalFragment();

				//Google Analytic event
				if (mTracker != null) {
					Utilities.sendSuccessEventTracker(mTracker, "Signup to OTP", ProfileInfoCacheManager.getAccountId());
				}

			} else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED) {
				if (getActivity() != null)
					Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

				// Previous OTP has not been expired yet
				SignupOrLoginActivity.otpDuration = mOtpResponsePersonalSignup.getOtpValidFor();
				((SignupOrLoginActivity) getActivity()).switchToOTPVerificationPersonalFragment();

			} else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED) {
				if (getActivity() != null)
					Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

				// Previous OTP has not been expired yet
				SignupOrLoginActivity.otpDuration = mOtpResponsePersonalSignup.getOtpValidFor();
				((SignupOrLoginActivity) getActivity()).switchToOTPVerificationPersonalFragment();

			} else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BAD_REQUEST) {
				InvalidInputResponse invalidInputResponse = gson.fromJson(result.getJsonString(), InvalidInputResponse.class);
				String[] errorFields = invalidInputResponse.getErrorFieldNames();
				String errorMessage = invalidInputResponse.getMessage();
				Toast.makeText(getActivity(),
						Utilities.getErrorMessageForInvalidInput(errorFields, errorMessage), Toast.LENGTH_LONG).show();
				if (mTracker != null) {
					Utilities.sendFailedEventTracker(mTracker, "Signup to OTP", ProfileInfoCacheManager.getAccountId(), Utilities.getErrorMessageForInvalidInput(errorFields, errorMessage));
				}
			} else {
				if (getActivity() != null)
					Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
				//Google Analytic event
				if (mTracker != null) {
					Utilities.sendFailedEventTracker(mTracker, "Signup to OTP", ProfileInfoCacheManager.getAccountId(), "Failed");
				}
			}

			mProgressDialog.dismiss();
			mRequestOTPTask = null;
		}
	}

	@Override
	public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

		final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
		mDOB = String.format(Locale.US, "%s/%s/%s", getFormattedDate(dayOfMonth),
				getFormattedDate(month+1), Integer.toString(year));
		final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE, dd MMMM, yyyy", Locale.getDefault());
		mBirthdayEditText.setError(null);
		mBirthdayEditText.setText(simpleDateFormat.format(calendar.getTime()));
	}

	private String getFormattedDate(int value) {
		 if(value<10) {
			 return String.format(Locale.US,"0%d",value);
		 } else {
		 	return Integer.toString(value);
		 }
	}
}