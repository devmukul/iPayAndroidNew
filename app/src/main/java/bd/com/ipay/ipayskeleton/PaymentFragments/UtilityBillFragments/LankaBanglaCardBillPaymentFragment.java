package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomPinCheckerWithInputDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.LankaBanglaAmountSelectorDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationForTwoFactorAuthenticationServicesDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.BusinessRuleV2;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.MandatoryBusinessRules;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.Rule;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GenericBillPayResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.LankaBanglaCardBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.LankaBanglaCustomerResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleConstants;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class LankaBanglaCardBillPaymentFragment extends BaseFragment implements HttpResponseListener {
    private TextView mNameTextView;
    private EditText mCardNumberEditText;
    private TextView mCreditBalanceView;
    private TextView mMinimumPayView;
    private EditText mAmountSelectorEditText;
    private EditText mOtherAmountEditText;
    private Button mContinueButton;
    private View infoView;
    private TextView mCardNumberView;
    private ProgressDialog mProgressDialog;
    private OTPVerificationForTwoFactorAuthenticationServicesDialog mOTPVerificationForTwoFactorAuthenticationServicesDialog;

    private String mAmount;
    private String mCardNumber;

    private String cardType;

    private HttpRequestGetAsyncTask mGetLankaBanglaCardUserInfo = null;
    private LankaBanglaCustomerResponse lankaBanglaCustomerResponse;
    private HttpRequestPostAsyncTask mLankaBanglaCardBillPayTask = null;
    private LankaBanglaCardBillPayRequest mLankaBanglaCardBillPayRequest;
    private HttpRequestGetAsyncTask mGetBusinessRuleTask;
    private GenericBillPayResponse mGenericBillPayResponse;
    private CustomProgressDialog mCustomProgressDialog;
    private LankaBanglaAmountSelectorDialog lankaBanglaAmountSelectorDialog;
    private List<String> mAmountTypes;
    private String mAmountType;
    private String mMinimumPayAmount;
    private String mCreditBalanceAmount;
    private View amountLayout;

    private View mCustomerIDView;
    private View mInfoView;

    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lanka_bangla_card_bill_pay, container, false);
        if (getActivity() == null)
            return view;
        getActivity().setTitle("Lanka Bangla");
        attemptGetBusinessRule(ServiceIdConstants.UTILITY_BILL_PAYMENT);
        mProgressDialog = new ProgressDialog(getContext());
        mCustomProgressDialog = new CustomProgressDialog(getContext());
        setUpView(view);
        return view;
    }

    private void setUpView(View view) {
        if (UtilityBillPaymentActivity.mMandatoryBusinessRules == null) {
            UtilityBillPaymentActivity.mMandatoryBusinessRules = new MandatoryBusinessRules(Constants.UTILITY_BILL_PAYMENT);
        }

        mNameTextView = view.findViewById(R.id.name_view);
        mCardNumberView = (TextView) view.findViewById(R.id.card_number_view);
        infoView = view.findViewById(R.id.info_view);
        mCreditBalanceView = (TextView) view.findViewById(R.id.credit_balance_view);
        mMinimumPayView = (TextView) view.findViewById(R.id.minimum_pay_view);
        mCardNumberEditText = view.findViewById(R.id.card_number_edit_text);
        mCustomerIDView = view.findViewById(R.id.customer_id_view);
        infoView = view.findViewById(R.id.info_view);
        amountLayout = view.findViewById(R.id.amount_layout);
        mAmountSelectorEditText = (EditText) view.findViewById(R.id.amount_selector_edit_text);
        mAmountTypes = new ArrayList<>();
        mAmountTypes.add(Constants.credit_balance);
        mAmountTypes.add(Constants.minimun_pay);
        mAmountTypes.add(Constants.others);
        mAmountSelectorEditText.setFocusable(false);
        mAmountSelectorEditText.setClickable(true);
        mAmountSelectorEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lankaBanglaAmountSelectorDialog = new LankaBanglaAmountSelectorDialog(getContext(), mAmountTypes);
                lankaBanglaAmountSelectorDialog.setOnResourceSelectedListener(new LankaBanglaAmountSelectorDialog.OnResourceSelectedListener() {
                    @Override
                    public void onResourceSelected(String amountType) {
                        amountLayout.setVisibility(View.VISIBLE);
                        mAmountType = amountType;
                        if (mAmountType.equals(Constants.credit_balance)) {
                            mAmountSelectorEditText.setText(Constants.credit_balance);
                            mOtherAmountEditText.setText(mCreditBalanceAmount);
                            mOtherAmountEditText.setEnabled(false);
                        } else if (mAmountType.equals(Constants.minimun_pay)) {
                            mAmountSelectorEditText.setText(Constants.minimun_pay);
                            mOtherAmountEditText.setText(mMinimumPayAmount);
                            mOtherAmountEditText.setEnabled(false);

                        } else if (mAmountType.equals(Constants.others)) {
                            mAmountSelectorEditText.setText(Constants.others);
                            mOtherAmountEditText.setText("");
                            mOtherAmountEditText.setEnabled(true);
                        }
                        lankaBanglaAmountSelectorDialog.dismiss();
                    }
                });
                lankaBanglaAmountSelectorDialog.showDialog();
            }
        });
        mOtherAmountEditText = (EditText) view.findViewById(R.id.other_amount_edit_text);
        mContinueButton = view.findViewById(R.id.continue_button);

        UtilityBillPaymentActivity.mMandatoryBusinessRules = BusinessRuleCacheManager.getBusinessRules(Constants.UTILITY_BILL_PAYMENT);
        setUpButtonAction();
    }

    private void launchOTPVerification() {
        if (getActivity() == null)
            return;
        String jsonString = new Gson().toJson(mLankaBanglaCardBillPayRequest);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog = new OTPVerificationForTwoFactorAuthenticationServicesDialog(getActivity(), jsonString, Constants.COMMAND_BANGLALION_BILL_PAY,
                Constants.BASE_URL_UTILITY + Constants.URL_BANGLALION_BILL_PAY, Constants.METHOD_POST);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog.mParentHttpResponseListener = this;

    }

    private boolean ifUserEligibleToPay() {
        Editable amountEditable;
        amountEditable = mOtherAmountEditText.getText();
        if (amountEditable == null) {
            Toast.makeText(getContext(), "Please provide an amount to pay", Toast.LENGTH_LONG);
            return false;
        } else {
            mAmount = amountEditable.toString();
            if (mAmount == null || mAmount.equals("")) {
                Toast.makeText(getContext(), "Please provide an amount to pay", Toast.LENGTH_LONG).show();
                return false;
            } else {
                if (!Utilities.isValueAvailable(UtilityBillPaymentActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT())
                        || !Utilities.isValueAvailable(UtilityBillPaymentActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT())) {
                    DialogUtils.showDialogForBusinessRuleNotAvailable(getActivity());
                    return false;
                }

                if (UtilityBillPaymentActivity.mMandatoryBusinessRules.isVERIFICATION_REQUIRED() && !ProfileInfoCacheManager.isAccountVerified()) {
                    DialogUtils.showDialogVerificationRequired(getActivity());
                    return false;
                }
                if (SharedPrefManager.ifContainsUserBalance()) {
                    final BigDecimal balance = new BigDecimal(SharedPrefManager.getUserBalance());
                    final BigDecimal topUpAmount = new BigDecimal(mAmount);
                    if (topUpAmount.compareTo(balance) > 0) {
                        Toast.makeText(getContext(), getString(R.string.insufficient_balance), Toast.LENGTH_LONG).show();
                        return false;
                    } else {
                        final BigDecimal minimumTopupAmount = UtilityBillPaymentActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT();
                        final BigDecimal maximumTopupAmount = UtilityBillPaymentActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT().min(balance);
                        String errorMessage = InputValidator.isValidAmount(getActivity(), topUpAmount, minimumTopupAmount, maximumTopupAmount);
                        if (errorMessage != null && !errorMessage.equals("")) {
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }

                } else {
                    Toast.makeText(getContext(), getString(R.string.balance_not_available), Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        return true;
    }

    private void setUpButtonAction() {
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utilities.isConnectionAvailable(getContext())) {
                    if (mContinueButton.getText().toString().toUpperCase().equals("CONTINUE")) {
                        if (verifyUserInput()) {
                            getCustomerInfo();
                        }
                    } else {
                        if (ifUserEligibleToPay()) {
                            attemptBillPayWithPinCheck();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void attemptBillPayWithPinCheck() {
        if (UtilityBillPaymentActivity.mMandatoryBusinessRules.IS_PIN_REQUIRED()) {
            new CustomPinCheckerWithInputDialog(getActivity(), new CustomPinCheckerWithInputDialog.PinCheckAndSetListener() {
                @Override
                public void ifPinCheckedAndAdded(String pin) {
                    attemptBillPay(pin);

                }
            });
        } else {
            attemptBillPay(null);

        }
    }

    private void attemptBillPay(String pin) {
        if (mLankaBanglaCardBillPayTask == null) {
            mLankaBanglaCardBillPayRequest = new LankaBanglaCardBillPayRequest(mCardNumber, mAmount, mAmountType, pin);

            Gson gson = new Gson();
            String json = gson.toJson(mLankaBanglaCardBillPayRequest);
            String mUri = "";
            if (cardType.equals(Constants.VISA)) {
                mUri = Constants.BASE_URL_UTILITY + Constants.URL_LANKABANGLA_VISA_BILL_PAY;
            } else if (cardType.equals(Constants.MASTERCARD)) {
                mUri = Constants.BASE_URL_UTILITY + Constants.URL_LANKABANGLA_MASTERCARD_BILL_PAY;
            }
            mLankaBanglaCardBillPayTask = new HttpRequestPostAsyncTask(Constants.COMMAND_LANKABANGLA_BILL_PAY,
                    mUri, json, getActivity(), false);
            mLankaBanglaCardBillPayTask.mHttpResponseListener = this;
            mLankaBanglaCardBillPayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mCustomProgressDialog.setLoadingMessage(getString(R.string.please_wait));
            mCustomProgressDialog.showDialog();
        }
    }

    private void attemptGetBusinessRule(int serviceID) {
        if (mGetBusinessRuleTask != null) {
            return;
        }
        mGetBusinessRuleTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BUSINESS_RULE,
                Constants.BASE_URL_SM + Constants.URL_BUSINESS_RULE_V2 + "/" + serviceID, getActivity(), false);
        mGetBusinessRuleTask.mHttpResponseListener = this;
        mGetBusinessRuleTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getCustomerInfo() {
        if (mGetLankaBanglaCardUserInfo == null) {
            mProgressDialog.setMessage(getString(R.string.please_wait));
            String mUri = "";
            if (mCardNumber.startsWith("4")) {
                cardType = "VISA";
            } else if (mCardNumber.startsWith("5")) {
                cardType = "MASTERCARD";
            }

            if (cardType.equals(Constants.VISA)) {
                mUri = Constants.BASE_URL_UTILITY + Constants.URL_GET_LANKA_BANGLA_VISA_CUSTOMER + mCardNumber;

            } else if (cardType.equals(Constants.MASTERCARD)) {
                mUri = Constants.BASE_URL_UTILITY + Constants.URL_GET_LANKA_BANGLA_MASTERCARD_CUSTOMER + mCardNumber;
            }

            mGetLankaBanglaCardUserInfo = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_LANKA_BANGLA_CUSTOMER, mUri,
                    getActivity(), this, false);
            mGetLankaBanglaCardUserInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mProgressDialog.show();
        }
    }

    private boolean verifyUserInput() {
        Editable mCardNumberEditable;
        mCardNumberEditable = mCardNumberEditText.getText();
        if (mCardNumberEditable == null) {
            mCardNumberEditText.setError("Please enter your lanka bangla card number");
            return false;
        } else {
            mCardNumber = mCardNumberEditable.toString();
            if (mCardNumber == null || mCardNumber.equals("")) {
                mCardNumberEditText.setError("Please enter your lanka bangla card number");
                return false;
            } else {
                if (!(mCardNumber.startsWith("4") || mCardNumber.startsWith("5"))) {
                    mCardNumberEditText.setError("Invalid Visa/Mastercard number");
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    private static final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    private void setupView() {
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        mNameTextView.setText(lankaBanglaCustomerResponse.getName());
        mMinimumPayView.setText(lankaBanglaCustomerResponse.getMinimumPay());
        mCreditBalanceView.setText(lankaBanglaCustomerResponse.getCreditBalance());
        mCardNumberView.setText(lankaBanglaCustomerResponse.getCardNumber());
        mContinueButton.setText(R.string.pay_bill);
        infoView.setVisibility(View.VISIBLE);
        mCustomerIDView.setVisibility(View.GONE);
        mMinimumPayAmount = lankaBanglaCustomerResponse.getMinimumPay();
        mCreditBalanceAmount = lankaBanglaCustomerResponse.getCreditBalance();
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (getActivity() == null)
            return;

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mCustomProgressDialog.dismissDialog();
            mGetLankaBanglaCardUserInfo = null;
            mLankaBanglaCardBillPayTask = null;
        } else {
            try {
                Gson gson = new Gson();
                switch (result.getApiCommand()) {
                    case Constants.COMMAND_GET_LANKA_BANGLA_CUSTOMER:
                        lankaBanglaCustomerResponse = gson.fromJson(result.getJsonString(), LankaBanglaCustomerResponse.class);
                        if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                            setupView();
                        } else {
                            Toast.makeText(getContext(), lankaBanglaCustomerResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        mGetLankaBanglaCardUserInfo = null;
                        break;
                    case Constants.COMMAND_GET_BUSINESS_RULE:
                        if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                            gson = new Gson();

                            BusinessRuleV2 businessRuleArray = gson.fromJson(result.getJsonString(), BusinessRuleV2.class);
                            List<Rule> rules = businessRuleArray.getRules();

                            for (Rule rule : rules) {
                                switch (rule.getRuleName()) {
                                    case BusinessRuleConstants.SERVICE_RULE_UTILITY_BILL_PAYMENT_MAX_AMOUNT_PER_PAYMENT:
                                        UtilityBillPaymentActivity.mMandatoryBusinessRules.setMAX_AMOUNT_PER_PAYMENT(rule.getRuleValue());
                                        break;
                                    case BusinessRuleConstants.SERVICE_RULE_UTILITY_BILL_PAYMENT_MIN_AMOUNT_PER_PAYMENT:
                                        UtilityBillPaymentActivity.mMandatoryBusinessRules.setMIN_AMOUNT_PER_PAYMENT(rule.getRuleValue());
                                        break;
                                    case BusinessRuleConstants.SERVICE_RULE_UTILITY_BILL_PAYMENT_VERIFICATION_REQUIRED:
                                        UtilityBillPaymentActivity.mMandatoryBusinessRules.setVERIFICATION_REQUIRED(rule.getRuleValue());
                                        break;
                                    case BusinessRuleConstants.SERVICE_RULE_UTILITY_BILL_PAYMENT_PIN_REQUIRED:
                                        UtilityBillPaymentActivity.mMandatoryBusinessRules.setPIN_REQUIRED(rule.getRuleValue());
                                        break;
                                }

                                BusinessRuleCacheManager.setBusinessRules(Constants.UTILITY_BILL_PAYMENT, UtilityBillPaymentActivity.mMandatoryBusinessRules);
                            }
                        }
                        mGetBusinessRuleTask = null;
                        break;
                    case Constants.COMMAND_LANKABANGLA_BILL_PAY:
                        try {
                            mGenericBillPayResponse = gson.fromJson(result.getJsonString(), GenericBillPayResponse.class);
                            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_PROCESSING) {
                                if (getActivity() != null) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            getActivity().setResult(Activity.RESULT_OK);
                                            getActivity().finish();
                                        }
                                    }, 2000);
                                }
                            } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                                if (mContinueButton != null) {
                                    mContinueButton.setClickable(false);
                                }

                                if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                                    mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                                } else {
                                    mCustomProgressDialog.showSuccessAnimationAndMessage(mGenericBillPayResponse.getMessage());
                                }
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        getActivity().setResult(Activity.RESULT_OK);
                                        getActivity().finish();

                                    }
                                }, 2000);
                                Utilities.sendSuccessEventTracker(mTracker, Constants.AMBER_BILL_PAY, ProfileInfoCacheManager.getAccountId(), new BigDecimal(mAmount).longValue());

                            } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                                mCustomProgressDialog.showFailureAnimationAndMessage(mGenericBillPayResponse.getMessage());
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((MyApplication) getActivity().getApplication()).launchLoginPage(mGenericBillPayResponse.getMessage());
                                    }
                                }, 2000);
                                Utilities.sendBlockedEventTracker(mTracker, Constants.AMBER_BILL_PAY, ProfileInfoCacheManager.getAccountId(), new BigDecimal(mAmount).longValue());
                            } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BAD_REQUEST) {
                                final String errorMessage;
                                if (!TextUtils.isEmpty(mGenericBillPayResponse.getMessage())) {
                                    errorMessage = mGenericBillPayResponse.getMessage();
                                } else {
                                    errorMessage = getString(R.string.recharge_failed);
                                }
                                mCustomProgressDialog.showFailureAnimationAndMessage(errorMessage);
                            } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED) {
                                Toast.makeText(getActivity(), mGenericBillPayResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                mCustomProgressDialog.dismissDialog();
                                SecuritySettingsActivity.otpDuration = mGenericBillPayResponse.getOtpValidFor();
                                launchOTPVerification();
                            } else {
                                if (getActivity() != null) {
                                    if (mOTPVerificationForTwoFactorAuthenticationServicesDialog == null) {
                                        mCustomProgressDialog.showFailureAnimationAndMessage(mGenericBillPayResponse.getMessage());
                                    } else {
                                        Toast.makeText(getContext(), mGenericBillPayResponse.getMessage(), Toast.LENGTH_LONG).show();
                                    }

                                    if (mGenericBillPayResponse.getMessage().toLowerCase().contains(TwoFactorAuthConstants.WRONG_OTP)) {
                                        if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.showOtpDialog();
                                            mCustomProgressDialog.dismissDialog();
                                        }
                                    } else {
                                        if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                                        }
                                    }
                                    //Google Analytic event
                                }
                                Utilities.sendFailedEventTracker(mTracker, Constants.AMBER_BILL_PAY, ProfileInfoCacheManager.getAccountId(), mGenericBillPayResponse.getMessage(), new BigDecimal(mAmount).longValue());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            mCustomProgressDialog.showFailureAnimationAndMessage(getString(R.string.payment_failed));
                            if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                                mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                            }
                        }
                        mLankaBanglaCardBillPayTask = null;
                        break;
                }
            } catch (Exception e) {
                mProgressDialog.dismiss();
                mGetLankaBanglaCardUserInfo = null;
                mLankaBanglaCardBillPayTask = null;
                mGetBusinessRuleTask = null;
                e.printStackTrace();
                Toast.makeText(getContext(), getString(R.string.request_failed), Toast.LENGTH_LONG).show();
            }
        }

    }
}
