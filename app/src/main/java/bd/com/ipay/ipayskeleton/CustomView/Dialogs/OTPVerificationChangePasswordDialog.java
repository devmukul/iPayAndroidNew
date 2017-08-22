package bd.com.ipay.ipayskeleton.CustomView.Dialogs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BroadcastReceivers.EnableDisableSMSBroadcastReceiver;
import bd.com.ipay.ipayskeleton.BroadcastReceivers.SMSReaderBroadcastReceiver;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.ChangeCredentials.ChangePasswordValidationRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.ChangeCredentials.ChangePasswordValidationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.ChangeCredentials.ChangePasswordWithOTPRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.ChangeCredentials.ChangePasswordWithOTPResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.CustomCountDownTimer;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class OTPVerificationChangePasswordDialog extends MaterialDialog.Builder implements HttpResponseListener {
    private HttpRequestPutAsyncTask mChangePasswordWithOTPTask = null;
    private ChangePasswordWithOTPResponse mChangePasswordWithOTPResponse;

    private HttpRequestPutAsyncTask mRequestOTPTask = null;
    private ChangePasswordValidationResponse mChangePasswordValidationResponse;

    private MaterialDialog mOTPInputDialog;

    private View view;
    private EditText mOTPEditText;

    private Button mActivateButton;
    private Button mResendOTPButton;
    private Button mCancelButton;

    private Activity context;
    private ProgressDialog mProgressDialog;

    private String mPassword;
    private String mNewPassword;
    private String mOTP;

    private EnableDisableSMSBroadcastReceiver mEnableDisableSMSBroadcastReceiver;

    public OTPVerificationChangePasswordDialog(Activity context, String password, String newPassword) {
        super(context);

        this.context = context;
        this.mPassword = password;
        this.mNewPassword = newPassword;
        initializeView();
    }

    public void initializeView() {
        mOTPInputDialog = new MaterialDialog.Builder(this.getContext())
                .title(R.string.title_otp_verification_for_change_password)
                .customView(R.layout.dialog_otp_verification_change_password, true)
                .show();

        view = mOTPInputDialog.getCustomView();

        mOTPEditText = (EditText) view.findViewById(R.id.otp_edittext);
        mActivateButton = (Button) view.findViewById(R.id.buttonVerifyOTP);
        mResendOTPButton = (Button) view.findViewById(R.id.buttonResend);
        mCancelButton = (Button) view.findViewById(R.id.buttonCancel);

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(true);

        setSMSBroadcastReceiver();
        setButtonActions();

        setCountDownTimer();
        setAutoLoginCredentials();
    }

    private void setButtonActions() {
        mActivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiding the keyboard after verifying OTP
                Utilities.hideKeyboard(context, v);
                if (Utilities.isConnectionAvailable(context)) verifyInput();
                else if (context != null)
                    Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        });

        mResendOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isConnectionAvailable(context)) resendOTP();
                else if (context != null)
                    Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEnableDisableSMSBroadcastReceiver.disableBroadcastReceiver(getContext());
                mOTPInputDialog.dismiss();
            }
        });
    }

    private void setSMSBroadcastReceiver() {//enable broadcast receiver to get the text message to get the OTP
        mEnableDisableSMSBroadcastReceiver = new EnableDisableSMSBroadcastReceiver();

        mEnableDisableSMSBroadcastReceiver.enableBroadcastReceiver(getContext(), new SMSReaderBroadcastReceiver.OnTextMessageReceivedListener() {
            @Override
            public void onTextMessageReceive(String otp) {
                mOTPEditText.setText(otp);
                mActivateButton.performClick();
            }
        });
    }

    private void setCountDownTimer() {
        mResendOTPButton.setEnabled(false);
        new CustomCountDownTimer(SecuritySettingsActivity.otpDuration, 500) {

            public void onTick(long millisUntilFinished) {
                mResendOTPButton.setText(context.getString(R.string.resend) + " " + new SimpleDateFormat("mm:ss").format(new Date(millisUntilFinished)));
            }

            public void onFinish() {
                mResendOTPButton.setEnabled(true);
            }
        }.start();
    }

    private void setAutoLoginCredentials() {
        if (Constants.DEBUG && Constants.AUTO_LOGIN && (Constants.SERVER_TYPE == 1 || Constants.SERVER_TYPE == 2)) {
            mOTPEditText.setText("123456");
            mActivateButton.callOnClick();
        }
    }

    private void verifyInput() {
        boolean cancel = false;
        View focusView = null;

        mOTP = mOTPEditText.getText().toString().trim();

        String errorMessage = InputValidator.isValidOTP(context, mOTP);
        if (errorMessage != null) {
            mOTPEditText.setError(errorMessage);
            focusView = mOTPEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            mOTP = mOTPEditText.getText().toString().trim();
            attemptChangePasswordWithOTP();
        }
    }

    private void showChangePasswordSuccessDialog() {
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(context);
        dialog
                .title(R.string.change_password_success)
                .content(R.string.change_password_success_message)
                .cancelable(false)
                .positiveText(R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switchToLoginPage();
                    }
                })
                .show();
    }

    private void switchToLoginPage() {
        ((MyApplication) context.getApplication()).launchLoginPage(null);
        ((MyApplication) context.getApplication()).clearTokenAndTimer();
        ProfileInfoCacheManager.setLoggedInStatus(false);
        mOTPInputDialog.cancel();
    }

    private void resendOTP() {
        if (mRequestOTPTask != null) return;

        mProgressDialog.setMessage(context.getString(R.string.sending_otp));
        mProgressDialog.show();

        ChangePasswordValidationRequest mChangePasswordRequest = new ChangePasswordValidationRequest(mPassword, mNewPassword);
        Gson gson = new Gson();
        String json = gson.toJson(mChangePasswordRequest);
        mRequestOTPTask = new HttpRequestPutAsyncTask(Constants.COMMAND_RESEND_OTP,
                Constants.BASE_URL_MM + Constants.URL_CHANGE_PASSWORD, json, context);
        mRequestOTPTask.mHttpResponseListener = this;
        mRequestOTPTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void attemptChangePasswordWithOTP() {
        if (mChangePasswordWithOTPTask != null) return;

        mProgressDialog.setMessage(context.getString(R.string.change_password_progress));
        mProgressDialog.show();
        ChangePasswordWithOTPRequest mChangePasswordWithOTPRequest = new ChangePasswordWithOTPRequest(mPassword, mNewPassword, mOTP);
        Gson gson = new Gson();
        String json = gson.toJson(mChangePasswordWithOTPRequest);
        mChangePasswordWithOTPTask = new HttpRequestPutAsyncTask(Constants.COMMAND_CHANGE_PASSWORD,
                Constants.BASE_URL_MM + Constants.URL_CHANGE_PASSWORD, json, context);
        mChangePasswordWithOTPTask.mHttpResponseListener = this;
        mChangePasswordWithOTPTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mChangePasswordWithOTPTask = null;
            mRequestOTPTask = null;
            if (context != null)
                Toast.makeText(context, R.string.service_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_CHANGE_PASSWORD)) {

            try {
                mChangePasswordWithOTPResponse = gson.fromJson(result.getJsonString(), ChangePasswordWithOTPResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (context != null) {
                        Toast.makeText(context, mChangePasswordWithOTPResponse.getMessage(), Toast.LENGTH_LONG).show();
                        Utilities.hideKeyboard(context, view);

                        mEnableDisableSMSBroadcastReceiver.disableBroadcastReceiver(context);
                        mOTPInputDialog.hide();
                        showChangePasswordSuccessDialog();
                    }
                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                    if (context != null) {
                        Toaster.makeText(context, mChangePasswordWithOTPResponse.getMessage(), Toast.LENGTH_LONG);
                        Utilities.hideKeyboard(context, view);
                        ((MyApplication) ((Activity) (getContext())).getApplication()).launchLoginPage(null);
                    }

                } else {

                    if (context != null)
                        Toast.makeText(context, mChangePasswordWithOTPResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (context != null)
                    Toast.makeText(context, R.string.change_pass_failed, Toast.LENGTH_LONG).show();
            }

            mProgressDialog.dismiss();
            mChangePasswordWithOTPTask = null;

        } else if (result.getApiCommand().equals(Constants.COMMAND_RESEND_OTP)) {

            try {
                mChangePasswordValidationResponse = gson.fromJson(result.getJsonString(), ChangePasswordValidationResponse.class);
                SecuritySettingsActivity.otpDuration = mChangePasswordValidationResponse.getOtpValidFor();
                String message = mChangePasswordValidationResponse.getMessage();

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED) {
                    if (context != null)
                        Toast.makeText(context, R.string.otp_sent, Toast.LENGTH_LONG).show();

                    setCountDownTimer();
                } else {
                    if (context != null)
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mProgressDialog.dismiss();
            mRequestOTPTask = null;
        }
    }
}


