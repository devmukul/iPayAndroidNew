package bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.PaymentActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestDeleteAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomPinCheckerWithInputDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationForTwoFactorAuthenticationServicesDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.BusinessRule;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.GetBusinessRuleRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.CancelOrderRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.CancelOrderResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.GetOrderDetails;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.GetOrderDetailsRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PayOrderRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PaymentRequestByDeepLink;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleConstants;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;


public class MakePaymentByDeepLinkFragment extends Fragment implements LocationListener, HttpResponseListener {

    private HttpRequestGetAsyncTask mGetBusinessRuleTask;

    private HttpRequestDeleteAsyncTask mCancelOrderTask;
    private CancelOrderResponse mCancelOrderResponse;

    private HttpRequestGetAsyncTask mGetOrderDetailsTask;
    private GetOrderDetails mGetOrderDetails;

    private HttpRequestPostAsyncTask mPaymentTask = null;
    private PaymentRequestByDeepLink mPaymentRequestByDeepLink;

    private ProgressDialog mProgressDialog;
    private OTPVerificationForTwoFactorAuthenticationServicesDialog
            mOTPVerificationForTwoFactorAuthenticationServicesDialog;

    private TextView mBusinessNameTextView;
    private TextView mAmountTextView;
    private TextView mDescriptionTextView;
    private ProfileImageView mBusinessLogoImageView;
    private Button mConfirmButton;
    private Button mCancelButton;
    private LinearLayout mReferenceLayout;
    private View divider;
    private String thirdPartyAppUrl;

    private String orderID;
    private String otp;

