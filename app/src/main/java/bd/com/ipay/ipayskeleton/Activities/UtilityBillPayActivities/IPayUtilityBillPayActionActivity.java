package bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import java.io.Serializable;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.BaseActivity;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.RecentBill;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.SavedBill;
import bd.com.ipay.ipayskeleton.PaymentFragments.SaveAndScheduleBill.BillPaySavedNumberSelectFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.SaveAndScheduleBill.LankaBanglaCARDSavedNumberSelectFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.SaveAndScheduleBill.LankaBanglaDPSSavedNumberSelectFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.SchedulePayment.ScheduledPaymentListFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.SchedulePayment.ScheduledPaymentApiFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.SchedulePayment.ScheduledPaymentDetailsFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IspSelectionFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.Carnival.CarnivalIdInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.CreditCard.CreditCardBankSelectionFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Card.LankaBanglaCardNumberInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Dps.LankaBanglaDpsNumberInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LinkThree.LinkThreeSubscriberIdInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public final class IPayUtilityBillPayActionActivity extends BaseActivity {

    public static final String BILL_PAY_LANKABANGLA_CARD = "LANKABANGLA_CARD";
    public static final String BILL_PAY_LINK_THREE = "LINK_THREE";
    public static final String BILL_PAY_CARNIVAL = "CARNIVAL";
    public static final String BILL_PAY_PARTY_NAME_KEY = "BILL_PAY_PARTY_NAME";
    public static final String BILL_PAY_LANKABANGLA_DPS = "LANKABANGLA_DPS";
    public static final String SCHEDULED_PAY_IPDC = "SCHEDULED_PAY_IPDC";
    public static final String CREDIT_CARD = "CREDIT_CARD";

    public static final String CARD_NUMBER_KEY = "CARD_NUMBER";
    public static final String CARD_USER_NAME_KEY = "CARD_USER_NAME";
    public static final String SAVE_CARD_INFO = "SAVE_CARD_INFO";
    public static final String BANK_ICON = "BANK_ICON";

    public static final String BILL_AMOUNT_KEY = "BILL_AMOUNT";
    public static final String BANK_CODE = "BANK_CODE";
    public static final String SCHEDULE_PAYMENT_LIST = "SCHEDULE_PAYMENT_LIST";
    private List<SavedBill> savedBills;
    private List<RecentBill> recentBills;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipay_utility_bill_pay_action);
        if(getIntent().hasExtra(Constants.FROM_DASHBOARD)){
            BusinessRuleCacheManager.fetchBusinessRule(this, ServiceIdConstants.UTILITY_BILL_PAYMENT);
            if(getIntent().getStringExtra(Constants.SERVICE).equals("CARD")) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(Constants.FROM_DASHBOARD, true);
                switchFragment(new CreditCardBankSelectionFragment(), bundle, 0, true);
            }else{
                switchFragment(new IspSelectionFragment(), null, 0, true);
            }

        }else if (getIntent().hasExtra(Constants.ACTION_FROM_NOTIFICATION)) {
            if (getIntent().getBooleanExtra(Constants.ACTION_FROM_NOTIFICATION, false)) {
                String id = getIntent().getStringExtra("id");
                Bundle bundle = new Bundle();
                bundle.putLong(Constants.ID, Long.parseLong(id));
                switchFragment(new ScheduledPaymentDetailsFragment(), bundle, 2, true);
            }
            return;
        }else{
            final String billPayPartyName = getIntent().getStringExtra(BILL_PAY_PARTY_NAME_KEY);
            BusinessRuleCacheManager.fetchBusinessRule(this, ServiceIdConstants.UTILITY_BILL_PAYMENT);
            final Bundle bundle = new Bundle();
            switch (billPayPartyName) {
                case BILL_PAY_LANKABANGLA_CARD:
                    if(getIntent().hasExtra("SAVED_DATA"))
                        savedBills = (List<SavedBill>) getIntent().getSerializableExtra("SAVED_DATA");
                    if(getIntent().hasExtra("RECENT_DATA"))
                        recentBills = (List<RecentBill>) getIntent().getSerializableExtra("RECENT_DATA");
                    if((recentBills !=null && recentBills.size()>0) || (savedBills != null&& savedBills.size()>0)){
                        bundle.putString(Constants.NAME, getString(R.string.lanka_bangla_card));
                        bundle.putSerializable("SAVED_DATA", (Serializable) savedBills);
                        bundle.putSerializable("RECENT_DATA", (Serializable) recentBills);
                        switchFragment(new LankaBanglaCARDSavedNumberSelectFragment(), bundle, 0, false);
                    }else{
                        switchFragment(new LankaBanglaCardNumberInputFragment(), bundle, 0, false);
                    }
                    break;
                case BILL_PAY_LINK_THREE:
                    if(getIntent().hasExtra("SAVED_DATA"))
                        savedBills = (List<SavedBill>) getIntent().getSerializableExtra("SAVED_DATA");
                    if(getIntent().hasExtra("RECENT_DATA"))
                        recentBills = (List<RecentBill>) getIntent().getSerializableExtra("RECENT_DATA");
                    if((recentBills !=null && recentBills.size()>0) || (savedBills != null&& savedBills.size()>0)){
                        bundle.putString(Constants.NAME, getString(R.string.link_three));
                        bundle.putSerializable("SAVED_DATA", (Serializable) savedBills);
                        bundle.putSerializable("RECENT_DATA", (Serializable) recentBills);
                        switchFragment(new BillPaySavedNumberSelectFragment(), bundle, 0, true);
                    }else{
                        switchFragment(new LinkThreeSubscriberIdInputFragment(), null, 0, true);
                    }
                    break;
                case BILL_PAY_CARNIVAL:
                    switchFragment(new CarnivalIdInputFragment(), bundle, 0, false);
                    break;
                case CREDIT_CARD:
                    switchFragment(new CreditCardBankSelectionFragment(), bundle, 0, false);
                    break;
                case BILL_PAY_LANKABANGLA_DPS:
                    if(getIntent().hasExtra("SAVED_DATA"))
                        savedBills = (List<SavedBill>) getIntent().getSerializableExtra("SAVED_DATA");
                    if(getIntent().hasExtra("RECENT_DATA"))
                        recentBills = (List<RecentBill>) getIntent().getSerializableExtra("RECENT_DATA");
                    if((recentBills !=null && recentBills.size()>0) || (savedBills != null&& savedBills.size()>0)){
                        bundle.putString(Constants.NAME, getString(R.string.lanka_bangla_dps));
                        bundle.putSerializable("SAVED_DATA", (Serializable) savedBills);
                        bundle.putSerializable("RECENT_DATA", (Serializable) recentBills);
                        switchFragment(new LankaBanglaDPSSavedNumberSelectFragment(), bundle, 0, false);
                    }else{
                        switchFragment(new LankaBanglaDpsNumberInputFragment(), bundle, 0, false);
                    }
                    break;
                case SCHEDULED_PAY_IPDC:
                    if(getIntent().hasExtra(Constants.MOBILE_NUMBER)) {
                        bundle.putString(Constants.MOBILE_NUMBER, getIntent().getStringExtra(Constants.MOBILE_NUMBER));
                        switchFragment(new ScheduledPaymentApiFragment(), bundle, 0, false);
                    }else{
                        switchFragment(new ScheduledPaymentListFragment(), bundle, 0, false);
                    }
                    break;
                default:
                    finish();
            }
        }

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
        return IPayUtilityBillPayActionActivity.this;
    }
}
