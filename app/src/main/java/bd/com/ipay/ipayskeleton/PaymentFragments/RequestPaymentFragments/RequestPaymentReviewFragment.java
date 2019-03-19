package bd.com.ipay.ipayskeleton.PaymentFragments.RequestPaymentFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.RequestPaymentActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationForTwoFactorAuthenticationServicesDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PaymentRequestSentResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.SendNewPaymentRequest;
import bd.com.ipay.ipayskeleton.PaymentFragments.CommonFragments.ReviewFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class RequestPaymentReviewFragment extends ReviewFragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mSendPaymentRequestTask = null;

    private SendNewPaymentRequest mSendNewPaymentRequest;

    private CustomProgressDialog mProgressDialog;
    private OTPVerificationForTwoFactorAuthenticationServicesDialog mOTPVerificationForTwoFactorAuthenticationServicesDialog;

    private String mReceiverMobileNumber;
    private String mDescription;
    private BigDecimal mAmount;

    private String mReceiverName;
    private String mPhotoUri;

    private TextView serviceChargeTextView;
    private TextView netAmountTextView;
    private View mNetAmountViewHolder;
    private View mServiceChargeViewHolder;

    private Context mContext;
    private AnimatedProgressDialog mCustomProgressDialog;

    private Tracker mTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReceiverMobileNumber = getActivity().getIntent().getStringExtra(Constants.RECEIVER_MOBILE_NUMBER);
        mDescription = getActivity().getIntent().getStringExtra(Constants.DESCRIPTION_TAG);
        mAmount = new BigDecimal(getActivity().getIntent().getStringExtra(Constants.AMOUNT));
        mReceiverName = getArguments().getString(Constants.NAME);
        mPhotoUri = getArguments().getString(Constants.PHOTO_URI);
        mProgressDialog = new CustomProgressDialog(getActivity());

        mContext = getContext();
        mCustomProgressDialog = new AnimatedProgressDialog(mContext);

        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_request_payment_review));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_request_payment_review, container, false);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ProfileImageView receiverProfileImageView = findViewById(R.id.receiver_profile_image_view);
        final TextView receiverNameTextView = findViewById(R.id.receiver_name_text_view);
        final TextView receiverMobileNumberTextView = findViewById(R.id.receiver_mobile_number_text_view);
        final TextView amountTextView = findViewById(R.id.amount_text_view);
        final View descriptionViewHolder = findViewById(R.id.description_view_holder);
        final TextView descriptionTextView = findViewById(R.id.description_text_view);
        final Button paymentRequestButton = findViewById(R.id.request_payment_button);

        serviceChargeTextView = findViewById(R.id.service_charge_text_view);
        netAmountTextView = findViewById(R.id.net_amount_text_view);
        mNetAmountViewHolder = findViewById(R.id.netAmountViewHolder);
        mServiceChargeViewHolder = findViewById(R.id.serviceChargeViewHolder);

        receiverProfileImageView.setProfilePicture(mPhotoUri, false);

        if (mReceiverName == null || mReceiverName.isEmpty()) {
            receiverNameTextView.setVisibility(View.GONE);
        } else {
            receiverNameTextView.setVisibility(View.VISIBLE);
            receiverNameTextView.setText(mReceiverName);
        }
        receiverMobileNumberTextView.setText(mReceiverMobileNumber);

        amountTextView.setText(Utilities.formatTaka(mAmount));
        serviceChargeTextView.setText(Utilities.formatTaka(new BigDecimal(0.0)));
        netAmountTextView.setText(Utilities.formatTaka(mAmount.subtract(new BigDecimal(0.0))));


        if (TextUtils.isEmpty(mDescription)) {
            descriptionViewHolder.setVisibility(View.GONE);
        } else {
            descriptionViewHolder.setVisibility(View.VISIBLE);
            descriptionTextView.setText(mDescription);
        }

        paymentRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utilities.isValueAvailable(RequestPaymentActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT())
                        && Utilities.isValueAvailable(RequestPaymentActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT())) {
                    final String errorMessage = InputValidator.isValidAmount(getActivity(), mAmount,
                            RequestPaymentActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT(),
                            RequestPaymentActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT());

                    if (errorMessage == null) {
                        attemptSendPaymentRequest();

                    } else {
                        showErrorDialog(errorMessage.replace(getString(R.string.payment_amount), getString(R.string.payment_total_amount)));
                    }
                }
            }
        });

        attemptGetServiceCharge();
    }

    public <T extends View> T findViewById(@IdRes int id) {
        //noinspection unchecked,ConstantConditions
        return (T) getView().findViewById(id);
    }

    private void showErrorDialog(final String errorMessage) {

        new AlertDialog.Builder(getContext())
                .setMessage(errorMessage)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void attemptSendPaymentRequest() {
        if (mSendPaymentRequestTask != null) {
            return;
        }
        mCustomProgressDialog.showDialog();
        mSendNewPaymentRequest = new SendNewPaymentRequest(mAmount, mReceiverMobileNumber, mDescription, null, null);
        Gson gson = new Gson();
        String json = gson.toJson(mSendNewPaymentRequest);
        mSendPaymentRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SEND_PAYMENT_REQUEST,
                Constants.BASE_URL_SM + Constants.URL_SEND_PAYMENT_REQUEST, json, getActivity(), true);
        mSendPaymentRequestTask.mHttpResponseListener = this;
        mSendPaymentRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void launchOTPVerification() {
        String jsonString = new Gson().toJson(mSendNewPaymentRequest);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog = new OTPVerificationForTwoFactorAuthenticationServicesDialog(getActivity(), jsonString, Constants.COMMAND_SEND_PAYMENT_REQUEST,
                Constants.BASE_URL_SM + Constants.URL_SEND_PAYMENT_REQUEST, Constants.METHOD_POST);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog.mParentHttpResponseListener = this;
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
        mServiceChargeViewHolder.setVisibility(View.VISIBLE);
        mNetAmountViewHolder.setVisibility(View.VISIBLE);
        serviceChargeTextView.setText(Utilities.formatTaka(serviceCharge));
        netAmountTextView.setText(Utilities.formatTaka(mAmount.subtract(serviceCharge)));
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        super.httpResponseReceiver(result);

        if (HttpErrorHandler.isErrorFound(result, getContext(), mCustomProgressDialog)) {
            mProgressDialog.dismiss();
            mSendPaymentRequestTask = null;

            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_SEND_PAYMENT_REQUEST)) {

            try {
                PaymentRequestSentResponse mPaymentRequestSentResponse = gson.fromJson(result.getJsonString(), PaymentRequestSentResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                        mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                    } else {
                        mCustomProgressDialog.showSuccessAnimationAndMessage(mPaymentRequestSentResponse.getMessage());
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mCustomProgressDialog.dismissDialog();
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                        }
                    }, 2000);

                    //getActivity().finish();
                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED ||
                        result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED) {
                    mCustomProgressDialog.dismissDialog();
                    Toast.makeText(getActivity(), mPaymentRequestSentResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    SecuritySettingsActivity.otpDuration = mPaymentRequestSentResponse.getOtpValidFor();
                    launchOTPVerification();
                } else {
                    if (getActivity() != null) {
                        if (mOTPVerificationForTwoFactorAuthenticationServicesDialog == null) {
                            mCustomProgressDialog.showFailureAnimationAndMessage(mPaymentRequestSentResponse.getMessage());
                        } else {
                            Toast.makeText(mContext, mPaymentRequestSentResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        if (mPaymentRequestSentResponse.getMessage().toLowerCase().contains(TwoFactorAuthConstants.WRONG_OTP)) {
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
                e.printStackTrace();
                mCustomProgressDialog.showFailureAnimationAndMessage(getString(R.string.service_not_available));
            }
            mSendPaymentRequestTask = null;
        }

        mProgressDialog.dismiss();
    }
}
