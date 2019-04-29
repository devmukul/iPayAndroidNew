package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.DPDC;

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

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.MetaData;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.Notification;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.RecentBill;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.DescoBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.DpdcBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GenericBillPayResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.LankaBanglaCardBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.NotificationForOther;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

import static bd.com.ipay.ipayskeleton.Utilities.Constants.TOTAL_AMOUNT;

public class DPDCBillConfirmationFragment extends IPayAbstractTransactionConfirmationFragment {

    protected static final String BILL_AMOUNT_KEY = "BILL_AMOUNT";
    private final Gson gson = new Gson();
    private DpdcBillPayRequest mDpdcBillPayRequest;

    private HttpRequestPostAsyncTask descoBillPayTask = null;

    private Number totalAmount;
    private String descoAccountId;
    private String billNumber;

    private String billMonth;
    private String billYear;
    private String locationCode;

    private String uri;

    private String otherPersonName;
    private String otherPersonMobile;
    DataHelper dataHelper ;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataHelper = DataHelper.getInstance(getContext());
        if (getArguments() != null) {
            descoAccountId = getArguments().getString(Constants.ACCOUNT_ID, "");
            totalAmount = (Number) getArguments().getSerializable(TOTAL_AMOUNT);
            billNumber = getArguments().getString(Constants.BILL_NUMBER, "");
            billMonth = getArguments().getString(Constants.BILL_MONTH);
            billYear = getArguments().getString(Constants.BILL_YEAR);
            locationCode = getArguments().getString(Constants.LOCATION_CODE);
            otherPersonName = getArguments().getString(UtilityBillPaymentActivity.OTHER_PERSON_NAME_KEY, "");
            otherPersonMobile = getArguments().getString(UtilityBillPaymentActivity.OTHER_PERSON_MOBILE_KEY, "");
        }
    }

    @Override
    protected void setupViewProperties() {
        setTransactionImageResource(R.drawable.dpdc);
        setTransactionDescription(getStyledTransactionDescription(R.string.pay_bill_confirmation_message, totalAmount));
        setName(getString(R.string.account_number)+": "+descoAccountId);
        setUserName(getString(R.string.bill_number)+": "+billNumber);
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
        return Constants.DESCO_BILL_PAY;
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
        if (descoBillPayTask == null) {

            if(TextUtils.isEmpty(otherPersonName) && TextUtils.isEmpty(otherPersonMobile)){
                mDpdcBillPayRequest = new DpdcBillPayRequest(descoAccountId, billMonth, billYear, null ,getPin(), locationCode);

            }else{
                bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.MetaData metaData = new bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.MetaData(new NotificationForOther(otherPersonName, otherPersonMobile));
                mDpdcBillPayRequest = new DpdcBillPayRequest(descoAccountId, billMonth, billYear, null ,getPin(), locationCode, metaData);
            }
            String json = gson.toJson(mDpdcBillPayRequest);
            uri = Constants.BASE_URL_UTILITY + Constants.URL_DPDC_BILL_PAY;
            descoBillPayTask = new HttpRequestPostAsyncTask(Constants.COMMAND_DPDC_BILL_PAY,
                    uri, json, getActivity(), this, false);
            descoBillPayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            customProgressDialog.showDialog();
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (getActivity() == null)
            return;

        if (HttpErrorHandler.isErrorFound(result, getContext(), customProgressDialog)) {
            customProgressDialog.dismissDialog();
            descoBillPayTask = null;
        } else {
            try {
                switch (result.getApiCommand()) {
                    case Constants.COMMAND_DPDC_BILL_PAY:
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
                                sendSuccessEventTracking(totalAmount);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        customProgressDialog.dismissDialog();
                                        Bundle bundle = new Bundle();
                                        bundle.putString(Constants.ACCOUNT_ID, descoAccountId);
                                        bundle.putString(Constants.BILL_NUMBER, billNumber);
                                        bundle.putSerializable(Constants.TOTAL_AMOUNT, totalAmount);
                                        bundle.putString(UtilityBillPaymentActivity.OTHER_PERSON_NAME_KEY, otherPersonName);
                                        bundle.putString(UtilityBillPaymentActivity.OTHER_PERSON_MOBILE_KEY, otherPersonMobile);
                                        bundle.putString(Constants.LOCATION_CODE, locationCode);
                                        if (getActivity() instanceof UtilityBillPaymentActivity) {
                                            ((UtilityBillPaymentActivity) getActivity()).switchToDPDCBillSuccessFragment(bundle);
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
                                    sendBlockedEventTracking(totalAmount);
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
                                launchOTPVerification(mGenericBillPayResponse.getOtpValidFor(), gson.toJson(mDpdcBillPayRequest), Constants.COMMAND_LINK_THREE_BILL_PAY, uri);
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
                                    sendFailedEventTracking(mGenericBillPayResponse.getMessage(), totalAmount);
                                    break;
                                }
                                break;
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                customProgressDialog.showFailureAnimationAndMessage(getString(R.string.payment_failed));
                sendFailedEventTracking(e.getMessage(), totalAmount);
            }
            descoBillPayTask = null;
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
        recentBill.setProviderCode("DPDC");
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
        recentBill.setParamLabel(getString(R.string.customer_number));
        recentBill.setParamValue(descoAccountId);
        recentBill.setAmount(String.valueOf(totalAmount));
        recentBill.setLocationCode(String.valueOf(totalAmount));
        recentBills.add(recentBill);
        dataHelper.createBills(recentBills);
    }

}
