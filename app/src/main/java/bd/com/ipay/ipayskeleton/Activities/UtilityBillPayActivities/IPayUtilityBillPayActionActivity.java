package bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import java.util.ArrayList;

import bd.com.ipay.ipayskeleton.Activities.BaseActivity;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.MyCard;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.RailwayTickets.JourneyInfoSelectFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.Carnival.CarnivalIdInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.CreditCard.CreditCardBankSelectionFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.CreditCard.CreditCardListShowFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Card.LankaBanglaCardNumberInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Dps.LankaBanglaDpsNumberInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LinkThree.LinkThreeSubscriberIdInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public final class IPayUtilityBillPayActionActivity extends BaseActivity {

    public static final String BILL_PAY_LANKABANGLA_CARD = "LANKABANGLA_CARD";
    public static final String BILL_PAY_LINK_THREE = "LINK_THREE";
    public static final String BILL_PAY_CARNIVAL = "CARNIVAL";
    public static final String BILL_PAY_PARTY_NAME_KEY = "BILL_PAY_PARTY_NAME";
    public static final String BILL_PAY_LANKABANGLA_DPS = "LANKABANGLA_DPS";
    public static final String CREDIT_CARD = "CREDIT_CARD";
    public static final String RAILWAY_TICKET = "RAILWAY_TICKET";

    public static final String CARD_NUMBER_KEY = "CARD_NUMBER";
    public static final String CARD_USER_NAME_KEY = "CARD_USER_NAME";
    public static final String SAVE_CARD_INFO = "SAVE_CARD_INFO";
    public static final String BANK_ICON = "BANK_ICON";

    public static final String BILL_AMOUNT_KEY = "BILL_AMOUNT";
    public static final String BANK_CODE = "BANK_CODE";

    public ArrayList<MyCard> myCards;

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
        final String billPayPartyName = getIntent().getStringExtra(BILL_PAY_PARTY_NAME_KEY);
        BusinessRuleCacheManager.fetchBusinessRule(this, ServiceIdConstants.UTILITY_BILL_PAYMENT);
        final Bundle bundle = new Bundle();
        switch (billPayPartyName) {
            case BILL_PAY_LANKABANGLA_CARD:
                switchFragment(new LankaBanglaCardNumberInputFragment(), bundle, 0, false);
                break;
            case BILL_PAY_LINK_THREE:
                switchFragment(new LinkThreeSubscriberIdInputFragment(), bundle, 0, false);
                break;
            case BILL_PAY_CARNIVAL:
                switchFragment(new CarnivalIdInputFragment(), bundle, 0, false);
                break;
            case CREDIT_CARD:
                switchFragment(new CreditCardBankSelectionFragment(), bundle, 0, false);
                break;
            case RAILWAY_TICKET:
                switchFragment(new JourneyInfoSelectFragment(), bundle, 0, false);
                break;
            case BILL_PAY_LANKABANGLA_DPS:

                switchFragment(new LankaBanglaDpsNumberInputFragment(), bundle, 0, false);
                break;
            default:
                finish();
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
