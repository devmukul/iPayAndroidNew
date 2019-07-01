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
import bd.com.ipay.ipayskeleton.Utilities.BracBankAddMoneyServicesAsynctaskMap;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.CustomCountDownTimer;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthServicesAsynctaskMap;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class OTPVerificationForBracBankAddMoneyDialog extends AlertDialog implements HttpResponseListener {

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

    public OTPVerificationForBracBankAddMoneyDialog(@NonNull Activity context, String json, String desiredRequest, String mUri, String method) {
        this(context, json, desiredRequest, mUri, method, null);
    }

    public OTPVerificationForBracBankAddMoneyDialog(@NonNull Activity context, String json, String desiredRequest, String mUri, String method, Long otpValidFor) {
        super(context);
        this.context = context;
        OTPVerificationForBracBankAddMoneyDialog.desiredRequest = desiredRequest;
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

    public OTPVerificationForBracBankAddMoneyDialog(Activity context) {
        super(context);

    }

    private void initializeView() {
        mOTPInputDialog = new MaterialDialog.Builder(this.getContext())
                .title(R.string.title_otp_verification_for_change_password)
                .customView(R.layout.dialog_otp_verification_barc_bank, true)
                .show();
        mOTPInputDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        view = mOTPInputDialog.getCustomView();


        if (view == null)
            return;
        mOTPEditText = view.findViewById(R.id.otp_edittext);
        mActivateButton = view.findViewById(R.id.buttonVerifyOTP);
        mCancelButton = view.findViewById(R.id.buttonCancel);

        mCustomProgressDialog = new AnimatedProgressDialog(context);
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
        mCustomProgressDialog.showDialog();
        hideOtpDialog();
        mHttpPostAsyncTask = BracBankAddMoneyServicesAsynctaskMap.getPostAsyncTask(desiredRequest, json, otp, context, mUri);
        if (mHttpPostAsyncTask == null)
            return;
        mHttpPostAsyncTask.mHttpResponseListener = this;
        mHttpPostAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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