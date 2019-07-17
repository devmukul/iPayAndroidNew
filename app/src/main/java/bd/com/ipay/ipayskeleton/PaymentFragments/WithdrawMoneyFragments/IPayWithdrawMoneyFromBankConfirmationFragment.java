package bd.com.ipay.ipayskeleton.PaymentFragments.WithdrawMoneyFragments;

import android.os.Bundle;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.WithdrawMoneyRequestV3;
import bd.com.ipay.ipayskeleton.PaymentFragments.BankTransactionFragments.IPayAbstractBankTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class IPayWithdrawMoneyFromBankConfirmationFragment extends IPayAbstractBankTransactionConfirmationFragment {

    @Override
    protected String getApiCommand() {
        return Constants.COMMAND_WITHDRAW_MONEY;
    }

    @Override
    protected String getRequestJson() {
        return gson.toJson(new WithdrawMoneyRequestV3(bankAccountList.getBankAccountId(), transactionAmount.doubleValue(), getNote(), isInstant));
    }

    @Override
    protected String getUrl() {
        return Constants.BASE_URL_SM + Constants.URL_WITHDRAW_MONEY_V3;
    }

    @Override
    protected void setupViewProperties() {
        setTransactionDescription(getStyledTransactionDescription(R.string.withdraw_money_confirmation_message, transactionAmount));
        setName(bankAccountList.getBankName());
        setTransactionImageResource(bankAccountList.getBankIcon(getContext()));
        setNoteEditTextHint(getString(R.string.short_note_optional_hint));
    }

    @Override
    protected void bankTransactionSuccess(final Bundle bundle) {
        if (getActivity() instanceof IPayTransactionActionActivity) {
            bundle.putBoolean(Constants.IS_INSTANT, isInstant);
            ((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayWithdrawMoneyFromBankSuccessFragment(), bundle, 3, true);
        }
    }

    @Override
    protected String getTrackerCategory() {
        return "Withdraw Money from Bank";
    }
}
