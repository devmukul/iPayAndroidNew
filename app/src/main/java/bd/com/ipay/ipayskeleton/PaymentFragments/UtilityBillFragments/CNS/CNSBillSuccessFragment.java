package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.CNS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;

import bd.com.ipay.android.fragment.transaction.IPayTransactionHistoryFragment;
import bd.com.ipay.android.utility.TransactionHistoryType;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.GetSavedBillResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayBillPayTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class CNSBillSuccessFragment extends IPayBillPayTransactionSuccessFragment implements HttpResponseListener {

	private Number billAmount;
	private String subscriberId;
    private HttpRequestPostAsyncTask linkThreeBillPayTask = null;
    protected AnimatedProgressDialog customProgressDialog;


    @Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			billAmount = (Number) getArguments().getSerializable(CNSBillConfirmationFragment.BILL_AMOUNT_KEY);
			subscriberId = getArguments().getString(CNSBillAmountInputFragment.SUBSCRIBER_ID_KEY, "");
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
		setReceiverImage(R.drawable.cns);
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
