package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.Carnival;

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
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.CarnivalBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GenericBillPayResponse;
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

public class CarnivalBillConfirmationFragment extends IPayAbstractTransactionConfirmationFragment {

	protected static final String BILL_AMOUNT_KEY = "BILL_AMOUNT";
	private final Gson gson = new Gson();

	private HttpRequestPostAsyncTask carnivalBillPayRequest = null;

	private Number billAmount;
	private String carnivalId;
	private String userName;

	private String uri;
	private CarnivalBillPayRequest carnivalBillPayRequest1;


	private String otherPersonName;
	private String otherPersonMobile;
	private LinkThreeBillPayRequest linkThreeBillPayRequest;
	DataHelper dataHelper ;
	private boolean isFromSaved;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		dataHelper = DataHelper.getInstance(getContext());
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			carnivalId = getArguments().getString(CarnivalBillAmountInputFragment.CARNIVAL_ID_KEY, "");
			billAmount = (Number) getArguments().getSerializable(BILL_AMOUNT_KEY);
			userName = getArguments().getString(CarnivalBillAmountInputFragment.USER_NAME_KEY, "");
			otherPersonName = getArguments().getString(CarnivalBillAmountInputFragment.OTHER_PERSON_NAME_KEY, "");
			otherPersonMobile = getArguments().getString(CarnivalBillAmountInputFragment.OTHER_PERSON_MOBILE_KEY, "");
			isFromSaved = getArguments().getBoolean("IS_FROM_HISTORY", false);
		}
	}

	@Override
	protected void setupViewProperties() {
		setTransactionImageResource(R.drawable.ic_carnival);
		setTransactionDescription(getStyledTransactionDescription(R.string.pay_bill_confirmation_message, billAmount));
		setName(carnivalId);
		setUserName(userName);
		setTransactionConfirmationButtonTitle(getString(R.string.pay));
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
		return Constants.CARNIVAL_BILL_PAY;
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
		if (carnivalBillPayRequest == null) {

            if(TextUtils.isEmpty(otherPersonName) && TextUtils.isEmpty(otherPersonMobile)){
                carnivalBillPayRequest1 = new CarnivalBillPayRequest(carnivalId, billAmount.toString(), getPin());
            }else{
                bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.MetaData metaData = new bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.MetaData(new NotificationForOther(otherPersonName, otherPersonMobile));
                carnivalBillPayRequest1 = new CarnivalBillPayRequest(carnivalId, billAmount.toString(), getPin(), metaData);

            }
			String json = gson.toJson(carnivalBillPayRequest1);
			uri = Constants.BASE_URL_UTILITY + Constants.URL_CARNIVAL_BILL_PAY;
			carnivalBillPayRequest = new HttpRequestPostAsyncTask(Constants.COMMAND_CARNIVAL_BILL_PAY,
					uri, json, getActivity(), this, false);
			carnivalBillPayRequest.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			customProgressDialog.showDialog();
		}
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		if (getActivity() == null)
			return;

		if (HttpErrorHandler.isErrorFound(result, getContext(), customProgressDialog)) {
			customProgressDialog.dismissDialog();
			carnivalBillPayRequest = null;
		} else {
			try {
				switch (result.getApiCommand()) {
					case Constants.COMMAND_CARNIVAL_BILL_PAY:
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
										bundle.putString(CarnivalBillAmountInputFragment.CARNIVAL_ID_KEY, carnivalId);
										bundle.putString(CarnivalBillAmountInputFragment.USER_NAME_KEY, userName);
										bundle.putSerializable(BILL_AMOUNT_KEY, billAmount);
										if (getActivity() instanceof IPayUtilityBillPayActionActivity) {
											((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new CarnivalBillSuccessFragment(), bundle, 3, true);
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
								launchOTPVerification(mGenericBillPayResponse.getOtpValidFor(), gson.toJson(carnivalBillPayRequest1), Constants.COMMAND_LINK_THREE_BILL_PAY, uri);
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
			carnivalBillPayRequest = null;
		}
	}

    public void saveRecent(){
        Date c = new Date();
        SimpleDateFormat curFormater = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String formattedDate = curFormater.format(c);

        List<RecentBill> recentBills = new ArrayList<>();
        RecentBill recentBill = new RecentBill();
        if(TextUtils.isEmpty(userName))
            recentBill.setShortName("");
        else
            recentBill.setShortName(userName);
        recentBill.setScheduledToo(false);
        recentBill.setSaved(false);
        recentBill.setProviderCode("CARNIVAL");
        recentBill.setDateOfBillPayment(0);
        recentBill.setLastPaid(formattedDate);
        if(!TextUtils.isEmpty(otherPersonName) && !TextUtils.isEmpty(otherPersonMobile)){
            MetaData metaData = new MetaData(new Notification(otherPersonName, otherPersonMobile));
            recentBill.setPaidForOthers(true);
            recentBill.setMetaData(new  Gson().toJson(metaData));
        }else{
            recentBill.setPaidForOthers(false);
        }
        recentBill.setParamId("carnivalId");
        recentBill.setParamLabel("Carnival Id");
        recentBill.setParamValue(carnivalId);
        recentBill.setAmount(String.valueOf(billAmount));
        recentBills.add(recentBill);
        dataHelper.createBills(recentBills);
    }
}
