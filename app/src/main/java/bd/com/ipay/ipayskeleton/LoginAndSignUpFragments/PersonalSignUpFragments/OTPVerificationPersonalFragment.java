package bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.PersonalSignUpFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

import bd.com.ipay.ipayskeleton.Activities.SignupOrLoginActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.OTPRequestPersonalSignup;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.OTPResponsePersonalSignup;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.SignupRequestPersonal;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.SignupResponsePersonal;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.BulkSignUp.GetUserDetailsResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.AddToTrustedDeviceRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.AddToTrustedDeviceResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.BulkSignupUserDetailsCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.CustomCountDownTimer;
import bd.com.ipay.ipayskeleton.Utilities.DeviceInfoFactory;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.InvalidInputResponse;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class OTPVerificationPersonalFragment extends Fragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mSignUpTask = null;

    private HttpRequestPostAsyncTask mRequestOTPTask = null;
    private OTPResponsePersonalSignup mOtpResponsePersonalSignup;

    private AddToTrustedDeviceResponse mAddToTrustedDeviceResponse;
    private HttpRequestPostAsyncTask mAddTrustedDeviceTask = null;

    private HttpRequestGetAsyncTask mGetBulkSignupUserDetailsTask = null;
    private GetUserDetailsResponse mGetUserDetailsResponse;

    private Button mActivateButton;
    private Button mResendOTPButton;
    private EditText mOTPEditText;
    private TextView mTimerTextView;

    private String mDeviceID;
    private String mDeviceName;

    private CustomProgressDialog mProgressDialog;
    private Tracker mTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_otp_verification_for_personal);
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_personal_otp_verification));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_otp_verification, container, false);
        mActivateButton = v.findViewById(R.id.buttonVerifyOTP);
        mResendOTPButton = v.findViewById(R.id.buttonResend);
        mTimerTextView = v.findViewById(R.id.txt_timer);
        mOTPEditText = v.findViewById(R.id.otp_edittext);

        Utilities.showKeyboard(getActivity(), mOTPEditText);

        mDeviceID = DeviceInfoFactory.getDeviceId(getActivity());
        mDeviceName = DeviceInfoFactory.getDeviceName();

        mProgressDialog = new CustomProgressDialog(getActivity());

        mResendOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SignupOrLoginActivity.mAccountType == Constants.PERSONAL_ACCOUNT_TYPE) {
                    if (Utilities.isConnectionAvailable(getActivity())) resendOTP();
                    else if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                } else {
                    ((SignupOrLoginActivity) getActivity()).switchToSignupPersonalStepOneFragment();
                }
            }
        });

        mActivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.hideKeyboard(getActivity());
                if (Utilities.isConnectionAvailable(getActivity())) attemptSignUp();
                else if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        });

        mResendOTPButton.setEnabled(false);
        mTimerTextView.setVisibility(View.VISIBLE);
        new CustomCountDownTimer(SignupOrLoginActivity.otpDuration, 500) {

            public void onTick(long millisUntilFinished) {
                mTimerTextView.setText(new SimpleDateFormat("mm:ss").format(new Date(millisUntilFinished)));
            }

            public void onFinish() {
                mResendOTPButton.setEnabled(true);
            }
        }.start();

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void resendOTP() {
        if (SignupOrLoginActivity.mAccountType == Constants.PERSONAL_ACCOUNT_TYPE) {

            if (mRequestOTPTask != null) {
                return;
            }

            mProgressDialog.show();

            OTPRequestPersonalSignup mOtpRequestPersonalSignup = new OTPRequestPersonalSignup(SignupOrLoginActivity.mMobileNumber,
                    Constants.MOBILE_ANDROID + mDeviceID, Constants.PERSONAL_ACCOUNT_TYPE);
            Gson gson = new Gson();
            String json = gson.toJson(mOtpRequestPersonalSignup);
            mRequestOTPTask = new
                    HttpRequestPostAsyncTask(Constants.COMMAND_OTP_VERIFICATION,
                    Constants.BASE_URL_MM + Constants.URL_OTP_REQUEST, json, getActivity(),false

            );
            mRequestOTPTask.mHttpResponseListener = this;
            mRequestOTPTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void attemptSignUp() {
        if (mSignUpTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;

        String otp = mOTPEditText.getText().toString().trim();
        String errorMessage = InputValidator.isValidOTP(getActivity(), otp);
        if (errorMessage != null) {
            mOTPEditText.requestFocus();
            mOTPEditText.setError(errorMessage);
        } else {
            mProgressDialog.show();
            SignupRequestPersonal mSignupModel = new SignupRequestPersonal(SignupOrLoginActivity.mMobileNumber,
                    Constants.MOBILE_ANDROID + mDeviceID,
                    SignupOrLoginActivity.mName,
                    SignupOrLoginActivity.mBirthday, SignupOrLoginActivity.mPassword, otp,
                    Constants.PERSONAL_ACCOUNT_TYPE,SignupOrLoginActivity.mPromoCode);
            Gson gson = new Gson();
            String json = gson.toJson(mSignupModel);
            mSignUpTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SIGN_UP,
                    Constants.BASE_URL_MM + Constants.URL_SIGN_UP, json, getActivity(),false);
            mSignUpTask.mHttpResponseListener = this;
            mSignUpTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result,getContext(),mProgressDialog) && !result.getApiCommand().equals(Constants.COMMAND_GET_BULK_SIGN_UP_USER_DETAILS)) {
            hideProgressDialog();
            mSignUpTask = null;
            mRequestOTPTask = null;
            return;
        }

        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_SIGN_UP:
                hideProgressDialog();

                try {
                    SignupResponsePersonal signupResponsePersonal = gson.fromJson(result.getJsonString(), SignupResponsePersonal.class);
                    String message = signupResponsePersonal.getMessage();

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        getBulkSignUpUserDetails();
                        Toaster.makeText(getActivity(), signupResponsePersonal.getMessage(), Toast.LENGTH_LONG);

                        // Saving the allowed services id for the user
                        if (signupResponsePersonal.getAccessControlList() != null) {
                            ACLManager.updateAllowedServiceArray(signupResponsePersonal.getAccessControlList());
                        }

                        //Google Analytic event
                        Utilities.sendSuccessEventTracker(mTracker, "Signup", ProfileInfoCacheManager.getAccountId());

                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                        Utilities.sendBlockedEventTracker(mTracker, "Signup", ProfileInfoCacheManager.getAccountId());
                        getActivity().finish();
                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BAD_REQUEST) {
                        InvalidInputResponse invalidInputResponse = gson.fromJson(result.getJsonString(), InvalidInputResponse.class);
                        String[] errorFields = invalidInputResponse.getErrorFieldNames();
                        String errorMessage = invalidInputResponse.getMessage();
                        if (errorFields != null) {
                            Toast.makeText(getActivity(),
                                    Utilities.getErrorMessageForInvalidInput(errorFields, errorMessage), Toast.LENGTH_LONG).show();
                            Utilities.sendFailedEventTracker(mTracker, "Signup", ProfileInfoCacheManager.getAccountId(), Utilities.getErrorMessageForInvalidInput(errorFields, errorMessage));

                        } else {
                            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                            Utilities.sendFailedEventTracker(mTracker, "Signup", ProfileInfoCacheManager.getAccountId(), errorMessage);
                        }
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.login_failed, Toast.LENGTH_LONG).show();

                    //Google Analytic event
                    if (!TextUtils.isEmpty(result.getJsonString()))
                        Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage() + " " + result.getJsonString());
                    else
                        Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage());
                }

                mSignUpTask = null;

                break;

            case Constants.COMMAND_GET_BULK_SIGN_UP_USER_DETAILS:
                try {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mGetUserDetailsResponse = gson.fromJson(result.getJsonString(), GetUserDetailsResponse.class);
                        BulkSignupUserDetailsCacheManager.updateBulkSignupUserInfoCache(mGetUserDetailsResponse);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                attemptAddTrustedDevice();
                mGetBulkSignupUserDetailsTask = null;
                break;

            case Constants.COMMAND_OTP_VERIFICATION:
                hideProgressDialog();

                try {
                    mOtpResponsePersonalSignup = gson.fromJson(result.getJsonString(), OTPResponsePersonalSignup.class);
                    String message = mOtpResponsePersonalSignup.getMessage();

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED ||
                            result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED) {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

                        // Start timer again
                        mTimerTextView.setVisibility(View.VISIBLE);
                        mResendOTPButton.setEnabled(false);
                        new CustomCountDownTimer(SignupOrLoginActivity.otpDuration, 500) {

                            public void onTick(long millisUntilFinished) {
                                mTimerTextView.setText(new SimpleDateFormat("mm:ss").format(new Date(millisUntilFinished)));
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
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.otp_request_failed, Toast.LENGTH_LONG).show();
                }

                mRequestOTPTask = null;

                break;
            case Constants.COMMAND_ADD_TRUSTED_DEVICE:
                hideProgressDialog();

                try {
                    mAddToTrustedDeviceResponse = gson.fromJson(result.getJsonString(), AddToTrustedDeviceResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        ProfileInfoCacheManager.setMobileNumber(SignupOrLoginActivity.mMobileNumber);
                        ProfileInfoCacheManager.setUserName(SignupOrLoginActivity.mName);
                        ProfileInfoCacheManager.setName(SignupOrLoginActivity.mName);
                        ProfileInfoCacheManager.setBirthday(SignupOrLoginActivity.mBirthday);
                        ProfileInfoCacheManager.setGender(SignupOrLoginActivity.mGender);
                        ProfileInfoCacheManager.setAccountType(Constants.PERSONAL_ACCOUNT_TYPE);

                        String UUID = mAddToTrustedDeviceResponse.getUUID();
                        ProfileInfoCacheManager.setUUID(UUID);
                        ProfileInfoCacheManager.uploadProfilePicture(false);
                        ProfileInfoCacheManager.uploadIdentificationDocument(false);
                        ProfileInfoCacheManager.addBasicInfo(false);
                        ProfileInfoCacheManager.addSourceOfFund(false);

                        ProfileInfoCacheManager.switchedFromSignup(true);
                        ((SignupOrLoginActivity) getActivity()).switchToProfileCompletionHelperActivity();

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
                Constants.BASE_URL_MM + Constants.URL_ADD_TRUSTED_DEVICE, json, getActivity(),false);
        mAddTrustedDeviceTask.mHttpResponseListener = this;
        mAddTrustedDeviceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getBulkSignUpUserDetails() {
        if (mGetBulkSignupUserDetailsTask != null) {
            return;
        }
        mGetBulkSignupUserDetailsTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BULK_SIGN_UP_USER_DETAILS,
                Constants.BASE_URL_MM + Constants.URL_GET_BULK_SIGN_UP_USER_DETAILS, getActivity(), this, true);
        mGetBulkSignupUserDetailsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}