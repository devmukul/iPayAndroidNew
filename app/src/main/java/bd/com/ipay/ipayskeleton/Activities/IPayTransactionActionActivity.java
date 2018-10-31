package bd.com.ipay.ipayskeleton.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.Toast;

import bd.com.ipay.ipayskeleton.HomeFragments.PayDashBoardFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.IPayAddMoneyFromBankOptionFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.IPayAddMoneyFromCardAmountInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.IPayAddMoneyOptionFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayTransactionAmountInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayTransactionSuccessFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments.IPayMakePaymentFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments.MakePaymentNewFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.SendMoneyFragments.IPayTransactionContactFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.WithdrawMoneyFragments.IPayWithdrawMoneyFromBankOptionFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IPayTransactionActionActivity extends BaseActivity {

	public static final String TRANSACTION_TYPE_KEY = "TRANSACTION_TYPE";
	public static final int TRANSACTION_TYPE_INVALID = -1;
	// 1
	public static final int TRANSACTION_TYPE_SEND_MONEY = ServiceIdConstants.SEND_MONEY;
	// 3001 | 3011 = 3067
	public static final int TRANSACTION_TYPE_ADD_MONEY = ServiceIdConstants.ADD_MONEY_BY_BANK | ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD;
	// 3001
	public static final int TRANSACTION_TYPE_ADD_MONEY_BY_BANK = ServiceIdConstants.ADD_MONEY_BY_BANK;
	// 3002
	public static final int TRANSACTION_TYPE_WITHDRAW_MONEY = ServiceIdConstants.WITHDRAW_MONEY;
	// 3011
	public static final int TRANSACTION_TYPE_ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD = ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD;
	// 6001
	public static final int TRANSACTION_TYPE_REQUEST_MONEY = ServiceIdConstants.REQUEST_MONEY;
	// 6002
	public static final int TRANSACTION_TYPE_MAKE_PAYMENT = ServiceIdConstants.MAKE_PAYMENT;
	// 2001
	public static final int TRANSACTION_TYPE_TOP_UP = ServiceIdConstants.TOP_UP;

	private int transactionType;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ipay_transaction_action);

		transactionType = getIntent().getIntExtra(TRANSACTION_TYPE_KEY, TRANSACTION_TYPE_INVALID);
		final Bundle bundle = new Bundle();
		bundle.putInt(TRANSACTION_TYPE_KEY, transactionType);
		switch (transactionType) {
			case TRANSACTION_TYPE_ADD_MONEY:
				switchToAddMoneyOptionFragment(bundle);
				break;
			case TRANSACTION_TYPE_ADD_MONEY_BY_BANK:
				BusinessRuleCacheManager.fetchBusinessRule(this, transactionType);
				switchToAddMoneyFromBankOptionFragment(bundle);
				break;
			case TRANSACTION_TYPE_WITHDRAW_MONEY:
				BusinessRuleCacheManager.fetchBusinessRule(this, transactionType);
				switchToWithdrawMoneyFromBankOptionFragment(bundle);
				break;
			case TRANSACTION_TYPE_ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD:
				BusinessRuleCacheManager.fetchBusinessRule(this, transactionType);
				switchFragment(new IPayAddMoneyFromCardAmountInputFragment(), bundle, 0, true);
				break;
			case TRANSACTION_TYPE_MAKE_PAYMENT:
				BusinessRuleCacheManager.fetchBusinessRule(this, transactionType);
				if (!getIntent().getBooleanExtra(Constants.FROM_CONTACT, false) &&
						!getIntent().getBooleanExtra(Constants.FROM_QR_SCAN, false)) {
					switchToMakePaymentFragment(bundle);
				} else {
					bundle.putString(Constants.MOBILE_NUMBER, getIntent().getStringExtra(Constants.MOBILE_NUMBER));
					bundle.putString(Constants.NAME, getIntent().getStringExtra(Constants.NAME));
					bundle.putString(Constants.PHOTO_URI, getIntent().getStringExtra(Constants.PHOTO_URI));
                    bundle.putString(Constants.ADDRESS, getIntent().getStringExtra(Constants.ADDRESS));
                    if(getIntent().hasExtra(Constants.OUTLET_ID))
						bundle.putString(Constants.OUTLET_ID, getIntent().getStringExtra(Constants.OUTLET_ID));
					switchToAmountInputFragment(bundle);
				}
				break;
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
			case TRANSACTION_TYPE_TOP_UP:
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

	private void switchToAddMoneyOptionFragment(@NonNull Bundle bundle) {
		switchFragment(new IPayAddMoneyOptionFragment(), bundle, 0, false);
	}

	private void switchToWithdrawMoneyFromBankOptionFragment(@NonNull Bundle bundle) {
		switchFragment(new IPayWithdrawMoneyFromBankOptionFragment(), bundle, 0, false);
	}

	private void switchToAddMoneyFromBankOptionFragment(@NonNull Bundle bundle) {
		switchFragment(new IPayAddMoneyFromBankOptionFragment(), bundle, 0, false);
	}

	private void switchToTransactionContactsFragment(@NonNull Bundle bundle) {
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

    private void switchToMakePaymentFragment(@NonNull Bundle bundle) {
        switchFragment(new IPayMakePaymentFragment(), bundle, 0, true);
    }

	public void switchFragment(@NonNull Fragment fragment, @NonNull Bundle bundle, int maxBackStackEntryCount, boolean shouldAnimate) {
		if (getSupportFragmentManager().getBackStackEntryCount() > maxBackStackEntryCount) {
			getSupportFragmentManager().popBackStackImmediate();
		}
		if (!bundle.containsKey(TRANSACTION_TYPE_KEY) || bundle.getInt(TRANSACTION_TYPE_KEY, TRANSACTION_TYPE_INVALID) == TRANSACTION_TYPE_INVALID) {
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
				if (fragment instanceof IPayTransactionSuccessFragment || fragment instanceof IPayAbstractTransactionSuccessFragment) {
					finish();
					return;
				} else if (fragment instanceof IPayAddMoneyOptionFragment) {
					if (((IPayAddMoneyOptionFragment) fragment).onBackPressed()) {
						return;
					}
				}
			}
			getSupportFragmentManager().popBackStackImmediate();
		} else {
			for (Fragment fragment : getSupportFragmentManager().getFragments()) {
				if (fragment instanceof IPayAddMoneyOptionFragment) {
					if (((IPayAddMoneyOptionFragment) fragment).onBackPressed()) {
						return;
					}
				}
			}
			finish();
		}
	}

	@Override
	protected Context setContext() {
		return IPayTransactionActionActivity.this;
	}
}
