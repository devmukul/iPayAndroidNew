package bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.LoginFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.NotificationApi.RegisterFCMTokenToServerAsyncTask;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LoginRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LoginResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.OTPRequestTrustedDevice;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.OTPResponseTrustedDevice;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.BulkSignUp.GetUserDetailsResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetProfileInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionStatusResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.AddToTrustedDeviceRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TrustedDevice.AddToTrustedDeviceResponse;
import bd.com.ipay.ipayskeleton.Model.GetCardResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.BulkSignupUserDetailsCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.CustomCountDownTimer;
import bd.com.ipay.ipayskeleton.Utilities.DeviceInfoFactory;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class OTPVerificationTrustFragment extends BaseFragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mLoginTask = null;
    private LoginResponse mLoginResponseModel;

    private HttpRequestPostAsyncTask mRequestOTPTask = null;
    private OTPResponseTrustedDevice mOTPResponseTrustedDevice;

    private AddToTrustedDeviceResponse mAddToTrustedDeviceResponse;
    private HttpRequestPostAsyncTask mAddTrustedDeviceTask = null;

    private HttpRequestGetAsyncTask mGetProfileCompletionStatusTask = null;
    private ProfileCompletionStatusResponse mProfileCompletionStatusResponse;

    private HttpRequestGetAsyncTask mGetProfileInfoTask = null;
    private GetProfileInfoResponse mGetProfileInfoResponse;

    private HttpRequestGetAsyncTask mGetAllAddedCards = null;
    private GetCardResponse mGetCardResponse;

    private HttpRequestGetAsyncTask mGetBulkSignupUserDetailsTask = null;
    private GetUserDetailsResponse mGetUserDetailsResponse;

    private Button mActivateButton;
    private EditText mOTPEditText;
    private TextView mTimerTextView;
    private Button mResendOTPButton;

    private String mDeviceID;
    private String mDeviceName;

    private CustomProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_otp_verification_trusted_device, container, false);
        mActivateButton = v.findViewById(R.id.buttonVerifyOTP);
        mResendOTPButton = v.findViewById(R.id.buttonResend);
        mOTPEditText = v.findViewById(R.id.otp_edittext);
        mTimerTextView = v.findViewById(R.id.txt_timer);

        mDeviceID = DeviceInfoFactory.getDeviceId(getActivity());
        mDeviceName = DeviceInfoFactory.getDeviceName();

        mProgressDialog = new CustomProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);

        mActivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiding the keyboard after verifying OTP
                Utilities.hideKeyboard(getActivity());
                if (Utilities.isConnectionAvailable(getActivity())) {
                    attemptLogin(SignupOrLoginActivity.mMobileNumber, SignupOrLoginActivity.mPassword);

                } else if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        });

        mResendOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isConnectionAvailable(getActivity()))
                    resendOTP(SignupOrLoginActivity.mMobileNumber, SignupOrLoginActivity.mPassword);
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

                //mTimerTextView.setVisibility(View.INVISIBLE);
                mResendOTPButton.setEnabled(true);
            }
        }.start();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_otp_verification_for_add_trusted_device);
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_otp_for_login));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Utilities.showKeyboard(getActivity(), mOTPEditText);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void resendOTP(String mUserNameLogin, String mPasswordLogin) {

        if (mRequestOTPTask != null) {
            return;
        }

        mProgressDialog.show();

        OTPRequestTrustedDevice mOTPRequestTrustedDevice = new OTPRequestTrustedDevice
                (mUserNameLogin, mPasswordLogin,
                        Constants.MOBILE_ANDROID + mDeviceID, SignupOrLoginActivity.mAccountType);
        Gson gson = new Gson();
        String json = gson.toJson(mOTPRequestTrustedDevice);
        mRequestOTPTask = new HttpRequestPostAsyncTask(Constants.COMMAND_OTP_VERIFICATION,
                Constants.BASE_URL_MM + Constants.URL_LOGIN, json, getActivity(), false);
        mRequestOTPTask.mHttpResponseListener = this;
        mRequestOTPTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void attemptLogin(String mUserNameLogin, String mPasswordLogin) {
        if (mLoginTask != null) {
            return;
        }
        String otp = mOTPEditText.getText().toString().trim();
        String errorMessage = InputValidator.isValidOTP(getActivity(), otp);
        if (errorMessage != null) {
            mOTPEditText.requestFocus();
            mOTPEditText.setError(errorMessage);
        } else {
            mProgressDialog.show();

            LoginRequest mLoginModel = new LoginRequest(mUserNameLogin, mPasswordLogin,
                    Constants.MOBILE_ANDROID + mDeviceID, null, otp, null, null, SignupOrLoginActivity.isRememberMe);
            Gson gson = new Gson();
            String json = gson.toJson(mLoginModel);
            mLoginTask = new HttpRequestPostAsyncTask(Constants.COMMAND_LOG_IN,
                    Constants.BASE_URL_MM + Constants.URL_LOGIN, json, getActivity(), false);
            mLoginTask.mHttpResponseListener = this;
            mLoginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void getAddedCards() {
        if (mGetAllAddedCards != null) return;
        else {
            mGetAllAddedCards = new HttpRequestGetAsyncTask(Constants.COMMAND_ADD_CARD,
                    Constants.BASE_URL_MM + Constants.URL_GET_CARD, getActivity(), this, true);
            mGetAllAddedCards.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), null) && !result.getApiCommand().equals(Constants.COMMAND_GET_BULK_SIGN_UP_USER_DETAILS)) {
            hideProgressDialog();
            mLoginTask = null;
            mGetAllAddedCards = null;
            mGetProfileCompletionStatusTask = null;
            mGetProfileInfoTask = null;
            return;
        }

        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_LOG_IN:
                try {
                    mLoginResponseModel = gson.fromJson(result.getJsonString(), LoginResponse.class);
                    String message = mLoginResponseModel.getMessage();

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        ProfileInfoCacheManager.setLoggedInStatus(true);

                        ProfileInfoCacheManager.setAccountType(mLoginResponseModel.getAccountType());

                        if (mLoginResponseModel.getAccountType() == Constants.PERSONAL_ACCOUNT_TYPE)
                            ProfileInfoCacheManager.setMobileNumber(SignupOrLoginActivity.mMobileNumber);
                        else
                            ProfileInfoCacheManager.setMobileNumber(SignupOrLoginActivity.mMobileNumberBusiness);

                        String pushRegistrationID = ProfileInfoCacheManager.getPushNotificationToken(null);
                        if (pushRegistrationID != null) {
                            new RegisterFCMTokenToServerAsyncTask(getContext());
                        }

                        // Saving the allowed services id for the user
                        if (mLoginResponseModel.getAccessControlList() != null) {
                            ACLManager.updateAllowedServiceArray(mLoginResponseModel.getAccessControlList());
                        }

                        // Save Remember me in shared preference
                        if (SignupOrLoginActivity.isRememberMe) {
                            SharedPrefManager.setRememberMeActive(SignupOrLoginActivity.isRememberMe);
                        }

                        attemptTrustedDeviceAdd();
                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                        hideProgressDialog();
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                        Utilities.sendBlockedEventTracker(mTracker, "Trusted Device", ProfileInfoCacheManager.getAccountId());

                        getActivity().finish();
                    } else {
                        hideProgressDialog();

                        if (getActivity() != null)
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    hideProgressDialog();

                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.login_failed, Toast.LENGTH_LONG).show();
                }
                mLoginTask = null;
                break;
            case Constants.COMMAND_OTP_VERIFICATION:
                hideProgressDialog();

                try {
                    mOTPResponseTrustedDevice = gson.fromJson(result.getJsonString(), OTPResponseTrustedDevice.class);
                    SignupOrLoginActivity.otpDuration = mOTPResponseTrustedDevice.getOtpValidFor();
                    String message = mOTPResponseTrustedDevice.getMessage();

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

                mRequestOTPTask = null;
                break;
            case Constants.COMMAND_ADD_TRUSTED_DEVICE:

                try {
                    mAddToTrustedDeviceResponse = gson.fromJson(result.getJsonString(), AddToTrustedDeviceResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        String UUID = mAddToTrustedDeviceResponse.getUUID();
                        ProfileInfoCacheManager.setUUID(UUID);

                        // Save Remember me in shared preference
                        if (SignupOrLoginActivity.isRememberMe) {
                            SharedPrefManager.setRememberMeActive(SignupOrLoginActivity.isRememberMe);
                        }

                        //Google Analytic event
                        Utilities.sendSuccessEventTracker(mTracker, "Login to Home", ProfileInfoCacheManager.getAccountId());
                        getProfileInfo();
                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_ACCEPTABLE) {
                        hideProgressDialog();
                        ((SignupOrLoginActivity) getActivity()).switchToDeviceTrustActivity();
                        //Google Analytic event
                        Utilities.sendSuccessEventTracker(mTracker, "Login to Add Trusted Device", ProfileInfoCacheManager.getAccountId());
                    } else {
                        hideProgressDialog();
                        Toast.makeText(getActivity(), mAddToTrustedDeviceResponse.getMessage(), Toast.LENGTH_LONG).show();
                        //Google Analytic event
                        Utilities.sendFailedEventTracker(mTracker, "Login", ProfileInfoCacheManager.getAccountId(), mAddToTrustedDeviceResponse.getMessage());
                    }
                } catch (Exception e) {
                    hideProgressDialog();
                    e.printStackTrace();
                    Toast.makeText(getActivity(), R.string.failed_add_trusted_device, Toast.LENGTH_LONG).show();
                    //Google Analytic event
                    Utilities.sendFailedEventTracker(mTracker, "Login", ProfileInfoCacheManager.getAccountId(), getString(R.string.failed_add_trusted_device));
                }
                mAddTrustedDeviceTask = null;
                break;

            case Constants.COMMAND_GET_PROFILE_INFO_REQUEST:

                try {
                    mGetProfileInfoResponse = gson.fromJson(result.getJsonString(), GetProfileInfoResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        ProfileInfoCacheManager.updateProfileInfoCache(mGetProfileInfoResponse);
                        ProfileInfoCacheManager.saveMainUserProfileInfo(Utilities.getMainUserProfileInfoString(mGetProfileInfoResponse));
                        getBulkSignUpUserDetails();
                    } else {
                        hideProgressDialog();
                        Toaster.makeText(getActivity(), R.string.profile_info_get_failed, Toast.LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    hideProgressDialog();
                    e.printStackTrace();
                    Toaster.makeText(getActivity(), R.string.profile_info_get_failed, Toast.LENGTH_SHORT);
                }
                mGetProfileInfoTask = null;
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
                getProfileCompletionStatus();
                mGetBulkSignupUserDetailsTask = null;
                break;

            case Constants.COMMAND_GET_PROFILE_COMPLETION_STATUS:
                hideProgressDialog();
                try {
                    mProfileCompletionStatusResponse = gson.fromJson(result.getJsonString(), ProfileCompletionStatusResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mProfileCompletionStatusResponse.initScoreFromPropertyName();
                        ProfileInfoCacheManager.switchedFromSignup(false);
                        ProfileInfoCacheManager.uploadProfilePicture(mProfileCompletionStatusResponse.isPhotoUpdated());
                        ProfileInfoCacheManager.uploadIdentificationDocument(mProfileCompletionStatusResponse.isPhotoIdUpdated());
                        ProfileInfoCacheManager.addBasicInfo(mProfileCompletionStatusResponse.isOnboardBasicInfoUpdated());
                        ProfileInfoCacheManager.addSourceOfFund(mProfileCompletionStatusResponse.isBankAdded());

                        if (ProfileInfoCacheManager.isSourceOfFundAdded()) {
                            if (ProfileInfoCacheManager.getAccountType() == Constants.PERSONAL_ACCOUNT_TYPE && !ProfileInfoCacheManager.isAccountVerified() && (!ProfileInfoCacheManager.isProfilePictureUploaded() || !ProfileInfoCacheManager.isIdentificationDocumentUploaded()
                                    || !ProfileInfoCacheManager.isBasicInfoAdded() || !ProfileInfoCacheManager.isSourceOfFundAdded())) {
                                ((SignupOrLoginActivity) getActivity()).switchToProfileCompletionHelperActivity();
                            } else {
                                ((SignupOrLoginActivity) getActivity()).switchToHomeActivity();

                            }
                        } else getAddedCards();
                    } else {
                        if (getActivity() != null) {
                            ((SignupOrLoginActivity) getActivity()).switchToHomeActivity();

                        }
                        hideProgressDialog();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        ((SignupOrLoginActivity) getActivity()).switchToHomeActivity();
                    }
                    hideProgressDialog();
                }
                mGetProfileCompletionStatusTask = null;
                break;

            case Constants.COMMAND_ADD_CARD:
                try {
                    mGetCardResponse = gson.fromJson(result.getJsonString(), GetCardResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                        if (!mGetCardResponse.isAnyCardVerified()) {
                            ProfileInfoCacheManager.addSourceOfFund(false);
                        } else ProfileInfoCacheManager.addSourceOfFund(true);

                        if (ProfileInfoCacheManager.getAccountType() == Constants.PERSONAL_ACCOUNT_TYPE && !ProfileInfoCacheManager.isAccountVerified() && (!ProfileInfoCacheManager.isProfilePictureUploaded() || !ProfileInfoCacheManager.isIdentificationDocumentUploaded()
                                || !ProfileInfoCacheManager.isBasicInfoAdded() || !ProfileInfoCacheManager.isSourceOfFundAdded())) {
                            ((SignupOrLoginActivity) getActivity()).switchToProfileCompletionHelperActivity();
                        } else {
                            ((SignupOrLoginActivity) getActivity()).switchToHomeActivity();
                        }
                    } else {
                        Toaster.makeText(getActivity(), mGetCardResponse.getMessage(), Toast.LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
                }
                mGetAllAddedCards = null;
                break;
            default:
                hideProgressDialog();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void hideProgressDialog() {
        mProgressDialog.dismiss();
    }

    private void attemptTrustedDeviceAdd() {
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

    private void getProfileCompletionStatus() {
        if (mGetProfileCompletionStatusTask != null) {
            return;
        }
        mGetProfileCompletionStatusTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_COMPLETION_STATUS,
                Constants.BASE_URL_MM + Constants.URL_GET_PROFILE_COMPLETION_STATUS, getActivity(), this, true);
        mGetProfileCompletionStatusTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getProfileInfo() {
        if (mGetProfileInfoTask != null) {
            return;
        }
        mGetProfileInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_INFO_REQUEST,
                Constants.BASE_URL_MM + Constants.URL_GET_PROFILE_INFO_REQUEST, getActivity(), true);
        mGetProfileInfoTask.mHttpResponseListener = this;
        mGetProfileInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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