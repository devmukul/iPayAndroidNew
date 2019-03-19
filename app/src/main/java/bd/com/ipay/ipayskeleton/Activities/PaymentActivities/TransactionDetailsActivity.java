package bd.com.ipay.ipayskeleton.Activities.PaymentActivities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.BaseActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HomeFragments.TransactionHistoryFragments.TransactionDetailsFragment;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.SingleTransactionHistoryRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionHistory;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;

public class TransactionDetailsActivity extends BaseActivity implements HttpResponseListener {

    private HttpRequestGetAsyncTask mTransactionHistoryTask = null;

    private TransactionHistory transactionHistory;
    private int status;
    private String requestID = null;

    private CustomProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new CustomProgressDialog(this);


        setContentView(R.layout.activity_transaction_details);
        status = getIntent().getIntExtra(Constants.STATUS, 0);

        if (status == Constants.PAYMENT_REQUEST_STATUS_ALL) {
            requestID = getIntent().getStringExtra(Constants.MONEY_REQUEST_ID);
            getTransactionHistory();
        } else {
            transactionHistory = getIntent().getParcelableExtra(Constants.TRANSACTION_DETAILS);
            TransactionDetailsFragment transactionDetailsFragment = new TransactionDetailsFragment();

            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.TRANSACTION_DETAILS, transactionHistory);
            transactionDetailsFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, transactionDetailsFragment).commit();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void getTransactionHistory() {
        if (mTransactionHistoryTask != null) {
            return;
        }
        mProgressDialog.show();

        SingleTransactionHistoryRequest mTransactionHistoryRequest;
        mTransactionHistoryRequest = new SingleTransactionHistoryRequest(requestID);

        Gson gson = new Gson();
        String json = gson.toJson(mTransactionHistoryRequest);
        mTransactionHistoryTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_TRANSACTION_HISTORY,
                Constants.BASE_URL_SM + Constants.URL_TRANSACTION_HISTORY_SINGLE + "/" + requestID, this.getApplicationContext(), false);
        mTransactionHistoryTask.mHttpResponseListener = this;
        mTransactionHistoryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, this, mProgressDialog)) {
            mTransactionHistoryTask = null;
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_GET_TRANSACTION_HISTORY)) {

            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                try {
                    transactionHistory = gson.fromJson(result.getJsonString(), TransactionHistory.class);
                    if (transactionHistory != null) {
                        TransactionDetailsFragment transactionDetailsFragment = new TransactionDetailsFragment();

                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Constants.TRANSACTION_DETAILS, transactionHistory);
                        transactionDetailsFragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, transactionDetailsFragment).commit();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    if (this != null)
                        Toaster.makeText(this, R.string.transaction_history_get_failed, Toast.LENGTH_LONG);
                }

            } else {
                if (this != null)
                    Toaster.makeText(this, R.string.transaction_history_get_failed, Toast.LENGTH_LONG);
            }

            mProgressDialog.dismiss();
            mTransactionHistoryTask = null;
        }
    }

    @Override
    protected Context setContext() {
        return TransactionDetailsActivity.this;
    }
}
