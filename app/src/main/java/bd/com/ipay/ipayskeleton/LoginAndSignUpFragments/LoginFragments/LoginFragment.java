package bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.LoginFragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.SignupOrLoginActivity;
import bd.com.ipay.ipayskeleton.Activities.WebViewActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.NotificationApi.RegisterFCMTokenToServerAsyncTask;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LoginRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LoginResponse;
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
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.DeviceInfoFactory;
import bd.com.ipay.ipayskeleton.Utilities.FingerPrintAuthenticationManager.FingerPrintAuthenticationManager;
import bd.com.ipay.ipayskeleton.Utilities.FingerPrintAuthenticationManager.FingerprintAuthenticationDialog;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TokenManager;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class LoginFragment extends BaseFragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mLoginTask = null;
    private LoginResponse mLoginResponseModel;

    private AddToTrustedDeviceResponse mAddToTrustedDeviceResponse;
    private HttpRequestPostAsyncTask mAddTrustedDeviceTask = null;

    private ProfileImageView mProfileImageView;
    private EditText mUserNameEditText;
    private EditText mPasswordEditText;
    private Button mButtonLogin;
    private Button mButtonForgetPassword;
    private Button mButtonJoinUs;
    private String mPasswordLogin;
    private String mUserNameLogin;
    private ImageView mInfoView;
    private CheckBox mRememberMeCheckbox;

    private CustomProgressDialog mProgressDialog;
    private boolean tryLogInWithTouchID = false;
    private FingerprintAuthenticationDialog mFingerprintAuthenticationDialog;

    private String mDeviceID;
    private String mDeviceName;

    private HttpRequestGetAsyncTask mGetProfileCompletionStatusTask = null;
    private ProfileCompletionStatusResponse mProfileCompletionStatusResponse;

    private HttpRequestGetAsyncTask mGetProfileInfoTask = null;
    private GetProfileInfoResponse mGetProfileInfoResponse;

    private HttpRequestGetAsyncTask mGetAllAddedCards = null;
    private GetCardResponse mGetCardResponse;

    private HttpRequestGetAsyncTask mGetBulkSignupUserDetailsTask = null;
    private GetUserDetailsResponse mGetUserDetailsResponse;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        mProgressDialog = new CustomProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                getActivity().onBackPressed();
            }
        });

        mDeviceID = DeviceInfoFactory.getDeviceId(getActivity());
        mDeviceName = DeviceInfoFactory.getDeviceName();

        mButtonLogin = v.findViewById(R.id.login_button);
        mButtonForgetPassword = v.findViewById(R.id.forget_password_button);
        mButtonJoinUs = v.findViewById(R.id.join_us_button);
        mProfileImageView = v.findViewById(R.id.profile_picture);
        mUserNameEditText = v.findViewById(R.id.login_mobile_number);
        mPasswordEditText = v.findViewById(R.id.login_password);
        mInfoView = v.findViewById(R.id.login_info);
        mRememberMeCheckbox = v.findViewById(R.id.remember_me_checkbox);

        mRememberMeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SignupOrLoginActivity.isRememberMe = isChecked;
            }
        });

        if (SharedPrefManager.ifContainsUserID()) {
            mButtonJoinUs.setVisibility(View.GONE);
        }

        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileInfoCacheManager.setAccountType(Constants.PERSONAL_ACCOUNT_TYPE);
                ProfileInfoCacheManager.updateBusinessInfoCache(null);
                ProfileInfoCacheManager.saveMainUserBusinessInfo(Utilities.getMainBusinessProfileInfoString(Constants.ACCOUNT_INFO_DEFAULT));
                ProfileInfoCacheManager.setSwitchAccount(Constants.ACCOUNT_DEFAULT);
                ProfileInfoCacheManager.setOnAccountId(Constants.ON_ACCOUNT_ID_DEFAULT);
                TokenManager.setOnAccountId(Constants.ON_ACCOUNT_ID_DEFAULT);
                ProfileInfoCacheManager.updateProfileInfoCache(Utilities.getMainUserInfoFromJsonString(ProfileInfoCacheManager.getMainUserProfileInfo()));
                ProfileInfoCacheManager.setSwitchAccount(Constants.ACCOUNT_DEFAULT);
                // Hiding the keyboard after login button pressed
                Utilities.hideKeyboard(getActivity());
                if (Utilities.isConnectionAvailable(getActivity())) attemptLogin();
                else if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        });

        mButtonForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(getActivity(), WebViewActivity.class);
                    intent.putExtra("url", Constants.BASE_URL_WEB + Constants.URL_FORGET_PASSWORD);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), R.string.no_browser_found_error_message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mButtonJoinUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SignupOrLoginActivity) getActivity()).switchToAccountSelectionFragment();
            }
        });

        mInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.login_info)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        if (!ProfileInfoCacheManager.getProfileImageUrl().isEmpty()) {
            Logger.logD("Profile Picture", ProfileInfoCacheManager.getProfileImageUrl());

            mProfileImageView.setAccountPhoto(Constants.BASE_URL_FTP_SERVER +
                    ProfileInfoCacheManager.getProfileImageUrl(), false);
        } else {
            if (ProfileInfoCacheManager.isBusinessAccount())
                mProfileImageView.setProfilePicture(R.drawable.ic_business_logo_round);
            else
                mProfileImageView.setProfilePicture(R.drawable.ic_profile);
        }

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        Utilities.hideKeyboard(getContext(), getView());

        if (mFingerprintAuthenticationDialog != null) {
            mFingerprintAuthenticationDialog.stopFingerprintAuthenticationListener();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_login_page);
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_login));

        /**
         * If UUID exists, it means device was set as trusted before.
         * Set the login username so that user can't change it.
         */
        if (SharedPrefManager.ifContainsUUID()) {
            mPasswordEditText.setText("");
            mPasswordEditText.requestFocus();

            mUserNameEditText.setEnabled(false);
            mInfoView.setVisibility(View.VISIBLE);

            String mobileNumber = ContactEngine.formatMobileNumberBD(ProfileInfoCacheManager.getMobileNumber());
            mUserNameEditText.setText(mobileNumber);
            mButtonJoinUs.setVisibility(View.GONE);

            // Login with fingerprint
            attemptLoginWithTouchID();
        } else {
            mPasswordEditText.setText("");
            mUserNameEditText.setText("");
            mButtonJoinUs.setVisibility(View.VISIBLE);
        }
    }

    private void attemptLoginWithTouchID() {
        FingerPrintAuthenticationManager fingerPrintAuthenticationManager = new FingerPrintAuthenticationManager(getActivity());
        if (fingerPrintAuthenticationManager.ifFingerprintAuthenticationSupported()) {
            // If fingerprint auth option is on
            boolean isFingerPrintAuthOn = ProfileInfoCacheManager.getFingerprintAuthenticationStatus();
            if (isFingerPrintAuthOn) {
                // If Fingerprint option is on and fingerprint is encrypted
                if (ProfileInfoCacheManager.ifPasswordEncrypted()) {
                    mFingerprintAuthenticationDialog = new FingerprintAuthenticationDialog(getActivity()
                            , FingerprintAuthenticationDialog.Stage.FINGERPRINT_DECRYPT);
                    mFingerprintAuthenticationDialog.setFinishDecryptionCheckerListener(new FingerprintAuthenticationDialog.FinishDecryptionCheckerListener() {
                        @Override
                        public void ifDecryptionFinished(String decryptedData) {
                            if (decryptedData != null) {
                                tryLogInWithTouchID = true;
                                mPasswordLogin = decryptedData;
                                attemptLogin();
                            }
                        }
                    });
                }
            }
        }
    }

    private void removeFingerprintAuthentication() {
        tryLogInWithTouchID = false;
        ProfileInfoCacheManager.clearEncryptedPassword();
        showLogInFailedWithFingerPrintAuthDialog();
        Utilities.sendFailedEventTracker(mTracker, "Login", ProfileInfoCacheManager.getAccountId(), "Password Changed Removing FingerPrintAuth");
    }

    private void showLogInFailedWithFingerPrintAuthDialog() {
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(getActivity());
        dialog
                .content(R.string.login_failed_with_touch_id)
                .cancelable(false)
                .positiveText(R.string.ok)
                .show();
    }

    private void attemptLogin() {
        if (mLoginTask != null) {
            return;
        }

        // Reset errors.
        mPasswordEditText.setError(null);

        // Store values at the time of the login attempt.
        String mobileNumber = mUserNameEditText.getText().toString().trim();

        mUserNameLogin = ContactEngine.formatMobileNumberBD(mobileNumber);
        if (!tryLogInWithTouchID)
            mPasswordLogin = mPasswordEditText.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        String passwordValidationMsg = InputValidator.isPasswordValid(mPasswordLogin);
        if (passwordValidationMsg.length() > 0) {
            mPasswordEditText.setError(passwordValidationMsg);
            focusView = mPasswordEditText;
            cancel = true;
        }

        if (!InputValidator.isValidMobileNumberBD(mUserNameEditText.getText().toString().trim())) {
            mUserNameEditText.setError(getString(R.string.error_invalid_mobile_number));
            focusView = mUserNameEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            /**
             * Show a progress spinner, and kick off a background task to perform the user login attempt.
             * Save user's login information while trying to login
             */
            SignupOrLoginActivity.mMobileNumber = mUserNameLogin;
            SignupOrLoginActivity.mPassword = mPasswordLogin;
            SignupOrLoginActivity.mMobileNumberBusiness = mUserNameLogin;
            SignupOrLoginActivity.mPasswordBusiness = mPasswordLogin;

            mProgressDialog.show();

            String UUID = null;
            if (SharedPrefManager.ifContainsUUID()) {
                UUID = ProfileInfoCacheManager.getUUID();
            }
            TokenManager.setOnAccountId(Constants.ON_ACCOUNT_ID_DEFAULT);
            LoginRequest mLoginModel = new LoginRequest(mUserNameLogin, mPasswordLogin,
                    Constants.MOBILE_ANDROID + mDeviceID, UUID, null, null, null, SignupOrLoginActivity.isRememberMe);
            Gson gson = new Gson();
            String json = gson.toJson(mLoginModel);
            mLoginTask = new HttpRequestPostAsyncTask(Constants.COMMAND_LOG_IN,
                    Constants.BASE_URL_MM + Constants.URL_LOGIN, json, getActivity(), false);
            mLoginTask.mHttpResponseListener = this;
            mLoginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog) && !result.getApiCommand().equals(Constants.COMMAND_GET_BULK_SIGN_UP_USER_DETAILS)) {
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
                    switch (result.getStatus()) {
                        case Constants.HTTP_RESPONSE_STATUS_OK:
                            ProfileInfoCacheManager.setLoggedInStatus(true);
                            new RegisterFCMTokenToServerAsyncTask(getContext());
                            ProfileInfoCacheManager.setMobileNumber(mUserNameLogin);
                            ProfileInfoCacheManager.setAccountType(mLoginResponseModel.getAccountType());

                            // When user logs in, we want that by default he would log in to his default account
                            TokenManager.deactivateEmployerAccount();

                            // Saving the allowed services id for the user
                            if (mLoginResponseModel.getAccessControlList() != null) {
                                ACLManager.updateAllowedServiceArray(mLoginResponseModel.getAccessControlList());
                            }

                            // Save Remember me in shared preference
                            if (SignupOrLoginActivity.isRememberMe) {
                                SharedPrefManager.setRememberMeActive(SignupOrLoginActivity.isRememberMe);
                            }

                            // Preference should contain UUID if user logged in before. If not, then launch the DeviceTrust Activity.
                            if (!SharedPrefManager.ifContainsUUID()) {
                                attemptAddTrustedDevice();
                            } else {
                                getProfileInfo();
                            }
                            //Google Analytic event
                            Utilities.sendSuccessEventTracker(mTracker, "Login to Home", ProfileInfoCacheManager.getAccountId());
                            break;
                        case Constants.HTTP_RESPONSE_STATUS_ACCEPTED:
                            hideProgressDialog();

                            if (getActivity() != null)
                                Toast.makeText(getActivity(), mLoginResponseModel.getMessage(), Toast.LENGTH_SHORT).show();

                            // First time login from this device. Verify OTP for secure login
                            SignupOrLoginActivity.otpDuration = mLoginResponseModel.getOtpValidFor();
                            ((SignupOrLoginActivity) getActivity()).switchToOTPVerificationTrustedFragment();

                            //Google Analytic event
                            Utilities.sendSuccessEventTracker(mTracker, "Login to OTP", ProfileInfoCacheManager.getAccountId());

                            break;
                        case Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED:
                            hideProgressDialog();

                            // OTP has not been expired yet
                            if (getActivity() != null)
                                Toast.makeText(getActivity(), mLoginResponseModel.getMessage(), Toast.LENGTH_SHORT).show();

                            // Enter previous OTP
                            SignupOrLoginActivity.otpDuration = mLoginResponseModel.getOtpValidFor();
                            ((SignupOrLoginActivity) getActivity()).switchToOTPVerificationTrustedFragment();
                            //Google Analytic event
                            Utilities.sendSuccessEventTracker(mTracker, "Login to OTP", ProfileInfoCacheManager.getAccountId());
                            break;
                        case Constants.HTTP_RESPONSE_STATUS_UNAUTHORIZED:

                            /*
                             * Two situation might arise here. Wrong user name or password throws 401
                             * Login request from an untrusted device with invalid UUID throws 401 too.
                             * We need to handle both case. In case of wrong username or password just showing the response message is enough.
                             */
                            if (mLoginResponseModel.getMessage().contains(Constants.DEVICE_IS_NOT_TRUSTED)) {
                                /*
                                 *  Logged in from an untrusted device with invalid UUID.
                                 *  Remove the saved UUID and send the login request again.
                                 */
                                ProfileInfoCacheManager.removeUUID();

                                // Attempt login
                                mLoginTask = null;
                                attemptLogin();
                            } else {
                                hideProgressDialog();

                                if (!tryLogInWithTouchID) {
                                    if (getActivity() != null)
                                        Toast.makeText(getActivity(), mLoginResponseModel.getMessage(), Toast.LENGTH_SHORT).show();
                                    Utilities.sendFailedEventTracker(mTracker, "Login", ProfileInfoCacheManager.getAccountId(), mLoginResponseModel.getMessage());
                                } else
                                    removeFingerprintAuthentication();
                            }
                            break;
                        case Constants.HTTP_RESPONSE_STATUS_BLOCKED:
                            hideProgressDialog();

                            Toast.makeText(getActivity(), mLoginResponseModel.getMessage(), Toast.LENGTH_LONG).show();
                            Utilities.sendBlockedEventTracker(mTracker, "Login", ProfileInfoCacheManager.getAccountId());
                            break;
                        default:
                            hideProgressDialog();

                            if (!tryLogInWithTouchID) {
                                if (getActivity() != null)
                                    Toast.makeText(getActivity(), mLoginResponseModel.getMessage(), Toast.LENGTH_SHORT).show();
                                Utilities.sendFailedEventTracker(mTracker, "Login", ProfileInfoCacheManager.getAccountId(), mLoginResponseModel.getMessage());
                            } else
                                removeFingerprintAuthentication();
                            break;
                    }
                } catch (Exception e) {
                    hideProgressDialog();

                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.login_failed, Toast.LENGTH_SHORT).show();
                    Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage());
                }

                mLoginTask = null;
                break;
            case Constants.COMMAND_ADD_TRUSTED_DEVICE:

                try {
                    mAddToTrustedDeviceResponse = gson.fromJson(result.getJsonString(), AddToTrustedDeviceResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        String UUID = mAddToTrustedDeviceResponse.getUUID();
                        ProfileInfoCacheManager.setUUID(UUID);
                        getProfileInfo();
                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_ACCEPTABLE) {
                        hideProgressDialog();
                        ((SignupOrLoginActivity) getActivity()).switchToDeviceTrustActivity();
                    }
                    else {
                        hideProgressDialog();
                        Toast.makeText(getActivity(), mAddToTrustedDeviceResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    hideProgressDialog();
                    e.printStackTrace();
                    Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), getString(R.string.failed_add_trusted_device));
                    Toast.makeText(getActivity(), R.string.failed_add_trusted_device, Toast.LENGTH_LONG).show();
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
                        Toaster.makeText(getActivity(), R.string.profile_info_get_failed, Toast.LENGTH_SHORT);
                    }
                } catch (Exception e) {
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

                        if (ProfileInfoCacheManager.getAccountType() == Constants.PERSONAL_ACCOUNT_TYPE && !ProfileInfoCacheManager.isAccountVerified() &&
                                (!ProfileInfoCacheManager.isProfilePictureUploaded() || !ProfileInfoCacheManager.isIdentificationDocumentUploaded()
                                || !ProfileInfoCacheManager.isBasicInfoAdded() || !ProfileInfoCacheManager.isSourceOfFundAdded())) {
                            ((SignupOrLoginActivity) getActivity()).switchToProfileCompletionHelperActivity();
                        } else {
                            ((SignupOrLoginActivity) getActivity()).switchToHomeActivity();
                        }
                    } else {
                        if (getActivity() != null)
                            ((SignupOrLoginActivity) getActivity()).switchToHomeActivity();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        ((SignupOrLoginActivity) getActivity()).switchToHomeActivity();
                }
                mGetProfileCompletionStatusTask = null;
                break;


            default:
                hideProgressDialog();
                Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), "Internal Error(Client Side)");
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void hideProgressDialog() {
        mProgressDialog.dismiss();
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

    private void getProfileCompletionStatus() {

        mProgressDialog.show();

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