package bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.Bank.Instant;

import android.os.Bundle;
import android.support.annotation.Nullable;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.PaymentFragments.BankTransactionFragments.IPayAbstractBankTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class IPayAddMoneyFromBankInstantlySuccessFragment extends IPayAbstractTransactionSuccessFragment {

	protected Number transactionAmount;
	protected BankAccountList bankAccountList;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			transactionAmount = (Number) getArguments().getSerializable(IPayAbstractBankTransactionConfirmationFragment.TRANSACTION_AMOUNT_KEY);
			bankAccountList = getArguments().getParcelable(Constants.SELECTED_BANK_ACCOUNT);
		}
	}

	@Override
	protected void setupViewProperties() {
		setTransactionSuccessMessage(getStyledTransactionDescription(R.string.add_money_instant_money_success_message, transactionAmount));
		setSuccessDescription(getString(R.string.add_instant_money_success_description));
		setName(bankAccountList.getBankName());
		setUserName(bankAccountList.getAccountNumber());
		setReceiverImage(bankAccountList.getBankIcon(getContext()));
	}
}
