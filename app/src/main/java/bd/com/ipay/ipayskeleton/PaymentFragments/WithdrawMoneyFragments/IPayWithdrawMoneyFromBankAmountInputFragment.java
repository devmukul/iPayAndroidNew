package bd.com.ipay.ipayskeleton.PaymentFragments.WithdrawMoneyFragments;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Balance.CreditBalanceResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.BankTransactionFragments.IPayAbstractBankTransactionAmountInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.BankTransactionFragments.IPayAbstractBankTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.WithdrawMoneyDetailsDialog;

public class IPayWithdrawMoneyFromBankAmountInputFragment extends IPayAbstractBankTransactionAmountInputFragment {

	@Override
	protected void setupViewProperties() {
		hideTransactionDescription();
		setName(bankAccountList.getBankName());
		setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
		setTransactionImageResource(bankAccountList.getBankIcon(getContext()));
		setBalanceType(BalanceType.SETTLED_BALANCE);
	}

	@Override
	protected void performContinueAction() {
		if (getActivity() instanceof IPayTransactionActionActivity) {
			double charge = flatRate + ((Double.valueOf( variableRate )*(getAmount().doubleValue()))/100);

			if(maxRate!=0 && maxRate<charge)
				charge = maxRate;
			if(charge>0) {
				showInfo(getAmount().doubleValue(), charge);
			}else {
				final Bundle bundle = new Bundle();
				bundle.putParcelable(Constants.SELECTED_BANK_ACCOUNT, bankAccountList);
				bundle.putBoolean("IS_INSTANT", isInstant);
				bundle.putSerializable(IPayAbstractBankTransactionConfirmationFragment.TRANSACTION_AMOUNT_KEY, getAmount());
				((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayWithdrawMoneyFromBankConfirmationFragment(), bundle, 2, true);
			}
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
		if (SharedPrefManager.ifContainsUserBalance()) {
			if (getAmount() == null) {
				errorMessage = getString(R.string.please_enter_amount);
			} else {
				final BigDecimal amount =  BigDecimal.valueOf(getAmount().doubleValue());
				final CreditBalanceResponse creditBalanceResponse = SharedPrefManager.getCreditBalance();
				final BigDecimal balance = new BigDecimal(SharedPrefManager.getUserBalance());
				final BigDecimal unsettledBalance = creditBalanceResponse.getCreditLimit().subtract(creditBalanceResponse.getAvailableCredit());
				final BigDecimal settledBalance = balance.subtract(unsettledBalance);
				if (amount.compareTo(settledBalance) > 0) {
					errorMessage = getString(R.string.insufficient_balance);
				} else {
					final BigDecimal minimumAmount = businessRules.getMIN_AMOUNT_PER_PAYMENT();
					final BigDecimal maximumAmount = businessRules.getMAX_AMOUNT_PER_PAYMENT().min(settledBalance);
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
	protected int getServiceId() {
		return ServiceIdConstants.WITHDRAW_MONEY;
	}

	private void showInfo(double ammount, double charge) {
		if (getActivity() == null)
			return;

		final WithdrawMoneyDetailsDialog billDetailsDialog = new WithdrawMoneyDetailsDialog(getContext());
		billDetailsDialog.setTitle(getString(R.string.transaction_details));
		billDetailsDialog.setClientLogoImageResource(bankAccountList.getBankIcon(getContext()));
		billDetailsDialog.setBillTitleInfo(bankAccountList.getBankName());
		billDetailsDialog.setBillSubTitleInfo(getString(R.string.withdraw_money));

		billDetailsDialog.setRequestAmountInfo(ammount);
		billDetailsDialog.setChargeInfo(charge);
		billDetailsDialog.setTotalInfo(ammount + charge);

		billDetailsDialog.setCloseButtonAction(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				billDetailsDialog.cancel();
			}
		});
		billDetailsDialog.setPayBillButtonAction(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				billDetailsDialog.cancel();

				final String errorMessage;

				final BigDecimal amount =  BigDecimal.valueOf(getAmount().doubleValue());
				final CreditBalanceResponse creditBalanceResponse = SharedPrefManager.getCreditBalance();
				final BigDecimal balance = new BigDecimal(SharedPrefManager.getUserBalance());
				final BigDecimal unsettledBalance = creditBalanceResponse.getCreditLimit().subtract(creditBalanceResponse.getAvailableCredit());
				final BigDecimal settledBalance = balance.subtract(unsettledBalance);
				if (amount.compareTo(settledBalance) > 0) {
					errorMessage = getString(R.string.insufficient_balance);
				} else {
					final BigDecimal minimumAmount = businessRules.getMIN_AMOUNT_PER_PAYMENT();
					final BigDecimal maximumAmount = businessRules.getMAX_AMOUNT_PER_PAYMENT().min(settledBalance);
					errorMessage = InputValidator.isValidAmount(getActivity(), amount, minimumAmount, maximumAmount);
				}

				if (errorMessage != null) {
					showErrorMessage(errorMessage);
				}else {
					final Bundle bundle = new Bundle();
					bundle.putParcelable(Constants.SELECTED_BANK_ACCOUNT, bankAccountList);
					bundle.putBoolean("IS_INSTANT", isInstant);
					bundle.putSerializable(IPayAbstractBankTransactionConfirmationFragment.TRANSACTION_AMOUNT_KEY, getAmount());
					((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayWithdrawMoneyFromBankConfirmationFragment(), bundle, 2, true);
				}

			}
		});
		billDetailsDialog.show();
	}


}
