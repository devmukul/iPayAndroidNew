package bd.com.ipay.ipayskeleton.CustomView.Dialogs;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TwoFA.TwoFactorAuthSettingsSaveResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.CustomCountDownTimer;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthServicesAsynctaskMap;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class OTPVerificationForTwoFactorAuthenticationServicesDialog extends AlertDialog implements HttpResponseListener {

    private Activity context;

    private static String desiredRequest;

    private HttpRequestPostAsyncTask mHttpPostAsyncTask;

    private HttpRequestPutAsyncTask mHttpPutAsyncTask;

    private String json;
    private String mUri;
    private String method;
    private EditText mOTPEditText;
    private Button mActivateButton;
    private Button mCancelButton;
    private Button mResendOTPButton;
    private View view;

    //applicable only in make payment
    private long sponsorAccountId;

    private MaterialDialog mOTPInputDialog;

    public long getSponsorAccountId() {
        return sponsorAccountId;
    }

    public void setSponsorAccountId(long sponsorAccountId) {
        this.sponsorAccountId = sponsorAccountId;
    }

    private AnimatedProgressDialog mCustomProgressDialog;

    public HttpResponseListener mParentHttpResponseListener;

    private HashMap<String, String> mProgressDialogStringMap;

    private Long otpValidFor = null;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

    public OTPVerificationForTwoFactorAuthenticationServicesDialog(@NonNull Activity context, String json, String desiredRequest, String mUri, String method) {
        this(context, json, desiredRequest, mUri, method, null);
    }

    public OTPVerificationForTwoFactorAuthenticationServicesDialog(@NonNull Activity context, String json, String desiredRequest, String mUri, String method, Long otpValidFor) {
        super(context);
        this.context = context;
        OTPVerificationForTwoFactorAuthenticationServicesDialog.desiredRequest = desiredRequest;
        this.json = json;
        this.mUri = mUri;
        this.method = method;
        this.otpValidFor = otpValidFor;
        initializeView();
        createProgressDialogStringMap();
    }

    private void createProgressDialogStringMap() {
        mProgressDialogStringMap = new HashMap<>();
        mProgressDialogStringMap = TwoFactorAuthConstants.getProgressDialogStringMap(context);
    }

    public OTPVerificationForTwoFactorAuthenticationServicesDialog(Activity context) {
        super(context);

    }

    private void initializeView() {
        mOTPInputDialog = new MaterialDialog.Builder(this.getContext())
                .title(R.string.title_otp_verification_for_change_password)
                .customView(R.layout.dialog_otp_verification_change_password, true)
                .show();
        mOTPInputDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        view = mOTPInputDialog.getCustomView();


        if (view == null)
            return;
        mOTPEditText = view.findViewById(R.id.otp_edittext);
        mActivateButton = view.findViewById(R.id.buttonVerifyOTP);
        mResendOTPButton = view.findViewById(R.id.buttonResend);
        mCancelButton = view.findViewById(R.id.buttonCancel);

        mCustomProgressDialog = new AnimatedProgressDialog(context);
        setCountDownTimer();
        setButtonActions();

    }

    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void show() {
        mOTPInputDialog.show();
    }

    @Override
    public boolean isShowing() {
        return mOTPInputDialog.isShowing();
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
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOTPInputDialog.dismiss();
            }
        });
        mResendOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isConnectionAvailable(context))
                    attemptDesiredRequestWithOTP(null);
                else
                    Toaster.makeText(context, R.string.no_internet_connection, Toast.LENGTH_LONG);
            }
        });
    }

    private void setCountDownTimer() {
        mResendOTPButton.setEnabled(false);
        final long otpValidTime = otpValidFor != null ? otpValidFor : SecuritySettingsActivity.otpDuration;
        new CustomCountDownTimer(otpValidTime, 500) {

            public void onTick(long millisUntilFinished) {
                mResendOTPButton.setText(String.format(Locale.getDefault(), "%s %s", context.getString(R.string.resend), simpleDateFormat.format(new Date(millisUntilFinished))));
            }

            public void onFinish() {
                mResendOTPButton.setEnabled(true);
            }
        }.start();
    }

    private void verifyInput() {
        boolean cancel = false;
        View focusView = null;

        String mOTP = mOTPEditText.getText().toString().trim();

        String errorMessage = InputValidator.isValidOTP(context, mOTP);
        if (errorMessage != null) {
            mOTPEditText.setError(errorMessage);
            focusView = mOTPEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mOTP = mOTPEditText.getText().toString().trim();
            attemptDesiredRequestWithOTP(mOTP);
        }
    }

    private void attemptDesiredRequestWithOTP(String otp) {
        if (mCustomProgressDialog != null) {
            mCustomProgressDialog.setTitle(R.string.please_wait_no_ellipsis);
        }
        if (method.equals(Constants.METHOD_PUT) && mHttpPutAsyncTask == null) {
            mCustomProgressDialog.showDialog();
            hideOtpDialog();
            mHttpPutAsyncTask = TwoFactorAuthServicesAsynctaskMap.getPutAsyncTask(desiredRequest, json, otp, context, mUri);
            if (mHttpPutAsyncTask == null)
                return;
            mHttpPutAsyncTask.mHttpResponseListener = this;
            mHttpPutAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else if (method.equals(Constants.METHOD_POST) && mHttpPostAsyncTask == null) {
            mCustomProgressDialog.showDialog();
            hideOtpDialog();
            mHttpPostAsyncTask = TwoFactorAuthServicesAsynctaskMap.getPostAsyncTask(desiredRequest, json, otp, context, mUri);
            if (desiredRequest.equals(Constants.COMMAND_PAYMENT)) {
                mHttpPostAsyncTask.setSponsorAccountId(sponsorAccountId);
            }
            if (mHttpPostAsyncTask == null)
                return;
            mHttpPostAsyncTask.mHttpResponseListener = this;
            mHttpPostAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void hideOtpDialog() {
        view.setVisibility(View.GONE);
        if (mOTPInputDialog.isShowing()) {
            mOTPInputDialog.cancel();
        }
    }

    public void showOtpDialog() {
        view.setVisibility(View.VISIBLE);
        if (!mOTPInputDialog.isShowing()) {
            mOTPInputDialog.show();
        }
    }

    public void dismissDialog() {
        mOTPInputDialog.dismiss();
    }

    public Long getOtpValidFor() {
        return otpValidFor;
    }

    public void setOtpValidFor(Long otpValidFor) {
        this.otpValidFor = otpValidFor;
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), mCustomProgressDialog)) {
            mHttpPutAsyncTask = null;
            mHttpPostAsyncTask = null;
            mOTPInputDialog.dismiss();
            return;
        } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
            TwoFactorAuthSettingsSaveResponse twoFactorAuthSettingsSaveResponse =
                    new Gson().fromJson(result.getJsonString(), TwoFactorAuthSettingsSaveResponse.class);
            mCustomProgressDialog.setTitle(R.string.success);
            mCustomProgressDialog.showSuccessAnimationAndMessage(twoFactorAuthSettingsSaveResponse.getMessage());
        } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_PROCESSING) {
            TwoFactorAuthSettingsSaveResponse twoFactorAuthSettingsSaveResponse =
                    new Gson().fromJson(result.getJsonString(), TwoFactorAuthSettingsSaveResponse.class);
            mCustomProgressDialog.setTitle(R.string.success);
            mCustomProgressDialog.showSuccessAnimationAndMessage(twoFactorAuthSettingsSaveResponse.getMessage());
        } else {
            mCustomProgressDialog.dismissDialog();
        }
        mHttpPutAsyncTask = null;
        mHttpPostAsyncTask = null;
        mParentHttpResponseListener.httpResponseReceiver(result);
    }

}