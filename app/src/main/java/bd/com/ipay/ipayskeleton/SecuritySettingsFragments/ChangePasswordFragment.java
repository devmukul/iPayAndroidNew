package bd.com.ipay.ipayskeleton.SecuritySettingsFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationChangePasswordDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.ChangeCredentials.ChangePasswordValidationRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.ChangeCredentials.ChangePasswordValidationResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.FingerPrintAuthenticationManager.FingerprintAuthenticationDialog;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class ChangePasswordFragment extends BaseFragment implements HttpResponseListener {
    private HttpRequestPutAsyncTask mChangePasswordValidationTask = null;
    private ChangePasswordValidationResponse mChangePasswordValidationResponse;

    private CustomProgressDialog mProgressDialog;

    private EditText mEnterCurrentPasswordEditText;
    private EditText mEnterNewPasswordEditText;
    private EditText mEnterConfirmNewPasswordEditText;
    private Button mChangePasswordButton;

    private String mPassword;
    private String mNewPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_change_password, container, false);
        setTitle();


        mEnterCurrentPasswordEditText = (EditText) v.findViewById(R.id.current_password);
        mEnterNewPasswordEditText = (EditText) v.findViewById(R.id.new_password);
        mEnterConfirmNewPasswordEditText = (EditText) v.findViewById(R.id.confirm_new_password);

        mChangePasswordButton = (Button) v.findViewById(R.id.save_pass);

        mProgressDialog = new CustomProgressDialog(getActivity());

        mEnterCurrentPasswordEditText.requestFocus();

        mChangePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptChangePasswordValidation();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_change_password));
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void attemptChangePasswordValidation() {
        if (mChangePasswordValidationTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;

        String passwordValidationMsg = InputValidator.isPasswordValid(mEnterNewPasswordEditText.getText().toString().trim());
        String currentPasswordValidationMsg = InputValidator.isPasswordValid(mEnterCurrentPasswordEditText.getText().toString().trim());

        if (currentPasswordValidationMsg.length() > 0) {
            mEnterCurrentPasswordEditText.setError(currentPasswordValidationMsg);
            focusView = mEnterCurrentPasswordEditText;
            cancel = true;

        } else if (passwordValidationMsg.length() > 0) {
            mEnterNewPasswordEditText.setError(passwordValidationMsg);
            focusView = mEnterNewPasswordEditText;
            cancel = true;

        } else if (!(mEnterNewPasswordEditText.getText().toString()
                .equals(mEnterConfirmNewPasswordEditText.getText().toString()))) {
            mEnterConfirmNewPasswordEditText.setError(getString(R.string.confirm_password_not_matched));
            focusView = mEnterConfirmNewPasswordEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Hiding keyboard after save button pressed in change password
            Utilities.hideKeyboard(getActivity());

            mNewPassword = mEnterNewPasswordEditText.getText().toString().trim();
            mPassword = mEnterCurrentPasswordEditText.getText().toString().trim();
            mProgressDialog.show();
            ChangePasswordValidationRequest mChangePasswordValidationRequest = new ChangePasswordValidationRequest(mPassword, mNewPassword);
            Gson gson = new Gson();
            String json = gson.toJson(mChangePasswordValidationRequest);
            mChangePasswordValidationTask = new HttpRequestPutAsyncTask(Constants.COMMAND_CHANGE_PASSWORD_VALIDATION,
                    Constants.BASE_URL_MM + Constants.URL_CHANGE_PASSWORD, json, getActivity(), false);
            mChangePasswordValidationTask.mHttpResponseListener = this;
            mChangePasswordValidationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    private void saveNewPasswordWithTouchID() {
        FingerprintAuthenticationDialog fingerprintAuthenticationDialog = new FingerprintAuthenticationDialog(getContext(),
                FingerprintAuthenticationDialog.Stage.FINGERPRINT_ENCRYPT);
        fingerprintAuthenticationDialog.setFinishEncryptionCheckerListener(new FingerprintAuthenticationDialog.FinishEncryptionCheckerListener() {
            @Override
            public void ifEncryptionFinished() {
                if (ProfileInfoCacheManager.ifPasswordEncrypted()) {
                    ProfileInfoCacheManager.setFingerprintAuthenticationStatus(true);
                } else
                    ProfileInfoCacheManager.setFingerprintAuthenticationStatus(false);

                ((SecuritySettingsActivity) getActivity()).switchToAccountSettingsFragment();
            }
        });
    }

    public void setTitle() {
        getActivity().setTitle(R.string.change_password);
    }

    private void launchOTPVerificationFragment() {
        SecuritySettingsActivity.otpDuration = mChangePasswordValidationResponse.getOtpValidFor();
        new OTPVerificationChangePasswordDialog(getActivity(), mPassword, mNewPassword);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mChangePasswordValidationTask = null;
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_CHANGE_PASSWORD_VALIDATION)) {

            try {
                mChangePasswordValidationResponse = gson.fromJson(result.getJsonString(), ChangePasswordValidationResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), mChangePasswordValidationResponse.getMessage(), Toast.LENGTH_LONG).show();
                        DialogUtils.showChangePasswordSuccessDialog(getActivity());
                    }

                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED) {
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), mChangePasswordValidationResponse.getMessage(), Toast.LENGTH_LONG).show();
                        launchOTPVerificationFragment();
                    }
                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                    if (getActivity() != null)
                        ((MyApplication) getActivity().getApplication()).launchLoginPage(mChangePasswordValidationResponse.getMessage());
                    Utilities.sendBlockedEventTracker(mTracker, "Change Password", ProfileInfoCacheManager.getAccountId());

                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), mChangePasswordValidationResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.change_pass_failed, Toast.LENGTH_LONG).show();
                Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage());

            }

            mProgressDialog.dismiss();
            mChangePasswordValidationTask = null;
        }
    }
}

