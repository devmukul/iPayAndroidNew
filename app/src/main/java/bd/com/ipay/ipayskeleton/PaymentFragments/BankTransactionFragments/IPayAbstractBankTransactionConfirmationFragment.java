package bd.com.ipay.ipayskeleton.PaymentFragments.BankTransactionFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SendMoney.IPayTransactionResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public abstract class IPayAbstractBankTransactionConfirmationFragment extends IPayAbstractTransactionConfirmationFragment {

	public static final String TRANSACTION_AMOUNT_KEY = "TRANSACTION_AMOUNT";
	protected HttpRequestPostAsyncTask httpRequestPostAsyncTask = null;
	protected AnimatedProgressDialog mCustomProgressDialog = null;
	protected Number transactionAmount;
	protected BankAccountList bankAccountList;
	protected final Gson gson = new GsonBuilder().create();

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			transactionAmount = (Number) getArguments().getSerializable(TRANSACTION_AMOUNT_KEY);
			bankAccountList = getArguments().getParcelable(Constants.SELECTED_BANK_ACCOUNT);
		}
		mCustomProgressDialog = new AnimatedProgressDialog(getContext());
	}

	@Override
	protected boolean isPinRequired() {
		return true;
	}

	@Override
	protected boolean canUserAddNote() {
		return true;
	}

	@Override
	protected boolean verifyInput() {
		if (isPinRequired()) {
			if (TextUtils.isEmpty(getPin())) {
				showErrorMessage(getString(R.string.please_enter_a_pin));
				return false;
			} else if (getPin().length() != 4) {
				showErrorMessage(getString(R.string.minimum_pin_length_message));
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	@Override
	protected void performContinueAction() {
		mCustomProgressDialog.showDialog();
		httpRequestPostAsyncTask = new HttpRequestPostAsyncTask(getApiCommand(), getUrl(), getRequestJson(), getActivity(), this, false);
		httpRequestPostAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	protected abstract String getApiCommand();

	protected abstract String getRequestJson();

	protected abstract String getUrl();

	protected abstract void bankTransactionSuccess(final Bundle bundle);

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		if (HttpErrorHandler.isErrorFound(result, getContext(), mCustomProgressDialog)) {
			httpRequestPostAsyncTask = null;
			mCustomProgressDialog.dismissDialog();
		} else {
			switch (result.getApiCommand()) {
				case Constants.COMMAND_ADD_MONEY_FROM_BANK:
				case Constants.COMMAND_ADD_MONEY_FROM_BANK_INSTANTLY:
				case Constants.COMMAND_WITHDRAW_MONEY:
					httpRequestPostAsyncTask = null;
					final IPayTransactionResponse iPayTransactionResponse = new Gson().fromJson(result.getJsonString(), IPayTransactionResponse.class);

					switch (result.getStatus()) {
						case Constants.HTTP_RESPONSE_STATUS_OK:
							if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
								mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
							}
							mCustomProgressDialog.showSuccessAnimationAndMessage(iPayTransactionResponse.getMessage());
							if (getActivity() != null)
								Utilities.hideKeyboard(getActivity());
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									mCustomProgressDialog.hide();
									Bundle bundle = new Bundle();
									bundle.putSerializable(TRANSACTION_AMOUNT_KEY, transactionAmount);
									bundle.putParcelable(Constants.SELECTED_BANK_ACCOUNT, bankAccountList);
									bankTransactionSuccess(bundle);
								}
							}, 2000);
							sendSuccessEventTracking(transactionAmount);
							break;
						case Constants.HTTP_RESPONSE_STATUS_ACCEPTED:
						case Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED:
							mCustomProgressDialog.dismissDialog();
							Toast.makeText(getActivity(), iPayTransactionResponse.getMessage(), Toast.LENGTH_SHORT).show();
							launchOTPVerification(iPayTransactionResponse.getOtpValidFor(), getRequestJson(), getApiCommand(), getUrl());
							break;
						case Constants.HTTP_RESPONSE_STATUS_BLOCKED:
							if (getActivity() != null) {
								mCustomProgressDialog.setTitle(getString(R.string.failed));
								mCustomProgressDialog.showFailureAnimationAndMessage(iPayTransactionResponse.getMessage());
								((MyApplication) getActivity().getApplication()).launchLoginPage("");
								sendBlockedEventTracking(transactionAmount);
							}
							break;
						default:
							if (getActivity() != null) {
								if (mOTPVerificationForTwoFactorAuthenticationServicesDialog == null) {
									mCustomProgressDialog.showFailureAnimationAndMessage(iPayTransactionResponse.getMessage());
								} else {
									Toast.makeText(getContext(), iPayTransactionResponse.getMessage(), Toast.LENGTH_LONG).show();
								}

								if (iPayTransactionResponse.getMessage().toLowerCase().contains(TwoFactorAuthConstants.WRONG_OTP)) {
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
								sendFailedEventTracking(iPayTransactionResponse.getMessage(), transactionAmount);
								break;
							}
					}
					break;
			}
		}
	}
}
