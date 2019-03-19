package bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.Card;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.PaymentFragments.BankTransactionFragments.IPayAbstractBankTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractAmountFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DecimalDigitsInputFilter;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.CardChargeDialog;

public class IPayAddMoneyFromCardAmountInputFragment extends IPayAbstractAmountFragment {

    protected String cardType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cardType = getArguments().getString(Constants.CARD_TYPE);
        }
    }

	@Override
	protected void setupViewProperties() {
    	hideTransactionDescription();
    	hideBalance();
		setName(getString(R.string.debit_credit_card));
		setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
		setTransactionImageResource(R.drawable.ic_debit_credit_card_icon);
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

	@Override
	protected void performContinueAction() {
		if (getActivity() instanceof IPayTransactionActionActivity) {
			showCahrgeInfo();
		}
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
		if (getAmount() == null) {
			errorMessage = getString(R.string.please_enter_amount);
		} else {
			final BigDecimal amount =  BigDecimal.valueOf(getAmount().doubleValue());
			final BigDecimal minimumAmount = businessRules.getMIN_AMOUNT_PER_PAYMENT();
			final BigDecimal maximumAmount = businessRules.getMAX_AMOUNT_PER_PAYMENT();
			errorMessage = InputValidator.isValidAmount(getActivity(), amount, minimumAmount, maximumAmount);
		}
		if (errorMessage != null) {
			showErrorMessage(errorMessage);
			return false;
		}
		return true;
	}

	@Override
	protected int getServiceId() {
		return ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD;
	}

	private void showCahrgeInfo() {
		if (getActivity() == null)
			return;

		final CardChargeDialog cardChargeDialog = new CardChargeDialog(getContext());
		cardChargeDialog.setTitle(getString(R.string.confirm_title));
		cardChargeDialog.setCloseButtonAction(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cardChargeDialog.cancel();
			}
		});
		cardChargeDialog.setPayBillButtonAction(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cardChargeDialog.cancel();
				final Bundle bundle = new Bundle();
				bundle.putString(Constants.CARD_TYPE, cardType);
				bundle.putSerializable(IPayAbstractBankTransactionConfirmationFragment.TRANSACTION_AMOUNT_KEY, getAmount());
				((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayAddMoneyFromCardTransactionConfirmationFragment(), bundle, 2, true);
			}
		});
		cardChargeDialog.show();
	}
}
