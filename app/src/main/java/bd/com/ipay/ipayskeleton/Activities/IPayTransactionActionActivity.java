package bd.com.ipay.ipayskeleton.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import bd.com.ipay.ipayskeleton.PaymentFragments.IPayTransactionAmountInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.SendMoneyFragments.IPayTransactionContactFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IPayTransactionActionActivity extends BaseActivity {

    public static final String TRANSACTION_TYPE_KEY = "TRANSACTION_TYPE";
    public static final int TRANSACTION_TYPE_INVALID = -1;
    public static final int TRANSACTION_TYPE_SEND_MONEY = ServiceIdConstants.SEND_MONEY;
    public static final int TRANSACTION_TYPE_REQUEST_MONEY = ServiceIdConstants.REQUEST_MONEY;

    private int transactionType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipay_transaction_action);

        transactionType = getIntent().getIntExtra(TRANSACTION_TYPE_KEY, TRANSACTION_TYPE_INVALID);
        final Bundle bundle = new Bundle();
        bundle.putInt(TRANSACTION_TYPE_KEY, transactionType);
        switch (transactionType) {
            case TRANSACTION_TYPE_SEND_MONEY:
            case TRANSACTION_TYPE_REQUEST_MONEY:
                BusinessRuleCacheManager.fetchBusinessRule(this, transactionType);
                if (!getIntent().getBooleanExtra(Constants.FROM_CONTACT, false) &&
                        !getIntent().getBooleanExtra(Constants.FROM_QR_SCAN, false)) {
                    switchToTransactionContactsFragment(bundle);
                } else {
                    bundle.putString(Constants.MOBILE_NUMBER, getIntent().getStringExtra(Constants.MOBILE_NUMBER));
                    bundle.putString(Constants.NAME, getIntent().getStringExtra(Constants.NAME));
                    bundle.putString(Constants.PHOTO_URI, getIntent().getStringExtra(Constants.PHOTO_URI));
                    switchToAmountInputFragment(bundle);
                }
                break;
            case TRANSACTION_TYPE_INVALID:
            default:
                Toaster.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT);
                finish();
                break;
        }
    }

    private void switchToTransactionContactsFragment(Bundle bundle) {
        switchFragment(new IPayTransactionContactFragment(), bundle, 0, false);
    }

    public void switchToAmountInputFragment(@NonNull Bundle bundle) {
        switchFragment(new IPayTransactionAmountInputFragment(), bundle, 1, true);
    }

    public void switchToTransactionConfirmationFragment(@NonNull Bundle bundle) {
        switchFragment(new IPayTransactionConfirmationFragment(), bundle, 2, true);
    }

    public void switchToTransactionSuccessFragment(@NonNull Bundle bundle) {
        switchFragment(new IPayTransactionSuccessFragment(), bundle, 3, true);
    }

    private void switchFragment(@NonNull Fragment fragment, @NonNull Bundle bundle, int maxBackStackEntryCount, boolean shouldAnimate) {
        if (getSupportFragmentManager().getBackStackEntryCount() > maxBackStackEntryCount) {
            getSupportFragmentManager().popBackStackImmediate();
        }
        if (!bundle.containsKey(TRANSACTION_TYPE_KEY) || TextUtils.isEmpty(bundle.getString(TRANSACTION_TYPE_KEY))) {
            bundle.putInt(TRANSACTION_TYPE_KEY, transactionType);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Utilities.hideKeyboard(this);

        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof IPayTransactionSuccessFragment) {
                    finish();
                    return;
                }
            }
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            finish();
        }
    }

    @Override
    protected Context setContext() {
        return IPayTransactionActionActivity.this;
    }
}