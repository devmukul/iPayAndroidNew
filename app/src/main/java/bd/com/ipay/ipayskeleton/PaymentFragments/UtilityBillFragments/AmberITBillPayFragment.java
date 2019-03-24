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
import java.text.NumberFormat;
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
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationForTwoFactorAuthenticationServicesDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.BusinessRuleV2;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.MandatoryBusinessRules;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.Rule;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.AmberITBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.AmberITCustomerInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.AmberItCustomerRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GenericBillPayResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleConstants;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class AmberITBillPayFragment extends BaseFragment implements HttpResponseListener {
	private TextView mNameTextView;
	private EditText mAccountIDEditText;
	private EditText mAmountEditText;
	private TextView mCurrentPackageView;
	private Button mContinueButton;
	private View infoView;
	private View customerIDView;
	private CustomProgressDialog mProgressDialog;
	private OTPVerificationForTwoFactorAuthenticationServicesDialog mOTPVerificationForTwoFactorAuthenticationServicesDialog;

    private String mAmount;
    private String mCustomerID;

	private HttpRequestPostAsyncTask mGetAmberItUserInfo = null;
	private AmberITCustomerInfoResponse amberITCustomerInfoResponse;
	private HttpRequestPostAsyncTask mAmberITBillPayTask = null;
	private AmberITBillPayRequest mAmberITBillPayRequest;
	private HttpRequestGetAsyncTask mGetBusinessRuleTask;
	private GenericBillPayResponse mGenericBillPayResponse;
	private AnimatedProgressDialog mCustomProgressDialog;

    @Nullable
    @Override

	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_amberit_bill_payment, container, false);
		if (getActivity() == null)
			return view;
		getActivity().setTitle("AmberIT");
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

        mNameTextView = view.findViewById(R.id.name_view);
        customerIDView = view.findViewById(R.id.customer_id_view);
        infoView = view.findViewById(R.id.info_view);
        mCurrentPackageView = view.findViewById(R.id.current_package_view);
        mAccountIDEditText = view.findViewById(R.id.customer_id_edit_text);
        mAmountEditText = view.findViewById(R.id.amount_edit_text);
        mContinueButton = view.findViewById(R.id.button_send_money);

        UtilityBillPaymentActivity.mMandatoryBusinessRules = BusinessRuleCacheManager.getBusinessRules(Constants.UTILITY_BILL_PAYMENT);
        setUpButtonAction();
    }

    private void launchOTPVerification() {
        if (getActivity() == null)
            return;
        String jsonString = new Gson().toJson(mAmberITBillPayRequest);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog = new OTPVerificationForTwoFactorAuthenticationServicesDialog(getActivity(), jsonString, Constants.COMMAND_AMBERIT_BILL_PAY,
                Constants.BASE_URL_UTILITY + Constants.URL_AMBERIT_BILL_PAY, Constants.METHOD_POST);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog.mParentHttpResponseListener = this;

    }

    private void setUpButtonAction() {
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utilities.isConnectionAvailable(getContext())) {
                    if (mContinueButton.getText().toString().toUpperCase().equals(getString(R.string.continue_button))) {
                        if (verifyUserInput()) {
                            getCustomerInfo();
                        }
                    } else {
                        if (ifUserEligibleToPaySufficient()) {
                            attemptBillPayWithPinCheck();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean ifUserEligibleToPaySufficient() {
        String errorMessage;
        if (SharedPrefManager.ifContainsUserBalance()) {
            BigDecimal balance = new BigDecimal(SharedPrefManager.getUserBalance());
            //validation check of amount
            if (mAmountEditText.getText() != null) {
                if (mAmountEditText.getText() != null && !TextUtils.isEmpty(mAmountEditText.getText().toString())) {
                    mAmount = mAmountEditText.getText().toString();
                    BigDecimal topUpAmount = new BigDecimal(mAmountEditText.getText().toString());
                    if (topUpAmount.compareTo(balance) > 0) {
                        errorMessage = getString(R.string.insufficient_balance);
                    } else {
                        BigDecimal minimumTopupAmount = UtilityBillPaymentActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT();
                        BigDecimal maximumTopupAmount = UtilityBillPaymentActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT().min(balance);

                        errorMessage = InputValidator.isValidAmount(getActivity(), topUpAmount, minimumTopupAmount, maximumTopupAmount);
                    }
                } else {
                    errorMessage = getString(R.string.please_enter_amount);
                }
            } else {
                errorMessage = getString(R.string.please_enter_amount);
            }
        } else {
            errorMessage = getString(R.string.balance_not_available);
        }
        if (errorMessage != null) {
            mAmountEditText.setError(errorMessage);
            return false;
        } else {
            return true;
        }
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
		if (mAmberITBillPayTask == null) {
			mAmberITBillPayRequest = new AmberITBillPayRequest(mCustomerID, mAmount, pin);
			Gson gson = new Gson();
			String json = gson.toJson(mAmberITBillPayRequest);
			mAmberITBillPayTask = new HttpRequestPostAsyncTask(Constants.COMMAND_AMBERIT_BILL_PAY,
					Constants.BASE_URL_UTILITY + Constants.URL_AMBERIT_BILL_PAY, json, getActivity(), false);
			mAmberITBillPayTask.mHttpResponseListener = this;
			mAmberITBillPayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
		if (mGetAmberItUserInfo == null) {
			String mUri = Constants.BASE_URL_UTILITY + Constants.URL_GET_AMBERIT_CUSTOMER;
			AmberItCustomerRequest amberItCustomerRequest = new AmberItCustomerRequest(mCustomerID, mAmount);
			String json = new Gson().toJson(amberItCustomerRequest);
			mGetAmberItUserInfo = new HttpRequestPostAsyncTask(Constants.COMMAND_GET_AMBERIT_CUSTOMER, mUri, json,
					getActivity(), this, false);
			mGetAmberItUserInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			mProgressDialog.show();
		}
	}

    private boolean verifyUserInput() {
        Editable editableAccountID;
        Editable editableAmount;
        boolean cancel = false;
        String errorMessage;
        editableAccountID = mAccountIDEditText.getText();
        if (editableAccountID == null) {
            mAccountIDEditText.setError(getString(R.string.enter_customer_id));
            cancel = true;
            return !cancel;
        } else {
            mCustomerID = editableAccountID.toString();
            if (mCustomerID == null || TextUtils.isEmpty(mCustomerID)) {
                mAccountIDEditText.setError(getString(R.string.enter_customer_id));
                cancel = true;
                return !cancel;
            } else {
                mCustomerID = Utilities.formatAmberItCID(mCustomerID);
            }
        }
        editableAmount = mAmountEditText.getText();
        if (editableAccountID == null) {
            mAmountEditText.setError(getString(R.string.please_enter_amount));
            cancel = true;
            return !cancel;
        } else {
            mAmount = editableAmount.toString();
            if (mAmount == null || TextUtils.isEmpty(mAmount)) {
                mAmountEditText.setError(getString(R.string.please_enter_amount));
                cancel = true;
                return !cancel;
            }
        }
        final BigDecimal topUpAmount = new BigDecimal(mAmount);
        final BigDecimal balance = new BigDecimal(SharedPrefManager.getUserBalance());
        if (topUpAmount.compareTo(balance) > 0) {
            errorMessage = getString(R.string.insufficient_balance);
            mAmountEditText.setError(errorMessage);
            cancel = true;
        } else {
            final BigDecimal minimumTopupAmount = UtilityBillPaymentActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT();
            final BigDecimal maximumTopupAmount = UtilityBillPaymentActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT().min(balance);
            errorMessage = InputValidator.isValidAmount(getActivity(), topUpAmount, minimumTopupAmount, maximumTopupAmount);
            if (errorMessage != null) {
                mAmountEditText.setError(errorMessage);
                cancel = true;
            }
        }
        return !cancel;
    }

    private static final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    private void setupView() {
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        mNameTextView.setText(amberITCustomerInfoResponse.getCustomerName());
        mCurrentPackageView.setText(numberFormat.format(new BigDecimal(mAmount)));
        mContinueButton.setText(R.string.pay);
        infoView.setVisibility(View.VISIBLE);
        customerIDView.setVisibility(View.GONE);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (getActivity() == null)
            return;

        if (HttpErrorHandler.isErrorFoundWithout404(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mCustomProgressDialog.dismissDialog();
            mGetAmberItUserInfo = null;
            mAmberITBillPayTask = null;
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
                Gson gson = new Gson();
                switch (result.getApiCommand()) {
                    case Constants.COMMAND_GET_AMBERIT_CUSTOMER:
                        mProgressDialog.dismiss();
                        mCustomProgressDialog.dismissDialog();
                        amberITCustomerInfoResponse = gson.fromJson(result.getJsonString(), AmberITCustomerInfoResponse.class);
                        if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                            setupView();
                        } else {
                            if (!TextUtils.isEmpty(amberITCustomerInfoResponse.getMessage())) {
                                Utilities.showErrorDialog(getContext(), amberITCustomerInfoResponse.getMessage());
                            } else {
                                Utilities.showErrorDialog(getContext(), getString(R.string.service_not_available));
                            }
                        }
                        mGetAmberItUserInfo = null;
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
                    case Constants.COMMAND_AMBERIT_BILL_PAY:
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
                        mAmberITBillPayTask = null;
                        break;
                }
            } catch (Exception e) {
                mProgressDialog.dismiss();
                mGetAmberItUserInfo = null;
                mAmberITBillPayTask = null;
                mGetBusinessRuleTask = null;
                e.printStackTrace();
                Toast.makeText(getContext(), getString(R.string.request_failed), Toast.LENGTH_LONG).show();
            }
        }

    }
}
