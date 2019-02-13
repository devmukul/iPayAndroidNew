package bd.com.ipay.ipayskeleton.Widget.View;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.BulkSignupUserDetailsCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class BulkSignUpHelperDialog implements HttpResponseListener {
	private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
	private AlertDialog alertDialog;
    private ProgressDialog mProgressDialog;
    private String checkedInfo;

    private HttpRequestPostAsyncTask mSendStatusCheckedTask = null;
	Context context;

	public BulkSignUpHelperDialog(final Context context, String text) {

        this.context = context;

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

		alertDialog = new AlertDialog.Builder(context)
				.setTitle("Info")
				.setMessage(text)
				.setCancelable(false)
				.setNegativeButton("CANCEL", null)
				.create();
	}

	public void setPositiveButton(CharSequence text, final DialogInterface.OnClickListener onClickListener) {
	    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, text ,onClickListener);
	}

	public void setNegativeButton(final DialogInterface.OnClickListener onClickListener) {
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL" ,onClickListener);
	}

    public void setCheckedResponse(String checkedInfo) {
	    this.checkedInfo = checkedInfo;
	    sendCheckedResponse(checkedInfo);
    }

	public void show() {
		if (!alertDialog.isShowing())
			alertDialog.show();
	}

	public void cancel() {
		if (alertDialog.isShowing())
			alertDialog.cancel();
	}

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, context , mProgressDialog)) {
            mProgressDialog.dismiss();
            mSendStatusCheckedTask = null;
            return;
        }
        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_SEND_BULK_SIGN_UP_USER_CHECKED_RESPONSE:
                try {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if(checkedInfo.equals("Bank"))
                            BulkSignupUserDetailsCacheManager.setBasicInfoChecked(true);
                        else
                            BulkSignupUserDetailsCacheManager.setBasicInfoChecked(true);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                mProgressDialog.dismiss();
                mSendStatusCheckedTask = null;
                break;
        }

    }

    private void sendCheckedResponse(String checkedInfo) {
	    mProgressDialog.setMessage(context.getString(R.string.loading));
        mProgressDialog.show();
        if (mSendStatusCheckedTask != null)
            return;

        mSendStatusCheckedTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SEND_BULK_SIGN_UP_USER_CHECKED_RESPONSE,
                Constants.BASE_URL_MM + Constants.URL_SEND_BULK_SIGN_UP_USER_RESPONSE+"?checkedInfoName="+checkedInfo, null, context, true);
        mSendStatusCheckedTask.mHttpResponseListener = this;
        mSendStatusCheckedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
