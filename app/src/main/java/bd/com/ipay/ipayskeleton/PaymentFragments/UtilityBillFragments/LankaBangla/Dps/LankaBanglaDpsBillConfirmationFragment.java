package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Dps;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.MetaData;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.Notification;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.RecentBill;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GenericBillPayResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.LankaBanglaDpsBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.LinkThreeBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.NotificationForOther;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LinkThree.LinkThreeBillAmountInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class LankaBanglaDpsBillConfirmationFragment extends IPayAbstractTransactionConfirmationFragment {

	private final Gson gson = new Gson();

	private HttpRequestPostAsyncTask lankaBanglaDpsPayTask = null;

	private String accountNumber;
	private String accountUserName;

	private Number billAmount;

	private String uri;


	private String otherPersonName;
	private String otherPersonMobile;
	DataHelper dataHelper ;
	private boolean isFromSaved;

	private LankaBanglaDpsBillPayRequest lankaBanglaDpsBillPayRequest;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataHelper = DataHelper.getInstance(getContext());
		if (getArguments() != null) {
			accountNumber = getArguments().getString(LankaBanglaDpsAmountInputFragment.ACCOUNT_NUMBER_KEY, "");
			accountUserName = getArguments().getString(LankaBanglaDpsAmountInputFragment.ACCOUNT_USER_NAME_KEY, "");
			billAmount = (Number) getArguments().getSerializable(LankaBanglaDpsAmountInputFragment.INSTALLMENT_AMOUNT_KEY);
			otherPersonName = getArguments().getString(LankaBanglaDpsAmountInputFragment.OTHER_PERSON_NAME_KEY, "");
			otherPersonMobile = getArguments().getString(LankaBanglaDpsAmountInputFragment.OTHER_PERSON_MOBILE_KEY, "");

			isFromSaved = getArguments().getBoolean("IS_FROM_HISTORY", false);
		}
	}

	@Override
	protected void setupViewProperties() {
		setTransactionImageResource(R.drawable.ic_lankabd2);
		setTransactionDescription(getStyledTransactionDescription(R.string.pay_bill_confirmation_message, billAmount));
		setName(accountNumber);
		setUserName(accountUserName);
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
		return Constants.LANKA_BANGLA_DPS_BILL_PAY;
	}

	@Override
	protected boolean verifyInput() {
		final String errorMessage;
		if (TextUtils.isEmpty(getPin())) {
			errorMessage = getString(R.string.please_enter_a_pin);
		} else if (getPin().length() != 4) {
			errorMessage = getString(R.string.minimum_pin_length_message);
		} else {
			errorMessage = null;
		}
		if (errorMessage != null) {
			showErrorMessage(errorMessage);
			return false;
		}
		return true;
	}

	@Override
	protected void performContinueAction() {
		if (!Utilities.isConnectionAvailable(getContext())) {
			Toaster.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT);
		}
		if (lankaBanglaDpsPayTask == null) {
			if(TextUtils.isEmpty(otherPersonName) && TextUtils.isEmpty(otherPersonMobile)){
				lankaBanglaDpsBillPayRequest = new LankaBanglaDpsBillPayRequest(accountNumber, getPin());
			}else{
				bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.MetaData metaData = new bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.MetaData(new NotificationForOther(otherPersonName, otherPersonMobile));
				lankaBanglaDpsBillPayRequest = new LankaBanglaDpsBillPayRequest(accountNumber, getPin(), metaData);

			}

			String json = gson.toJson(lankaBanglaDpsBillPayRequest);
			uri = Constants.BASE_URL_UTILITY + Constants.URL_LANKABANGLA_DPS_BILL_PAY;
			lankaBanglaDpsPayTask = new HttpRequestPostAsyncTask(Constants.COMMAND_LANKABANGLA_BILL_PAY,
					uri, json, getActivity(), this, false);
			lankaBanglaDpsPayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			customProgressDialog.showDialog();
		}
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		if (getActivity() == null)
			return;

		if (HttpErrorHandler.isErrorFound(result, getContext(), customProgressDialog)) {
			lankaBanglaDpsPayTask = null;
		} else {
			try {
				switch (result.getApiCommand()) {
					case Constants.COMMAND_LANKABANGLA_BILL_PAY:
						final GenericBillPayResponse mGenericBillPayResponse = gson.fromJson(result.getJsonString(), GenericBillPayResponse.class);
						switch (result.getStatus()) {
							case Constants.HTTP_RESPONSE_STATUS_PROCESSING:
								customProgressDialog.dismissDialog();
								if (getActivity() != null)
									Utilities.hideKeyboard(getActivity());
								Toaster.makeText(getContext(), R.string.request_on_process, Toast.LENGTH_SHORT);
								if (getActivity() != null) {
									Utilities.hideKeyboard(getActivity());
									getActivity().finish();
								}
								break;
							case Constants.HTTP_RESPONSE_STATUS_OK:
								if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
									mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
								} else {
									customProgressDialog.setTitle(R.string.success);
									customProgressDialog.showSuccessAnimationAndMessage(mGenericBillPayResponse.getMessage());
								}
								sendSuccessEventTracking(billAmount);
								new Handler().postDelayed(new Runnable() {
									@Override
									public void run() {
										customProgressDialog.dismissDialog();
										Bundle bundle = new Bundle();
										bundle.putString(LankaBanglaDpsAmountInputFragment.ACCOUNT_NUMBER_KEY, accountNumber);
										bundle.putString(LankaBanglaDpsAmountInputFragment.ACCOUNT_USER_NAME_KEY, accountUserName);
										bundle.putSerializable(LankaBanglaDpsAmountInputFragment.INSTALLMENT_AMOUNT_KEY, billAmount);
										bundle.putString(LankaBanglaDpsAmountInputFragment.OTHER_PERSON_NAME_KEY, otherPersonName);
										bundle.putString(LankaBanglaDpsAmountInputFragment.OTHER_PERSON_MOBILE_KEY, otherPersonMobile);
										if (getActivity() instanceof IPayUtilityBillPayActionActivity) {
											int maxBackStack=3;
											if(isFromSaved)
												maxBackStack =4;
											((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new LankaBanglaDpsBillSuccessFragment(), bundle, maxBackStack, true);
										}

									}
								}, 2000);
								if (getActivity() != null)
									Utilities.hideKeyboard(getActivity());

								saveRecent();
								break;
							case Constants.HTTP_RESPONSE_STATUS_BLOCKED:
								if (getActivity() != null) {
									customProgressDialog.setTitle(R.string.failed);
									customProgressDialog.showFailureAnimationAndMessage(mGenericBillPayResponse.getMessage());
									sendBlockedEventTracking(billAmount);
								}
								new Handler().postDelayed(new Runnable() {
									@Override
									public void run() {
										((MyApplication) getActivity().getApplication()).launchLoginPage(mGenericBillPayResponse.getMessage());
									}
								}, 2000);
								break;
							case Constants.HTTP_RESPONSE_STATUS_ACCEPTED:
							case Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED:
								customProgressDialog.dismissDialog();
								Toast.makeText(getActivity(), mGenericBillPayResponse.getMessage(), Toast.LENGTH_SHORT).show();
								launchOTPVerification(mGenericBillPayResponse.getOtpValidFor(), gson.toJson(lankaBanglaDpsBillPayRequest), Constants.COMMAND_LANKABANGLA_BILL_PAY, uri);
								break;
							case Constants.HTTP_RESPONSE_STATUS_BAD_REQUEST:
								final String errorMessage;
								if (!TextUtils.isEmpty(mGenericBillPayResponse.getMessage())) {
									errorMessage = mGenericBillPayResponse.getMessage();
								} else {
									errorMessage = getString(R.string.server_down);
								}
								customProgressDialog.setTitle(R.string.failed);
								customProgressDialog.showFailureAnimationAndMessage(errorMessage);
								break;
							default:
								if (getActivity() != null) {
									if (mOTPVerificationForTwoFactorAuthenticationServicesDialog == null) {
										customProgressDialog.showFailureAnimationAndMessage(mGenericBillPayResponse.getMessage());
									} else {
										Toast.makeText(getContext(), mGenericBillPayResponse.getMessage(), Toast.LENGTH_LONG).show();
									}

									if (mGenericBillPayResponse.getMessage().toLowerCase().contains(TwoFactorAuthConstants.WRONG_OTP)) {
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
									sendFailedEventTracking(mGenericBillPayResponse.getMessage(), billAmount);
									break;
								}
								break;
						}
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				customProgressDialog.showFailureAnimationAndMessage(getString(R.string.payment_failed));
				sendFailedEventTracking(e.getMessage(), billAmount);
			}
			lankaBanglaDpsPayTask = null;
		}
	}

	public void saveRecent(){
		Date c = new Date();
		SimpleDateFormat curFormater = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		String formattedDate = curFormater.format(c);

		List<RecentBill> recentBills = new ArrayList<>();
		RecentBill recentBill = new RecentBill();
		recentBill.setShortName("");
		recentBill.setScheduledToo(false);
		recentBill.setSaved(false);
		recentBill.setProviderCode("LANKABANGLA-DPS");
		recentBill.setDateOfBillPayment(0);
		recentBill.setLastPaid(formattedDate);
		if(!TextUtils.isEmpty(otherPersonName) && !TextUtils.isEmpty(otherPersonMobile)){
			MetaData metaData = new MetaData(new Notification(otherPersonName, otherPersonMobile));
			recentBill.setPaidForOthers(true);
			recentBill.setMetaData(new  Gson().toJson(metaData));
		}else{
			recentBill.setPaidForOthers(false);
		}
		recentBill.setParamId("accountNumber");
		recentBill.setParamLabel(getString(R.string.account_number));
		recentBill.setParamValue(accountNumber);
		recentBill.setAmount(String.valueOf(billAmount));
		recentBills.add(recentBill);
		dataHelper.createBills(recentBills);
	}
}
