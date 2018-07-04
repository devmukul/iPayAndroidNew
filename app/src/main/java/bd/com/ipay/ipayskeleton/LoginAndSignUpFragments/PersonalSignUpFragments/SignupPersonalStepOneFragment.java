package bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.PersonalSignUpFragments;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hbb20.CountryCodePicker;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import bd.com.ipay.ipayskeleton.Activities.SignupOrLoginActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
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

public class SignupPersonalStepOneFragment extends BaseFragment implements HttpResponseListener {
    private HttpRequestPostAsyncTask mRequestOTPTask = null;
    private OTPResponsePersonalSignup mOtpResponsePersonalSignup;

    private CountryCodePicker mCountryCodePicker;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private EditText mMobileNumberView;
    private EditText mPromoCodeEditText;

    private EditText mNameView;
    private Button mNextButton;
    private CheckBox mMaleCheckBox;
    private CheckBox mFemaleCheckBox;
    private EditText mBirthdayEditText;
    private EditText mGenderEditText;
    private ImageView mCrossButton;
    private Button mLoginButton;
    private TextView mTermsConditions;
    private TextView mPrivacyPolicy;
    private CheckBox mAgreementCheckBox;

    private ProgressDialog mProgressDialog;

    private String mDeviceID;

    private DatePickerDialog mDatePickerDialog;
    private String mDOB;

    private int mYear;
    private int mMonth;
    private int mDay;
    private String mDayName;

    private DeepLinkAction mDeepLinkAction;

