package bd.com.ipay.ipayskeleton.PaymentFragments.WithdrawMoneyFragments;

import android.os.Bundle;
import android.support.annotation.Nullable;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.PaymentFragments.BankTransactionFragments.IPayAbstractBankTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class IPayWithdrawMoneyFromBankSuccessFragment extends IPayAbstractTransactionSuccessFragment {
	protected Number transactionAmount;
	protected BankAccountList bankAccountList;
	protected boolean isInstant;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			transactionAmount = (Number) getArguments().getSerializable(IPayAbstractBankTransactionConfirmationFragment.TRANSACTION_AMOUNT_KEY);
			bankAccountList = getArguments().getParcelable(Constants.SELECTED_BANK_ACCOUNT);
			isInstant = getArguments().getBoolean(Constants.IS_INSTANT, false);
		}
	}

	@Override
	protected void setupViewProperties() {
		setTransactionSuccessMessage(getStyledTransactionDescription(R.string.withdraw_money_bank_success_message, transactionAmount));
		setSuccessDescription(getString(R.string.withdraw_money_success_description));
		setName(bankAccountList.getBankName());
		setReceiverImage(bankAccountList.getBankIcon(getContext()));

		if(isInstant)
			hideDesc();
	}
}
