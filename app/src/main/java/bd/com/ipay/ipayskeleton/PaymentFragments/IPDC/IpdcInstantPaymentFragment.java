package bd.com.ipay.ipayskeleton.PaymentFragments.IPDC;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractUserIdInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.IpdcAmountFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class IpdcInstantPaymentFragment extends IPayAbstractUserIdInputFragment {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected boolean verifyInput() {
        if (getUserId() == null || getUserId().isEmpty()) {
            showErrorMessage(getString(R.string.please_enter_an_installment_id));
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void performContinueAction() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.INSTALLMENT_NUMBER, getUserId());
        ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new IpdcAmountFragment(), bundle, 1, true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_ipdc, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.schedule_list) {
            ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new GlobalScheduledPaymentListFragment(), null, 1, true);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void setupViewProperties() {
        setTitle(getString(R.string.ipdc));
        setInputMessage(getString(R.string.ipdc_instant_payment_input_message));
        setUserIdHint(getString(R.string.installment_id));
        setMerchantIconImage(R.drawable.ic_ipdc);
    }
}