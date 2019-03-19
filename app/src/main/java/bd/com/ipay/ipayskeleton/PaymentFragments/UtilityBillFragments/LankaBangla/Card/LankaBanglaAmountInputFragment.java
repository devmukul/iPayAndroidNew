package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Card;

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
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class LankaBanglaAmountInputFragment extends IPayAbstractAmountFragment {

    static final String TOTAL_OUTSTANDING_AMOUNT_KEY = "TOTAL_OUTSTANDING";
    static final String MINIMUM_PAY_AMOUNT_KEY = "MINIMUM_PAY";
    static final String CARD_NUMBER_KEY = "CARD_NUMBER";
    static final String CARD_USER_NAME_KEY = "CARD_USER_NAME";

    private int totalOutstandingAmount;
    private int minimumPayAmount;
    private String cardNumber;
    private String cardUserName;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            totalOutstandingAmount = getArguments().getInt(TOTAL_OUTSTANDING_AMOUNT_KEY, 0);
            minimumPayAmount = getArguments().getInt(MINIMUM_PAY_AMOUNT_KEY, 0);
            cardNumber = getArguments().getString(CARD_NUMBER_KEY, "");
            cardUserName = getArguments().getString(CARD_USER_NAME_KEY, "");
        }
    }

    @Override
    protected void setupViewProperties() {
        if (totalOutstandingAmount > 0)
            addShortCutOption(1, getString(R.string.total_outstanding).toUpperCase(), totalOutstandingAmount);
        if (minimumPayAmount > 0)
            addShortCutOption(2, getString(R.string.minimum_pay).toUpperCase(), minimumPayAmount);

        setBalanceInfoLayoutVisibility(View.VISIBLE);
        hideTransactionDescription();
        setInputType(InputType.TYPE_CLASS_NUMBER);
        setTransactionImageResource(R.drawable.ic_lankabd2);
        setName(CardNumberValidator.deSanitizeEntry(cardNumber, ' '));
        setUserName(cardUserName);
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
        bundle.putSerializable(LankaBanglaBillConfirmationFragment.BILL_AMOUNT_KEY, getAmount());
        bundle.putSerializable(LankaBanglaAmountInputFragment.CARD_USER_NAME_KEY, cardUserName);

        if (getAmount().intValue() == minimumPayAmount)
            bundle.putString(LankaBanglaBillConfirmationFragment.AMOUNT_TYPE_KEY, Constants.MINIMUM_PAY);
        else if (getAmount().intValue() == totalOutstandingAmount)
            bundle.putString(LankaBanglaBillConfirmationFragment.AMOUNT_TYPE_KEY, Constants.CREDIT_BALANCE);
        else
            bundle.putString(LankaBanglaBillConfirmationFragment.AMOUNT_TYPE_KEY, Constants.OTHER);

        if (getActivity() instanceof IPayUtilityBillPayActionActivity) {
            ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new LankaBanglaBillConfirmationFragment(), bundle, 3, true);
        }
    }

    @Override
    protected int getServiceId() {
        return ServiceIdConstants.UTILITY_BILL_PAYMENT;
    }
}
