package bd.com.ipay.ipayskeleton.PaymentFragments.SchedulePayment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.DpdcBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GenericBillPayResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPayAbstractTransactionConfirmationFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class SchedulePaymentConfirmationFragment extends IPayAbstractTransactionConfirmationFragment {

    protected static final String BILL_AMOUNT_KEY = "BILL_AMOUNT";
    private final Gson gson = new Gson();
    private DpdcBillPayRequest mDpdcBillPayRequest;

    private HttpRequestPostAsyncTask schedulePaymentPayTask = null;

    private Number totalAmount;
    private int transactionId;
    private String uri;
    private String name;
    private String imageUrl;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transactionId = getArguments().getInt(Constants.INSTALLMENT_ID, 0);
            totalAmount = (Number) getArguments().getSerializable(BILL_AMOUNT_KEY);
            imageUrl = getArguments().getString(Constants.IMAGE_URL);
            name = getArguments().getString(Constants.NAME);
        }
    }

    @Override
    protected void setupViewProperties() {
        setName(name);

        setTransactionImage(Constants.BASE_URL_FTP_SERVER+imageUrl);
        setTransactionDescription(getStyledTransactionDescription(R.string.pay_bill_confirmation_message, totalAmount));
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
        if (schedulePaymentPayTask == null) {

            uri = Constants.BASE_URL_SCHEDULED_PAYMENT + Constants.URL_GET_SCHEDULED_PAYMENT_DETAILS+"pay/"+transactionId;
            schedulePaymentPayTask = new HttpRequestPostAsyncTask(Constants.COMMAND_DPDC_BILL_PAY,
                    uri, null, getActivity(), this, false);
            schedulePaymentPayTask.setPinAsHeader(getPin());
            schedulePaymentPayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            customProgressDialog.showDialog();
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (getActivity() == null)
            return;

        if (HttpErrorHandler.isErrorFound(result, getContext(), customProgressDialog)) {
            customProgressDialog.dismissDialog();
            schedulePaymentPayTask = null;
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
                                        bundle.putInt(Constants.TRANSACTION_ID, transactionId);
                                        bundle.putString(Constants.IMAGE_URL, imageUrl);
                                        bundle.putString(Constants.NAME, name);
                                        bundle.putSerializable(Constants.TOTAL_AMOUNT, totalAmount);
                                        ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new SchedulePaymentSuccessFragment(), bundle, 4, true);


                                    }
                                }, 2000);
                                if (getActivity() != null)
                                    Utilities.hideKeyboard(getActivity());
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
            schedulePaymentPayTask = null;
        }
    }
}
