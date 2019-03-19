package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments;


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
import bd.com.ipay.ipayskeleton.Utilities.CardNumberValidator;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class CreditCardAmountInputFragment extends IPayAbstractAmountFragment {
    static final String CARD_NUMBER_KEY = "CARD_NUMBER";
    static final String CARD_USER_NAME_KEY = "CARD_USER_NAME";

    private String cardNumber;
    private String cardUserName;
    private boolean saveCardInfo;
    private int bankIconId;
    private String selectedBankCode;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cardNumber = getArguments().getString(CARD_NUMBER_KEY, "");
            cardUserName = getArguments().getString(CARD_USER_NAME_KEY, "");
            saveCardInfo = getArguments().getBoolean(IPayUtilityBillPayActionActivity.SAVE_CARD_INFO, false);
            bankIconId = getArguments().getInt(IPayUtilityBillPayActionActivity.BANK_ICON, 0);
            selectedBankCode = getArguments().getString(IPayUtilityBillPayActionActivity.BANK_CODE, "");
        }
    }

    @Override
    protected void setupViewProperties() {
        setBalanceInfoLayoutVisibility(View.VISIBLE);
        hideTransactionDescription();
        setInputType(InputType.TYPE_CLASS_NUMBER);
        setTransactionImageResource(bankIconId);
        setName(CardNumberValidator.deSanitizeEntry(cardNumber, ' '));
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
        if (getAmount() == null)
            return;

        Bundle bundle = new Bundle();
        bundle.putString(CARD_NUMBER_KEY, cardNumber);
        bundle.putSerializable(IPayUtilityBillPayActionActivity.BILL_AMOUNT_KEY, getAmount());
        bundle.putSerializable(CARD_USER_NAME_KEY, cardUserName);
        bundle.putSerializable(IPayUtilityBillPayActionActivity.SAVE_CARD_INFO, saveCardInfo);
        bundle.putSerializable(IPayUtilityBillPayActionActivity.BANK_ICON, bankIconId);
        bundle.putSerializable(IPayUtilityBillPayActionActivity.BANK_CODE, selectedBankCode);

        if (getActivity() instanceof IPayUtilityBillPayActionActivity) {
            ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new CreditCardBillPaymentConfirmationFragment(), bundle, 3, true);
        }
    }

    @Override
    protected int getServiceId() {
        return ServiceIdConstants.UTILITY_BILL_PAYMENT;
    }
}
