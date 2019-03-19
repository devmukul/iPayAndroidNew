package bd.com.ipay.ipayskeleton.Utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AddPinDialogBuilder;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.ChangeCredentials.PinInfoResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;

/**
 * Checks if pin has been added for this user. If not, show pin input dialog. Otherwise,
 * performs desired work passed through the PinCheckerListener.
 */
public class PinChecker implements HttpResponseListener {

    private final Context mContext;
    private final PinCheckerListener mPinCheckerListener;

    private HttpRequestGetAsyncTask mGetPinInfoTask = null;
    private PinInfoResponse mPinInfoResponse;

    private final CustomProgressDialog mProgressDialog;

    private boolean cancel;

    public PinChecker(Context context, PinCheckerListener pinCheckerListener) {
        mContext = context;
        mPinCheckerListener = pinCheckerListener;
        mProgressDialog = new CustomProgressDialog(context);
    }

    public void execute() {
        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.SEE_PIN_EXISTS)) {
            DialogUtils.showServiceNotAllowedDialog(mContext);
            return;
        }
        cancel = false;

        if (SharedPrefManager.isPinAdded(false)) {
            if (mPinCheckerListener != null) {
                mPinCheckerListener.ifPinAdded();
            }
        } else {
            getPinInfo();
        }
    }

    private void getPinInfo() {
        if (mGetPinInfoTask != null) {
            return;
        }

        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                cancel = true;
            }
        });
        mProgressDialog.show();

        mGetPinInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PIN_INFO,
                Constants.BASE_URL_MM + Constants.URL_GET_PIN_INFO, mContext, this, false);
        mGetPinInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        mProgressDialog.dismiss();

        if (HttpErrorHandler.isErrorFound(result, mContext, null)) {
            mGetPinInfoTask = null;
            return;
        }


        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_GET_PIN_INFO)) {
            if (!cancel) {
                try {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mPinInfoResponse = gson.fromJson(result.getJsonString(), PinInfoResponse.class);

                        if (mPinInfoResponse.isPinExists()) {
                            if (mPinCheckerListener != null) {
                                mPinCheckerListener.ifPinAdded();
                            }

                            // Save the information so that we don't need to get pin info again and again
                            SharedPrefManager.setPinAdded(true);

                        } else {
                            AddPinDialogBuilder addPinDialogBuilder = new AddPinDialogBuilder(mContext, new AddPinDialogBuilder.AddPinListener() {
                                @Override
                                public void onPinAddSuccess() {
                                    if (mPinCheckerListener != null)
                                        mPinCheckerListener.ifPinAdded();
                                }
                            });
                            addPinDialogBuilder.show();
                        }
                    } else {
                        if (mContext != null) {
                            Toast.makeText(mContext, mPinInfoResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mContext != null) {
                        Toast.makeText(mContext, R.string.failed_loading_pin, Toast.LENGTH_LONG).show();
                    }
                }

                mGetPinInfoTask = null;
            }
        }
    }

    /**
     * If pin has been set for this user, PinChecker calls ifPinAdded() function.
     */
    public interface PinCheckerListener {
        void ifPinAdded();
    }
}
