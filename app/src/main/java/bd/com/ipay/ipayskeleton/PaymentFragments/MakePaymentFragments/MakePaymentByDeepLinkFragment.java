package bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.NumberFormat;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.PaymentActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestDeleteAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomPinCheckerWithInputDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.GetBusinessRuleRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.CancelOrderRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.CancelOrderResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.GetOrderDetailsRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.GetPayByDeepLinkResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.OrderDetailsResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PayOrderRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PaymentRequestByDeepLink;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;


public class MakePaymentByDeepLinkFragment extends Fragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetBusinessRuleTask;

    private HttpRequestDeleteAsyncTask mCancelOrderTask;

    private HttpRequestGetAsyncTask mGetOrderDetailsTask;
    private OrderDetailsResponse mOrderDetailsResponse;

    private HttpRequestPostAsyncTask mPaymentTask = null;

    private TextView mBusinessNameTextView;
    private TextView mAmountTextView;
    private TextView mDescriptionTextView;
    private ProfileImageView mBusinessLogoImageView;
    private Button mConfirmButton;
    private Button mCancelButton;

    private String orderID;

    private final Gson gson = new GsonBuilder().create();
    private AnimatedProgressDialog mCustomProgressDialog;
    private NumberFormat mNumberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (PaymentActivity.mMandatoryBusinessRules == null) {
            PaymentActivity.mMandatoryBusinessRules = BusinessRuleCacheManager.getBusinessRules(Constants.MAKE_PAYMENT);
        }

        if (getActivity() != null) {
            mCustomProgressDialog = new AnimatedProgressDialog(getContext());
            orderID = getActivity().getIntent().getStringExtra(Constants.ORDER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pay_by_deep_link, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBusinessNameTextView = view.findViewById(R.id.business_name_text_view);
        mAmountTextView = view.findViewById(R.id.amount_text_view);
        mDescriptionTextView = view.findViewById(R.id.description_text_view);
        mBusinessLogoImageView = view.findViewById(R.id.business_profile_image_view);
        mConfirmButton = view.findViewById(R.id.make_payment_button);
        mCancelButton = view.findViewById(R.id.cancel_button);
        SharedPrefManager.setFirstLaunch(false);
        fetchOrderDetails();
        setButtonActions();
    }

    private void fetchOrderDetails() {
        if (getActivity() != null) {
            if (mGetOrderDetailsTask == null) {
                if (mCustomProgressDialog != null) {
                    mCustomProgressDialog.setCancelable(false);
                    mCustomProgressDialog.show();
                }
                String mUri = new GetOrderDetailsRequestBuilder(orderID).getGeneratedUri();
                mGetOrderDetailsTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_ORDER_DETAILS,
                        mUri, getActivity(), this, false);
                mGetOrderDetailsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    private void setupViewWithData() {
        mBusinessNameTextView.setText(mOrderDetailsResponse.getMerchantName());
        mBusinessLogoImageView.setBusinessProfilePicture(
                Constants.BASE_URL_FTP_SERVER + mOrderDetailsResponse.getMerchantLogoUrl(), false);

        mAmountTextView.setText(mNumberFormat.format(mOrderDetailsResponse.getAmount()));
        mDescriptionTextView.setText(mOrderDetailsResponse.getDescription());
    }

    private void attemptGetBusinessRules(int serviceID) {
        if (mGetBusinessRuleTask != null)
            return;

        String mUri = new GetBusinessRuleRequestBuilder(serviceID).getGeneratedUri();
        mGetBusinessRuleTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BUSINESS_RULE,
                mUri, getActivity(), this, true);

        mGetBusinessRuleTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGetOrderDetailsTask == null)
            mCustomProgressDialog.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGetOrderDetailsTask == null)
            mCustomProgressDialog.dismiss();
    }

    private void setButtonActions() {
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    Utilities.hideKeyboard(getContext(), getView());
                    attemptPaymentWithPinCheck();
                } else if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getString(R.string.do_you_want_to_cancel_payment))
                            .setCancelable(true)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    attemptCancelPayment();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
    }

    private void attemptCancelPayment() {
        if (mCancelOrderTask == null) {
            if (mCustomProgressDialog != null) {
                mCustomProgressDialog.show();
            }
            mCancelOrderTask = new HttpRequestDeleteAsyncTask(Constants.COMMAND_CANCEL_ORDER,
                    new CancelOrderRequestBuilder(orderID).getGeneratedUri(), getActivity(), false);
            mCancelOrderTask.mHttpResponseListener = this;
            mCancelOrderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
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
        if (mPaymentTask == null) {
            if (mCustomProgressDialog != null) {
                mCustomProgressDialog.showDialog();
            }
            PaymentRequestByDeepLink mPaymentRequestByDeepLink = new PaymentRequestByDeepLink(pin);

            String mUri = new PayOrderRequestBuilder(orderID).getGeneratedUri();
            mPaymentTask = new HttpRequestPostAsyncTask(Constants.COMMAND_PAYMENT_BY_DEEP_LINK,
                    mUri, new Gson().toJson(mPaymentRequestByDeepLink), getActivity(), false);
            mPaymentTask.mHttpResponseListener = this;
            mPaymentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void launchThirdPartyApp(String message, String redirectUrl) {
        final String thirdPartyAppUrl = "ipay" + mOrderDetailsResponse.getMerchantAppUriSchemeAndroid() + "://" + orderID + "/" + message;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(thirdPartyAppUrl)));
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl));
                startActivity(intent);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        if (getActivity() != null)
            getActivity().finishAffinity();
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (result != null && result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND
                && result.getApiCommand().equals(Constants.COMMAND_GET_ORDER_DETAILS)) {
            if (mCustomProgressDialog != null)
                mCustomProgressDialog.dismiss();

            Gson gson = new Gson();
            mOrderDetailsResponse = gson.fromJson(result.getJsonString(), OrderDetailsResponse.class);
            DialogUtils.showNecessaryDialogForDeepLinkAction(getActivity(), mOrderDetailsResponse.getMessage());
        } else if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            if (mCustomProgressDialog != null)
                mCustomProgressDialog.dismissDialog();
            mGetOrderDetailsTask = null;
            mPaymentTask = null;

        } else {
            switch (result.getApiCommand()) {
                case Constants.COMMAND_GET_ORDER_DETAILS:
                    if (mCustomProgressDialog != null)
                        mCustomProgressDialog.dismiss();

                    mOrderDetailsResponse = gson.fromJson(result.getJsonString(), OrderDetailsResponse.class);
                    try {
                        if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                            if (mOrderDetailsResponse != null) {
                                setupViewWithData();
                            }
                        } else {
                            DialogUtils.showNecessaryDialogForDeepLinkAction(getActivity(), mOrderDetailsResponse.getMessage());
                        }
                    } catch (Exception e) {
                        DialogUtils.showNecessaryDialogForDeepLinkAction(getActivity(), mOrderDetailsResponse.getMessage());
                    }
                    mGetOrderDetailsTask = null;
                    break;
                case Constants.COMMAND_PAYMENT_BY_DEEP_LINK:
                    final GetPayByDeepLinkResponse getPayByDeepLinkResponse = gson.fromJson(result.getJsonString(), GetPayByDeepLinkResponse.class);
                    try {
                        if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                            if (mCustomProgressDialog != null) {
                                mCustomProgressDialog.showSuccessAnimationAndMessage(getPayByDeepLinkResponse.getMessage());
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    launchThirdPartyApp(Constants.ORDER_CHECKOUT_SUCCESS, getPayByDeepLinkResponse.getRedirectUrl());
                                }
                            }, 2000);

                        } else {
                            if (mCustomProgressDialog != null) {
                                mCustomProgressDialog.showFailureAnimationAndMessage(getPayByDeepLinkResponse.getMessage());
                            }
                            if (!getPayByDeepLinkResponse.getMessage().toLowerCase().contains(Constants.INVALID_CREDENTIAL)) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        launchThirdPartyApp(Constants.ORDER_CHECKOUT_FAILED, getPayByDeepLinkResponse.getRedirectUrl());
                                    }
                                }, 2000);
                            }

                        }
                    } catch (Exception e) {
                        if (mCustomProgressDialog != null) {
                            mCustomProgressDialog.dismissDialog();
                        }
                        Toast.makeText(getActivity(), getString(R.string.payment_failed), Toast.LENGTH_LONG).show();
                    }
                    mPaymentTask = null;
                    break;
                case Constants.COMMAND_CANCEL_ORDER:
                    if (mCustomProgressDialog != null) {
                        mCustomProgressDialog.dismissDialog();
                    }
                    CancelOrderResponse mCancelOrderResponse = gson.fromJson(result.getJsonString(),
                            CancelOrderResponse.class);
                    try {
                        Toast.makeText(getContext(), mCancelOrderResponse.getMessage(), Toast.LENGTH_LONG).show();
                        launchThirdPartyApp(Constants.ORDER_CHECKOUT_CANCELLED, mCancelOrderResponse.getRedirectUrl());
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), mCancelOrderResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    mCancelOrderTask = null;
                    break;
            }
        }
    }
}