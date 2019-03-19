package bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.Card;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.CardPaymentWebViewActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.AddMoneyByCreditOrDebitCardRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.AddMoneyByCreditOrDebitCardResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;

public class IPayAddMoneyFromCardTransactionConfirmationFragment extends IPayAbstractTransactionConfirmationFragment {

	private static final int CARD_PAYMENT_WEB_VIEW_REQUEST = 2001;
	public static final String TRANSACTION_AMOUNT_KEY = "TRANSACTION_AMOUNT";
	protected HttpRequestPostAsyncTask httpRequestPostAsyncTask = null;
	protected AnimatedProgressDialog mCustomProgressDialog = null;
	protected Number transactionAmount;
	protected String cardType;
	protected final Gson gson = new GsonBuilder().create();

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			transactionAmount = (Number) getArguments().getSerializable(TRANSACTION_AMOUNT_KEY);
			cardType = getArguments().getString(Constants.CARD_TYPE);
		}
		mCustomProgressDialog = new AnimatedProgressDialog(getContext());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case CARD_PAYMENT_WEB_VIEW_REQUEST:
				if (data != null) {
					final int transactionStatusCode = data.getIntExtra(Constants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD_STATUS, CardPaymentWebViewActivity.CARD_TRANSACTION_CANCELED);
					switch (transactionStatusCode) {
						case CardPaymentWebViewActivity.CARD_TRANSACTION_CANCELED:
							if (getActivity() != null)
								getActivity().finish();
							break;
						case CardPaymentWebViewActivity.CARD_TRANSACTION_FAILED:
							if (getActivity() != null)
								getActivity().finish();
							sendFailedEventTracking(getString(R.string.add_money_from_credit_or_debit_card_failed_message), transactionAmount);
							break;
						case CardPaymentWebViewActivity.CARD_TRANSACTION_SUCCESSFUL:
							ProfileInfoCacheManager.addSourceOfFund(true);
							sendSuccessEventTracking(transactionAmount);

							Bundle bundle = new Bundle();
							bundle.putSerializable(TRANSACTION_AMOUNT_KEY, transactionAmount);
							if (getActivity() instanceof IPayTransactionActionActivity)
								((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayAddMoneyFromCardSuccessFragment(), bundle, 3, true);
							break;
					}
				}
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
		}
	}

	@Override
	protected void setupViewProperties() {
		setTransactionDescription(getStyledTransactionDescription(R.string.add_money_confirmation_message, transactionAmount));
		setName(getString(R.string.debit_credit_card));
		setTransactionImageResource(R.drawable.ic_debit_credit_card_icon);
		setNoteEditTextHint(getString(R.string.short_note_optional_hint));
	}

	@Override
	protected boolean isPinRequired() {
		return false;
	}

	@Override
	protected boolean canUserAddNote() {
		return true;
	}

	@Override
	protected String getTrackerCategory() {
		return null;
	}

	@Override
	protected boolean verifyInput() {
		return true;
	}

	@Override
	protected void performContinueAction() {
		final String requestJson = gson.toJson(new AddMoneyByCreditOrDebitCardRequest(transactionAmount.doubleValue(), cardType, getNote(), null));
		httpRequestPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_MONEY_FROM_CREDIT_DEBIT_CARD, Constants.BASE_URL_CARD + Constants.URL_ADD_MONEY_CREDIT_OR_DEBIT_CARD,
				requestJson, getActivity(), this, false);
		httpRequestPostAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		mCustomProgressDialog.showDialog();
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		if (HttpErrorHandler.isErrorFound(result, getContext(), mCustomProgressDialog)) {
			httpRequestPostAsyncTask = null;
			mCustomProgressDialog.dismissDialog();
		} else {
			switch (result.getApiCommand()) {
				case Constants.COMMAND_ADD_MONEY_FROM_CREDIT_DEBIT_CARD:
					httpRequestPostAsyncTask = null;
					mCustomProgressDialog.dismissDialog();
					final AddMoneyByCreditOrDebitCardResponse mAddMoneyByCreditOrDebitResponse = gson.fromJson(result.getJsonString(), AddMoneyByCreditOrDebitCardResponse.class);
					switch (result.getStatus()) {
						case Constants.HTTP_RESPONSE_STATUS_OK:
							Intent intent = new Intent(getActivity(), CardPaymentWebViewActivity.class);
							intent.putExtra(Constants.CARD_PAYMENT_URL, mAddMoneyByCreditOrDebitResponse.getForwardUrl());
							startActivityForResult(intent, CARD_PAYMENT_WEB_VIEW_REQUEST);
							break;
						case Constants.HTTP_RESPONSE_STATUS_BAD_REQUEST:
						case Constants.HTTP_RESPONSE_STATUS_NOT_ACCEPTABLE:
							if (getActivity() != null)
								Toaster.makeText(getActivity(), mAddMoneyByCreditOrDebitResponse.getMessage(), Toast.LENGTH_SHORT);
							break;
						default:
							if (getActivity() != null)
								Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
							break;
					}
					break;
			}
		}
	}
}
