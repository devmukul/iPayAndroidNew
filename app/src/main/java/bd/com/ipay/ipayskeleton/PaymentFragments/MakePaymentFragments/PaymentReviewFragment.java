package bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
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
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.PaymentActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomPinCheckerWithInputDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationForTwoFactorAuthenticationServicesDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PaymentRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PaymentResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class PaymentReviewFragment extends BaseFragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mPaymentTask = null;

    private PaymentRequest mPaymentRequest;

    private ProgressDialog mProgressDialog;
    private OTPVerificationForTwoFactorAuthenticationServicesDialog mOTPVerificationForTwoFactorAuthenticationServicesDialog;

    private BigDecimal mAmount;
    private String mReceiverBusinessName;
    private String mReceiverBusinessMobileNumber;
    private String mAddressString;
    private String mCountry;
    private String mDistrict;
    private String mThana;
    private String mPhotoUri;
    private String mDescription;
    private String mReferenceNumber;
    private double latitude;
    private double longitude;

    private Tracker mTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAmount = (BigDecimal) getActivity().getIntent().getSerializableExtra(Constants.AMOUNT);
        mReceiverBusinessMobileNumber = getActivity().getIntent().getStringExtra(Constants.RECEIVER_MOBILE_NUMBER);
        mDescription = getActivity().getIntent().getStringExtra(Constants.DESCRIPTION_TAG);
        mReferenceNumber = getActivity().getIntent().getStringExtra(Constants.REFERENCE_NUMBER);
        latitude = getActivity().getIntent().getDoubleExtra(Constants.LATITUDE, 0.0);
        longitude = getActivity().getIntent().getDoubleExtra(Constants.LONGITUDE, 0.0);


        if (getArguments() != null) {
            mReceiverBusinessName = getArguments().getString(Constants.NAME);
            mPhotoUri = getArguments().getString(Constants.PHOTO_URI);
            mAddressString = getArguments().getString(Constants.ADDRESS);
            mCountry = getArguments().getString(Constants.COUNTRY);
            mDistrict = getArguments().getString(Constants.DISTRICT);
            mThana = getArguments().getString(Constants.THANA);
        }

        mProgressDialog = new ProgressDialog(getActivity());

        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker,
                getString(R.string.screen_name_make_payment_review));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_review, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ProfileImageView businessProfileImageView = findViewById(R.id.business_profile_image_view);
        final TextView businessNameTextView = findViewById(R.id.business_name_text_view);
        final TextView businessAddressTextView = findViewById(R.id.business_address_line_1_text_view);
        final TextView businessDistrictAndThanaTextView = findViewById(R.id.business_address_line_2_text_view);
        final TextView amountTextView = findViewById(R.id.amount_text_view);
        final View referenceNumberViewHolder = findViewById(R.id.reference_number_view_holder);
        final TextView referenceNumberTextView = findViewById(R.id.reference_number_text_view);
        final View descriptionViewHolder = findViewById(R.id.description_view_holder);
        final TextView descriptionTextView = findViewById(R.id.description_text_view);
        final TextView mCountryTextView = findViewById(R.id.business_address_line_3_text_view);
        final Button makePaymentButton = findViewById(R.id.make_payment_button);

        try {
            if (!TextUtils.isEmpty(mPhotoUri)) {
                businessProfileImageView.setBusinessProfilePicture(Constants.BASE_URL_FTP_SERVER + mPhotoUri, false);
            }
            if (TextUtils.isEmpty(mReceiverBusinessName)) {
                businessNameTextView.setVisibility(View.GONE);
            } else {
                businessNameTextView.setVisibility(View.VISIBLE);
                businessNameTextView.setText(mReceiverBusinessName);
            }
            if (mAddressString != null && mDistrict != null && mCountry != null) {
                businessAddressTextView.setVisibility(View.VISIBLE);
                businessDistrictAndThanaTextView.setVisibility(View.VISIBLE);
                mCountryTextView.setVisibility(View.VISIBLE);
                businessAddressTextView.setText(mAddressString);
                businessDistrictAndThanaTextView.setText(mThana + " , " + mDistrict);
                mCountryTextView.setText(mCountry);
            }

            amountTextView.setText(Utilities.formatTaka(mAmount));

            if (TextUtils.isEmpty(mReferenceNumber)) {
                referenceNumberViewHolder.setVisibility(View.GONE);
            } else {
                referenceNumberViewHolder.setVisibility(View.VISIBLE);
                referenceNumberTextView.setText(mReferenceNumber);
            }

            if (TextUtils.isEmpty(mDescription)) {
                descriptionViewHolder.setVisibility(View.GONE);
            } else {
                descriptionViewHolder.setVisibility(View.VISIBLE);
                descriptionTextView.setText(mDescription);
            }
        } catch (Exception e) {

        }

        makePaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptPaymentWithPinCheck();
            }
        });
    }

    public <T extends View> T findViewById(@IdRes int id) {
        //noinspection unchecked,ConstantConditions
        return (T) getView().findViewById(id);
    }

    private void attemptPaymentWithPinCheck() {
        if (PaymentActivity.mMandatoryBusinessRules.IS_PIN_REQUIRED()) {
            new CustomPinCheckerWithInputDialog(getActivity(), new CustomPinCheckerWithInputDialog.PinCheckAndSetListener() {
                @Override
                public void ifPinCheckedAndAdded(String pin) {
                    attemptPayment(pin);
                }
            });
        } else {
            attemptPayment(null);
        }
    }

    private void attemptPayment(String pin) {
        if (mPaymentTask != null) {
            return;
        }

        mProgressDialog.setMessage(getString(R.string.progress_dialog_text_payment));
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mPaymentRequest = new PaymentRequest(
                ContactEngine.formatMobileNumberBD(mReceiverBusinessMobileNumber),
                mAmount.toString(), mDescription, pin, mReferenceNumber, null,latitude, longitude);

        Gson gson = new Gson();
        String json = gson.toJson(mPaymentRequest);
        mPaymentTask = new HttpRequestPostAsyncTask(Constants.COMMAND_PAYMENT,
                Constants.BASE_URL_SM + Constants.URL_PAYMENT, json, getActivity(), false);
        mPaymentTask.mHttpResponseListener = this;
        mPaymentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    private void launchOTPVerification() {
        String jsonString = new Gson().toJson(mPaymentRequest);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog = new OTPVerificationForTwoFactorAuthenticationServicesDialog(getActivity(), jsonString, Constants.COMMAND_PAYMENT,
                Constants.BASE_URL_SM + Constants.URL_PAYMENT, Constants.METHOD_POST);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog.mParentHttpResponseListener = this;
    }


    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mPaymentTask = null;
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_PAYMENT)) {

            try {
                PaymentResponse mPaymentResponse = gson.fromJson(result.getJsonString(), PaymentResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (getActivity() != null) {
                        Toaster.makeText(getActivity(), mPaymentResponse.getMessage(), Toast.LENGTH_LONG);
                        if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                        }
                    }
                    getActivity().setResult(Activity.RESULT_OK);
                    switchToPaymentSuccessFragment(mReceiverBusinessName, mPhotoUri, mPaymentResponse.getTransactionId());

                    Utilities.sendSuccessEventTracker(mTracker, "Make Payment", ProfileInfoCacheManager.getAccountId(), mAmount.longValue());

                    //getActivity().finish();
                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                    ((MyApplication) getActivity().getApplication()).launchLoginPage(mPaymentResponse.getMessage());
                    Utilities.sendBlockedEventTracker(mTracker, "Make Payment", ProfileInfoCacheManager.getAccountId(), mAmount.longValue());

                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED) {
                    Toast.makeText(getActivity(), mPaymentResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    SecuritySettingsActivity.otpDuration = mPaymentResponse.getOtpValidFor();
                    launchOTPVerification();
                } else {
                    if (getActivity() != null) {
                        Toaster.makeText(getActivity(), mPaymentResponse.getMessage(), Toast.LENGTH_LONG);
                        if (mPaymentResponse.getMessage().toLowerCase().contains(TwoFactorAuthConstants.WRONG_OTP)) {
                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.showOtpDialog();
                        } else if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                        }
                    }

                    //Google Analytic event
                    Utilities.sendFailedEventTracker(mTracker, "Make Payment", ProfileInfoCacheManager.getAccountId(), mPaymentResponse.getMessage(), mAmount.longValue());

                }
            } catch (Exception e) {
                if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                    mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                }
                e.printStackTrace();
                Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage());
            }

            mProgressDialog.dismiss();
            mPaymentTask = null;

        }
    }


    private void switchToPaymentSuccessFragment(String name, String profilePictureUrl, String tansactionId) {
        PaymentSucessFragment paymentSuccessFragment = new PaymentSucessFragment();

        Bundle bundle = new Bundle();
        bundle.putString(Constants.NAME, name);
        bundle.putString(Constants.PHOTO_URI, profilePictureUrl);
        bundle.putString(Constants.TRANSACTION_ID, tansactionId);
        paymentSuccessFragment.setArguments(bundle);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, paymentSuccessFragment).commit();

    }
}
