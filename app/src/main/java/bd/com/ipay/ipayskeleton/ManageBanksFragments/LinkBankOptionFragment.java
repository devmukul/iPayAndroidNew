package bd.com.ipay.ipayskeleton.ManageBanksFragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.NumberFormat;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.BracBankLinkWebViewActivity;
import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ManageBanksActivity;
import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.IsInstantResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Balance.CreditBalanceResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LinkBracBankResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.WithdrawMoneyFragments.IPayWithdrawMoneyFromBankAmountInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;

public class LinkBankOptionFragment extends Fragment implements HttpResponseListener {

	CardView internetBanking;
	CardView tokenMoney;
	private String mSelectedBankName;
	private int mSelectedBankId = -1;
	private boolean startedFromProfileCompletion = false;
	private boolean isSwitchedFromOnBoard = false;
	private static final String STARTED_FROM_PROFILE_ACTIVITY = "started_from_profile_activity";

	private static final int CARD_PAYMENT_WEB_VIEW_REQUEST = 2001;
	protected HttpRequestPostAsyncTask httpRequestPostAsyncTask = null;
	protected AnimatedProgressDialog mCustomProgressDialog = null;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		isSwitchedFromOnBoard = false;
		if (getArguments() != null) {
			startedFromProfileCompletion = getArguments() .getBoolean(STARTED_FROM_PROFILE_ACTIVITY);
			if (getArguments().getBoolean(Constants.FROM_ON_BOARD, false)) {
				isSwitchedFromOnBoard = getArguments() .getBoolean(Constants.FROM_ON_BOARD, false);
			}
			mSelectedBankId = getArguments() .getInt(Constants.SELECTED_BANK_ID);
			mSelectedBankName = getArguments() .getString(Constants.SELECTED_BANK_BANE, null);
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_ipay_link_bank_option, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getActivity().setTitle(R.string.link_bank);

		mCustomProgressDialog = new AnimatedProgressDialog(getContext());

		internetBanking = view.findViewById(R.id.internet_banking);
		tokenMoney = view.findViewById(R.id.token_money);

		internetBanking.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getBracBankUrl();
			}
		});

		tokenMoney.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putInt(Constants.SELECTED_BANK_ID, mSelectedBankId);
				bundle.putString(Constants.SELECTED_BANK_BANE, mSelectedBankName);
				bundle.putBoolean(Constants.FROM_ON_BOARD, isSwitchedFromOnBoard);
				bundle.putBoolean(Constants.IS_STARTED_FROM_PROFILE_COMPLETION, startedFromProfileCompletion);
				((ManageBanksActivity) getActivity()).switchToAddNewBankFragmentTest(bundle);

			}
		});

	}


	public void getBracBankUrl() {
		httpRequestPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_MONEY_FROM_BARC_BANK, Constants.BASE_URL_MM+ "bank/brac",
				null, getActivity(), this, false);
		httpRequestPostAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		mCustomProgressDialog.showDialog();
	}

	public void getBracBankToken(long bakId) {

		httpRequestPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_GET_BRAC_BANK_TOKEN, Constants.BASE_URL_MM+ "bank/brac/"+bakId,
				null, getActivity(), this, false);
		httpRequestPostAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		mCustomProgressDialog.showDialog();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case CARD_PAYMENT_WEB_VIEW_REQUEST:
				if (data != null) {
					final long bankID = data.getLongExtra("BANK_ID",0);
					getBracBankToken(bankID);
				}
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
		}
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {

		if (HttpErrorHandler.isErrorFound(result, getContext(), mCustomProgressDialog)) {
			httpRequestPostAsyncTask = null;
			mCustomProgressDialog.dismissDialog();
		} else {
			switch (result.getApiCommand()) {
				case Constants.COMMAND_GET_BRAC_BANK_TOKEN:
					httpRequestPostAsyncTask = null;
					mCustomProgressDialog.dismissDialog();
					final LinkBracBankResponse mAddMoneyByCreditOrDebitResponse1 = new Gson().fromJson(result.getJsonString(), LinkBracBankResponse.class);
					switch (result.getStatus()) {
						case Constants.HTTP_RESPONSE_STATUS_OK:
							Toaster.makeText(getActivity(), mAddMoneyByCreditOrDebitResponse1.getMessage(), Toast.LENGTH_SHORT);
							((ManageBanksActivity) getActivity()).switchToBankAccountsFragment();
							break;
						default:
							Toaster.makeText(getActivity(), mAddMoneyByCreditOrDebitResponse1.getMessage(), Toast.LENGTH_SHORT);
							((ManageBanksActivity) getActivity()).switchToBankAccountsFragment();
							break;
					}
					httpRequestPostAsyncTask = null;
					break;
				case Constants.COMMAND_ADD_MONEY_FROM_BARC_BANK:
					httpRequestPostAsyncTask = null;
					mCustomProgressDialog.dismissDialog();
					final LinkBracBankResponse mAddMoneyByCreditOrDebitResponse = new Gson().fromJson(result.getJsonString(), LinkBracBankResponse.class);
					switch (result.getStatus()) {
						case Constants.HTTP_RESPONSE_STATUS_OK:
							Intent intent = new Intent(getActivity(), BracBankLinkWebViewActivity.class);
							intent.putExtra("BANK_ID", mAddMoneyByCreditOrDebitResponse.getId());
							intent.putExtra(Constants.CARD_PAYMENT_URL, mAddMoneyByCreditOrDebitResponse.getSessionIdURL());
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
					httpRequestPostAsyncTask = null;
					break;
			}
		}

	}

}
