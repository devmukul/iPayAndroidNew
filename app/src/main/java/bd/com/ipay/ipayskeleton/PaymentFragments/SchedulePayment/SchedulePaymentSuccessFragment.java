package bd.com.ipay.ipayskeleton.PaymentFragments.SchedulePayment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class SchedulePaymentSuccessFragment extends IPayAbstractTransactionSuccessFragment {

    private Number billAmount;
    private String descoAccountId;
    private String billNumber;
    private String name;

    private String uri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            billAmount = (Number) getArguments().getSerializable(Constants.TOTAL_AMOUNT);
            descoAccountId = getArguments().getString(Constants.ACCOUNT_ID, "");
            billNumber = getArguments().getString(Constants.BILL_NUMBER, "");
            uri = getArguments().getString(Constants.IMAGE_URL);
            name = getArguments().getString(Constants.NAME);
        }
    }

    @Override
    protected void setupViewProperties() {
        setTransactionSuccessMessage(getStyledTransactionDescription(R.string.pay_installment_success_message, billAmount));
        setSuccessDescription(getString(R.string.pay_installment_success_description));
        setName(name);
        setUserName(descoAccountId);
        setSenderImage(ProfileInfoCacheManager.getProfileImageUrl());
        setReceiverImage(Constants.BASE_URL_FTP_SERVER + uri);
    }
}