    private LocationManager locationManager;
    private Location userLocation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pay_by_deep_link, container, false);
        mProgressDialog = new ProgressDialog(getActivity());
        setUpViews(view);
        return view;
    }

    private void setUpViews(View view) {
        mBusinessNameTextView = (TextView) view.findViewById(R.id.business_name_text_view);
        mAmountTextView = (TextView) view.findViewById(R.id.amount_text_view);
        mDescriptionTextView = (TextView) view.findViewById(R.id.description_text_view);
        mBusinessLogoImageView = (ProfileImageView) view.findViewById(R.id.business_profile_image_view);
        mConfirmButton = (Button) view.findViewById(R.id.make_payment_button);
        mCancelButton = (Button) view.findViewById(R.id.cancel_button);
        getOrderID();
        setButtonActions();
        //attemptGetBusinessRules(ServiceIdConstants.MAKE_PAYMENT);
    }

    private void getOrderID() {
        orderID = getActivity().getIntent().getStringExtra("DEEP_LINK_ACTION_VALUE");

//        if (orderID == null) {
//            orderID = "FBETC33-6C3E581E88D46A";
//        }
        getOrderDetails(orderID);

    }

    private void fillNecessaryfiledsWithData(GetOrderDetails getOrderDetails) {
        mBusinessNameTextView.setText(getOrderDetails.getMerchantName());
        mBusinessLogoImageView.setBusinessProfilePicture(
                Constants.BASE_URL_FTP_SERVER + getOrderDetails.getMerchantLogoUrl(), false);
        mAmountTextView.setText(Double.toString(getOrderDetails.getAmount()));
        mDescriptionTextView.setText(getOrderDetails.getDescription());
    }

    private void attemptGetBusinessRules(int serviceID) {
        if (mGetBusinessRuleTask != null)
            return;

        mProgressDialog.setMessage(getString(R.string.progress_dialog_fetching));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.show();
        String mUri = new GetBusinessRuleRequestBuilder(serviceID).getGeneratedUri();
        mGetBusinessRuleTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BUSINESS_RULE,
                mUri, getActivity(), this);

        mGetBusinessRuleTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setButtonActions() {
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    // For now, we are making payment without location (by deep link)
                    attemptMakePayment(null);
                } else if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.do_you_want_to_cancel_payment))
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                attemptCancelPayment();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void attemptCancelPayment() {
        if (mCancelOrderTask != null) {
            return;
        } else {
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.show();
            mCancelOrderTask = new HttpRequestDeleteAsyncTask(Constants.COMMAND_CANCEL_ORDER,
                    new CancelOrderRequestBuilder(orderID).getGeneratedUri(), getActivity());
            mCancelOrderTask.mHttpResponseListener = this;
            mCancelOrderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void attemptMakePayment(@Nullable Location location) {
        attemptPaymentWithPinCheck();
    }

    private void attemptPaymentWithPinCheck() {
        new CustomPinCheckerWithInputDialog(getActivity(), new CustomPinCheckerWithInputDialog.PinCheckAndSetListener() {
            @Override
            public void ifPinCheckedAndAdded(String pin) {
                attemptPayment(pin);
            }
        });

    }

    private void attemptPayment(String pin) {
        if (mPaymentTask != null) {
            return;
        }

        mProgressDialog.setMessage(getString(R.string.progress_dialog_text_payment));
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mPaymentRequestByDeepLink = new PaymentRequestByDeepLink(pin);

        String mUri = new PayOrderRequestBuilder(orderID).getGeneratedUri();
        mPaymentTask = new HttpRequestPostAsyncTask(Constants.COMMAND_PAYMENT_BY_DEEP_LINK,
                mUri, new Gson().toJson(mPaymentRequestByDeepLink), getActivity());
        mPaymentTask.mHttpResponseListener = this;
        mPaymentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void launchOtpVerification() {
        String jsonString = new Gson().toJson(mPaymentRequestByDeepLink);
        String mUri = new GetOrderDetailsRequestBuilder(orderID).getGeneratedUri();
        mOTPVerificationForTwoFactorAuthenticationServicesDialog = new OTPVerificationForTwoFactorAuthenticationServicesDialog(getActivity(),
                jsonString, Constants.COMMAND_PAYMENT,
                mUri, Constants.METHOD_POST);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog.mParentHttpResponseListener = this;
    }

    @SuppressLint("MissingPermission")
    private void getLocationAndAttemptMakePayment() {
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.show();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this, Looper.getMainLooper());
        } else {
            Utilities.showGPSHighAccuracyDialog(this);
        }
    }

    private void verifyOrderDetails(GetOrderDetails getOrderDetails) {
        if (getOrderDetails != null) {
            fillNecessaryfiledsWithData(getOrderDetails);
        }

        /*String errorMessage = null;
        if (!Utilities.isValueAvailable(PaymentActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT())
                || !Utilities.isValueAvailable(PaymentActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT())) {
            DialogUtils.showDialogForBusinessRuleNotAvailable(getActivity());

        } else if (PaymentActivity.mMandatoryBusinessRules.isVERIFICATION_REQUIRED() && !ProfileInfoCacheManager.isAccountVerified()) {
            DialogUtils.showDialogVerificationRequired(getActivity());
        }
        if (SharedPrefManager.ifContainsUserBalance()) {
            final BigDecimal balance = new BigDecimal(SharedPrefManager.getUserBalance());

            //validation check of amount
            if (getOrderDetails.getAmount() == 0.0) {
                errorMessage = "Invalid Amount";
            } else {
                final BigDecimal paymentAmount = new BigDecimal(getOrderDetails.getAmount());
                if (paymentAmount.compareTo(balance) > 0) {
                    errorMessage = getString(R.string.insufficient_balance);
                } else {
                    final BigDecimal minimumPaymentAmount = PaymentActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT();
                    final BigDecimal maximumPaymentAmount = PaymentActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT().min(balance);

                    errorMessage = InputValidator.isValidAmount(getActivity(), paymentAmount, minimumPaymentAmount, maximumPaymentAmount);
                }
            }
        } else {
            errorMessage = getString(R.string.balance_not_available);
        }
        if (ProfileInfoCacheManager.getMobileNumber().equals(getOrderDetails.getMerchantMobileNumber())) {
            errorMessage = "You can't pay to yourself";
        }
        if (errorMessage != null) {
            DialogUtils.showNecessaryDialog(getActivity(), errorMessage);
        } else {
            fillNecessaryfiledsWithData(getOrderDetails);
        }*/
    }

    private void launchParentThirdPartyApp() {
        if (thirdPartyAppUrl == null) {
            thirdPartyAppUrl = "com.priyo.go";
        }
        Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.priyo.go");
        startActivity(intent);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void getOrderDetails(String orderID) {
        if (mGetOrderDetailsTask != null) {
            return;
        } else {
            mProgressDialog.setMessage(getString(R.string.progress_dialog_fetching));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.show();
            String mUri = new GetOrderDetailsRequestBuilder(orderID).getGeneratedUri();
            mGetOrderDetailsTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_ORDER_DETAILS,
                    mUri, getActivity(), this);
            mGetOrderDetailsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        mProgressDialog.dismiss();

        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mGetOrderDetailsTask = null;
            mPaymentTask = null;
            if (getActivity() != null) {
                DialogUtils.showNecessaryDialog(getActivity(), getString(R.string.service_not_available));
            }
        } else if (result.getApiCommand().equals(Constants.COMMAND_GET_BUSINESS_RULE)) {
            mProgressDialog.dismiss();
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                try {
                    Gson gson = new Gson();

                    BusinessRule[] businessRuleArray = gson.fromJson(result.getJsonString(), BusinessRule[].class);

                    if (businessRuleArray != null) {
                        for (BusinessRule rule : businessRuleArray) {
                            if (rule.getRuleID().equals(BusinessRuleConstants.SERVICE_RULE_MAKE_PAYMENT_MAX_AMOUNT_PER_PAYMENT)) {
                                PaymentActivity.mMandatoryBusinessRules.setMAX_AMOUNT_PER_PAYMENT(rule.getRuleValue());
                            } else if (rule.getRuleID().equals(BusinessRuleConstants.SERVICE_RULE_MAKE_PAYMENT_MIN_AMOUNT_PER_PAYMENT)) {
                                PaymentActivity.mMandatoryBusinessRules.setMIN_AMOUNT_PER_PAYMENT(rule.getRuleValue());
                            } else if (rule.getRuleID().equals(BusinessRuleConstants.SERVICE_RULE_MAKE_PAYMENT_VERIFICATION_REQUIRED)) {
                                PaymentActivity.mMandatoryBusinessRules.setVERIFICATION_REQUIRED(rule.getRuleValue());
                            } else if (rule.getRuleID().equals(BusinessRuleConstants.SERVICE_RULE_MAKE_PAYMENT_PIN_REQUIRED)) {
                                PaymentActivity.mMandatoryBusinessRules.setPIN_REQUIRED(rule.getRuleValue());
                            } else if (rule.getRuleID().equals(BusinessRuleConstants.SERVICE_RULE_MAKE_PAYMENT_LOCATION_REQUIRED)) {
                                PaymentActivity.mMandatoryBusinessRules.setLOCATION_REQUIRED(rule.getRuleValue());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        DialogUtils.showDialogForBusinessRuleNotAvailable(getActivity());
                }
            } else {
                if (getActivity() != null)
                    DialogUtils.showDialogForBusinessRuleNotAvailable(getActivity());
            }

            mGetBusinessRuleTask = null;
        } else if (result.getApiCommand().equals(Constants.COMMAND_GET_ORDER_DETAILS)) {
            mGetOrderDetails = new Gson().fromJson(result.getJsonString(), GetOrderDetails.class);
            try {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    verifyOrderDetails(mGetOrderDetails);
                } else {
                    DialogUtils.showNecessaryDialog(getActivity(), mGetOrderDetails.getMessage());
                }
            } catch (Exception e) {
                DialogUtils.showNecessaryDialog(getActivity(), mGetOrderDetails.getMessage());
            }
            mGetOrderDetailsTask = null;
        } else if (result.getApiCommand().equals(Constants.COMMAND_PAYMENT_BY_DEEP_LINK)) {
            try {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    Toast.makeText(getActivity(), getString(R.string.payment_success), Toast.LENGTH_LONG).show();
                    launchParentThirdPartyApp();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.payment_failed), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), getString(R.string.payment_failed), Toast.LENGTH_LONG).show();
            }
            mPaymentTask = null;
        } else if (result.getApiCommand().equals(Constants.COMMAND_CANCEL_ORDER)) {
            mCancelOrderResponse = new Gson().fromJson(result.getJsonString(),
                    CancelOrderResponse.class);
            try {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    Toast.makeText(getActivity(), mCancelOrderResponse.getMessage(), Toast.LENGTH_LONG).show();
                    launchParentThirdPartyApp();
                } else {
                    Toast.makeText(getActivity(), mCancelOrderResponse.getMessage(), Toast.LENGTH_LONG).show();
                    launchParentThirdPartyApp();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), mCancelOrderResponse.getMessage(), Toast.LENGTH_LONG).show();
                launchParentThirdPartyApp();

            }
            mCancelOrderTask = null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        userLocation = location;
        attemptPaymentWithPinCheck();
        if (locationManager != null)
            locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
