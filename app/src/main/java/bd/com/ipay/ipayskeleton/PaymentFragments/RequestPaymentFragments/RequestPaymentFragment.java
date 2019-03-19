package bd.com.ipay.ipayskeleton.PaymentFragments.RequestPaymentFragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.DialogActivities.ContactPickerDialogActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.RequestPaymentActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.RequestPaymentReviewActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.ContactsSearchView;
import bd.com.ipay.ipayskeleton.CustomView.CustomContactsSearchView;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.BusinessRule;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.GetBusinessRuleRequestBuilder;
import bd.com.ipay.ipayskeleton.QRScanner.BarcodeCaptureActivity;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleConstants;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class RequestPaymentFragment extends BaseFragment implements LocationListener, HttpResponseListener {

    private HttpRequestGetAsyncTask mGetBusinessRuleTask = null;

    private final int PICK_CONTACT_REQUEST = 100;
    private static final int REQUEST_PAYMENT_REVIEW = 101;

    private Button buttonRequestPayment;
    private ImageView buttonSelectFromContacts;
    private CustomContactsSearchView mMobileNumberEditText;
    private EditText mDescriptionEditText;
    private EditText mAmountEditText;
    private CustomProgressDialog mProgressDialog;

    private String mAmount;
    private String mDescription;
    private String mReceiverMobileNumber;

    private LocationManager locationManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_request_payment, container, false);
        getActivity().setTitle(R.string.request_payment);

        mMobileNumberEditText = (CustomContactsSearchView) v.findViewById(R.id.mobile_number);
        buttonSelectFromContacts = (ImageView) v.findViewById(R.id.select_sender_from_contacts);
        buttonRequestPayment = (Button) v.findViewById(R.id.button_request_payment);
        mDescriptionEditText = (EditText) v.findViewById(R.id.description);
        mAmountEditText = (EditText) v.findViewById(R.id.amount);

        mMobileNumberEditText.setCurrentFragmentTag(Constants.REQUEST_PAYMENT);

        mProgressDialog = new CustomProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);

        RequestPaymentActivity.mMandatoryBusinessRules = BusinessRuleCacheManager.getBusinessRules(Constants.REQUEST_PAYMENT);
        buttonRequestPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utilities.isConnectionAvailable(getActivity())) {
                    // For now, we are directly sending the money without going through any send money query
                    // sendMoneyQuery();
                    Utilities.hideKeyboard(getContext(), getView());
                    if (verifyUserInputs()) {
                        if (RequestPaymentActivity.mMandatoryBusinessRules.IS_LOCATION_REQUIRED()) {
                            if (Utilities.hasForcedLocationPermission(RequestPaymentFragment.this)) {
                                getLocationAndLaunchReviewPage();
                            }
                        } else {
                            launchReviewPage(null);
                        }
                    }
                } else if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        });

        buttonSelectFromContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ContactPickerDialogActivity.class);
                intent.putExtra(Constants.IPAY_MEMBERS_ONLY, true);
                startActivityForResult(intent, PICK_CONTACT_REQUEST);
            }
        });

        mMobileNumberEditText.setCustomTextChangeListener(new ContactsSearchView.CustomTextChangeListener() {
            @Override
            public void onTextChange(String inputText) {

            }

            @Override
            public void onTextChange(String inputText, String name, String imageURL) {

            }
        });

        // Get business rule
        attemptGetBusinessRule(Constants.SERVICE_ID_REQUEST_PAYMENT);

        return v;
    }

    @SuppressLint("MissingPermission")
    private void getLocationAndLaunchReviewPage() {
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mProgressDialog.show();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this, Looper.getMainLooper());
        } else {
            Utilities.showGPSHighAccuracyDialog(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_request_payment));
    }

    private boolean verifyUserInputs() {
        boolean cancel = false;
        View focusView = null;
        String errorMessage = null;

        mReceiverMobileNumber = mMobileNumberEditText.getText().toString();
        mDescription = mDescriptionEditText.getText().toString();
        mAmount = mAmountEditText.getText().toString().trim();

        if (!Utilities.isValueAvailable(RequestPaymentActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT())
                || !Utilities.isValueAvailable(RequestPaymentActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT())) {
            DialogUtils.showDialogForBusinessRuleNotAvailable(getActivity());
            return false;
        } else if (RequestPaymentActivity.mMandatoryBusinessRules.isVERIFICATION_REQUIRED() && !ProfileInfoCacheManager.isAccountVerified()) {
            DialogUtils.showDialogVerificationRequired(getActivity());
            return false;
        }

        //validation check of amount
        if (TextUtils.isEmpty(mAmountEditText.getText())) {
            errorMessage = getString(R.string.please_enter_amount);
        } else if (!InputValidator.isValidDigit(mAmountEditText.getText().toString().trim())) {
            errorMessage = getString(R.string.please_enter_amount);
        } else if (mAmount.trim().length() > 0 && InputValidator.isValidDigit(mAmountEditText.getText().toString().trim())) {
            errorMessage = InputValidator.isValidAmount(getActivity(), new BigDecimal(mAmount),
                    RequestPaymentActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT(),
                    RequestPaymentActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT());
        }
        if (errorMessage != null) {
            focusView = mAmountEditText;
            mAmountEditText.setError(errorMessage);
            cancel = true;
        }

        if (mDescription.length() == 0) {
            mDescriptionEditText.setError(getString(R.string.please_add_description));
            focusView = mDescriptionEditText;
            cancel = true;
        }

        if (!InputValidator.isValidNumber(mReceiverMobileNumber)) {
            focusView = mMobileNumberEditText;
            mMobileNumberEditText.setError(getString(R.string.please_enter_valid_mobile_number));
            cancel = true;
        } else if (ContactEngine.formatMobileNumberBD(mReceiverMobileNumber).equals(ProfileInfoCacheManager.getMobileNumber())) {
            focusView = mMobileNumberEditText;
            mMobileNumberEditText.setError(getString(R.string.you_cannot_request_money_from_your_number));
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private void launchReviewPage(@Nullable Location location) {
        mProgressDialog.dismiss();

        Intent intent = new Intent(getActivity(), RequestPaymentReviewActivity.class);
        intent.putExtra(Constants.DESCRIPTION_TAG, mDescription);
        intent.putExtra(Constants.AMOUNT_TAG, mAmount);
        intent.putExtra(Constants.RECEIVER_MOBILE_NUMBER, ContactEngine.formatMobileNumberBD(mReceiverMobileNumber));

        if (location != null) {
            intent.putExtra(Constants.LATITUDE, location.getLatitude());
            intent.putExtra(Constants.LONGITUDE, location.getLongitude());
        }

        startActivityForResult(intent, REQUEST_PAYMENT_REVIEW);

    }


    private void attemptGetBusinessRule(int serviceID) {

        if (mGetBusinessRuleTask != null) {
            return;
        }

        String mUri = new GetBusinessRuleRequestBuilder(serviceID).getGeneratedUri();
        mGetBusinessRuleTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BUSINESS_RULE,
                mUri, getActivity(), this, true);

        mGetBusinessRuleTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Utilities.LOCATION_SETTINGS_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(getContext(), BarcodeCaptureActivity.class);
                    startActivityForResult(intent, Constants.RC_BARCODE_CAPTURE);
                } else {
                    buttonRequestPayment.performClick();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_CONTACT_REQUEST) {

            String mobileNumber = data.getStringExtra(Constants.MOBILE_NUMBER);
            if (mobileNumber != null) {
                mMobileNumberEditText.setText(mobileNumber);
            }
        } else if (resultCode == Activity.RESULT_CANCELED && requestCode == PICK_CONTACT_REQUEST) {
            if (getActivity() != null)
                Toaster.makeText(getActivity(), getString(R.string.no_contact_selected),
                        Toast.LENGTH_SHORT);
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PAYMENT_REVIEW) {
            getActivity().finish();
        } else if (requestCode == Utilities.LOCATION_SETTINGS_RESULT_CODE || requestCode == Utilities.LOCATION_SOURCE_SETTINGS_RESULT_CODE) {
            buttonRequestPayment.performClick();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        launchReviewPage(location);
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
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            mGetBusinessRuleTask = null;
            mProgressDialog.dismiss();
            return;
        } else if (result.getApiCommand().equals(Constants.COMMAND_GET_BUSINESS_RULE)) {
            mProgressDialog.dismiss();
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                try {
                    Gson gson = new Gson();

                    BusinessRule[] businessRuleArray = gson.fromJson(result.getJsonString(), BusinessRule[].class);

                    if (businessRuleArray != null) {
                        for (BusinessRule rule : businessRuleArray) {
                            switch (rule.getRuleID()) {
                                case BusinessRuleConstants.SERVICE_RULE_REQUEST_PAYMENT_MAX_AMOUNT_PER_PAYMENT:
                                    RequestPaymentActivity.mMandatoryBusinessRules.setMAX_AMOUNT_PER_PAYMENT(rule.getRuleValue());
                                    break;
                                case BusinessRuleConstants.SERVICE_RULE_REQUEST_PAYMENT_MIN_AMOUNT_PER_PAYMENT:
                                    RequestPaymentActivity.mMandatoryBusinessRules.setMIN_AMOUNT_PER_PAYMENT(rule.getRuleValue());
                                    break;
                                case BusinessRuleConstants.SERVICE_RULE_REQUEST_PAYMENT_VERIFICATION_REQUIRED:
                                    RequestPaymentActivity.mMandatoryBusinessRules.setVERIFICATION_REQUIRED(rule.getRuleValue());
                                    break;
                                case BusinessRuleConstants.SERVICE_RULE_REQUEST_PAYMENT_PIN_REQUIRED:
                                    RequestPaymentActivity.mMandatoryBusinessRules.setPIN_REQUIRED(rule.getRuleValue());
                                    break;
                                case BusinessRuleConstants.SERVICE_RULE_REQUEST_PAYMENT_LOCATION_REQUIRED:
                                    RequestPaymentActivity.mMandatoryBusinessRules.setLOCATION_REQUIRED(rule.getRuleValue());
                                    break;
                            }
                        }
                        BusinessRuleCacheManager.setBusinessRules(Constants.REQUEST_PAYMENT, RequestPaymentActivity.mMandatoryBusinessRules);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mGetBusinessRuleTask = null;
        }
    }
}
