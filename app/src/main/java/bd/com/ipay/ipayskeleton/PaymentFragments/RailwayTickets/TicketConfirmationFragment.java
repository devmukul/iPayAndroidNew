package bd.com.ipay.ipayskeleton.PaymentFragments.RailwayTickets;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.RailwayTicketActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RailwayTickets.PurchaseTicketRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GenericBillPayResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class TicketConfirmationFragment extends IPayAbstractTransactionConfirmationFragment {

	private final Gson gson = new Gson();
	private HttpRequestPostAsyncTask railwayTicketTask = null;
	private PurchaseTicketRequest purchaseTicketRequest;

	private String uri;
	private String mSelectedStationFrom = null;
    private String mSelectedStationTo = null;
    private String mSelectedGender = null;
    private int mSelectedDate;
    private int mSelectedAdult;
    private int mSelectedChild;

    private String mSelectedTrain = null;
    private String mSelectedClass = null;
    private String mSelectedTicketId = null;
    private String mSelectedMessage = null;
    private int mSelectedTrainNo;
    private double mFareAmount;
    private double mVatAmount;
    private double mTotalAmount;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mSelectedClass = getArguments().getString(RailwayTicketActionActivity.KEY_TICKET_CLASS_NAME, "");
            mSelectedGender = getArguments().getString(RailwayTicketActionActivity.KEY_TICKET_GENDER, "");
            mSelectedDate = getArguments().getInt(RailwayTicketActionActivity.KEY_TICKET_DATE, 0);
            mSelectedAdult = getArguments().getInt(RailwayTicketActionActivity.KEY_TICKET_ADULTS, 0);
            mSelectedChild = getArguments().getInt(RailwayTicketActionActivity.KEY_TICKET_CHILD, 0);
            mSelectedStationFrom = getArguments().getString(RailwayTicketActionActivity.KEY_TICKET_STATION_FROM, "");
            mSelectedStationTo = getArguments().getString(RailwayTicketActionActivity.KEY_TICKET_STATION_TO, "");
            mSelectedTrain = getArguments().getString(RailwayTicketActionActivity.KEY_TICKET_TRAIN_NAME, "");
            mSelectedTicketId = getArguments().getString(RailwayTicketActionActivity.KEY_TICKET_TICKET_ID, "");
            mSelectedMessage = getArguments().getString(RailwayTicketActionActivity.KEY_TICKET_MESSAGE_ID, "");
            mSelectedTrainNo = getArguments().getInt(RailwayTicketActionActivity.KEY_TICKET_TRAIN_NO, 0);
            mFareAmount = getArguments().getDouble(RailwayTicketActionActivity.KEY_TICKET_FARE_AMOUNT, 0);
            mVatAmount = getArguments().getDouble(RailwayTicketActionActivity.KEY_TICKET_VAT_AMOUNT, 0);
            mTotalAmount = getArguments().getDouble(RailwayTicketActionActivity.KEY_TICKET_TOTAL_AMOUNT, 0);
		}
	}

	@Override
	protected void setupViewProperties() {
		setTransactionImageResource(R.drawable.bd_railway);
		setTransactionDescription(getStyledTransactionDescription(R.string.make_payment_confirmation_message_tk, mTotalAmount));
        setName(getString(R.string.railway_ticket_name));
        setUserName(Utilities.formatJourneyInfoText(getContext(),mSelectedTrain +" - "+mSelectedTrainNo, mSelectedAdult, mSelectedChild));
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
		return Constants.COMMAND_RAILWAY_TICKET_PURCHASE;
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
		if (railwayTicketTask == null) {
			purchaseTicketRequest = new PurchaseTicketRequest();
			purchaseTicketRequest.setClassName(mSelectedClass);
			purchaseTicketRequest.setFare(mFareAmount);
			purchaseTicketRequest.setGender(mSelectedGender);
			purchaseTicketRequest.setJourneyDate(mSelectedDate);
			purchaseTicketRequest.setNumberOfAdults(mSelectedAdult);
			purchaseTicketRequest.setNumberOfChildren(mSelectedChild);
			purchaseTicketRequest.setStationFrom(mSelectedStationFrom);
			purchaseTicketRequest.setStationTo(mSelectedStationTo);
			purchaseTicketRequest.setTicketId(mSelectedTicketId);
			purchaseTicketRequest.setTotalFare(mTotalAmount);
			purchaseTicketRequest.setTrainNumber(mSelectedTrainNo);
			purchaseTicketRequest.setVat(mVatAmount);
			purchaseTicketRequest.setPin(getPin());
			String json = gson.toJson(purchaseTicketRequest);
			uri = Constants.BASE_URL_CNS + Constants.URL_PURCHASE_TICKET;
			railwayTicketTask = new HttpRequestPostAsyncTask(Constants.COMMAND_RAILWAY_TICKET_PURCHASE,
					uri, json, getActivity(), this, false);
			railwayTicketTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			customProgressDialog.showDialog();
		}
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		if (getActivity() == null)
			return;

		if (HttpErrorHandler.isErrorFound(result, getContext(), customProgressDialog)) {
			railwayTicketTask = null;
		} else {
			try {
				switch (result.getApiCommand()) {
					case Constants.COMMAND_RAILWAY_TICKET_PURCHASE:
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
								sendSuccessEventTracking(mTotalAmount);
								new Handler().postDelayed(new Runnable() {
									@Override
									public void run() {
										customProgressDialog.dismissDialog();
                                        Bundle bundle = new Bundle();
                                        bundle.putString(RailwayTicketActionActivity.KEY_TICKET_TRAIN_NAME, mSelectedTrain);
                                        bundle.putString(RailwayTicketActionActivity.KEY_TICKET_CLASS_NAME, mSelectedClass);
                                        bundle.putDouble(RailwayTicketActionActivity.KEY_TICKET_FARE_AMOUNT, mFareAmount);
                                        bundle.putString(RailwayTicketActionActivity.KEY_TICKET_GENDER, mSelectedGender);
                                        bundle.putInt(RailwayTicketActionActivity.KEY_TICKET_DATE, mSelectedDate);
                                        bundle.putInt(RailwayTicketActionActivity.KEY_TICKET_ADULTS, mSelectedAdult);
                                        bundle.putInt(RailwayTicketActionActivity.KEY_TICKET_CHILD, mSelectedChild);
                                        bundle.putString(RailwayTicketActionActivity.KEY_TICKET_STATION_FROM, mSelectedStationFrom);
                                        bundle.putString(RailwayTicketActionActivity.KEY_TICKET_STATION_TO, mSelectedStationTo);
                                        bundle.putString(RailwayTicketActionActivity.KEY_TICKET_TICKET_ID, mSelectedTicketId);
                                        bundle.putDouble(RailwayTicketActionActivity.KEY_TICKET_TOTAL_AMOUNT, mTotalAmount);
                                        bundle.putString(RailwayTicketActionActivity.KEY_TICKET_MESSAGE_ID, mSelectedMessage);
                                        bundle.putInt(RailwayTicketActionActivity.KEY_TICKET_TRAIN_NO, mSelectedTrainNo);
                                        bundle.putDouble(RailwayTicketActionActivity.KEY_TICKET_VAT_AMOUNT, mVatAmount);
										if (getActivity() instanceof RailwayTicketActionActivity) {
											((RailwayTicketActionActivity) getActivity()).switchFragment(new TicketSuccessFragment(), bundle, 3, true);
										}

									}
								}, 2000);
								if (getActivity() != null)
									Utilities.hideKeyboard(getActivity());
								break;
							case Constants.HTTP_RESPONSE_STATUS_BLOCKED:
								if (getActivity() != null) {
									customProgressDialog.setTitle(R.string.failed);
									customProgressDialog.showFailureAnimationAndMessage(mGenericBillPayResponse.getMessage());
									sendBlockedEventTracking(mTotalAmount);
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
								launchOTPVerification(mGenericBillPayResponse.getOtpValidFor(), gson.toJson(purchaseTicketRequest), Constants.COMMAND_RAILWAY_TICKET_PURCHASE, uri);
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
									sendFailedEventTracking(mGenericBillPayResponse.getMessage(), mTotalAmount);
									break;
								}
								break;
						}
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				customProgressDialog.showFailureAnimationAndMessage(getString(R.string.payment_failed));
				sendFailedEventTracking(e.getMessage(), mTotalAmount);
			}
			railwayTicketTask = null;
		}
	}
}
