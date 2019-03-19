package bd.com.ipay.ipayskeleton.PaymentFragments.TopupFragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractAmountFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DecimalDigitsInputFilter;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IPayTopUpAmountInputFragment extends IPayAbstractAmountFragment {

	public static final String TRANSACTION_AMOUNT_KEY = "TRANSACTION_AMOUNT";
	private String name;
	private String mobileNumber;
	private String profilePicture;
	private String operatorCode;
	private int operatorType;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			name = getArguments().getString(Constants.NAME);
			mobileNumber = getArguments().getString(Constants.MOBILE_NUMBER);
			profilePicture = getArguments().getString(Constants.PHOTO_URI);
			operatorCode = getArguments().getString(Constants.OPERATOR_CODE);
			operatorType = getArguments().getInt(Constants.OPERATOR_TYPE);
		}
	}

	@Override
	protected void setupViewProperties() {
		hideTransactionDescription();
		setName(name);
		setUserName(mobileNumber);
		setInputType(InputType.TYPE_CLASS_NUMBER);
		setTransactionImage(profilePicture);
		addShortCutOption(1,"",50);
		addShortCutOption(2,"",100);
		addShortCutOption(3,"",200);
		addShortCutOption(4,"",500);
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
				final BigDecimal amount = BigDecimal.valueOf(getAmount().doubleValue());
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
		if (getActivity() instanceof IPayTransactionActionActivity) {
			final Bundle bundle = new Bundle();
			bundle.putString(Constants.NAME, name);
			bundle.putString(Constants.MOBILE_NUMBER, mobileNumber);
			bundle.putString(Constants.PHOTO_URI, profilePicture);
			bundle.putInt(Constants.OPERATOR_TYPE, operatorType);
			bundle.putString(Constants.OPERATOR_CODE, operatorCode);
			bundle.putSerializable(TRANSACTION_AMOUNT_KEY, getAmount());
			((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayTopUpConfirmationFragment(), bundle, 2, true);
		}
	}

	@Override
	protected int getServiceId() {
		return ServiceIdConstants.SEND_MONEY;
	}

	@Override
	protected InputFilter getInputFilter() {
		return new DecimalDigitsInputFilter() {
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
				return super.filter(source, start, end, dest, dstart, dend);
			}
		};
	}
}
