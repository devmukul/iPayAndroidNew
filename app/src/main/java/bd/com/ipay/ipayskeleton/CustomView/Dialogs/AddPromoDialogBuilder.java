package bd.com.ipay.ipayskeleton.CustomView.Dialogs;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.PromoCode.AddPromoRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.PromoCode.AddPromoResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class AddPromoDialogBuilder extends MaterialDialog.Builder implements HttpResponseListener {

    private HttpRequestPostAsyncTask mAddPromoTask = null;
    private Context context;
    private AddPromoResponse mAddPromoResponse;
    private CustomProgressDialog mProgressDialog;
    private EditText mPromoField;
    private final AddPromoListener mAddPromoListener;

    public AddPromoDialogBuilder(Context context, AddPromoListener addPromoListener) {
        super(context);
        this.context = context;
        initializeView();
        this.mAddPromoListener = addPromoListener;
    }

    private void initializeView() {
        customView(R.layout.dialog_add_promo, true);

        View v = this.build().getCustomView();
        autoDismiss(false);

        mPromoField = (EditText) v.findViewById(R.id.promo);

        title(R.string.dialog_prompt_add_promo);

        positiveText(R.string.ok);
        negativeText(R.string.cancel);

        mProgressDialog = new CustomProgressDialog(context);

        onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                View focusView;
                String promoCode = mPromoField.getText().toString().trim();

                if (promoCode.trim().length() < 6 || promoCode.contains(" ")) {
                    mPromoField.setError(getContext().getString(R.string.error_invalid_promo_code));
                    focusView = mPromoField;
                    focusView.requestFocus();
                } else {
                    hideKeyboard();
                    dialog.dismiss();
                    attemptSavePromo(promoCode);
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
        Utilities.hideKeyboard(getContext(), mPromoField);
    }

    private void attemptSavePromo(String promoCode) {
        if (mAddPromoTask != null) {
            return;
        }
        mProgressDialog.show();

        AddPromoRequest addPromoRequest = new AddPromoRequest(promoCode);


        Gson gson = new Gson();
        String json = gson.toJson(addPromoRequest);

        mAddPromoTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_PROMO,
                Constants.BASE_URL_OFFER + Constants.URL_PROMO_ACTIVE, json, getContext(), this, false);
        mAddPromoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        mProgressDialog.dismiss();

        if (HttpErrorHandler.isErrorFound(result, context, mProgressDialog)) {
            mAddPromoTask = null;
            return;
        }


        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_ADD_PROMO
        )) {

            try {
                mAddPromoResponse = gson.fromJson(result.getJsonString(), AddPromoResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (getContext() != null)
                        Toaster.makeText(context, mAddPromoResponse.getMessage(), Toast.LENGTH_LONG);
                    mAddPromoListener.onPromoAddSuccess();
                } else {
                    if (getContext() != null)
                        Toaster.makeText(context, mAddPromoResponse.getMessage(), Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getContext() != null)
                    Toaster.makeText(getContext(), R.string.save_failed, Toast.LENGTH_LONG);
            }

            mProgressDialog.dismiss();
            mAddPromoTask = null;
        }
    }

    public interface AddPromoListener {
        void onPromoAddSuccess();
    }
}
