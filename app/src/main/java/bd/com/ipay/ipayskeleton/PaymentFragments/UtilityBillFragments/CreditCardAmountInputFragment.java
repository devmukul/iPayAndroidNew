package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Balance.CreditBalanceResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.ServiceCharge.GetServiceChargeRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.ServiceCharge.GetServiceChargeResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Notification.GetMoneyAndPaymentRequestResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Introducer.GetPendingIntroducerListResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.IntroductionAndInvite.GetIntroductionRequestsResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.GetScheduledPaymentInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.GroupedScheduledPaymentList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.ReceiverInfo;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.ScheduledPaymentInfo;
import bd.com.ipay.ipayskeleton.PaymentFragments.BankTransactionFragments.IPayAbstractBankTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractAmountFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.WithdrawMoneyFragments.IPayWithdrawMoneyFromBankConfirmationFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.GetBeneficiaryListResponse;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.GetSponsorListResponse;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.CardNumberValidator;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.CardChargeDialog;
import bd.com.ipay.ipayskeleton.Widget.View.CreditCardChargeDialog;

public class CreditCardAmountInputFragment extends IPayAbstractAmountFragment implements HttpResponseListener {
    static final String CARD_NUMBER_KEY = "CARD_NUMBER";
    static final String CARD_USER_NAME_KEY = "CARD_USER_NAME";

    private String cardNumber;
    private String cardUserName;
    private boolean saveCardInfo;
    private int bankIconId;
    private String selectedBankCode;

    private HttpRequestPostAsyncTask mServiceChargeTask = null;
    private GetServiceChargeResponse mGetServiceChargeResponse;
    private CustomProgressDialog mProgressDialog;
    private BigDecimal mServiceCharge;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = new CustomProgressDialog(getActivity());
        if (getArguments() != null) {
            cardNumber = getArguments().getString(CARD_NUMBER_KEY, "");
            cardUserName = getArguments().getString(CARD_USER_NAME_KEY, "");
            saveCardInfo = getArguments().getBoolean(IPayUtilityBillPayActionActivity.SAVE_CARD_INFO, false);
            bankIconId = getArguments().getInt(IPayUtilityBillPayActionActivity.BANK_ICON, 0);
            System.out.println("Image Resource "+bankIconId);
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


        attemptGetServiceCharge(Constants.SERVICE_ID_CREDIT_CARD_BILL_PAYMENT);

    }

    @Override
    protected int getServiceId() {
        return ServiceIdConstants.UTILITY_BILL_PAYMENT;
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        try {

            if (HttpErrorHandler.isErrorFound(result, getActivity(), mProgressDialog)) {
                mServiceChargeTask = null;
                if (mProgressDialog != null) {
                    mProgressDialog.dismissDialogue();
                }
                return;
            }

            Gson gson = new Gson();

            switch (result.getApiCommand()) {

                case Constants.COMMAND_GET_SERVICE_CHARGE:
                    mProgressDialog.dismissDialogue();
                    try {
                        mGetServiceChargeResponse = gson.fromJson(result.getJsonString(), GetServiceChargeResponse.class);

                        if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                            if (mGetServiceChargeResponse != null) {
                                mServiceCharge = mGetServiceChargeResponse.getServiceCharge(new BigDecimal(getAmount().toString()));

                                if (mServiceCharge.compareTo(BigDecimal.ZERO) < 0) {
                                    Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
                                }else if(mServiceCharge.compareTo(BigDecimal.ZERO) == 0) {
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
                                }else {
                                    launchChargeDialogue(getAmount().doubleValue(), mServiceCharge.doubleValue());
                                }

                            } else {
                                Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
                                return;
                            }
                        } else {
                            if (getActivity() != null) {
                                Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT);
                    }

                    mServiceChargeTask = null;
                    break;


                default:
                    break;
            }
        } catch (Exception e) {
            mServiceChargeTask = null;
            if (mProgressDialog != null) {
                mProgressDialog.dismissDialogue();
            }

        }

    }

    private void launchChargeDialogue(double ammount, double charge) {
        if (getActivity() == null)
            return;

        System.out.println("Image Resource "+bankIconId);

        final CreditCardChargeDialog billDetailsDialog = new CreditCardChargeDialog(getContext());
        billDetailsDialog.setTitle(getString(R.string.credit_card_bill_title));
        billDetailsDialog.setClientLogoImageResource(bankIconId);
        billDetailsDialog.setBillTitleInfo(cardNumber);
        billDetailsDialog.setBillSubTitleInfo(cardUserName);

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

            }
        });
        billDetailsDialog.show();
    }

    private void attemptGetServiceCharge(int serviceId) {

        if (mServiceChargeTask != null) {
            return;
        }

        mProgressDialog.show();

        int accountType = ProfileInfoCacheManager.getAccountType();
        int accountClass = Constants.DEFAULT_USER_CLASS;

        GetServiceChargeRequest mServiceChargeRequest = new GetServiceChargeRequest(serviceId, accountType, accountClass);
        Gson gson = new Gson();
        String json = gson.toJson(mServiceChargeRequest);
        mServiceChargeTask = new HttpRequestPostAsyncTask(Constants.COMMAND_GET_SERVICE_CHARGE,
                Constants.BASE_URL_SM + Constants.URL_SERVICE_CHARGE, json, getActivity(), true);
        mServiceChargeTask.mHttpResponseListener = this;
        mServiceChargeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
