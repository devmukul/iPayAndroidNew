package bd.com.ipay.ipayskeleton.LoginAndSignUpFragments.LoginFragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.SignupOrLoginActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.NotificationApi.RegisterFCMTokenToServerAsyncTask;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LoginRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LoginResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.DeviceInfoFactory;
import bd.com.ipay.ipayskeleton.Utilities.FingerPrintAuthenticationManager.FingerPrintAuthenticationManager;
import bd.com.ipay.ipayskeleton.Utilities.FingerPrintAuthenticationManager.FingerprintAuthenticationDialog;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;
import bd.com.ipay.ipayskeleton.Utilities.TokenManager;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class LoginFragment extends Fragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mLoginTask = null;
    private LoginResponse mLoginResponseModel;

    private ProfileImageView mProfileImageView;
    private EditText mUserNameEditText;
    private EditText mPasswordEditText;
    private Button mButtonLogin;
    private Button mButtonForgetPassword;
    private Button mButtonJoinUs;
    private String mPasswordLogin;
    private String mUserNameLogin;
    private ImageView mInfoView;

    private ProgressDialog mProgressDialog;
    private String mDeviceID;
    private boolean tryLogInWithTouchID = false;
    private FingerprintAuthenticationDialog mFingerprintAuthenticationDialog;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_login_page);
        Utilities.showKeyboard(getActivity());

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

        // Auto Login
        if (SharedPrefManager.ifContainsUserID() && Constants.DEBUG && Constants.AUTO_LOGIN) {
            mPasswordEditText.setText("qqqqqqq1");
            //           mUserNameEditText.setText("+8801677258077");
            attemptLogin();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                getActivity().onBackPressed();
            }
        });

        mDeviceID = DeviceInfoFactory.getDeviceId(getActivity());

        mButtonLogin = (Button) v.findViewById(R.id.login_button);
        mButtonForgetPassword = (Button) v.findViewById(R.id.forget_password_button);
        mButtonJoinUs = (Button) v.findViewById(R.id.join_us_button);
        mProfileImageView = (ProfileImageView) v.findViewById(R.id.profile_picture);
        mUserNameEditText = (EditText) v.findViewById(R.id.login_mobile_number);
        mPasswordEditText = (EditText) v.findViewById(R.id.login_password);
        mInfoView = (ImageView) v.findViewById(R.id.login_info);

        if (SharedPrefManager.ifContainsUserID()) {
            mButtonJoinUs.setVisibility(View.GONE);
        }

        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(Constants.BASE_URL_WEB + Constants.URL_FORGET_PASSWORD));
                startActivity(intent);
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

            mProfileImageView.setProfilePicture(Constants.BASE_URL_FTP_SERVER +
                    ProfileInfoCacheManager.getProfileImageUrl(), false);
        } else {
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

    private void attemptLoginWithTouchID() {
        FingerPrintAuthenticationManager fingerPrintAuthenticationManager = new FingerPrintAuthenticationManager(getActivity());
        if (fingerPrintAuthenticationManager.ifFingerprintAuthenticationSupported()) {
            // If fingerprint auth option is on
            boolean isFingerPrintAuthOn = ProfileInfoCacheManager.getFingerprintAuthenticationStatus(false);
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

        mUserNameLogin = ContactEngine.formatMobileNumberBD(mUserNameEditText.getText().toString().trim());
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

        if (!ContactEngine.isValidNumber(mUserNameLogin)) {
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

            mProgressDialog.setMessage(getString(R.string.progress_dialog_text_logging_in));
            mProgressDialog.show();

            String UUID = null;
            if (SharedPrefManager.ifContainsUUID()) {
                UUID = ProfileInfoCacheManager.getUUID(null);
            }

            LoginRequest mLoginModel = new LoginRequest(mUserNameLogin, mPasswordLogin,
                    Constants.MOBILE_ANDROID + mDeviceID, UUID, null, null, null);
            Gson gson = new Gson();
            String json = gson.toJson(mLoginModel);
            mLoginTask = new HttpRequestPostAsyncTask(Constants.COMMAND_LOG_IN,
                    Constants.BASE_URL_MM + Constants.URL_LOGIN, json, getActivity());
            mLoginTask.mHttpResponseListener = this;
            mLoginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (isAdded())
            mProgressDialog.dismiss();

        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mLoginTask = null;
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_LOG_IN)) {
            try {
                mLoginResponseModel = gson.fromJson(result.getJsonString(), LoginResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    ProfileInfoCacheManager.setLoggedInStatus(true);
                    String pushRegistrationID = ProfileInfoCacheManager.getPushNotificationToken(null);
                    if (pushRegistrationID != null) {
                        new RegisterFCMTokenToServerAsyncTask(getContext());
                    }

                    ProfileInfoCacheManager.setMobileNumber(mUserNameLogin);
                    ProfileInfoCacheManager.setAccountType(mLoginResponseModel.getAccountType());
                    // When user logs in, we want that by default he would log in to his default account
                    TokenManager.deactivateEmployerAccount();

                    // Preference should contain UUID if user logged in before. If not, then launch the DeviceTrust Activity.
                    if (!SharedPrefManager.ifContainsUUID())
                        ((SignupOrLoginActivity) getActivity()).switchToDeviceTrustActivity();
                    else ((SignupOrLoginActivity) getActivity()).switchToHomeActivity();

                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED) {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mLoginResponseModel.getMessage(), Toast.LENGTH_SHORT).show();

                    // First time login from this device. Verify OTP for secure login
                    SignupOrLoginActivity.otpDuration = mLoginResponseModel.getOtpValidFor();
                    ((SignupOrLoginActivity) getActivity()).switchToOTPVerificationTrustedFragment();

                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_ACCEPTABLE) {

                    // OTP has not been expired yet
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mLoginResponseModel.getMessage(), Toast.LENGTH_SHORT).show();

                    // Enter previous OTP
                    SignupOrLoginActivity.otpDuration = mLoginResponseModel.getOtpValidFor();
                    ((SignupOrLoginActivity) getActivity()).switchToOTPVerificationTrustedFragment();

                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_UNAUTHORIZED) {
                    /**
                     * Two situation might arise here. Wrong user name or password throws 401
                     * Login request from an untrusted device with invalid UUID throws 401 too.
                     * We need to handle both case. In case of wrong username or password just showing the response message is enough.
                     */
                    if (mLoginResponseModel.getMessage().contains(Constants.DEVICE_IS_NOT_TRUSTED)) {
                        /**
                         *  Logged in from an untrusted device with invalid UUID.
                         *  Remove the saved UUID and send the login request again.
                         */
                        ProfileInfoCacheManager.removeUUID();

                        // Attempt login
                        mLoginTask = null;
                        attemptLogin();
                    } else {
                        if (!tryLogInWithTouchID) {
                            if (getActivity() != null)
                                Toast.makeText(getActivity(), mLoginResponseModel.getMessage(), Toast.LENGTH_SHORT).show();
                        } else
                            removeFingerprintAuthentication();
                    }
                } else {
                    if (!tryLogInWithTouchID) {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), mLoginResponseModel.getMessage(), Toast.LENGTH_SHORT).show();
                    } else
                        removeFingerprintAuthentication();
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.login_failed, Toast.LENGTH_SHORT).show();
            }

            mLoginTask = null;
        }
    }
}
