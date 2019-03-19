package bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.BusinessSignUpFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.SignupOrLoginActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.OTPRequestBusinessSignup;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.OTPResponseBusinessSignup;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.SignupRequestBusiness;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.SignupResponseBusiness;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.AddToTrustedDeviceRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.AddToTrustedDeviceResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.CustomCountDownTimer;
import bd.com.ipay.ipayskeleton.Utilities.DeviceInfoFactory;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.InvalidInputResponse;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class OTPVerificationBusinessFragment extends BaseFragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mSignUpTask = null;

    private HttpRequestPostAsyncTask mRequestOTPTask = null;

    private HttpRequestPostAsyncTask mAddTrustedDeviceTask = null;

    private Button mActivateButton;
    private TextView mResendOTPButton;
    private EditText mOTPEditText;
    private TextView mTimerTextView;

    private String mDeviceID;
    private String mDeviceName;

    private CustomProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_otp_verification, container, false);
        mActivateButton = v.findViewById(R.id.buttonVerifyOTP);
        mResendOTPButton = v.findViewById(R.id.buttonResend);
        mTimerTextView = v.findViewById(R.id.txt_timer);
        mOTPEditText = v.findViewById(R.id.otp_edittext);

        mDeviceID = DeviceInfoFactory.getDeviceId(getActivity());
        mDeviceName = DeviceInfoFactory.getDeviceName();

        mProgressDialog = new CustomProgressDialog(getActivity());

        mResendOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SignupOrLoginActivity.mAccountType == Constants.BUSINESS_ACCOUNT_TYPE) {
                    if (Utilities.isConnectionAvailable(getActivity())) resendOTP();
                    else if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                } else {
                    ((SignupOrLoginActivity) getActivity()).switchToBusinessStepOneFragment();
                }
            }
        });

        mActivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isConnectionAvailable(getActivity())) attemptSignUp();
                else if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        });

        mResendOTPButton.setEnabled(false);
        mTimerTextView.setVisibility(View.VISIBLE);
        new CustomCountDownTimer(SignupOrLoginActivity.otpDuration, 500) {

            public void onTick(long millisUntilFinished) {
                mTimerTextView.setText(new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(millisUntilFinished)));
            }

            public void onFinish() {
                mTimerTextView.setVisibility(View.INVISIBLE);
                mResendOTPButton.setEnabled(true);
            }
        }.start();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_otp_verification_for_business);
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_business_otp_verifications));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void resendOTP() {
        if (SignupOrLoginActivity.mAccountType == Constants.BUSINESS_ACCOUNT_TYPE) {

            if (mRequestOTPTask != null) {
                return;
            }

            mProgressDialog.show();

            OTPRequestBusinessSignup mOtpRequestBusinessSignup = new OTPRequestBusinessSignup
                    (SignupOrLoginActivity.mMobileNumberBusiness,
                            Constants.MOBILE_ANDROID + mDeviceID, Constants.BUSINESS_ACCOUNT_TYPE);
            Gson gson = new Gson();
            String json = gson.toJson(mOtpRequestBusinessSignup);
            mRequestOTPTask = new
                    HttpRequestPostAsyncTask(Constants.COMMAND_OTP_VERIFICATION,
                    Constants.BASE_URL_MM + Constants.URL_OTP_REQUEST_BUSINESS, json, getActivity(), false

            );
            mRequestOTPTask.mHttpResponseListener = this;
            mRequestOTPTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void attemptSignUp() {
        if (mSignUpTask != null) {
            return;
        }

        String otp = mOTPEditText.getText().toString().trim();
        String errorMessage = InputValidator.isValidOTP(getActivity(), otp);
        if (errorMessage != null) {
            mOTPEditText.requestFocus();
            mOTPEditText.setError(errorMessage);
        } else {
            mProgressDialog.show();

            SignupRequestBusiness mSignupBusinessRequest = new SignupRequestBusiness.Builder()
                    .mobileNumber(SignupOrLoginActivity.mMobileNumberBusiness)
                    .deviceId(Constants.MOBILE_ANDROID + mDeviceID)
                    .name(SignupOrLoginActivity.mNameBusiness)
                    .accountType(SignupOrLoginActivity.mAccountType)
                    .dob(SignupOrLoginActivity.mBirthdayBusinessHolder)
                    .password(SignupOrLoginActivity.mPasswordBusiness)
                    .otp(otp)
                    .businessName(SignupOrLoginActivity.mBusinessName)
                    .companyName(SignupOrLoginActivity.mCompanyName)
                    .businessType(SignupOrLoginActivity.mTypeofBusiness)
                    .personalEmail(SignupOrLoginActivity.mEmailBusiness)
                    .personalMobileNumber(SignupOrLoginActivity.mMobileNumberPersonal)
                    .businessAddress(SignupOrLoginActivity.mAddressBusiness)
                    .personalAddress(SignupOrLoginActivity.mAddressBusinessHolder)
                    .build();

            Gson gson = new Gson();
            String json = gson.toJson(mSignupBusinessRequest);
            mSignUpTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SIGN_UP_BUSINESS,
                    Constants.BASE_URL_MM + Constants.URL_SIGN_UP_BUSINESS, json, getActivity(), false);
            mSignUpTask.mHttpResponseListener = this;
            mSignUpTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            hideProgressDialog();

            mSignUpTask = null;
            mRequestOTPTask = null;

            return;
        }

        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_SIGN_UP_BUSINESS:
                hideProgressDialog();
                try {
                    SignupResponseBusiness signupResponseBusiness = gson.fromJson(result.getJsonString(), SignupResponseBusiness.class);
                    String message = signupResponseBusiness.getMessage();

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        ProfileInfoCacheManager.setMobileNumber(SignupOrLoginActivity.mMobileNumberBusiness);
                        ProfileInfoCacheManager.setName(SignupOrLoginActivity.mNameBusiness);
                        ProfileInfoCacheManager.setBirthday(SignupOrLoginActivity.mBirthdayBusinessHolder);
                        ProfileInfoCacheManager.setGender("M");
                        ProfileInfoCacheManager.setAccountType(Constants.BUSINESS_ACCOUNT_TYPE);


                        // Saving the allowed services id for the user
                        if (signupResponseBusiness.getAccessControlList() != null) {
                            ACLManager.updateAllowedServiceArray(signupResponseBusiness.getAccessControlList());
                        }

                        if (getActivity() != null)
                            Toast.makeText(getActivity(), getString(R.string.signup_successful), Toast.LENGTH_LONG).show();
                        attemptAddTrustedDevice();

                        // TODO: For now, switch to login fragment after a successful sign up. Don't remove it either. Can be used later
//                ((SignupOrLoginActivity) getActivity()).switchToLoginFragment();

                        //Google Analytic event
                        Utilities.sendSuccessEventTracker(mTracker, "Business Signup", ProfileInfoCacheManager.getAccountId());


                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BAD_REQUEST) {
                        InvalidInputResponse invalidInputResponse = gson.fromJson(result.getJsonString(), InvalidInputResponse.class);
                        String[] errorFields = invalidInputResponse.getErrorFieldNames();
                        String errorMessage = invalidInputResponse.getMessage();
                        if (errorFields != null) {
                            Toast.makeText(getActivity(),
                                    Utilities.getErrorMessageForInvalidInput(errorFields, errorMessage), Toast.LENGTH_LONG).show();
                            Utilities.sendFailedEventTracker(mTracker, "Business Signup", ProfileInfoCacheManager.getAccountId(), Utilities.getErrorMessageForInvalidInput(errorFields, errorMessage));
                        } else {
                            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                            Utilities.sendFailedEventTracker(mTracker, "Business Signup", ProfileInfoCacheManager.getAccountId(), errorMessage);
                        }
                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                        Utilities.sendBlockedEventTracker(mTracker, "OTP", ProfileInfoCacheManager.getAccountId());

                        getActivity().finish();
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                        //Google Analytic event
                        Utilities.sendFailedEventTracker(mTracker, "Business Signup", ProfileInfoCacheManager.getAccountId(), message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mProgressDialog.dismiss();
                mSignUpTask = null;

                break;

            case Constants.COMMAND_OTP_VERIFICATION:
                hideProgressDialog();

                try {
                    OTPResponseBusinessSignup mOtpResponseBusinessSignup = gson.fromJson(result.getJsonString(), OTPResponseBusinessSignup.class);
                    String message = mOtpResponseBusinessSignup.getMessage();

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED ||
                            result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED) {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

                        // Start timer again
                        mTimerTextView.setVisibility(View.VISIBLE);
                        mResendOTPButton.setEnabled(false);
                        new CustomCountDownTimer(SignupOrLoginActivity.otpDuration, 500) {

                            public void onTick(long millisUntilFinished) {
                                mTimerTextView.setText(new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(millisUntilFinished)));
                            }

                            public void onFinish() {
                                mResendOTPButton.setEnabled(true);
                            }
                        }.start();
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mProgressDialog.dismiss();
                mRequestOTPTask = null;
                break;
            case Constants.COMMAND_ADD_TRUSTED_DEVICE:
                hideProgressDialog();

                try {
                    AddToTrustedDeviceResponse mAddToTrustedDeviceResponse = gson.fromJson(result.getJsonString(), AddToTrustedDeviceResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        String UUID = mAddToTrustedDeviceResponse.getUUID();
                        ProfileInfoCacheManager.setUUID(UUID);

                        // Launch HomeActivity from here on successful trusted device add
                        ((SignupOrLoginActivity) getActivity()).switchToHomeActivity();
                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_ACCEPTABLE)
                        ((SignupOrLoginActivity) getActivity()).switchToDeviceTrustActivity();
                    else
                        Toast.makeText(getActivity(), mAddToTrustedDeviceResponse.getMessage(), Toast.LENGTH_LONG).show();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), R.string.failed_add_trusted_device, Toast.LENGTH_LONG).show();
                }

                mAddTrustedDeviceTask = null;
                break;
            default:
                hideProgressDialog();

                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void hideProgressDialog() {
        if (isAdded()) mProgressDialog.dismiss();
    }

    private void attemptAddTrustedDevice() {
        if (mAddTrustedDeviceTask != null)
            return;

        AddToTrustedDeviceRequest mAddToTrustedDeviceRequest = new AddToTrustedDeviceRequest(mDeviceName,
                Constants.MOBILE_ANDROID + mDeviceID, null);
        Gson gson = new Gson();
        String json = gson.toJson(mAddToTrustedDeviceRequest);
        mAddTrustedDeviceTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_TRUSTED_DEVICE,
                Constants.BASE_URL_MM + Constants.URL_ADD_TRUSTED_DEVICE, json, getActivity(), true);
        mAddTrustedDeviceTask.mHttpResponseListener = this;
        mAddTrustedDeviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}