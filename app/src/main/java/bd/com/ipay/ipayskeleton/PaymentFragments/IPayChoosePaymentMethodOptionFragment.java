package bd.com.ipay.ipayskeleton.PaymentFragments;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.devspark.progressfragment.ProgressFragment;

import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ManageBanksActivity;
import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Adapters.OnItemClickListener;
import bd.com.ipay.ipayskeleton.Adapters.UserBankListAdapter;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.Bank.IPayAddMoneyFromBankAmountInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.Bank.Instant.IPayAddMoneyFromBankInstantlyAmountInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.WithdrawMoneyFragments.IPayWithdrawMoneyFromBankAmountInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.ViewModel.IPayChooseBankOptionViewModel;

public class IPayChoosePaymentMethodOptionFragment extends ProgressFragment {

	private UserBankListAdapter userBankListAdapter;
	private IPayChooseBankOptionViewModel iPayChooseBankOptionViewModel;
	private RecyclerView userBankListRecyclerView;
	private View userBankStatusErrorViewHolder;
	private TextView bankStatusErrorMessageTextView;
	private Button bankStatusErrorActionButton;

	private int transactionType = IPayTransactionActionActivity.TRANSACTION_TYPE_INVALID;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
			userBankListAdapter = new UserBankListAdapter(getContext(), new OnItemClickListener() {
				@Override
				public void onItemClick(int position, View view) {
					if (transactionType != IPayTransactionActionActivity.TRANSACTION_TYPE_INVALID) {
						BusinessRuleCacheManager.fetchBusinessRule(getActivity(), transactionType);
						Bundle bundle = new Bundle();
						bundle.putParcelable(Constants.SELECTED_BANK_ACCOUNT, iPayChooseBankOptionViewModel.getBankAccount(position));
						if (getActivity() instanceof IPayTransactionActionActivity) {
							switch (transactionType) {
								case IPayTransactionActionActivity.TRANSACTION_TYPE_ADD_MONEY_BY_BANK:
									((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayAddMoneyFromBankAmountInputFragment(), bundle, 1, true);
									break;
								case IPayTransactionActionActivity.TRANSACTION_TYPE_ADD_MONEY_BY_BANK_INSTANTLY:
									((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayAddMoneyFromBankInstantlyAmountInputFragment(), bundle, 1, true);
									break;
								case IPayTransactionActionActivity.TRANSACTION_TYPE_WITHDRAW_MONEY:
									((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayWithdrawMoneyFromBankAmountInputFragment(), bundle, 1, true);
									break;
							}
						}
					}
				}
			});
			userBankListRecyclerView.setAdapter(userBankListAdapter);

			if (iPayChooseBankOptionViewModel.getUserBankAccountListLiveData().getValue() != null) {
				userBankListAdapter.submitList(iPayChooseBankOptionViewModel.getUserBankAccountListLiveData().getValue());
				setContentShown(true);
			}
		}
		progressTextView.setText(R.string.fetching_bank_list);

		bankStatusErrorActionButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (iPayChooseBankOptionViewModel.getUserBankAccountStatus().getValue() != null) {
					final Intent intent = new Intent(getActivity(), ManageBanksActivity.class);
					switch (iPayChooseBankOptionViewModel.getUserBankAccountStatus().getValue()) {
						case NOT_ADDED:
							intent.putExtra(Constants.TARGET_FRAGMENT, Constants.ADD_BANK);
							break;
						case NOT_VERIFIED:
							intent.putExtra(Constants.TARGET_FRAGMENT, Constants.VERIFY_BANK);
							break;
					}
					startActivity(intent);
				}
			}
		});
		// Just to stop a small flickering. This is also give a little rest time before fetching the bank list.
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				iPayChooseBankOptionViewModel.fetchUserBankList();
			}
		}, 500);
	}
}
