package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.Rule;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.BrilliantBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.BrilliantRechargeResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleConstants;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;


public class BrilliantBillPayFragment extends BaseFragment implements HttpResponseListener {
    private EditText mCustomerIdEditText;
    private EditText amountEditText;
    private Button mContinueButton;

	private CustomProgressDialog mProgressDialog;
	private AnimatedProgressDialog mCustomProgressDialog;

    private String mCustomerID;
    private String mAmount;
    private String jsonString;

    private HttpRequestPostAsyncTask mBrilliantRechargeTask = null;

    private BrilliantBillPayRequest brilliantBillPayRequest;
    private BrilliantRechargeResponse brilliantRechargeResponse;

    private HttpRequestGetAsyncTask mGetBusinessRuleTask;

    private OTPVerificationForTwoFactorAuthenticationServicesDialog mOTPVerificationForTwoFactorAuthenticationServicesDialog;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_brilliant_recahrge, container, false);
		mCustomerIdEditText = (EditText) view.findViewById(R.id.customer_id_edit_text);
		amountEditText = (EditText) view.findViewById(R.id.amount_edit_text);
		mContinueButton = (Button) view.findViewById(R.id.button_send_money);
		mProgressDialog = new CustomProgressDialog(getContext());
		mCustomProgressDialog = new AnimatedProgressDialog(getContext());
		if (UtilityBillPaymentActivity.mMandatoryBusinessRules == null) {
			UtilityBillPaymentActivity.mMandatoryBusinessRules = BusinessRuleCacheManager.getBusinessRules(Constants.UTILITY_BILL_PAYMENT);
		}
		attemptGetBusinessRule(Constants.SERVICE_ID_UTILITY_BILL);
		mContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (Utilities.isConnectionAvailable(getContext())) {
					if (verifyUserInputs()) {
						attemptRechargeWithPinCheck();
					}
				} else {
					Toast.makeText(getContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
				}
			}
		});
		getActivity().setTitle("Brilliant");
		return view;
	}

    private void attemptRechargeWithPinCheck() {
        if (UtilityBillPaymentActivity.mMandatoryBusinessRules.IS_PIN_REQUIRED()) {
            new CustomPinCheckerWithInputDialog(getActivity(), new CustomPinCheckerWithInputDialog.PinCheckAndSetListener() {
                @Override
                public void ifPinCheckedAndAdded(String pin) {
                    attemptRecharge(pin);
                }
            });
        } else {
            attemptRecharge(null);
        }
    }

	private void attemptRecharge(String pin) {
		if (mBrilliantRechargeTask != null) {
			return;
		} else {
			mCustomerID = mCustomerIdEditText.getText().toString();
			if (mCustomerID == null || mCustomerID.isEmpty()) {
				mCustomerIdEditText.setError("Please enter a valid customer ID");
			} else {
				if (verifyUserInputs()) {
					mAmount = amountEditText.getText().toString();
					brilliantBillPayRequest = new BrilliantBillPayRequest(mCustomerID, Long.parseLong(mAmount), pin);
					jsonString = "";
					jsonString = new Gson().toJson(brilliantBillPayRequest);
					mBrilliantRechargeTask = new HttpRequestPostAsyncTask(Constants.COMMAND_BRILLIANT_RECHARGE,
							Constants.BASE_URL_UTILITY + Constants.URL_BRILLIANT_RECHARGE, jsonString, getContext(), this, false);
					mBrilliantRechargeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					mCustomProgressDialog.showDialog();
				}
			}
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

    private boolean verifyUserInputs() {
        amountEditText.setError(null);
        mCustomerIdEditText.setError(null);
        String errorMessage = null;
        View focusView;
        mCustomerID = mCustomerIdEditText.getText().toString().trim();
        if (mCustomerID == null || mCustomerID.isEmpty()) {
            errorMessage = "Please enter a valid customer ID";
            mCustomerIdEditText.setError(errorMessage);
            focusView = mCustomerIdEditText;
            return false;
        }
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
            if (TextUtils.isEmpty(amountEditText.getText())) {
                errorMessage = getString(R.string.please_enter_amount);
            } else if (!InputValidator.isValidDigit(amountEditText.getText().toString().trim())) {
                errorMessage = getString(R.string.please_enter_amount);
            } else {
                final BigDecimal topUpAmount = new BigDecimal(amountEditText.getText().toString());
                if (topUpAmount.compareTo(balance) > 0) {
                    errorMessage = getString(R.string.insufficient_balance);
                } else {
                    final BigDecimal minimumTopupAmount = UtilityBillPaymentActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT();
                    final BigDecimal maximumTopupAmount = UtilityBillPaymentActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT().min(balance);
                    errorMessage = InputValidator.isValidAmount(getActivity(), topUpAmount, minimumTopupAmount, maximumTopupAmount);
                }
            }
        } else {
            focusView = amountEditText;
            errorMessage = getString(R.string.balance_not_available);
        }
        if (errorMessage == null || errorMessage.equals("")) {
            return true;
        } else {
            amountEditText.setError(errorMessage);
            return false;
        }
    }

    private void launchOTPVerification() {
        jsonString = new Gson().toJson(brilliantBillPayRequest);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog = new OTPVerificationForTwoFactorAuthenticationServicesDialog(getActivity(), jsonString, Constants.COMMAND_BRILLIANT_RECHARGE,
                Constants.BASE_URL_UTILITY + Constants.URL_BRILLIANT_RECHARGE, Constants.METHOD_POST);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog.mParentHttpResponseListener = this;
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFoundWithout404(result, getContext(), null)) {
            mCustomProgressDialog.dismissDialog();
            mGetBusinessRuleTask = null;
            mBrilliantRechargeTask = null;
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
            Gson gson = new Gson();
            if (result.getApiCommand().equals(Constants.COMMAND_GET_BUSINESS_RULE)) {
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
            } else if (result.getApiCommand().equals(Constants.COMMAND_BRILLIANT_RECHARGE)) {
                try {
                    brilliantRechargeResponse = gson.fromJson(result.getJsonString(), BrilliantRechargeResponse.class);
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
                            mCustomProgressDialog.showSuccessAnimationAndMessage(brilliantRechargeResponse.getMessage());
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getActivity().setResult(Activity.RESULT_OK);
                                getActivity().finish();

                            }
                        }, 2000);
                        Utilities.sendSuccessEventTracker(mTracker, Constants.BRILLIANT_BILL_PAY, ProfileInfoCacheManager.getAccountId(), new BigDecimal(mAmount).longValue());

                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                        mCustomProgressDialog.showFailureAnimationAndMessage(brilliantRechargeResponse.getMessage());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ((MyApplication) getActivity().getApplication()).launchLoginPage(brilliantRechargeResponse.getMessage());
                            }
                        }, 2000);

                        Utilities.sendBlockedEventTracker(mTracker, Constants.BRILLIANT_BILL_PAY, ProfileInfoCacheManager.getAccountId(), new BigDecimal(mAmount).longValue());

                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BAD_REQUEST) {
                        final String errorMessage;
                        if (!TextUtils.isEmpty(brilliantRechargeResponse.getMessage())) {
                            errorMessage = brilliantRechargeResponse.getMessage();
                        } else {
                            errorMessage = getString(R.string.recharge_failed);
                        }
                        mCustomProgressDialog.showFailureAnimationAndMessage(errorMessage);
                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED) {
                        Toast.makeText(getActivity(), brilliantRechargeResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        mCustomProgressDialog.dismissDialog();
                        SecuritySettingsActivity.otpDuration = brilliantRechargeResponse.getOtpValidFor();
                        launchOTPVerification();
                    } else {
                        if (getActivity() != null) {
                            if (mOTPVerificationForTwoFactorAuthenticationServicesDialog == null) {
                                mCustomProgressDialog.showFailureAnimationAndMessage(brilliantRechargeResponse.getMessage());
                            } else {
                                Toast.makeText(getContext(), brilliantRechargeResponse.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            if (brilliantRechargeResponse.getMessage().toLowerCase().contains(TwoFactorAuthConstants.WRONG_OTP)) {
                                if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                                    mOTPVerificationForTwoFactorAuthenticationServicesDialog.showOtpDialog();
                                    mCustomProgressDialog.dismissDialog();
                                }
                            } else {
                                if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                                    mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                                }
                            }
                            Utilities.sendFailedEventTracker(mTracker, Constants.BRILLIANT_BILL_PAY, ProfileInfoCacheManager.getAccountId(), brilliantRechargeResponse.getMessage(), new BigDecimal(mAmount).longValue());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mCustomProgressDialog.showFailureAnimationAndMessage(getString(R.string.recharge_failed));
                    if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                        mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                    }
                }
                mBrilliantRechargeTask = null;
            }
        }
    }
}
