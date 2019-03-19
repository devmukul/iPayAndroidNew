package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.CreditCardBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GenericBillPayResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.CreditCard.CreditCardBillSuccessFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CardNumberValidator;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;


public class CreditCardBillPaymentConfirmationFragment extends IPayAbstractTransactionConfirmationFragment {
    protected static final String BILL_AMOUNT_KEY = "BILL_AMOUNT";
    protected static final String AMOUNT_TYPE_KEY = "AMOUNT_TYPE";
    private final Gson gson = new Gson();

    private HttpRequestPostAsyncTask creditCardBillPayTask = null;

    private String amountType;
    private Number billAmount;
    private String cardNumber;
    private String cardUserName;
    private boolean saveCardInfo;
    private int bankIconId;
    private String selectedBankCode;

    private String uri;
    private CreditCardBillPayRequest creditCardBillPayRequest;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            billAmount = (Number) getArguments().getSerializable(BILL_AMOUNT_KEY);
            cardNumber = getArguments().getString(IPayUtilityBillPayActionActivity.CARD_NUMBER_KEY, "");
            cardUserName = getArguments().getString(IPayUtilityBillPayActionActivity.CARD_USER_NAME_KEY, "");
            saveCardInfo = getArguments().getBoolean(IPayUtilityBillPayActionActivity.SAVE_CARD_INFO, false);
            bankIconId = getArguments().getInt(IPayUtilityBillPayActionActivity.BANK_ICON, 0);
            selectedBankCode = getArguments().getString(IPayUtilityBillPayActionActivity.BANK_CODE, "");
        }
    }

    @Override
    protected void setupViewProperties() {
        setTransactionImageResource(bankIconId);
        setTransactionDescription(getStyledTransactionDescription(R.string.pay_bill_confirmation_message, billAmount));
        setName(CardNumberValidator.deSanitizeEntry(cardNumber, ' '));
    }

    @Override
    protected boolean isPinRequired() {
        return true;
    }

    @Override
    protected boolean canUserAddNote() {
        return false;
    }

    @Override
    protected String getTrackerCategory() {
        return Constants.LANKA_BANGLA_BILL_PAY;
    }

    @Override
    protected boolean verifyInput() {
        final String errorMessage;
        if (TextUtils.isEmpty(getPin())) {
            errorMessage = getString(R.string.please_enter_a_pin);
        } else if (getPin().length() != 4) {
            errorMessage = getString(R.string.minimum_pin_length_message);
        } else {
            errorMessage = null;
        }
        if (errorMessage != null) {
            showErrorMessage(errorMessage);
            return false;
        }
        return true;
    }

    @Override
    protected void performContinueAction() {
        if (!Utilities.isConnectionAvailable(getContext())) {
            Toaster.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT);
        }
        if (creditCardBillPayTask == null) {
            creditCardBillPayRequest = new CreditCardBillPayRequest(billAmount.toString(), getPin(), "",
                    cardNumber, cardUserName, saveCardInfo, selectedBankCode);
            String json = gson.toJson(creditCardBillPayRequest);
            CardNumberValidator.Cards cards = CardNumberValidator.getCardType(cardNumber);
            if (cards != null) {
                uri = Constants.BASE_URL_SM + Constants.URL_CREDIT_CARD_BILL_PAY;
            } else {
                return;
            }
            creditCardBillPayTask = new HttpRequestPostAsyncTask(Constants.COMMAND_CREDIT_CARD_BILL_PAY,
                    uri, json, getActivity(), this, false);
            creditCardBillPayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            customProgressDialog.showDialog();
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (getActivity() == null)
            return;

        if (HttpErrorHandler.isErrorFound(result, getContext(), customProgressDialog)) {
            creditCardBillPayTask = null;
        } else {
            try {
                switch (result.getApiCommand()) {
                    case Constants.COMMAND_CREDIT_CARD_BILL_PAY:
                        final GenericBillPayResponse mGenericBillPayResponse = gson.fromJson(result.getJsonString(), GenericBillPayResponse.class);
                        switch (result.getStatus()) {
                            case Constants.HTTP_RESPONSE_STATUS_PROCESSING:
                                customProgressDialog.dismissDialog();
                                if (getActivity() != null)
                                    Utilities.hideKeyboard(getActivity());
                                Toaster.makeText(getContext(), R.string.request_on_process, Toast.LENGTH_SHORT);
                                if (getActivity() != null) {
                                    Utilities.hideKeyboard(getActivity());
                                    getActivity().finish();
                                }
                                break;
                            case Constants.HTTP_RESPONSE_STATUS_OK:
                                if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                                    mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                                } else {
                                    customProgressDialog.setTitle(R.string.success);
                                    customProgressDialog.showSuccessAnimationAndMessage(mGenericBillPayResponse.getMessage());
                                }
                                sendSuccessEventTracking(billAmount);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        customProgressDialog.dismissDialog();
                                        Bundle bundle = new Bundle();
                                        bundle.putString(IPayUtilityBillPayActionActivity.CARD_NUMBER_KEY, cardNumber);
                                        bundle.putSerializable(IPayUtilityBillPayActionActivity.BILL_AMOUNT_KEY, billAmount);
                                        bundle.putString(IPayUtilityBillPayActionActivity.CARD_USER_NAME_KEY, cardUserName);
                                        bundle.putInt(IPayUtilityBillPayActionActivity.BANK_ICON, bankIconId);
                                        if (getActivity() instanceof IPayUtilityBillPayActionActivity) {
                                            ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new CreditCardBillSuccessFragment(), bundle, 4, true);
                                        }

                                    }
                                }, 2000);
                                if (getActivity() != null)
                                    Utilities.hideKeyboard(getActivity());
                                break;
                            case Constants.HTTP_RESPONSE_STATUS_BLOCKED:
                                if (getActivity() != null) {
                                    customProgressDialog.setTitle(R.string.failed);
                                    customProgressDialog.showFailureAnimationAndMessage(mGenericBillPayResponse.getMessage());
                                    sendBlockedEventTracking(billAmount);
                                }
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((MyApplication) getActivity().getApplication()).launchLoginPage(mGenericBillPayResponse.getMessage());
                                    }
                                }, 2000);
                                break;
                            case Constants.HTTP_RESPONSE_STATUS_ACCEPTED:
                            case Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED:
                                customProgressDialog.dismissDialog();
                                Toast.makeText(getActivity(), mGenericBillPayResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                launchOTPVerification(mGenericBillPayResponse.getOtpValidFor(), gson.toJson(creditCardBillPayRequest), Constants.COMMAND_LANKABANGLA_BILL_PAY, uri);
                                break;
                            case Constants.HTTP_RESPONSE_STATUS_BAD_REQUEST:
                                final String errorMessage;
                                if (!TextUtils.isEmpty(mGenericBillPayResponse.getMessage())) {
                                    errorMessage = mGenericBillPayResponse.getMessage();
                                } else {
                                    errorMessage = getString(R.string.server_down);
                                }
                                customProgressDialog.setTitle(R.string.failed);
                                customProgressDialog.showFailureAnimationAndMessage(errorMessage);
                                break;
                            default:
                                if (getActivity() != null) {
                                    if (mOTPVerificationForTwoFactorAuthenticationServicesDialog == null) {
                                        customProgressDialog.showFailureAnimationAndMessage(mGenericBillPayResponse.getMessage());
                                    } else {
                                        Toast.makeText(getContext(), mGenericBillPayResponse.getMessage(), Toast.LENGTH_LONG).show();
                                    }

                                    if (mGenericBillPayResponse.getMessage().toLowerCase().contains(TwoFactorAuthConstants.WRONG_OTP)) {
                                        if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.showOtpDialog();
                                            customProgressDialog.dismissDialog();
                                        }
                                    } else {
                                        if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                                        }
                                    }
                                    //Google Analytic event
                                    sendFailedEventTracking(mGenericBillPayResponse.getMessage(), billAmount);
                                    break;
                                }
                                break;
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                customProgressDialog.showFailureAnimationAndMessage(getString(R.string.payment_failed));
                sendFailedEventTracking(e.getMessage(), billAmount);
            }
            creditCardBillPayTask = null;
        }
    }
}