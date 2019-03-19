package bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomPinCheckerWithInputDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationForTwoFactorAuthenticationServicesDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.BusinessRule;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.GetBusinessRuleRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.MandatoryBusinessRules;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PaymentAcceptRejectOrCancelRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PaymentAcceptRejectOrCancelResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.CommonFragments.ReviewFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleConstants;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactSearchHelper;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class PaymentRequestReceivedDetailsFragment extends ReviewFragment implements LocationListener, HttpResponseListener {

    public static MandatoryBusinessRules mMandatoryBusinessRules;

    private HttpRequestPostAsyncTask mAcceptRequestTask = null;

    private HttpRequestPostAsyncTask mCancelRequestTask = null;

    private HttpRequestPostAsyncTask mRejectRequestTask = null;

    private HttpRequestGetAsyncTask mGetBusinessRuleTask = null;

    private PaymentAcceptRejectOrCancelRequest mRequestPaymentAcceptRejectOrCancelRequest;
    private PaymentAcceptRejectOrCancelResponse mRequestPaymentAcceptRejectOrCancelResponse;

    private OTPVerificationForTwoFactorAuthenticationServicesDialog mOTPVerificationForTwoFactorAuthenticationServicesDialog;

    private CustomProgressDialog mProgressDialog;

    private int mRequestType;
    private BigDecimal mAmount;
    private String mReceiverName;
    private String mReceiverMobileNumber;
    private String mPhotoUri;
    private String mDescription;
    private long mRequestID;
    private String mTransactionID;
    private int mStatus;

    private ProfileImageView mProfileImageView;
    private TextView mNameView;
    private TextView mMobileNumberView;
    private TextView mDescriptionTagView;
    private TextView mDescriptionView;
    private TextView mAmountView;
    private TextView mServiceChargeView;
    private TextView mNetAmountView;
    private Button mRejectButton;
    private Button mAcceptButton;
    private Button mCancelButton;
    private View mNetAmountViewHolder;
    private View mServiceChargeViewHolder;

    private Context mContext;
    private AnimatedProgressDialog mCustomProgressDialog;

    private boolean switchedFromTransactionHistory = false;
    private Tracker mTracker;

    private String mPin;

    private LocationManager locationManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_request_money_sent_review));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sent_received_request_review, container, false);

        Bundle bundle = getArguments();
        mMandatoryBusinessRules = BusinessRuleCacheManager.getBusinessRules(Constants.MAKE_PAYMENT);

        bundle.putBoolean(Constants.IS_IN_CONTACTS, new ContactSearchHelper(getActivity()).searchMobileNumber(mReceiverMobileNumber));

        mRequestType = bundle.getInt(Constants.REQUEST_TYPE, Constants.REQUEST_TYPE_RECEIVED_REQUEST);
        mAmount = new BigDecimal(bundle.getString(Constants.AMOUNT));
        mReceiverMobileNumber = bundle.getString(Constants.RECEIVER_MOBILE_NUMBER);
        mDescription = bundle.getString(Constants.DESCRIPTION);
        mReceiverName = bundle.getString(Constants.NAME);
        mStatus = bundle.getInt(Constants.STATUS, Constants.HTTP_RESPONSE_STATUS_PROCESSING);
        mPhotoUri = bundle.getString(Constants.PHOTO_URI);

        mContext = getContext();
        mCustomProgressDialog = new AnimatedProgressDialog(mContext);
        switchedFromTransactionHistory = getActivity().getIntent()
                .getBooleanExtra(Constants.SWITCHED_FROM_TRANSACTION_HISTORY, false);

        if (switchedFromTransactionHistory)
            mTransactionID = getActivity().getIntent().getStringExtra(Constants.TRANSACTION_ID);
        else
            mRequestID = (long) getActivity().getIntent().getSerializableExtra(Constants.MONEY_REQUEST_ID);

        mProfileImageView = (ProfileImageView) v.findViewById(R.id.image_view_profile);
        mNameView = (TextView) v.findViewById(R.id.textview_name);
        mMobileNumberView = (TextView) v.findViewById(R.id.textview_mobile_number);
        mDescriptionTagView = (TextView) v.findViewById(R.id.description);
        mDescriptionView = (TextView) v.findViewById(R.id.textview_description);
        mAmountView = (TextView) v.findViewById(R.id.textview_amount);
        mServiceChargeView = (TextView) v.findViewById(R.id.textview_service_charge);
        mNetAmountView = (TextView) v.findViewById(R.id.textview_net_amount);
        mNetAmountViewHolder = v.findViewById(R.id.netAmountViewHolder);
        mServiceChargeViewHolder = v.findViewById(R.id.serviceChargeViewHolder);

        mAcceptButton = (Button) v.findViewById(R.id.button_accept);
        mRejectButton = (Button) v.findViewById(R.id.button_reject);
        mCancelButton = (Button) v.findViewById(R.id.button_cancel);

        mProgressDialog = new CustomProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);

        getActivity().setTitle(R.string.request_payment);
        mProfileImageView.setProfilePicture(mPhotoUri, false);

        if (mReceiverName == null || mReceiverName.isEmpty()) {
            mNameView.setVisibility(View.GONE);
        } else {
            mNameView.setText(mReceiverName);
        }

        mMobileNumberView.setText(mReceiverMobileNumber);

        if (mDescription == null || mDescription.isEmpty()) {
            mDescriptionTagView.setVisibility(View.GONE);
            mDescriptionView.setVisibility(View.GONE);
        } else
            mDescriptionView.setText(mDescription);

        if (mStatus == Constants.HTTP_RESPONSE_STATUS_PROCESSING) {
            if (mRequestType == Constants.REQUEST_TYPE_RECEIVED_REQUEST) {
                mAcceptButton.setVisibility(View.VISIBLE);
                mRejectButton.setVisibility(View.VISIBLE);
                mCancelButton.setVisibility(View.GONE);
            } else {
                mAcceptButton.setVisibility(View.GONE);
                mRejectButton.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.VISIBLE);
            }
        } else {
            mAcceptButton.setVisibility(View.GONE);
            mRejectButton.setVisibility(View.GONE);
            mCancelButton.setVisibility(View.GONE);
        }

        mAmountView.setText(Utilities.formatTaka(mAmount));

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.ACCEPT_REQUEST)
            public void onClick(View v) {
                verifyBalance();
            }
        });

        mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.REJECT_REQUEST)
            public void onClick(View v) {
                MaterialDialog.Builder rejectDialog = new MaterialDialog.Builder(getActivity());
                rejectDialog.content(R.string.confirm_request_rejection);
                rejectDialog.positiveText(R.string.yes);
                rejectDialog.negativeText(R.string.no);
                rejectDialog.onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        rejectRequestPayment();
                    }
                });
                rejectDialog.show();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @ValidateAccess(ServiceIdConstants.CANCEL_REQUEST)
            public void onClick(View v) {
                showAlertDialogue(getString(R.string.cancel_money_request_confirm), mRequestID);
            }
        });

        attemptGetBusinessRule(Constants.SERVICE_ID_MAKE_PAYMENT);

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        mCustomProgressDialog.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCustomProgressDialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utilities.LOCATION_SETTINGS_RESULT_CODE || requestCode == Utilities.LOCATION_SOURCE_SETTINGS_RESULT_CODE) {
            mAcceptButton.performClick();
        }
    }

    private void getLocationAndAttemptAcceptRequestWithPinCheck() {
        if (mMandatoryBusinessRules.IS_PIN_REQUIRED()) {
            new CustomPinCheckerWithInputDialog(getActivity(), new CustomPinCheckerWithInputDialog.PinCheckAndSetListener() {
                @Override
                public void ifPinCheckedAndAdded(String pin) {
                    getLocationAndAcceptRequestPayment(pin);
                }
            });
        } else {
            getLocationAndAcceptRequestPayment(null);
        }
    }

    private void attemptAcceptRequestWithPinCheck() {
        if (mMandatoryBusinessRules.IS_PIN_REQUIRED()) {
            new CustomPinCheckerWithInputDialog(getActivity(), new CustomPinCheckerWithInputDialog.PinCheckAndSetListener() {
                @Override
                public void ifPinCheckedAndAdded(String pin) {
                    acceptRequestPayment(pin, null);
                }
            });
        } else {
            acceptRequestPayment(null, null);
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocationAndAcceptRequestPayment(String pin) {
        this.mPin = pin;
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mProgressDialog.show();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this, Looper.getMainLooper());
        } else {
            Utilities.showGPSHighAccuracyDialog(this);
        }
    }

    private void showAlertDialogue(String msg, final long id) {
        AlertDialog.Builder alertDialogue = new AlertDialog.Builder(getActivity());
        alertDialogue.setTitle(R.string.confirm_query);
        alertDialogue.setMessage(msg);

        alertDialogue.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                cancelRequestPayment();
            }
        });

        alertDialogue.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        alertDialogue.show();
    }

    private void verifyBalance() {
        String errorMessage = null;

        if (SharedPrefManager.ifContainsUserBalance()) {
            final BigDecimal balance = new BigDecimal(SharedPrefManager.getUserBalance());
            if (mAmount.compareTo(balance) > 0) {
                errorMessage = getString(R.string.insufficient_balance);
            }
        } else {
            errorMessage = getString(R.string.balance_not_available);
        }

        if (errorMessage != null) {
            DialogUtils.showBalanceErrorInTransaction(getActivity(), errorMessage);
        } else {
            if (mMandatoryBusinessRules.IS_LOCATION_REQUIRED()) {
                if (Utilities.hasForcedLocationPermission(PaymentRequestReceivedDetailsFragment.this)) {
                    getLocationAndAttemptAcceptRequestWithPinCheck();
                }
            } else {
                attemptAcceptRequestWithPinCheck();
            }
        }
    }

    private void cancelRequestPayment() {
        if (mCancelRequestTask != null) {
            return;
        }

        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        // No PIN needed for now to place a request from me
        if (!switchedFromTransactionHistory) {
            mRequestPaymentAcceptRejectOrCancelRequest =
                    new PaymentAcceptRejectOrCancelRequest(mRequestID, null);
        } else {
            mRequestPaymentAcceptRejectOrCancelRequest =
                    new PaymentAcceptRejectOrCancelRequest(mTransactionID, null);
        }

        Gson gson = new Gson();
        String json = gson.toJson(mRequestPaymentAcceptRejectOrCancelRequest);
        mCancelRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_CANCEL_PAYMENT_REQUEST,
                Constants.BASE_URL_SM + Constants.URL_CANCEL_NOTIFICATION_REQUEST, json, getActivity(), false);
        mCancelRequestTask.mHttpResponseListener = this;
        mCancelRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void rejectRequestPayment() {
        if (mRejectRequestTask != null) {
            return;
        }
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        if (!switchedFromTransactionHistory) {
            mRequestPaymentAcceptRejectOrCancelRequest =
                    new PaymentAcceptRejectOrCancelRequest(mRequestID);
        } else mRequestPaymentAcceptRejectOrCancelRequest =
                new PaymentAcceptRejectOrCancelRequest(mTransactionID);

        Gson gson = new Gson();
        String json = gson.toJson(mRequestPaymentAcceptRejectOrCancelRequest);
        mRejectRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_REJECT_PAYMENT_REQUEST,
                Constants.BASE_URL_SM + Constants.URL_REJECT_NOTIFICATION_REQUEST, json, getActivity(), false);
        mRejectRequestTask.mHttpResponseListener = this;
        mRejectRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void launchOTPVerification() {
        String jsonString = new Gson().toJson(mRequestPaymentAcceptRejectOrCancelRequest);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog = new OTPVerificationForTwoFactorAuthenticationServicesDialog(getActivity(), jsonString, Constants.COMMAND_ACCEPT_PAYMENT_REQUEST,
                Constants.BASE_URL_SM + Constants.URL_ACCEPT_NOTIFICATION_REQUEST, Constants.METHOD_POST);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog.mParentHttpResponseListener = this;
    }

    private void acceptRequestPayment(final String pin, final Location location) {
        if (mAcceptRequestTask != null) {
            return;
        }
        mCustomProgressDialog.showDialog();

        if (!switchedFromTransactionHistory) {
            mRequestPaymentAcceptRejectOrCancelRequest =
                    new PaymentAcceptRejectOrCancelRequest(mRequestID, pin);
        } else {
            mRequestPaymentAcceptRejectOrCancelRequest =
                    new PaymentAcceptRejectOrCancelRequest(mTransactionID, pin);
        }

        if (location != null) {
            mRequestPaymentAcceptRejectOrCancelRequest.setLatitude(location.getLatitude());
            mRequestPaymentAcceptRejectOrCancelRequest.setLongitude(location.getLongitude());
        }
        Gson gson = new Gson();
        String json = gson.toJson(mRequestPaymentAcceptRejectOrCancelRequest);
        mAcceptRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ACCEPT_PAYMENT_REQUEST,
                Constants.BASE_URL_SM + Constants.URL_ACCEPT_NOTIFICATION_REQUEST, json, getActivity(), false);
        mAcceptRequestTask.mHttpResponseListener = this;
        mAcceptRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onLocationChanged(Location location) {
        acceptRequestPayment(mPin, location);
        if (locationManager != null)
            locationManager.removeUpdates(this);
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public int getServiceID() {
        return Constants.SERVICE_ID_REQUEST_PAYMENT;
    }

    @Override
    public BigDecimal getAmount() {
        return mAmount;
    }

    @Override
    public void onServiceChargeLoadFinished(BigDecimal serviceCharge) {
        // User who're accepting the request should not see the service charge. By force action. Deal with it :)
        if (mRequestType == Constants.REQUEST_TYPE_RECEIVED_REQUEST) {
            mServiceChargeView.setText(Utilities.formatTaka(new BigDecimal(0.0)));
            mNetAmountView.setText(Utilities.formatTaka(mAmount.subtract(new BigDecimal(0.0))));
        } else {
            mServiceChargeViewHolder.setVisibility(View.VISIBLE);
            mNetAmountViewHolder.setVisibility(View.VISIBLE);
            mServiceChargeView.setText(Utilities.formatTaka(serviceCharge));
            mNetAmountView.setText(Utilities.formatTaka(mAmount.subtract(serviceCharge)));
        }
    }

    private void attemptGetBusinessRule(int serviceID) {

        if (mGetBusinessRuleTask != null) {
            return;
        }

        String mUri = new GetBusinessRuleRequestBuilder(serviceID).getGeneratedUri();
        mGetBusinessRuleTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BUSINESS_RULE,
                mUri, getActivity(), this, false);

        mGetBusinessRuleTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        mProgressDialog.dismiss();
        super.httpResponseReceiver(result);

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            return;
        }
        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_GET_BUSINESS_RULE:

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                    try {
                        BusinessRule[] businessRuleArray = gson.fromJson(result.getJsonString(), BusinessRule[].class);

                        if (businessRuleArray != null) {
                            for (BusinessRule rule : businessRuleArray) {
                                if (rule.getRuleID().equals(BusinessRuleConstants.SERVICE_RULE_REQUEST_PAYMENT_LOCATION_REQUIRED)) {
                                    mMandatoryBusinessRules.setLOCATION_REQUIRED(rule.getRuleValue());
                                }
                                if (rule.getRuleID().equals(BusinessRuleConstants.SERVICE_RULE_REQUEST_PAYMENT_PIN_REQUIRED)) {
                                    mMandatoryBusinessRules.setPIN_REQUIRED(rule.getRuleValue());
                                }
                            }
                        }
                        attemptGetServiceCharge();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mProgressDialog.dismiss();
                    mGetBusinessRuleTask = null;
                }
                mGetBusinessRuleTask = null;
                break;
            case Constants.COMMAND_CANCEL_PAYMENT_REQUEST:

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    try {
                        mRequestPaymentAcceptRejectOrCancelResponse = gson.fromJson(result.getJsonString(),
                                PaymentAcceptRejectOrCancelResponse.class);
                        String message = mRequestPaymentAcceptRejectOrCancelResponse.getMessage();
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), message, Toast.LENGTH_LONG);

                        if (switchedFromTransactionHistory) {
                            Utilities.finishLauncherActivity(getActivity());
                        } else
                            getActivity().finish();
                        // ((RequestPaymentActivity) getActivity()).switchToSentPaymentRequestsFragment();

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), R.string.could_not_cancel_money_request, Toast.LENGTH_LONG);
                    }

                } else {
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), R.string.could_not_cancel_money_request, Toast.LENGTH_LONG);
                }

                mProgressDialog.dismiss();
                mCancelRequestTask = null;
                break;
            case Constants.COMMAND_ACCEPT_PAYMENT_REQUEST:

                try {
                    mRequestPaymentAcceptRejectOrCancelResponse = gson.fromJson(result.getJsonString(),
                            PaymentAcceptRejectOrCancelResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                        } else {
                            mCustomProgressDialog.showSuccessAnimationAndMessage
                                    (mRequestPaymentAcceptRejectOrCancelResponse.getMessage());
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mCustomProgressDialog.dismissDialog();
                                getActivity().onBackPressed();
                            }
                        }, 2000);
                        //getActivity().finish();
                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED ||
                            result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED) {
                        mCustomProgressDialog.dismissDialog();
                        Toaster.makeText(getActivity(), mRequestPaymentAcceptRejectOrCancelResponse.getMessage(), Toast.LENGTH_LONG);
                        SecuritySettingsActivity.otpDuration = mRequestPaymentAcceptRejectOrCancelResponse.getOtpValidFor();
                        launchOTPVerification();
                    } else {
                        if (getActivity() != null) {
                            if (mOTPVerificationForTwoFactorAuthenticationServicesDialog == null) {
                                mCustomProgressDialog.showFailureAnimationAndMessage
                                        (mRequestPaymentAcceptRejectOrCancelResponse.getMessage());
                            } else {
                                if (mCustomProgressDialog != null) {
                                    mCustomProgressDialog.showFailureAnimationAndMessage(mRequestPaymentAcceptRejectOrCancelResponse.getMessage());
                                } else {
                                    Toast.makeText(mContext, mRequestPaymentAcceptRejectOrCancelResponse.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }

                            if (mRequestPaymentAcceptRejectOrCancelResponse.getMessage().toLowerCase().contains(TwoFactorAuthConstants.WRONG_OTP)) {
                                if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                                    mOTPVerificationForTwoFactorAuthenticationServicesDialog.showOtpDialog();
                                    mCustomProgressDialog.dismissDialog();
                                }
                            } else {
                                if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                                    mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                        mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                    }
                    e.printStackTrace();
                    mCustomProgressDialog.showFailureAnimationAndMessage(getString(R.string.service_not_available));
                }
                mProgressDialog.dismiss();
                mAcceptRequestTask = null;

                break;
            case Constants.COMMAND_REJECT_PAYMENT_REQUEST:

                try {
                    mRequestPaymentAcceptRejectOrCancelResponse = gson.fromJson(result.getJsonString(),
                            PaymentAcceptRejectOrCancelResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        String message = mRequestPaymentAcceptRejectOrCancelResponse.getMessage();
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

                            if (switchedFromTransactionHistory)
                                Utilities.finishLauncherActivity(getActivity());
                            else
                                getActivity().onBackPressed();
                        }

                    } else {
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), mRequestPaymentAcceptRejectOrCancelResponse.getMessage(), Toast.LENGTH_LONG);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), R.string.could_not_reject_money_request, Toast.LENGTH_LONG);
                }

                mProgressDialog.dismiss();
                mRejectRequestTask = null;

                break;
        }
    }
}


