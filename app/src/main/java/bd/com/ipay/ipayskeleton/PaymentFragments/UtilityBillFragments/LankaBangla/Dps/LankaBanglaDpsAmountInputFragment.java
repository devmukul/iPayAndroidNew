package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Dps;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractAmountFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class LankaBanglaDpsAmountInputFragment extends IPayAbstractAmountFragment {

	static final String ACCOUNT_NUMBER_KEY = "ACCOUNT_NUMBER";
	static final String ACCOUNT_USER_NAME_KEY = "ACCOUNT_USER_NAME";
	public static final String INSTALLMENT_AMOUNT_KEY = "INSTALLMENT_AMOUNT";


	private String installmentAmount;
	private String accountNumber;
	private String accountUserName;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			installmentAmount = getArguments().getString(INSTALLMENT_AMOUNT_KEY, "");
			accountNumber = getArguments().getString(ACCOUNT_NUMBER_KEY, "");
			accountUserName = getArguments().getString(ACCOUNT_USER_NAME_KEY, "");
		}
	}

	@Override
	protected void setupViewProperties() {
		setBalanceInfoLayoutVisibility(View.VISIBLE);
		hideTransactionDescription();
		setInputType(InputType.TYPE_CLASS_NUMBER);
		setTransactionImageResource(R.drawable.ic_lankabd2);
		setName(accountNumber);
		setUserName(accountUserName);
		setAmount(installmentAmount);
		setAmountFieldEnabled(false);
	}

	@Override
	protected InputFilter getInputFilter() {
		return new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				if (source != null) {
					try {
						String formattedSource = source.subSequence(start, end).toString();

						String destPrefix = dest.subSequence(0, dstart).toString();

						String destSuffix = dest.subSequence(dend, dest.length()).toString();

						String resultString = destPrefix + formattedSource + destSuffix;

						resultString = resultString.replace(",", ".");

						double result = Double.valueOf(resultString);
						if (result > Integer.MAX_VALUE)
							return "";
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		};
	}

	@Override
	protected boolean verifyInput() {
		if (!Utilities.isValueAvailable(businessRules.getMIN_AMOUNT_PER_PAYMENT())
				|| !Utilities.isValueAvailable(businessRules.getMAX_AMOUNT_PER_PAYMENT())) {
			DialogUtils.showDialogForBusinessRuleNotAvailable(getActivity());
			return false;
		} else if (businessRules.isVERIFICATION_REQUIRED() && !ProfileInfoCacheManager.isAccountVerified()) {
			DialogUtils.showDialogVerificationRequired(getActivity());
			return false;
		}

		final String errorMessage;
		if (SharedPrefManager.ifContainsUserBalance()) {
			if (getAmount() == null) {
				errorMessage = getString(R.string.please_enter_amount);
			} else {
				final BigDecimal amount =  BigDecimal.valueOf(getAmount().doubleValue());
				final BigDecimal balance = new BigDecimal(SharedPrefManager.getUserBalance());

				if (amount.compareTo(balance) > 0) {
					errorMessage = getString(R.string.insufficient_balance);
				} else {
					final BigDecimal minimumAmount = businessRules.getMIN_AMOUNT_PER_PAYMENT();
					final BigDecimal maximumAmount = businessRules.getMAX_AMOUNT_PER_PAYMENT().min(balance);
					errorMessage = InputValidator.isValidAmount(getActivity(), amount, minimumAmount, maximumAmount);
				}
			}
		} else {
			errorMessage = getString(R.string.balance_not_available);
		}
		if (errorMessage != null) {
			showErrorMessage(errorMessage);
			return false;
		}
		return true;
	}

	@Override
	protected void performContinueAction() {
		Bundle bundle = new Bundle();
		bundle.putString(ACCOUNT_NUMBER_KEY, accountNumber);
		bundle.putString(ACCOUNT_USER_NAME_KEY, accountUserName);
		bundle.putSerializable(INSTALLMENT_AMOUNT_KEY, getAmount());

		if (getActivity() instanceof IPayUtilityBillPayActionActivity) {
			((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new LankaBanglaDpsBillConfirmationFragment(), bundle, 2, true);
		}
	}

	@Override
	protected int getServiceId() {
		return ServiceIdConstants.UTILITY_BILL_PAYMENT;
	}
}
