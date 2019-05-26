package bd.com.ipay.ipayskeleton.ManageBanksFragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressFragment;
import com.google.gson.Gson;

import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.BracBankLinkWebViewActivity;
import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ManageBanksActivity;
import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.CardPaymentWebViewActivity;
import bd.com.ipay.ipayskeleton.Adapters.IPayBankListAdapter;
import bd.com.ipay.ipayskeleton.Adapters.OnItemClickListener;
import bd.com.ipay.ipayskeleton.Adapters.UserBankListAdapter;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.AddMoneyByCreditOrDebitCardRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.AddMoneyByCreditOrDebitCardResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LinkBracBankResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.Bank;
import bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.Bank.IPayAddMoneyFromBankAmountInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.Bank.Instant.IPayAddMoneyFromBankInstantlyAmountInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.Card.IPayAddMoneyFromCardSuccessFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayChooseBankOptionFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.WithdrawMoneyFragments.IPayWithdrawMoneyFromBankAmountInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.ViewModel.IPayBankListViewModel;
import bd.com.ipay.ipayskeleton.ViewModel.IPayChooseBankOptionViewModel;

public class IPayBankListFragment extends ProgressFragment implements HttpResponseListener {

	private IPayBankListAdapter userBankListAdapter;
	private IPayBankListViewModel iPayBankListViewModel;
	private RecyclerView userBankListRecyclerView;
	private View userBankStatusErrorViewHolder;
	private TextView bankStatusErrorMessageTextView;
	private Button bankStatusErrorActionButton;

	private int transactionType = IPayTransactionActionActivity.TRANSACTION_TYPE_INVALID;

	private static final int CARD_PAYMENT_WEB_VIEW_REQUEST = 2001;
	protected HttpRequestPostAsyncTask httpRequestPostAsyncTask = null;
	protected AnimatedProgressDialog mCustomProgressDialog = null;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			transactionType = getArguments().getInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_INVALID);
		}

		mCustomProgressDialog = new AnimatedProgressDialog(getContext());
		iPayBankListViewModel = ViewModelProviders.of(this).get(IPayBankListViewModel.class);
		iPayBankListViewModel.getUserBankAccountListLiveData().observe(this, new Observer<List<Bank>>() {
			@Override
			public void onChanged(@Nullable List<Bank> bankAccountLists) {
				if (bankAccountLists != null) {
					userBankListAdapter.submitList(bankAccountLists);
					setContentShown(true);
				}
			}
		});
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_ipay_choose_bank_option, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final TextView progressTextView = view.findViewById(R.id.progress_text_view);
		userBankListRecyclerView = view.findViewById(R.id.user_bank_list_recycler_view);
		userBankStatusErrorViewHolder = view.findViewById(R.id.user_bank_status_error_view_holder);
		bankStatusErrorMessageTextView = view.findViewById(R.id.bank_status_error_message_text_view);
		bankStatusErrorActionButton = view.findViewById(R.id.bank_status_error_action_button);
		userBankListRecyclerView.setHasFixedSize(false);
		setContentShown(false);
		if (getContext() != null) {
			userBankListAdapter = new IPayBankListAdapter(getContext(), new OnItemClickListener() {
				@Override
				public void onItemClick(int position, View view) {

					Bank bank = iPayBankListViewModel.getBankAccount(position);
					switch (bank.getBankCode()) {
						case "060":
							getBracBankUrl();
							break;
						default:
							((ManageBanksActivity) getActivity()).switchToAddNewBankFragment();
							break;
					}

				}
			});
			userBankListRecyclerView.setAdapter(userBankListAdapter);

			if (iPayBankListViewModel.getUserBankAccountListLiveData().getValue() != null) {
				userBankListAdapter.submitList(iPayBankListViewModel.getUserBankAccountListLiveData().getValue());
				setContentShown(true);
			}
		}
		progressTextView.setText(R.string.fetching_bank_list);


		// Just to stop a small flickering. This is also give a little rest time before fetching the bank list.
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				iPayBankListViewModel.fetchBankList();
			}
		}, 500);
	}

	public void getBracBankUrl() {
		httpRequestPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_MONEY_FROM_BARC_BANK, "http://10.100.44.10:8085/api/v1/bank/brac",
				null, getActivity(), this, false);
		httpRequestPostAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		mCustomProgressDialog.showDialog();
	}

	public void getBracBankToken(long bakId) {
		httpRequestPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_GET_BRAC_BANK_TOKEN, "http://10.100.44.10:8085/api/v1/bank/brac/"+bakId,
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
					//final LinkBracBankResponse mAddMoneyByCreditOrDebitResponse = new Gson().fromJson(result.getJsonString(), LinkBracBankResponse.class);
					switch (result.getStatus()) {
						case Constants.HTTP_RESPONSE_STATUS_OK:
							((ManageBanksActivity) getActivity()).switchToLinkBracBankSuccess();

//							Intent intent = new Intent(getActivity(), BracBankLinkWebViewActivity.class);
//							intent.putExtra("BANK_ID", mAddMoneyByCreditOrDebitResponse.getId());
//							intent.putExtra(Constants.CARD_PAYMENT_URL, mAddMoneyByCreditOrDebitResponse.getCallbackUrl());
//							startActivityForResult(intent, CARD_PAYMENT_WEB_VIEW_REQUEST);
							break;
						case 417:
							((ManageBanksActivity) getActivity()).switchToBankAccountsFragment();

//							Intent intent = new Intent(getActivity(), BracBankLinkWebViewActivity.class);
//							intent.putExtra("BANK_ID", mAddMoneyByCreditOrDebitResponse.getId());
//							intent.putExtra(Constants.CARD_PAYMENT_URL, mAddMoneyByCreditOrDebitResponse.getCallbackUrl());
//							startActivityForResult(intent, CARD_PAYMENT_WEB_VIEW_REQUEST);
							break;
						default:
							if (getActivity() != null) {
								Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
								getActivity().finish();
							}
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
