package bd.com.ipay.ipayskeleton.PaymentFragments.TopupFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SendMoney.IPayTransactionResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TopUp.TopupRequest;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IPayTopUpConfirmationFragment extends IPayAbstractTransactionConfirmationFragment {

	private String name;
	private String mobileNumber;
	private String operatorCode;
	private int operatorType;
	private String profilePicture;
	private Number transactionAmount;
	private final Gson gson = new Gson();
	private HttpRequestPostAsyncTask topupRequestTask = null;
	private TopupRequest topupRequest;
	private String uri;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			name = getArguments().getString(Constants.NAME);
			mobileNumber = getArguments().getString(Constants.MOBILE_NUMBER);
			profilePicture = getArguments().getString(Constants.PHOTO_URI);
			transactionAmount = (Number) getArguments().getSerializable(IPayTopUpAmountInputFragment.TRANSACTION_AMOUNT_KEY);
			operatorCode = getArguments().getString(Constants.OPERATOR_CODE);
			operatorType = getArguments().getInt(Constants.OPERATOR_TYPE);
		}
	}

	@Override
	protected void setupViewProperties() {
		setTransactionDescription(getStyledTransactionDescription(R.string.top_up_confirmation_message, transactionAmount));
		setName(name);
		setUserName(mobileNumber);
		setTransactionImage(profilePicture);
		setNoteEditTextHint(getString(R.string.short_note_optional_hint));
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
		return "Top Up";
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
		if (!Utilities.isConnectionAvailable(getContext())) {
			Toaster.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT);
		}
		if (topupRequestTask == null) {
			topupRequest = new TopupRequest(mobileNumber, operatorType, operatorCode, transactionAmount.intValue());
			topupRequest.setPin(getPin());
			String json = gson.toJson(topupRequest);
			uri = Constants.BASE_URL_SM + Constants.URL_TOPUP_REQUEST_V3;
			topupRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_TOPUP_REQUEST,
					uri, json, getActivity(), this, false);
			topupRequestTask.setPinAsHeader(getPin());
			topupRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			customProgressDialog.showDialog();
		}
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		if (getActivity() == null)
			return;

		if (HttpErrorHandler.isErrorFound(result, getContext(), customProgressDialog)) {
			customProgressDialog.dismissDialog();
			topupRequestTask = null;
		} else {
			try {
				switch (result.getApiCommand()) {
					case Constants.COMMAND_TOPUP_REQUEST:
						final IPayTransactionResponse iPayTransactionResponse = gson.fromJson(result.getJsonString(), IPayTransactionResponse.class);
						switch (result.getStatus()) {
							case Constants.HTTP_RESPONSE_STATUS_OK:
								if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
									mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
								} else {
									customProgressDialog.setTitle(R.string.success);
									customProgressDialog.showSuccessAnimationAndMessage(iPayTransactionResponse.getMessage());
								}
								sendSuccessEventTracking(transactionAmount);
								new Handler().postDelayed(new Runnable() {
									@Override
									public void run() {
										customProgressDialog.dismissDialog();
										Bundle bundle = new Bundle();
										bundle.putString(Constants.NAME, name);
										bundle.putString(Constants.MOBILE_NUMBER, mobileNumber);
										bundle.putString(Constants.PHOTO_URI, profilePicture);
										bundle.putSerializable(IPayTopUpAmountInputFragment.TRANSACTION_AMOUNT_KEY, transactionAmount);
										if (getActivity() instanceof IPayTransactionActionActivity) {
											Toast.makeText(getContext(), "You have made a top up request to " +
													ContactEngine.formatMobileNumberBD(mobileNumber) + ".\n" + "Please check transaction" +
													" history to see the status", Toast.LENGTH_LONG).show();
											getActivity().finish();
										}

									}
								}, 2000);
								if (getActivity() != null)
									Utilities.hideKeyboard(getActivity());
								break;
							case Constants.HTTP_RESPONSE_STATUS_BLOCKED:
								if (getActivity() != null) {
									customProgressDialog.setTitle(R.string.failed);
									customProgressDialog.showFailureAnimationAndMessage(iPayTransactionResponse.getMessage());
									sendBlockedEventTracking(transactionAmount);
								}
								new Handler().postDelayed(new Runnable() {
									@Override
									public void run() {
										((MyApplication) getActivity().getApplication()).launchLoginPage(iPayTransactionResponse.getMessage());
									}
								}, 2000);
								break;
							case Constants.HTTP_RESPONSE_STATUS_ACCEPTED:
							case Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED:
								customProgressDialog.dismissDialog();
								Toast.makeText(getActivity(), iPayTransactionResponse.getMessage(), Toast.LENGTH_SHORT).show();
								launchOTPVerification(iPayTransactionResponse.getOtpValidFor(), gson.toJson(topupRequest), Constants.COMMAND_TOPUP_REQUEST, uri);
								break;
							case Constants.HTTP_RESPONSE_STATUS_BAD_REQUEST:
								final String errorMessage;
								if (!TextUtils.isEmpty(iPayTransactionResponse.getMessage())) {
									errorMessage = iPayTransactionResponse.getMessage();
								} else {
									errorMessage = getString(R.string.server_down);
								}
								customProgressDialog.setTitle(R.string.failed);
								customProgressDialog.showFailureAnimationAndMessage(errorMessage);
								break;
							default:
								if (getActivity() != null) {
									if (mOTPVerificationForTwoFactorAuthenticationServicesDialog == null) {
										customProgressDialog.showFailureAnimationAndMessage(iPayTransactionResponse.getMessage());
									} else {
										Toast.makeText(getContext(), iPayTransactionResponse.getMessage(), Toast.LENGTH_LONG).show();
									}

									if (iPayTransactionResponse.getMessage().toLowerCase().contains(TwoFactorAuthConstants.WRONG_OTP)) {
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
									sendFailedEventTracking(iPayTransactionResponse.getMessage(), transactionAmount);
									break;
								}
								break;
						}
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				customProgressDialog.showFailureAnimationAndMessage(getString(R.string.payment_failed));
				sendFailedEventTracking(e.getMessage(), transactionAmount);
			}
			topupRequestTask = null;
		}
	}
}
