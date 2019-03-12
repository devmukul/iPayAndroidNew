package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.WASA;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationForTwoFactorAuthenticationServicesDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.BusinessRuleV2;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.MandatoryBusinessRules;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.Rule;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.DescoBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.DescoBillPayResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.DescoCustomerInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.WasaCustomerInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.WasaUserInfoGetRequest;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleConstants;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;


public class WASAEnterBillNumberFragment extends BaseFragment implements HttpResponseListener {
    private EditText mEnterAccountNumberEditText;
    private EditText mEnterBillNumberEditText;
    private Button mContinueButton;
    private CustomProgressDialog mProgressDialog;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
    private OTPVerificationForTwoFactorAuthenticationServicesDialog mOTPVerificationForTwoFactorAuthenticationServicesDialog;

    private String mUri;
    private String mAccountNumber;
    private String mBillNumber;

    private HttpRequestPostAsyncTask mWasaCustomerInfoTask = null;
    private WasaUserInfoGetRequest mWasaUserInfoGetRequest;
    private WasaCustomerInfoResponse mWasaCustomerInfoResponse;
    private HttpRequestGetAsyncTask mGetBusinessRuleTask;
    private AnimatedProgressDialog mCustomProgressDialog;

    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wasa_bill_payment, container, false);
        getActivity().setTitle(getString(R.string.wasa));
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
        mEnterAccountNumberEditText = (EditText) view.findViewById(R.id.account_no_edit_text);
        mEnterBillNumberEditText = (EditText) view.findViewById(R.id.customer_id_edit_text);
        mContinueButton = (Button) view.findViewById(R.id.continue_button);
        UtilityBillPaymentActivity.mMandatoryBusinessRules = BusinessRuleCacheManager.getBusinessRules(Constants.UTILITY_BILL_PAYMENT);
        setUpButtonAction();
    }

    private void setUpButtonAction() {
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utilities.isConnectionAvailable(getContext())) {
                    if (verifyUserInput()) {
                        getCustomerInfo();
                    }

                } else {
                    Toast.makeText(getContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                }
            }
        });
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
        if (mWasaCustomerInfoTask != null) {
            return;
        } else {
            mUri = Constants.BASE_URL_UTILITY + Constants.URL_WASA_CUSTOMER_INFO;
            mWasaUserInfoGetRequest = new WasaUserInfoGetRequest(mAccountNumber, mBillNumber);
            String json = new Gson().toJson(mWasaUserInfoGetRequest);
            mWasaCustomerInfoTask = new HttpRequestPostAsyncTask(Constants.COMMAND_GET_WASA_CUSTOMER, mUri, json,
                    getActivity(), this, false);
            mWasaCustomerInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mProgressDialog.show();
        }
    }

    private boolean verifyUserInput() {
        Editable editable;
        editable = mEnterBillNumberEditText.getText();
        if (editable == null) {
            mEnterBillNumberEditText.setError(getString(R.string.enter_customer_id));
            return false;
        } else {
            mBillNumber = editable.toString();
            if (mBillNumber == null || mBillNumber.isEmpty()) {
                mEnterBillNumberEditText.setError(getString(R.string.enter_bill_no));
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFoundWithout404(result, getContext(), mCustomProgressDialog)) {
            mProgressDialog.dismiss();
            mCustomProgressDialog.dismissDialog();
            mWasaCustomerInfoTask = null;
            mGetBusinessRuleTask = null;
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
        } else {
            try {
                mProgressDialog.dismiss();
                Gson gson = new Gson();
                if (result.getApiCommand().equals(Constants.URL_WASA_CUSTOMER_INFO)) {
                    mWasaCustomerInfoResponse = gson.fromJson(result.getJsonString(), WasaCustomerInfoResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        Utilities.hideKeyboard(getActivity());
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.BILL_NUMBER, mBillNumber);
                        bundle.putSerializable(Constants.ZONE_CODE, numberFormat.parse(mWasaCustomerInfoResponse.getZoneCode()));
                        bundle.putString(Constants.DUE_DATE, mWasaCustomerInfoResponse.getDueDate());
                        bundle.putString(Constants.ACCOUNT_ID, mWasaCustomerInfoResponse.getAccountNumber());
                        bundle.putSerializable(Constants.BILL_AMOUNT, numberFormat.parse(mWasaCustomerInfoResponse.getBillAmount()));
                        if (mWasaCustomerInfoResponse.getStampAmount() != null && Integer.parseInt(mWasaCustomerInfoResponse.getStampAmount()) != 0) {
                            bundle.putSerializable(Constants.STAMP_AMOUNT, numberFormat.parse(mWasaCustomerInfoResponse.getStampAmount()));
                        }
                        bundle.putSerializable(Constants.VAT_AMOUNT, numberFormat.parse(mWasaCustomerInfoResponse.getVatAmount()));
                        bundle.putSerializable(Constants.LPC_AMOUNT, numberFormat.parse(mWasaCustomerInfoResponse.getLpcAmount()));
                        bundle.putSerializable(Constants.TOTAL_AMOUNT, numberFormat.parse(mWasaCustomerInfoResponse.getTotalAmount()));
                        ((UtilityBillPaymentActivity) getActivity()).switchToWASABillInfoFragment(bundle);
                    } else {
                        if (!TextUtils.isEmpty(mWasaCustomerInfoResponse.getMessage())) {
                            Utilities.showErrorDialog(getContext(), mWasaCustomerInfoResponse.getMessage());
                        } else {
                            Utilities.showErrorDialog(getContext(), getString(R.string.not_found));

                        }
                    }
                    mWasaCustomerInfoTask = null;
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
                }
            } catch (Exception e) {
                mProgressDialog.dismiss();
                mWasaCustomerInfoTask = null;
                mGetBusinessRuleTask = null;
                Utilities.showErrorDialog(getContext(), getString(R.string.request_failed));
                e.printStackTrace();
            }
        }

    }
}

