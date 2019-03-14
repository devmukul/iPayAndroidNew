package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.BanglalionPackageSelectorDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomPinCheckerWithInputDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationForTwoFactorAuthenticationServicesDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.BusinessRuleV2;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.MandatoryBusinessRules;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.Rule;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.AllowablePackage;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.BanglalionBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.BanglalionBillPayResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GetCustomerInfoResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleConstants;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class BanglalionBillPayFragment extends BaseFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetCustomerInfoTask = null;
    private HttpRequestGetAsyncTask mGetBusinessRuleTask = null;
    private HttpRequestPostAsyncTask mBanglalionBillPayTask = null;

    private BanglalionBillPayResponse mBanglalionBillPayResponse;
    private BanglalionBillPayRequest mBanglalionBillPayRequestModel;

    private View mCustomerIdView;
    private View mBillPayOptionView;
    private View mUserInfoView;
    private View mPostPaidBillPayView;
    private View mPrepaidBillPayView;
    private View mPrepaidAmmountView;

    private EditText mCustomerIdEditText;
    private EditText mPostpaidAmountEditText;
    private EditText mPrepaidAmountEditText;
    private EditText mPrepaidPackageSelectEditText;

    private TextView mCustomerNameTextView;
    private TextView mPackageTypeTextView;
    private TextView mCustomerIdTextView;
    private TextView mErrorTextView;

    private Button mPayBillButton;
    private Button mContinue;

    private List<AllowablePackage> mAllowedPackage;
    private CustomProgressDialog mProgressDialog;
    private AnimatedProgressDialog mCustomProgressDialog;

    private String mCunnectionType = "";
    private int mAmount;
    private String mCustomerId;

    private BanglalionPackageSelectorDialog mBanglalionPackageSelectorDialog;
    private OTPVerificationForTwoFactorAuthenticationServicesDialog mOTPVerificationForTwoFactorAuthenticationServicesDialog;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = new CustomProgressDialog(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_banglalion_bill_pay, container, false);
        getActivity().setTitle(R.string.banglalion);
        initView(view);

        mPayBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    if (verifyUserInputs(mCunnectionType)) {
                        attemptBillPayPinCheck();
                    }
                } else if (getActivity() != null)
                    Toaster.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG);
            }
        });

        mContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    if (verifyUserInputs("CUSTOMER_INFO")) {
                        getCustomerInfo();
                    }
                } else if (getActivity() != null)
                    Toaster.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG);

            }
        });

        mPrepaidPackageSelectEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAllowedPackage.size() > 0) {
                    mBanglalionPackageSelectorDialog = new BanglalionPackageSelectorDialog(getContext(), mAllowedPackage);
                    mBanglalionPackageSelectorDialog.setOnResourceSelectedListener(new BanglalionPackageSelectorDialog.OnResourceSelectedListener() {
                        @Override
                        public void onResourceSelected(AllowablePackage allowablePackage) {
                            mPrepaidPackageSelectEditText.setText(allowablePackage.getPackageName());
                            mPrepaidAmmountView.setVisibility(View.VISIBLE);
                            mPrepaidAmountEditText.setText(allowablePackage.getAmount().toString());

                            mErrorTextView.setVisibility(View.GONE);
                            mPrepaidPackageSelectEditText.setError(null);
                            mPrepaidPackageSelectEditText.clearFocus();
                        }
                    });
                    mBanglalionPackageSelectorDialog.showDialog();
                }
            }
        });

        // Get business rule
        attemptGetBusinessRule(Constants.SERVICE_ID_UTILITY_BILL);

        return view;
    }

    private void initView(View v) {
        if (UtilityBillPaymentActivity.mMandatoryBusinessRules == null) {
            UtilityBillPaymentActivity.mMandatoryBusinessRules = new MandatoryBusinessRules(Constants.UTILITY_BILL_PAYMENT);
        }

        mCustomerIdView = v.findViewById(R.id.customer_id_view);
        mBillPayOptionView = v.findViewById(R.id.bill_pay_option_selector_view_holder);
        mUserInfoView = v.findViewById(R.id.user_info_view_holder);
        mPostPaidBillPayView = v.findViewById(R.id.postpaid_bill_view_holder);
        mPrepaidBillPayView = v.findViewById(R.id.prepaid_package_selector_view_holder);
        mPrepaidAmmountView = v.findViewById(R.id.package_amount_view);

        mCustomerIdEditText = v.findViewById(R.id.customer_id_edit_text);
        mPostpaidAmountEditText = v.findViewById(R.id.postpaid_amount_edit_text);
        mPrepaidAmountEditText = v.findViewById(R.id.prepaid_amount_edit_text);
        mPrepaidPackageSelectEditText = v.findViewById(R.id.package_selector_edit_text);

        mCustomerNameTextView = v.findViewById(R.id.name_text_view);
        mPackageTypeTextView = v.findViewById(R.id.package_type_text_view);
        mCustomerIdTextView = v.findViewById(R.id.acount_id_text_view);
        mErrorTextView = v.findViewById(R.id.errortext);

        mPayBillButton = v.findViewById(R.id.bill_pay_button);
        mContinue = v.findViewById(R.id.button_send_money);
        mCustomProgressDialog = new AnimatedProgressDialog(getContext());

        UtilityBillPaymentActivity.mMandatoryBusinessRules = BusinessRuleCacheManager.getBusinessRules(Constants.UTILITY_BILL_PAYMENT);
    }

    private void getCustomerInfo() {
        if (mGetCustomerInfoTask != null) {
            return;
        }
        mProgressDialog.show();
        mCustomerId = mCustomerIdEditText.getText().toString();
        mGetCustomerInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BANGLALION_CUSTOMER_INFO,
                Constants.BASE_URL_UTILITY + Constants.URL_GET_BANGLALION_CUSTOMER_INFO + mCustomerId, getActivity(), false);
        mGetCustomerInfoTask.mHttpResponseListener = this;
        mGetCustomerInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    private void launchOTPVerification() {
        String jsonString = new Gson().toJson(mBanglalionBillPayRequestModel);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog = new OTPVerificationForTwoFactorAuthenticationServicesDialog(getActivity(), jsonString, Constants.COMMAND_BANGLALION_BILL_PAY,
                Constants.BASE_URL_UTILITY + Constants.URL_BANGLALION_BILL_PAY, Constants.METHOD_POST);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog.mParentHttpResponseListener = this;
    }

    private void attemptBillPayPinCheck() {
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
        if (mCunnectionType.equals("POSTPAID")) {
            mAmount = Integer.parseInt(mPostpaidAmountEditText.getText().toString().trim());
        } else {
            mAmount = Integer.parseInt(mPrepaidAmountEditText.getText().toString().trim());
        }

        if (mBanglalionBillPayTask != null)
            return;
        mBanglalionBillPayRequestModel = new BanglalionBillPayRequest(mCustomerId, mAmount, pin);

        mCustomProgressDialog.showDialog();
        Gson gson = new Gson();
        String json = gson.toJson(mBanglalionBillPayRequestModel);
        mBanglalionBillPayTask = new HttpRequestPostAsyncTask(Constants.COMMAND_BANGLALION_BILL_PAY,
                Constants.BASE_URL_UTILITY + Constants.URL_BANGLALION_BILL_PAY, json, getActivity(), false);
        mBanglalionBillPayTask.mHttpResponseListener = this;
        mBanglalionBillPayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private boolean verifyUserInputs(String key) {
        boolean cancel = false;
        View focusView = null;
        String errorMessage;
        switch (key) {
            case "CUSTOMER_INFO":
                mCustomerIdEditText.setError(null);
                if (TextUtils.isEmpty(mCustomerIdEditText.getText())) {
                    errorMessage = "Please Enter a Valid Customer ID..";
                    mCustomerIdEditText.setError(errorMessage);
                    focusView = mCustomerIdEditText;
                    cancel = true;
                }
                if (cancel) {
                    focusView.requestFocus();
                    return false;
                } else {
                    return true;
                }

            case "POSTPAID":
                mPostpaidAmountEditText.setError(null);

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

                    //validation check of amount
                    if (TextUtils.isEmpty(mPostpaidAmountEditText.getText())) {
                        errorMessage = getString(R.string.please_enter_amount);
                    } else if (!InputValidator.isValidDigit(mPostpaidAmountEditText.getText().toString().trim())) {
                        errorMessage = getString(R.string.please_enter_amount);
                    } else {
                        final BigDecimal topUpAmount = new BigDecimal(mPostpaidAmountEditText.getText().toString());
                        if (topUpAmount.compareTo(balance) > 0) {
                            errorMessage = getString(R.string.insufficient_balance);
                        } else {
                            final BigDecimal minimumTopupAmount = UtilityBillPaymentActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT();
                            final BigDecimal maximumTopupAmount = UtilityBillPaymentActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT().min(balance);

                            errorMessage = InputValidator.isValidAmount(getActivity(), topUpAmount, minimumTopupAmount, maximumTopupAmount);
                        }
                    }
                } else {
                    focusView = mPostpaidAmountEditText;
                    errorMessage = getString(R.string.balance_not_available);
                    cancel = true;
                }

                if (errorMessage != null) {
                    focusView = mPostpaidAmountEditText;
                    mPostpaidAmountEditText.setError(errorMessage);
                    cancel = true;
                }

                if (cancel) {
                    focusView.requestFocus();
                    return false;
                } else {
                    return true;
                }
            case "PREPAID":
                mPrepaidAmountEditText.setError(null);
                mPrepaidPackageSelectEditText.setError(null);

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

                    //validation check of amount
                    if (TextUtils.isEmpty(mPrepaidPackageSelectEditText.getText())) {
                        mErrorTextView.setVisibility(View.VISIBLE);
                        errorMessage = getString(R.string.please_select_package);
                        focusView = mPrepaidPackageSelectEditText;
                        mPrepaidPackageSelectEditText.setError(errorMessage);
                        cancel = true;
                    } else {
                        final BigDecimal topUpAmount = new BigDecimal(mPrepaidAmountEditText.getText().toString());
                        if (topUpAmount.compareTo(balance) > 0) {
                            errorMessage = getString(R.string.insufficient_balance);
                        } else {
                            final BigDecimal minimumTopupAmount = UtilityBillPaymentActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT();
                            final BigDecimal maximumTopupAmount = UtilityBillPaymentActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT().min(balance);

                            errorMessage = InputValidator.isValidAmount(getActivity(), topUpAmount, minimumTopupAmount, maximumTopupAmount);
                        }

                        if (errorMessage != null) {
                            focusView = mPrepaidAmountEditText;
                            mPrepaidAmountEditText.setError(errorMessage);
                            cancel = true;
                        }
                    }
                } else {
                    focusView = mPrepaidAmountEditText;
                    errorMessage = getString(R.string.balance_not_available);
                    cancel = true;
                }


                if (cancel) {
                    focusView.requestFocus();
                    return false;
                } else {
                    return true;
                }
        }
        return false;
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFoundWithout404(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mGetCustomerInfoTask = null;
            mGetBusinessRuleTask = null;
            mBanglalionBillPayTask = null;
            if (result != null && result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
                try {
                    GenericResponseWithMessageOnly genericResponseWithMessageOnly = new Gson().
                            fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                    Utilities.showErrorDialog(getContext(), genericResponseWithMessageOnly.getMessage());
                } catch (Exception e) {
                    Utilities.showErrorDialog(getContext(), getString(R.string.not_found));
                }
            }
            return;
        }

        if (isAdded()) mProgressDialog.dismiss();

        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_GET_BANGLALION_CUSTOMER_INFO:
                mProgressDialog.dismiss();
                mCustomProgressDialog.dismissDialog();
                GetCustomerInfoResponse mCustomerInfoResponse = null;
                switch (result.getStatus()) {
                    case Constants.HTTP_RESPONSE_STATUS_OK:
                        try {
                            mCustomerInfoResponse = gson.fromJson(result.getJsonString(), GetCustomerInfoResponse.class);
                            mAllowedPackage = mCustomerInfoResponse.getAllowablePackages();
                            mCustomerIdView.setVisibility(View.GONE);
                            mBillPayOptionView.setVisibility(View.VISIBLE);
                            mUserInfoView.setVisibility(View.VISIBLE);
                            mPayBillButton.setVisibility(View.VISIBLE);
                            mContinue.setVisibility(View.GONE);
                            mCustomerNameTextView.setText(mCustomerInfoResponse.getUserName());
                            mCustomerIdTextView.setText("Customer ID: " + mCustomerId);
                            mPackageTypeTextView.setText("( " + mCustomerInfoResponse.getUserType() + " )");
                            if (mCustomerInfoResponse.getUserType().equals("POSTPAID")) {
                                mCunnectionType = "POSTPAID";
                                mPostPaidBillPayView.setVisibility(View.VISIBLE);
                                mPrepaidBillPayView.setVisibility(View.GONE);
                                mPayBillButton.setText("PAY BILL");
                            } else {
                                mCunnectionType = "PREPAID";
                                mPostPaidBillPayView.setVisibility(View.GONE);
                                mPrepaidBillPayView.setVisibility(View.VISIBLE);
                                mPayBillButton.setText("BUY PACKAGE");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (getActivity() != null)
                                Toaster.makeText(getActivity(), R.string.fetch_info_failed, Toast.LENGTH_LONG);
                        }
                        break;
                    default:
                        if (!TextUtils.isEmpty(mCustomerInfoResponse.getMessage())) {
                            Utilities.showErrorDialog(getContext(), mCustomerInfoResponse.getMessage());
                        } else {
                            Utilities.showErrorDialog(getContext(), getString(R.string.service_not_available));
                        }
                        break;
                }
                break;
            case Constants.COMMAND_GET_BUSINESS_RULE:
                mGetBusinessRuleTask = null;
                switch (result.getStatus()) {
                    case Constants.HTTP_RESPONSE_STATUS_OK:
                        try {
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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
                break;
            case Constants.COMMAND_BANGLALION_BILL_PAY:
                try {
                    mBanglalionBillPayResponse = gson.fromJson(result.getJsonString(), BanglalionBillPayResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_PROCESSING) {
                        if (getActivity() != null) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getActivity().setResult(Activity.RESULT_OK);
                                    getActivity().finish();
                                }
                            }, 3000);
                        }
                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                        if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                        } else {
                            mCustomProgressDialog.showSuccessAnimationAndMessage(mBanglalionBillPayResponse.getMessage());
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().setResult(Activity.RESULT_OK);
                                getActivity().finish();

                            }
                        }, 3000);
                        Utilities.sendSuccessEventTracker(mTracker, Constants.BANGLALION_BILL_PAY, ProfileInfoCacheManager.getAccountId(), new BigDecimal(mAmount).longValue());

                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                        mCustomProgressDialog.showFailureAnimationAndMessage(mBanglalionBillPayResponse.getMessage());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ((MyApplication) getActivity().getApplication()).launchLoginPage(mBanglalionBillPayResponse.getMessage());
                            }
                        }, 2000);

                        Utilities.sendBlockedEventTracker(mTracker, Constants.BANGLALION_BILL_PAY, ProfileInfoCacheManager.getAccountId(), new BigDecimal(mAmount).longValue());

                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BAD_REQUEST) {
                        final String errorMessage;
                        if (!TextUtils.isEmpty(mBanglalionBillPayResponse.getMessage())) {
                            errorMessage = mBanglalionBillPayResponse.getMessage();
                        } else {
                            errorMessage = getString(R.string.recharge_failed);
                        }
                        mCustomProgressDialog.showFailureAnimationAndMessage(errorMessage);
                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED) {
                        Toast.makeText(getActivity(), mBanglalionBillPayResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        mCustomProgressDialog.dismissDialog();
                        SecuritySettingsActivity.otpDuration = mBanglalionBillPayResponse.getOtpValidFor();
                        launchOTPVerification();
                    } else {
                        if (getActivity() != null) {
                            if (mOTPVerificationForTwoFactorAuthenticationServicesDialog == null) {
                                mCustomProgressDialog.showFailureAnimationAndMessage(mBanglalionBillPayResponse.getMessage());
                            } else {
                                Toast.makeText(getContext(), mBanglalionBillPayResponse.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            if (mBanglalionBillPayResponse.getMessage().toLowerCase().contains(TwoFactorAuthConstants.WRONG_OTP)) {
                                if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                                    mOTPVerificationForTwoFactorAuthenticationServicesDialog.showOtpDialog();
                                    mCustomProgressDialog.dismissDialog();
                                }
                            } else {
                                if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                                    mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                                }
                            }
                            Utilities.sendFailedEventTracker(mTracker, Constants.BANGLALION_BILL_PAY, ProfileInfoCacheManager.getAccountId(), mBanglalionBillPayResponse.getMessage(), new BigDecimal(mAmount).longValue());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mCustomProgressDialog.showFailureAnimationAndMessage(getString(R.string.payment_failed));
                    if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                        mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                    }
                }
                mBanglalionBillPayTask = null;
                break;

            default:
                if (getActivity() != null)
                    Toaster.makeText(getActivity(), R.string.fetch_info_failed, Toast.LENGTH_LONG);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mCustomProgressDialog.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCustomProgressDialog.dismiss();
    }

}