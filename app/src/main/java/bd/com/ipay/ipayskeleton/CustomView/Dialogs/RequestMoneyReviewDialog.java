package bd.com.ipay.ipayskeleton.CustomView.Dialogs;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PaymentAcceptRejectOrCancelRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PaymentAcceptRejectOrCancelResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RequestMoney.RequestMoneyAcceptRejectOrCancelRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RequestMoney.RequestMoneyAcceptRejectOrCancelResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class RequestMoneyReviewDialog extends MaterialDialog.Builder implements HttpResponseListener {

    private HttpRequestPostAsyncTask mAcceptRequestTask = null;
    private RequestMoneyAcceptRejectOrCancelResponse mRequestMoneyAcceptRejectOrCancelResponse;

    private HttpRequestPostAsyncTask mAcceptPaymentTask = null;
    private PaymentAcceptRejectOrCancelResponse mPaymentAcceptRejectOrCancelResponse;

    private final BigDecimal mAmount;
    private final BigDecimal mServiceCharge;
    private final String mReceiverName;
    private final String mReceiverMobileNumber;
    private final String mPhotoUri;
    private final long requestId;
    private final String mTitle;
    private final String mDescription;
    private final int mServiceID;

    private CustomProgressDialog mProgressDialog;
    private final ReviewDialogFinishListener mReviewFinishListener;

    private ProfileImageView mProfileImageView;
    private TextView mNameView;
    private TextView mMobileNumberView;
    private TextView mTitleView;
    private TextView mAmountView;
    private TextView mServiceChargeView;
    private TextView mNetAmountView;
    private EditText mPinField;

    public RequestMoneyReviewDialog(Context context, long moneyRequestId, String receiverMobileNumber,
                                    String receiverName, String photoUri, BigDecimal amount, BigDecimal serviceCharge,
                                    String title, String description, int serviceID, ReviewDialogFinishListener reviewFinishListener) {
        super(context);

        this.requestId = moneyRequestId;
        this.mReceiverMobileNumber = receiverMobileNumber;
        this.mReceiverName = receiverName;
        this.mPhotoUri = photoUri;
        this.mAmount = amount;
        this.mServiceCharge = serviceCharge;
        this.mTitle = title;
        this.mDescription = description;
        this.mReviewFinishListener = reviewFinishListener;
        this.mServiceID = serviceID;

        initializeView();
    }

    private void initializeView() {
        customView(R.layout.dialog_notification_review, true);

        View v = this.build().getCustomView();
        autoDismiss(false);

        mProgressDialog = new CustomProgressDialog(context);

        mProfileImageView = (ProfileImageView) v.findViewById(R.id.profile_picture);
        mNameView = (TextView) v.findViewById(R.id.textview_name);
        mMobileNumberView = (TextView) v.findViewById(R.id.textview_mobile_number);
        mTitleView = (TextView) v.findViewById(R.id.textview_title);
        mAmountView = (TextView) v.findViewById(R.id.textview_amount);
        mServiceChargeView = (TextView) v.findViewById(R.id.textview_service_charge);
        mNetAmountView = (TextView) v.findViewById(R.id.textview_net_amount);
        mPinField = (EditText) v.findViewById(R.id.pin);

        mProfileImageView.setProfilePicture(mPhotoUri, false);

        if (mReceiverName == null || mReceiverName.isEmpty()) {
            mNameView.setVisibility(View.GONE);
        } else {
            mNameView.setText(mReceiverName);
        }

        mMobileNumberView.setText(mReceiverMobileNumber);

        if (mTitle == null || mTitle.isEmpty()) {
            mTitleView.setVisibility(View.GONE);
        } else {
            mTitleView.setText(mTitle);
        }

        mAmountView.setText(Utilities.formatTaka(mAmount));
        mServiceChargeView.setText(Utilities.formatTaka(mServiceCharge));
        mNetAmountView.setText(Utilities.formatTaka(mAmount.subtract(mServiceCharge)));

        positiveText(R.string.send_money);
        negativeText(R.string.cancel);

        onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                String pin = mPinField.getText().toString();
                if (pin.isEmpty()) {
                    mPinField.setError(getContext().getString(R.string.failed_empty_pin));
                    View focusView = mPinField;
                    focusView.requestFocus();
                } else {
                    dialog.dismiss();
                    if (mServiceID == Constants.SERVICE_ID_REQUEST_MONEY)
                        acceptRequestMoney(requestId, pin);
                    else
                        acceptPaymentRequest(requestId, pin);
                }
            }
        });

        onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        });
    }

    private void acceptRequestMoney(long id, String pin) {
        if (mAcceptRequestTask != null) {
            return;
        }

        mProgressDialog.show();
        RequestMoneyAcceptRejectOrCancelRequest requestMoneyAcceptRejectOrCancelRequest =
                new RequestMoneyAcceptRejectOrCancelRequest(id, pin);
        Gson gson = new Gson();
        String json = gson.toJson(requestMoneyAcceptRejectOrCancelRequest);
        mAcceptRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ACCEPT_REQUESTS_MONEY,
                Constants.BASE_URL_SM + Constants.URL_ACCEPT_NOTIFICATION_REQUEST, json, context, false);
        mAcceptRequestTask.mHttpResponseListener = this;
        mAcceptRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void acceptPaymentRequest(long id, String pin) {

        if (mAcceptPaymentTask != null) {
            return;
        }

        mProgressDialog.show();
        PaymentAcceptRejectOrCancelRequest mPaymentAcceptRejectOrCancelRequest =
                new PaymentAcceptRejectOrCancelRequest(id, pin);
        Gson gson = new Gson();
        String json = gson.toJson(mPaymentAcceptRejectOrCancelRequest);
        mAcceptPaymentTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ACCEPT_PAYMENT_REQUEST,
                Constants.BASE_URL_SM + Constants.URL_ACCEPT_NOTIFICATION_REQUEST, json, context, false);
        mAcceptPaymentTask.mHttpResponseListener = this;
        mAcceptPaymentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mAcceptRequestTask = null;
            mAcceptPaymentTask = null;
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_ACCEPT_REQUESTS_MONEY)) {
            try {
                mRequestMoneyAcceptRejectOrCancelResponse = gson.fromJson(result.getJsonString(),
                        RequestMoneyAcceptRejectOrCancelResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    String message = mRequestMoneyAcceptRejectOrCancelResponse.getMessage();
                    if (context != null)
                        Toaster.makeText(context, message, Toast.LENGTH_LONG);

                    if (mReviewFinishListener != null)
                        mReviewFinishListener.onReviewFinish();

                } else {
                    if (context != null)
                        Toaster.makeText(context, mRequestMoneyAcceptRejectOrCancelResponse.getMessage(), Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (context != null)
                    Toaster.makeText(context, R.string.could_not_accept_money_request, Toast.LENGTH_LONG);
            }

            mProgressDialog.dismiss();
            mAcceptRequestTask = null;

        } else if (result.getApiCommand().equals(Constants.COMMAND_ACCEPT_PAYMENT_REQUEST)) {

            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                try {
                    mPaymentAcceptRejectOrCancelResponse = gson.fromJson(result.getJsonString(),
                            PaymentAcceptRejectOrCancelResponse.class);
                    String message = mPaymentAcceptRejectOrCancelResponse.getMessage();
                    if (context != null)
                        Toaster.makeText(context, message, Toast.LENGTH_LONG);

                    if (mReviewFinishListener != null)
                        mReviewFinishListener.onReviewFinish();

                } catch (Exception e) {
                    e.printStackTrace();
                    if (context != null)
                        Toaster.makeText(context, R.string.could_not_accept_money_request, Toast.LENGTH_LONG);
                }

            } else {
                if (context != null)
                    Toaster.makeText(context, R.string.could_not_accept_money_request, Toast.LENGTH_LONG);
            }

            mProgressDialog.dismiss();
            mAcceptPaymentTask = null;

        }
    }
}
