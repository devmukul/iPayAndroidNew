package bd.com.ipay.ipayskeleton.CustomView.Dialogs;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.ChangeCredentials.SetPinRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.ChangeCredentials.SetPinResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class AddPinDialogBuilder extends MaterialDialog.Builder implements HttpResponseListener {

    private final AddPinListener mAddPinListener;
    private HttpRequestPutAsyncTask mSavePINTask = null;
    private SetPinResponse mSetPinResponse;
    private CustomProgressDialog mProgressDialog;
    private EditText mPinField;
    private EditText mConfirmPinField;
    private EditText mPasswordField;

    public AddPinDialogBuilder(Context context, AddPinListener addPinListener) {
        super(context);
        initializeView();
        this.mAddPinListener = addPinListener;
    }

    private void initializeView() {
        customView(R.layout.dialog_add_pin, true);

        View v = this.build().getCustomView();
        autoDismiss(false);

        mPinField = (EditText) v.findViewById(R.id.new_pin);
        mConfirmPinField = (EditText) v.findViewById(R.id.confirm_pin);
        mPasswordField = (EditText) v.findViewById(R.id.password);

        title(R.string.dialog_prompt_add_pin);

        positiveText(R.string.set_pin);
        negativeText(R.string.cancel);

        mProgressDialog = new CustomProgressDialog(context);

        onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                View focusView;
                String pin = mPinField.getText().toString();
                String password = mPasswordField.getText().toString();
                String passwordValidationMsg = InputValidator.isPasswordValid(password);

                if (pin.trim().length() != 4) {
                    mPinField.setError(getContext().getString(R.string.error_invalid_pin));
                    focusView = mPinField;
                    focusView.requestFocus();
                } else if (mConfirmPinField.getText().toString().length() != 4
                        || !(mPinField.getText().toString().equals(mConfirmPinField.getText().toString()))) {
                    mConfirmPinField.setError(getContext().getString(R.string.confirm_pin_not_matched));
                    focusView = mConfirmPinField;
                    focusView.requestFocus();
                } else if (passwordValidationMsg.length() > 0) {
                    mPasswordField.setError(passwordValidationMsg);
                    focusView = mPasswordField;
                    focusView.requestFocus();
                } else {
                    hideKeyboard();
                    dialog.dismiss();

                    attemptSavePin(pin, password);
                }
            }
        });

        onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                hideKeyboard();
                dialog.dismiss();
            }
        });

        Utilities.showKeyboard(context);

    }

    private void hideKeyboard() {
        Utilities.hideKeyboard(getContext(), mPinField);
        Utilities.hideKeyboard(getContext(), mConfirmPinField);
        Utilities.hideKeyboard(getContext(), mPasswordField);
    }

    private void attemptSavePin(String pin, String password) {
        if (mSavePINTask != null) {
            return;
        }

        mProgressDialog.show();

        SetPinRequest setPinRequest = new SetPinRequest(pin, password);

        Gson gson = new Gson();
        String json = gson.toJson(setPinRequest);

        mSavePINTask = new HttpRequestPutAsyncTask(Constants.COMMAND_SET_PIN,
                Constants.BASE_URL_MM + Constants.URL_SET_PIN, json, getContext(), this, false);
        mSavePINTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        mProgressDialog.dismiss();

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mSavePINTask = null;
            return;
        }


        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_SET_PIN)) {

            try {
                mSetPinResponse = gson.fromJson(result.getJsonString(), SetPinResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (getContext() != null)
                        Toaster.makeText(getContext(), mSetPinResponse.getMessage(), Toast.LENGTH_LONG);

                    mAddPinListener.onPinAddSuccess();
                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                    if (getContext() != null) {
                        ((MyApplication) ((Activity) getContext()).getApplication()).launchLoginPage(mSetPinResponse.getMessage());
                    }
                    if (getContext() instanceof AppCompatActivity)
                        Utilities.sendBlockedEventTracker(Utilities.getTracker(((AppCompatActivity) getContext())), "Add Pin", ProfileInfoCacheManager.getAccountId());

                } else {
                    if (getContext() != null)
                        Toaster.makeText(getContext(), mSetPinResponse.getMessage(), Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getContext() != null)
                    Toaster.makeText(getContext(), R.string.save_failed, Toast.LENGTH_LONG);
            }

            mProgressDialog.dismiss();
            mSavePINTask = null;
        }
    }

    public interface AddPinListener {
        void onPinAddSuccess();
    }
}
