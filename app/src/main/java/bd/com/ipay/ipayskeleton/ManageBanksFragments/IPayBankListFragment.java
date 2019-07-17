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

public class IPayBankListFragment extends ProgressFragment{

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

    private boolean startedFromProfileCompletion = false;
    private boolean isSwitchedFromOnBoard = false;

	private static final String STARTED_FROM_PROFILE_ACTIVITY = "started_from_profile_activity";

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			transactionType = getArguments().getInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, IPayTransactionActionActivity.TRANSACTION_TYPE_INVALID);

			isSwitchedFromOnBoard = false;
			startedFromProfileCompletion = getArguments().getBoolean(STARTED_FROM_PROFILE_ACTIVITY);
			if (getArguments().getBoolean(Constants.FROM_ON_BOARD, false)) {
				isSwitchedFromOnBoard = getArguments().getBoolean(Constants.FROM_ON_BOARD, false);
			}
		}

		getActivity().setTitle(R.string.bank);

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

					Bundle bundle = new Bundle();
					bundle.putInt(Constants.SELECTED_BANK_ID, bank.getId());
					bundle.putString(Constants.SELECTED_BANK_BANE, bank.getName());
					bundle.putBoolean(Constants.FROM_ON_BOARD, isSwitchedFromOnBoard);
					bundle.putBoolean(Constants.IS_STARTED_FROM_PROFILE_COMPLETION, startedFromProfileCompletion);
					switch (bank.getBankCode()) {
						case "060":
							((ManageBanksActivity) getActivity()).switchToLinkBankOptionFragment(bundle);
							break;
						default:
							((ManageBanksActivity) getActivity()).switchToAddNewBankFragmentTest(bundle);
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
}
