package bd.com.ipay.ipayskeleton.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.MandatoryBusinessRules;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.RailwayTickets.JourneyInfoSelectFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public final class RailwayTicketActionActivity extends BaseActivity {

    public static final String KEY_TICKET_CLASS_NAME = "KEY_TICKET_CLASS_NAME";
    public static final String KEY_TICKET_FARE_AMOUNT = "KEY_TICKET_FARE_AMOUNT";
    public static final String KEY_TICKET_VAT_AMOUNT = "KEY_TICKET_VAT_AMOUNT";
    public static final String KEY_TICKET_TOTAL_AMOUNT = "KEY_TICKET_TOTAL_AMOUNT";
    public static final String KEY_TICKET_ADULTS = "KEY_TICKET_ADULTS";
    public static final String KEY_TICKET_CHILD = "KEY_TICKET_CHILD";
    public static final String KEY_TICKET_DATE = "KEY_TICKET_DATE";
    public static final String KEY_TICKET_MESSAGE_ID = "KEY_TICKET_MESSAGE_ID";
    public static final String KEY_TICKET_STATION_TO = "KEY_TICKET_STATION_TO";
    public static final String KEY_TICKET_STATION_FROM = "KEY_TICKET_STATION_FROM";
    public static final String KEY_TICKET_TICKET_ID = "KEY_TICKET_TICKET_ID";
    public static final String KEY_TICKET_TRAIN_NO = "KEY_TICKET_TRAIN_NO";
    public static final String KEY_TICKET_GENDER = "KEY_TICKET_GENDER";

    public static final String KEY_TICKET_TRAIN_NAME = "KEY_TICKET_TRAIN_NAME";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipay_utility_bill_pay_action);

        if (UtilityBillPaymentActivity.mMandatoryBusinessRules == null) {
            UtilityBillPaymentActivity.mMandatoryBusinessRules = new MandatoryBusinessRules(Constants.RAILWAY_TICKET);
        }

        final Bundle bundle = new Bundle();
        switchFragment(new JourneyInfoSelectFragment(), bundle, 0, false);
    }

    public void switchFragment(@NonNull Fragment fragment, @NonNull Bundle bundle, int maxBackStackEntryCount, boolean shouldAnimate) {
        if (getSupportFragmentManager().getBackStackEntryCount() > maxBackStackEntryCount) {
            getSupportFragmentManager().popBackStackImmediate();
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (shouldAnimate) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                fragmentTransaction.setCustomAnimations(R.anim.right_to_left_enter,
                        R.anim.right_to_left_exit, R.anim.left_to_right_enter, R.anim.left_to_right_exit);
            }
        }
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.fragment_container, fragment).addToBackStack(fragment.getTag());
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        Utilities.hideKeyboard(this);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof IPayAbstractTransactionSuccessFragment) {
                finish();
                return;
            }
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected Context setContext() {
        return RailwayTicketActionActivity.this;
    }
}
