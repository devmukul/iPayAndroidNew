package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments;

import android.app.Activity;
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
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomPinCheckerWithInputDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationForTwoFactorAuthenticationServicesDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.BusinessRuleV2;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.MandatoryBusinessRules;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.Rule;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GenericBillPayResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.WestZoneBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.WestZoneCustomerInfoResponse;
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


public class WestzoneBillPaymentFragment extends BaseFragment implements HttpResponseListener {
    private TextView mNameTextView;
    private TextView mAccountIDTextView;
    private TextView mPrimaryAmountTextView;
    private TextView mVatTextView;
    private TextView mTotalAmountTextView;
    private TextView mBillMonthTextView;
    private TextView mBillStatusTextView;
    private TextView mBillNumberTextView;
    private EditText mAccountIDEditText;
    private TextView mPrevMonthView;
    private Button mContinueButton;
    private View infoView;
    private View customerIDView;
    private CustomProgressDialog mProgressDialog;
    private OTPVerificationForTwoFactorAuthenticationServicesDialog mOTPVerificationForTwoFactorAuthenticationServicesDialog;

    private String mUri;
    private String mCustomerID;
    private String mAmount;

    private HttpRequestGetAsyncTask mWestZoneCustomerInfoTask = null;
    private WestZoneCustomerInfoResponse westZoneCustomerInfoResponse;
    private HttpRequestPostAsyncTask mWestZoneBillPayTask = null;
    private WestZoneBillPayRequest mWestZoneBillPayRequest;
    private HttpRequestGetAsyncTask mGetBusinessRuleTask;
    private GenericBillPayResponse mGenericBillPayResponse;
    private AnimatedProgressDialog mCustomProgressDialog;

    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_westzone_bill_pay, container, false);
        getActivity().setTitle("West Zone");
        attemptGetBusinessRule(ServiceIdConstants.UTILITY_BILL_PAYMENT);
        mProgressDialog = new CustomProgressDialog(getContext());
        mCustomProgressDialog = new AnimatedProgressDialog(getContext());
        setUpView(view);
        return view;
    }

    private void setUpView(View view) {
        if (UtilityBillPaymentActivity.mMandatoryBusinessRules == null) {
            UtilityBillPaymentActivity.mMandatoryBusinessRules = new MandatoryBusinessRules(Constants.UTILITY_BILL_PAYMENT);
        }

        mNameTextView = (TextView) view.findViewById(R.id.name_view);
        mAccountIDTextView = (TextView) view.findViewById(R.id.account_number_view);
        mPrimaryAmountTextView = (TextView) view.findViewById(R.id.principal_amount_view);
        mVatTextView = (TextView) view.findViewById(R.id.vat_amount);
        mTotalAmountTextView = (TextView) view.findViewById(R.id.total_amount_view);
        mPrevMonthView = (TextView) view.findViewById(R.id.bill_month_prev);
        mBillMonthTextView = (TextView) view.findViewById(R.id.bill_month_view);
        mBillStatusTextView = (TextView) view.findViewById(R.id.bill_status_view);
        customerIDView = view.findViewById(R.id.customer_id_view);
        infoView = view.findViewById(R.id.info_view);
        mAccountIDEditText = (EditText) view.findViewById(R.id.customer_id_edit_text);
        mContinueButton = (Button) view.findViewById(R.id.button_send_money);
        mBillNumberTextView = (TextView) view.findViewById(R.id.bill_number);

        UtilityBillPaymentActivity.mMandatoryBusinessRules = BusinessRuleCacheManager.getBusinessRules(Constants.UTILITY_BILL_PAYMENT);
        setUpButtonAction();
    }

    private void launchOTPVerification() {
        String jsonString = new Gson().toJson(mWestZoneBillPayRequest);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog = new OTPVerificationForTwoFactorAuthenticationServicesDialog(getActivity(), jsonString, Constants.COMMAND_WEST_ZONE_BILL_PAY,
                Constants.BASE_URL_UTILITY + Constants.URL_WEST_ZONE_BILL_PAY, Constants.METHOD_POST);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog.mParentHttpResponseListener = this;

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
                        if (isUserEligibleToPaySufficient()) {
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
        if (mWestZoneBillPayTask != null) {
            return;
        } else {
            mWestZoneBillPayRequest = new WestZoneBillPayRequest(mCustomerID, pin);

            Gson gson = new Gson();
            String json = gson.toJson(mWestZoneBillPayRequest);
            mWestZoneBillPayTask = new HttpRequestPostAsyncTask(Constants.COMMAND_WEST_ZONE_BILL_PAY,
                    Constants.BASE_URL_UTILITY + Constants.URL_WEST_ZONE_BILL_PAY, json, getActivity(), false);
            mWestZoneBillPayTask.mHttpResponseListener = this;
            mWestZoneBillPayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        if (mWestZoneCustomerInfoTask != null) {
            return;
        } else {
            mUri = Constants.BASE_URL_UTILITY + Constants.URL_WEST_ZONE + mCustomerID;
            mWestZoneCustomerInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_WEST_ZONE_CUSTOMER, mUri,
                    getActivity(), this, true);
            mWestZoneCustomerInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mProgressDialog.show();
        }
    }

    private boolean isUserEligibleToPaySufficient() {
        boolean cancel = false;
        mTotalAmountTextView.setError(null);
        String errorMessage = null;
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
            if (mTotalAmountTextView.getText() != null) {
                final BigDecimal topUpAmount = new BigDecimal(mTotalAmountTextView.getText().toString());
                if (topUpAmount.compareTo(balance) > 0) {
                    errorMessage = getString(R.string.insufficient_balance);
                } else {
                    final BigDecimal minimumTopupAmount = UtilityBillPaymentActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT();
                    final BigDecimal maximumTopupAmount = UtilityBillPaymentActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT().min(balance);

                    errorMessage = InputValidator.isValidAmount(getActivity(), topUpAmount, minimumTopupAmount, maximumTopupAmount);
                }
            }
        } else {
            errorMessage = getString(R.string.balance_not_available);
            cancel = true;
        }

        if (errorMessage != null) {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            cancel = true;
        }
        return !cancel;
    }

    private boolean verifyUserInput() {
        Editable editable;
        editable = mAccountIDEditText.getText();
        if (editable == null) {
            mAccountIDEditText.setError(getString(R.string.enter_customer_id));
            return false;
        } else {
            mCustomerID = editable.toString();
            if (mCustomerID == null || mCustomerID.isEmpty()) {
                mAccountIDEditText.setError(getString(R.string.enter_customer_id));
                return false;
            } else {
                return true;
            }
        }
    }

    private void fillUpFiledsWithData() {
        mNameTextView.setText(westZoneCustomerInfoResponse.getName());
        mPrimaryAmountTextView.setText(westZoneCustomerInfoResponse.getPrincipalAmount());
        mBillStatusTextView.setText(westZoneCustomerInfoResponse.getBillStatus());
        mBillMonthTextView.setText(westZoneCustomerInfoResponse.getBillMonth());
        mAccountIDTextView.setText(westZoneCustomerInfoResponse.getAccountNumber());
        mVatTextView.setText(westZoneCustomerInfoResponse.getVatAmount());
        mTotalAmountTextView.setText(westZoneCustomerInfoResponse.getTotalAmount());
        mAmount = westZoneCustomerInfoResponse.getTotalAmount();
        mBillNumberTextView.setText(westZoneCustomerInfoResponse.getBillNumber());
        mContinueButton.setText("Pay bill");
        infoView.setVisibility(View.VISIBLE);
        customerIDView.setVisibility(View.GONE);
        if (westZoneCustomerInfoResponse.getBillStatus() != null) {
            if (westZoneCustomerInfoResponse.getBillStatus().toLowerCase().equals("paid")) {
                mContinueButton.setEnabled(false);
            } else {
                mContinueButton.setEnabled(true);
            }
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mCustomProgressDialog.dismissDialog();
            mWestZoneCustomerInfoTask = null;
            mWestZoneBillPayTask = null;
            return;
        } else {
            try {
                Gson gson = new Gson();
                if (result.getApiCommand().equals(Constants.COMMAND_GET_WEST_ZONE_CUSTOMER)) {
                    westZoneCustomerInfoResponse = gson.fromJson(result.getJsonString(), WestZoneCustomerInfoResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        fillUpFiledsWithData();
                    } else {
                        Toast.makeText(getContext(), westZoneCustomerInfoResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    mWestZoneCustomerInfoTask = null;
                } else if (result.getApiCommand().equals(Constants.COMMAND_GET_BUSINESS_RULE)) {
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
                } else if (result.getApiCommand().equals(Constants.COMMAND_WEST_ZONE_BILL_PAY)) {
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
                            Utilities.sendSuccessEventTracker(mTracker, Constants.WESTZONE_BILL_PAY, ProfileInfoCacheManager.getAccountId(), new BigDecimal(mAmount).longValue());

                        } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                            mCustomProgressDialog.showFailureAnimationAndMessage(mGenericBillPayResponse.getMessage());
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ((MyApplication) getActivity().getApplication()).launchLoginPage(mGenericBillPayResponse.getMessage());
                                }
                            }, 2000);
                            Utilities.sendBlockedEventTracker(mTracker, Constants.WESTZONE_BILL_PAY, ProfileInfoCacheManager.getAccountId(), new BigDecimal(mAmount).longValue());
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
                            Utilities.sendFailedEventTracker(mTracker, Constants.WESTZONE_BILL_PAY, ProfileInfoCacheManager.getAccountId(), mGenericBillPayResponse.getMessage(), new BigDecimal(mAmount).longValue());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mCustomProgressDialog.showFailureAnimationAndMessage(getString(R.string.recharge_failed));
                        if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                        }
                    }
                    mWestZoneBillPayTask = null;
                }
            } catch (Exception e) {
                mProgressDialog.dismiss();
                mWestZoneCustomerInfoTask = null;
                mWestZoneBillPayTask = null;
                mGetBusinessRuleTask = null;
                e.printStackTrace();
            }
        }

    }
}