    private final DatePickerDialog.OnDateSetListener mDateSetListener =
        new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear, int dayOfMonth) {
                String[] mMonthArray;
                String birthDate, birthMonth, birthYear;

                mYear = year;
                mMonth = monthOfYear + 1;
                mDay = dayOfMonth;
                mMonthArray = getResources().getStringArray(R.array.month_name);

                if (mDay < 10) birthDate = "0" + mDay;
                else birthDate = mDay + "";
                if (mMonth < 10) birthMonth = "0" + mMonth;
                else birthMonth = mMonth + "";
                birthYear = mYear + "";
                mDOB = birthDate + "/" + birthMonth + "/" + birthYear;
                try {
                    Date date = new SimpleDateFormat("dd/MM/yyyy").parse(mDOB);
                    mDayName = new SimpleDateFormat("EE").format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                mBirthdayEditText.setError(null);
                mBirthdayEditText.setText(mDayName + " , " + mDay + " " + mMonthArray[mMonth - 1] + " , " + mYear);
            }
        };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_signup_personal_page);
        if (mMaleCheckBox.isChecked())
            mMaleCheckBox.setTextColor((Color.WHITE));
        if (mFemaleCheckBox.isChecked())
            mFemaleCheckBox.setTextColor((Color.WHITE));
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_personal_signup_step_1));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_signup_personal_step_one, container, false);

        mDeepLinkAction = getActivity().getIntent().getParcelableExtra(Constants.DEEP_LINK_ACTION);
        mNameView = (EditText) v.findViewById(R.id.user_name);
        mPasswordView = (EditText) v.findViewById(R.id.password);
        mConfirmPasswordView = (EditText) v.findViewById(R.id.confirm_password);
        mCountryCodePicker = (CountryCodePicker) v.findViewById(R.id.ccp);
        mMobileNumberView = (EditText) v.findViewById(R.id.mobile_number);
        mNextButton = (Button) v.findViewById(R.id.personal_sign_in_button);
        mMaleCheckBox = (CheckBox) v.findViewById(R.id.checkBoxMale);
        mFemaleCheckBox = (CheckBox) v.findViewById(R.id.checkBoxFemale);
        mBirthdayEditText = (EditText) v.findViewById(R.id.birthdayEditText);
        mPromoCodeEditText = (EditText) v.findViewById(R.id.promoCodeEditText);
        mGenderEditText = (EditText) v.findViewById(R.id.genderEditText);
        mCrossButton = (ImageView) v.findViewById(R.id.button_cross);
        mLoginButton = (Button) v.findViewById(R.id.button_log_in);
        mTermsConditions = (TextView) v.findViewById(R.id.textViewTermsConditions);
        mPrivacyPolicy = (TextView) v.findViewById(R.id.textViewPrivacyPolicy);
        mAgreementCheckBox = (CheckBox) v.findViewById(R.id.checkBoxTermsConditions);

        mProgressDialog = new ProgressDialog(getActivity());

        mCountryCodePicker.registerCarrierNumberEditText(mMobileNumberView);
        mCountryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                mMobileNumberView.requestFocus();
            }
        });

        mNameView.requestFocus();
        // Enable hyperlinked
        mTermsConditions.setMovementMethod(LinkMovementMethod.getInstance());
        mPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());
        setGenderCheckBoxTextColor(mMaleCheckBox.isChecked(), mFemaleCheckBox.isChecked());
        mDatePickerDialog = Utilities.getDatePickerDialog(getActivity(), null, mDateSetListener);
        mBirthdayEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePickerDialog.show();
            }
        });

        if(mDeepLinkAction !=null && !StringUtils.isEmpty(mDeepLinkAction.getInvitationCode())){
            mPromoCodeEditText.setText(mDeepLinkAction.getInvitationCode());
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
                ((SignupOrLoginActivity) getActivity()).switchToTourActivity();
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SignupOrLoginActivity) getActivity()).switchToLoginFragment();
            }
        });

        return v;
    }

    private void setGenderCheckBoxTextColor(boolean maleCheckBoxChecked, boolean femaleCheckBoxChecked) {
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
        SignupOrLoginActivity.mCountryCode = mCountryCodePicker.getSelectedCountryNameCode();
        SignupOrLoginActivity.mMobileNumber = ContactEngine.formatMobileNumberInternational(mMobileNumberView.getText().toString().trim(),
                SignupOrLoginActivity.mCountryCode);
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

        } else if (!InputValidator.isValidMobileNumberWithCountryCode(mMobileNumberView.getText().toString(), SignupOrLoginActivity.mCountryCode)) {
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

        }else if (!mAgreementCheckBox.isChecked()) {
            cancel = true;
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.please_check_terms_and_conditions, Toast.LENGTH_LONG).show();
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            if (focusView != null) focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mProgressDialog.setMessage(getString(R.string.progress_dialog_requesting));
            mProgressDialog.show();
            OTPRequestPersonalSignup mOtpRequestPersonalSignup = new OTPRequestPersonalSignup(SignupOrLoginActivity.mMobileNumber,
                    Constants.MOBILE_ANDROID + mDeviceID, Constants.PERSONAL_ACCOUNT_TYPE);
            Gson gson = new Gson();
            String json = gson.toJson(mOtpRequestPersonalSignup);
            mRequestOTPTask = new HttpRequestPostAsyncTask(Constants.COMMAND_OTP_VERIFICATION,
                    Constants.BASE_URL_MM + Constants.URL_OTP_REQUEST, json, getActivity(),false);
            mRequestOTPTask.mHttpResponseListener = this;
            mRequestOTPTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result,getContext(),mProgressDialog)) {
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
                Utilities.sendSuccessEventTracker(mTracker, "Signup to OTP", ProfileInfoCacheManager.getAccountId());

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
                Utilities.sendFailedEventTracker(mTracker, "Signup to OTP", ProfileInfoCacheManager.getAccountId(), Utilities.getErrorMessageForInvalidInput(errorFields, errorMessage));

            } else {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                //Google Analytic event
                Utilities.sendFailedEventTracker(mTracker, "Signup to OTP", ProfileInfoCacheManager.getAccountId(), "Failed");
            }

            mProgressDialog.dismiss();
            mRequestOTPTask = null;
        }
    }

}

