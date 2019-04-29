package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Card;

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
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.LankaBanglaCardBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.LankaBanglaDpsBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.NotificationForOther;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Dps.LankaBanglaDpsAmountInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CardNumberValidator;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class LankaBanglaBillConfirmationFragment extends IPayAbstractTransactionConfirmationFragment {

	protected static final String BILL_AMOUNT_KEY = "BILL_AMOUNT";
	protected static final String AMOUNT_TYPE_KEY = "AMOUNT_TYPE";
	private final Gson gson = new Gson();

	private HttpRequestPostAsyncTask lankaBanglaCardBillPayTask = null;

	private String amountType;
	private Number billAmount;
	private String cardNumber;
	private String cardUserName;

	private String uri;
	private LankaBanglaCardBillPayRequest lankaBanglaCardBillPayRequest;

	private String otherPersonName;
	private String otherPersonMobile;
	DataHelper dataHelper ;
	private boolean isFromSaved;
	private String cardType;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        dataHelper = DataHelper.getInstance(getContext());
		if (getArguments() != null) {
			amountType = getArguments().getString(AMOUNT_TYPE_KEY, Constants.OTHER);
			billAmount = (Number) getArguments().getSerializable(BILL_AMOUNT_KEY);
			cardNumber = getArguments().getString(LankaBanglaAmountInputFragment.CARD_NUMBER_KEY, "");
			cardUserName = getArguments().getString(LankaBanglaAmountInputFragment.CARD_USER_NAME_KEY, "");
			otherPersonName = getArguments().getString(LankaBanglaAmountInputFragment.OTHER_PERSON_NAME_KEY, "");
			otherPersonMobile = getArguments().getString(LankaBanglaAmountInputFragment.OTHER_PERSON_MOBILE_KEY, "");
			cardType = getArguments().getString(Constants.CARD_TYPE,"");
			isFromSaved = getArguments().getBoolean("IS_FROM_HISTORY", false);
		}
	}

	@Override
	protected void setupViewProperties() {
		setTransactionImageResource(R.drawable.ic_lankabd2);
		setTransactionDescription(getStyledTransactionDescription(R.string.pay_bill_confirmation_message, billAmount));
		setName(CardNumberValidator.deSanitizeEntry(cardNumber, ' '));
		setUserName(cardUserName);
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
		return Constants.LANKA_BANGLA_BILL_PAY;
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
		if (lankaBanglaCardBillPayTask == null) {

			if(TextUtils.isEmpty(otherPersonName) && TextUtils.isEmpty(otherPersonMobile)){
				lankaBanglaCardBillPayRequest = new LankaBanglaCardBillPayRequest(cardNumber, billAmount.toString(), amountType, getPin());
			}else{
				bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.MetaData metaData = new bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.MetaData(new NotificationForOther(otherPersonName, otherPersonMobile));
				lankaBanglaCardBillPayRequest = new LankaBanglaCardBillPayRequest(cardNumber, billAmount.toString(), amountType, getPin(), metaData);
			}
			String json = gson.toJson(lankaBanglaCardBillPayRequest);
			CardNumberValidator.Cards cards = CardNumberValidator.getCardType(cardNumber);
			if (cards != null) {
				if (cards == CardNumberValidator.Cards.VISA) {
					uri = Constants.BASE_URL_UTILITY + Constants.URL_LANKABANGLA_VISA_BILL_PAY;
				} else if (cards == CardNumberValidator.Cards.MASTERCARD) {
					uri = Constants.BASE_URL_UTILITY + Constants.URL_LANKABANGLA_MASTERCARD_BILL_PAY;
				} else {
					return;
				}
			} else {
				return;
			}
			lankaBanglaCardBillPayTask = new HttpRequestPostAsyncTask(Constants.COMMAND_LANKABANGLA_BILL_PAY,
					uri, json, getActivity(), this, false);
			lankaBanglaCardBillPayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			customProgressDialog.showDialog();
		}
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		if (getActivity() == null)
			return;

		if (HttpErrorHandler.isErrorFound(result, getContext(), customProgressDialog)) {
			customProgressDialog.dismissDialog();
			lankaBanglaCardBillPayTask = null;
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
										bundle.putString(LankaBanglaAmountInputFragment.CARD_NUMBER_KEY, cardNumber);
										bundle.putSerializable(LankaBanglaBillConfirmationFragment.BILL_AMOUNT_KEY, billAmount);
										bundle.putString(LankaBanglaAmountInputFragment.OTHER_PERSON_NAME_KEY, otherPersonName);
										bundle.putString(LankaBanglaAmountInputFragment.OTHER_PERSON_MOBILE_KEY, otherPersonMobile);
										bundle.putString(LankaBanglaBillConfirmationFragment.AMOUNT_TYPE_KEY, amountType);
										bundle.putString(Constants.CARD_TYPE, cardType);

										if(isFromSaved) {
											bundle.putBoolean("IS_FROM_HISTORY", true);
										}
										if (getActivity() instanceof IPayUtilityBillPayActionActivity) {
											int maxBackStack=4;
											if(isFromSaved)
												maxBackStack =5;
											((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new LankaBanglaBillSuccessFragment(), bundle, 4, true);
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
								launchOTPVerification(mGenericBillPayResponse.getOtpValidFor(), gson.toJson(lankaBanglaCardBillPayRequest), Constants.COMMAND_LANKABANGLA_BILL_PAY, uri);
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
			lankaBanglaCardBillPayTask = null;
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
		recentBill.setProviderCode(cardType);
		recentBill.setDateOfBillPayment(0);
		recentBill.setLastPaid(formattedDate);
		if(!TextUtils.isEmpty(otherPersonName) && !TextUtils.isEmpty(otherPersonMobile)){
			MetaData metaData = new MetaData(new Notification(otherPersonName, otherPersonMobile));
			recentBill.setPaidForOthers(true);
			recentBill.setMetaData(new  Gson().toJson(metaData));
		}else{
			recentBill.setPaidForOthers(false);
		}
		recentBill.setParamId("cardNumber");
		recentBill.setParamLabel(getString(R.string.card_number));
		recentBill.setParamValue(cardNumber);
		recentBill.setAmount(String.valueOf(billAmount));
		recentBill.setAmountType(amountType);
		recentBills.add(recentBill);
		dataHelper.createBills(recentBills);
	}
}
