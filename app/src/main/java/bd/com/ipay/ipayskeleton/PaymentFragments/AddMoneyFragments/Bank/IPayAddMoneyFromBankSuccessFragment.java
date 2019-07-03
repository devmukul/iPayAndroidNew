package bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.Bank;

import android.os.Bundle;
import android.support.annotation.Nullable;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.PaymentFragments.BankTransactionFragments.IPayAbstractBankTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class IPayAddMoneyFromBankSuccessFragment extends IPayAbstractTransactionSuccessFragment {

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
		setTransactionSuccessMessage(getStyledTransactionDescription(R.string.add_money_bank_success_message, transactionAmount));
		if(!bankAccountList.getBankCode().equals("060")) {
			setSuccessDescription(getString(R.string.add_money_bank_success_description));
		}else{
			hideDesc();
		}
		setName(bankAccountList.getBankName());
		setReceiverImage(bankAccountList.getBankIcon(getContext()));
	}
}
