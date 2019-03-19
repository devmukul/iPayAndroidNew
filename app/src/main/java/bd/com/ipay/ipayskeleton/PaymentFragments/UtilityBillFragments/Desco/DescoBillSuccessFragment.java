package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.Desco;

import android.os.Bundle;
import android.support.annotation.Nullable;

import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class DescoBillSuccessFragment extends IPayAbstractTransactionSuccessFragment {

    private Number billAmount;
    private String descoAccountId;
    private String billNumber;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            billAmount = (Number) getArguments().getSerializable(Constants.TOTAL_AMOUNT);
            descoAccountId = getArguments().getString(Constants.ACCOUNT_ID, "");
            billNumber = getArguments().getString(Constants.BILL_NUMBER, "");
        }
    }

    @Override
    protected void setupViewProperties() {
        setTransactionSuccessMessage(getStyledTransactionDescription(R.string.pay_bill_success_message, billAmount));
        setSuccessDescription(getString(R.string.pay_bill_success_description));
        setName(billNumber);
        setUserName(descoAccountId);
        setReceiverImage(R.drawable.desco);
    }
}
