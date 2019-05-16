package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.AkashDTH;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.android.fragment.transaction.IPayTransactionHistoryFragment;
import bd.com.ipay.android.utility.TransactionHistoryType;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.BillParam;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.GetSavedBillResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.MetaData;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.Notification;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.SavedBill;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayBillPayTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LinkThree.LinkThreeBillAmountInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LinkThree.LinkThreeBillConfirmationFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.SaveBillDialog;

public class AkashBillSuccessFragment extends IPayBillPayTransactionSuccessFragment implements HttpResponseListener {

	private Number billAmount;
	private String subscriberId;
    private HttpRequestPostAsyncTask linkThreeBillPayTask = null;
    protected AnimatedProgressDialog customProgressDialog;


    @Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			billAmount = (Number) getArguments().getSerializable(AkashBillConfirmationFragment.BILL_AMOUNT_KEY);
			subscriberId = getArguments().getString(AkashBillAmountInputFragment.SUBSCRIBER_ID_KEY, "");
		}


        customProgressDialog = new AnimatedProgressDialog(getActivity());
        customProgressDialog.setCancelable(false);
	}

	@Override
	protected void setupViewProperties() {
		setTransactionSuccessMessage(getStyledTransactionDescription(R.string.pay_bill_success_message, billAmount));
		setSuccessDescription(getString(R.string.pay_bill_success_description));
		setName(getString(R.string.subscriber_id)+": "+subscriberId);
		hideUserName();
		hideSaveButton();
		setReceiverImage(R.drawable.akash);
	}

	@Override
	protected void performContinueAction() {
	}

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (getActivity() == null)
            return;

        if (HttpErrorHandler.isErrorFound(result, getContext(), customProgressDialog)) {
            customProgressDialog.dismissDialog();
            linkThreeBillPayTask = null;
        } else {
            try {
                switch (result.getApiCommand()) {
                    case Constants.COMMAND_LINK_THREE_BILL_PAY:
                        GetSavedBillResponse deleteBillResponse = new Gson().fromJson(result.getJsonString(), GetSavedBillResponse.class);

                        switch (result.getStatus()) {
                            case Constants.HTTP_RESPONSE_STATUS_OK:
                                customProgressDialog.setTitle(R.string.success);
                                customProgressDialog.showSuccessAnimationAndMessage(deleteBillResponse.getMessage());

                                if (getContext() != null) {
                                    Intent broadcastIntent = new Intent();
                                    Intent intent = new Intent();
                                    intent.setAction(IPayTransactionHistoryFragment.TRANSACTION_HISTORY_UPDATE_ACTION);
                                    intent.putExtra(IPayTransactionHistoryFragment.TRANSACTION_HISTORY_TYPE_KEY,
                                            TransactionHistoryType.COMPLETED);
                                    LocalBroadcastManager.getInstance(getContext())
                                            .sendBroadcast(broadcastIntent);
                                }

                                if (getActivity() != null) {
                                    getActivity().setResult(Activity.RESULT_OK);
                                    getActivity().finish();
                                }
                                break;
                            default:
                                if (getActivity() != null) {
                                    customProgressDialog.showFailureAnimationAndMessage(deleteBillResponse.getMessage());
                                    break;
                                }
                                break;
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                customProgressDialog.showFailureAnimationAndMessage(getString(R.string.failed));
            }
            linkThreeBillPayTask = null;
        }

    }
}
