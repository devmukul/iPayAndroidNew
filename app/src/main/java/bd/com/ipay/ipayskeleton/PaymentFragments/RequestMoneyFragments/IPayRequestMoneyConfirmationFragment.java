package bd.com.ipay.ipayskeleton.PaymentFragments.RequestMoneyFragments;

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
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RequestMoney.RequestMoneyRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SendMoney.IPayTransactionResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IPayRequestMoneyConfirmationFragment extends IPayAbstractTransactionConfirmationFragment {

	private String name;
	private String mobileNumber;
	private String profilePicture;
	private Number transactionAmount;
	private final Gson gson = new Gson();
	private HttpRequestPostAsyncTask sendMoneyRequestTask = null;
	private RequestMoneyRequest requestMoneyRequest;
	private String uri;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			name = getArguments().getString(Constants.NAME);
			mobileNumber = getArguments().getString(Constants.MOBILE_NUMBER);
			profilePicture = getArguments().getString(Constants.PHOTO_URI);
			transactionAmount = (Number) getArguments().getSerializable(IPayRequestMoneyAmountInputFragment.TRANSACTION_AMOUNT_KEY);
		}
	}

	@Override
	protected void setupViewProperties() {
		setTransactionDescription(getStyledTransactionDescription(R.string.request_money_confirmation_message, transactionAmount));
		setName(name);
		setUserName(mobileNumber);
		setTransactionImage(profilePicture);
		setNoteEditTextHint(getString(R.string.short_note_hint));
	}

	@Override
	protected boolean isPinRequired() {
		return false;
	}

	@Override
	protected boolean canUserAddNote() {
		return true;
	}

	@Override
	protected String getTrackerCategory() {
		return "Request Money";
	}

	@Override
	protected boolean verifyInput() {
		if (TextUtils.isEmpty(getNote())) {
			showErrorMessage(getString(R.string.please_write_note));
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected void performContinueAction() {
		if (!Utilities.isConnectionAvailable(getContext())) {
			Toaster.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT);
		}
		if (sendMoneyRequestTask == null) {
			requestMoneyRequest = new RequestMoneyRequest(mobileNumber, transactionAmount.doubleValue(), getNote());
			String json = gson.toJson(requestMoneyRequest);
			uri = Constants.BASE_URL_SM + Constants.URL_REQUEST_MONEY;
			sendMoneyRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_REQUEST_MONEY,
					uri, json, getActivity(), this, false);
			sendMoneyRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			customProgressDialog.showDialog();
		}
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		if (getActivity() == null)
			return;

		if (HttpErrorHandler.isErrorFound(result, getContext(), customProgressDialog)) {
			customProgressDialog.dismissDialog();
			sendMoneyRequestTask = null;
		} else {
			try {
				switch (result.getApiCommand()) {
					case Constants.COMMAND_REQUEST_MONEY:
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
										bundle.putSerializable(IPayRequestMoneyAmountInputFragment.TRANSACTION_AMOUNT_KEY, transactionAmount);
										if (getActivity() instanceof IPayTransactionActionActivity) {
											((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayRequestMoneySuccessFragment(), bundle, 3, true);
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
								launchOTPVerification(iPayTransactionResponse.getOtpValidFor(), gson.toJson(requestMoneyRequest), Constants.COMMAND_REQUEST_MONEY, uri);
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
			sendMoneyRequestTask = null;
		}
	}
}
