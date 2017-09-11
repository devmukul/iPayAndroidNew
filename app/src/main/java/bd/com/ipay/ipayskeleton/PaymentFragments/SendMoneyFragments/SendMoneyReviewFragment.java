package bd.com.ipay.ipayskeleton.PaymentFragments.SendMoneyFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.SendMoneyActivity;
import bd.com.ipay.ipayskeleton.Api.ContactApi.AddContactAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomPinCheckerWithInputDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SendMoney.SendMoneyRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SendMoney.SendMoneyResponse;
import bd.com.ipay.ipayskeleton.Model.Contact.AddContactRequestBuilder;
import bd.com.ipay.ipayskeleton.PaymentFragments.CommonFragments.ReviewFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class SendMoneyReviewFragment extends ReviewFragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mSendMoneyTask = null;
    private SendMoneyResponse mSendMoneyResponse;

    private ProgressDialog mProgressDialog;

    private BigDecimal mAmount;
    private String mReceiverName;
    private String mReceiverMobileNumber;
    private String mSenderMobileNumber;
    private String mPhotoUri;
    private String mDescription;
    private String mError_message;

    private boolean isInContacts;

    private LinearLayout mLinearLayoutDescriptionHolder;
    private ProfileImageView mProfileImageView;
    private TextView mNameView;
    private TextView mMobileNumberView;
    private TextView mDescriptionView;
    private TextView mAmountView;
    private TextView mServiceChargeView;
    private TextView mNetAmountView;
    private Button mSendMoneyButton;
    private CheckBox mAddInContactsCheckBox;
    private Tracker mTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_send_money_review));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_send_money_review, container, false);

        mAmount = (BigDecimal) getActivity().getIntent().getSerializableExtra(Constants.AMOUNT);
        mReceiverMobileNumber = getActivity().getIntent().getStringExtra(Constants.RECEIVER_MOBILE_NUMBER);
        mDescription = getActivity().getIntent().getStringExtra(Constants.DESCRIPTION_TAG);

        mReceiverName = getArguments().getString(Constants.NAME);
        mPhotoUri = getArguments().getString(Constants.PHOTO_URI);

        isInContacts = getActivity().getIntent().getBooleanExtra(Constants.IS_IN_CONTACTS, false);

        mProfileImageView = (ProfileImageView) v.findViewById(R.id.profile_picture);
        mNameView = (TextView) v.findViewById(R.id.textview_name);
        mMobileNumberView = (TextView) v.findViewById(R.id.textview_mobile_number);
        mLinearLayoutDescriptionHolder = (LinearLayout) v.findViewById(R.id.layout_description_holder);
        mDescriptionView = (TextView) v.findViewById(R.id.textview_description);
        mAmountView = (TextView) v.findViewById(R.id.textview_amount);
        mServiceChargeView = (TextView) v.findViewById(R.id.textview_service_charge);
        mNetAmountView = (TextView) v.findViewById(R.id.textview_net_amount);
        mSendMoneyButton = (Button) v.findViewById(R.id.button_send_money);
        mAddInContactsCheckBox = (CheckBox) v.findViewById(R.id.add_in_contacts);

        mProgressDialog = new ProgressDialog(getActivity());

        mSenderMobileNumber = ProfileInfoCacheManager.getMobileNumber();

        mProfileImageView.setProfilePicture(mPhotoUri, false);

        if (mReceiverName == null || mReceiverName.isEmpty()) {
            mNameView.setVisibility(View.GONE);
        } else {
            mNameView.setText(mReceiverName);
        }

        mMobileNumberView.setText(mReceiverMobileNumber);

        if (mDescription == null || mDescription.isEmpty()) {
            mLinearLayoutDescriptionHolder.setVisibility(View.GONE);
        } else {
            mDescriptionView.setText(mDescription);
        }

        mAmountView.setText(Utilities.formatTaka(mAmount));

        if (!isInContacts) {
            mAddInContactsCheckBox.setVisibility(View.VISIBLE);
            mAddInContactsCheckBox.setChecked(true);
        }

        mSendMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utilities.isValueAvailable(SendMoneyActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT())
                        && Utilities.isValueAvailable(SendMoneyActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT())) {
                    mError_message = InputValidator.isValidAmount(getActivity(), mAmount,
                            SendMoneyActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT(),
                            SendMoneyActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT());

                    if (mError_message == null) {
                        attemptSendMoneyWithPinCheck();
                    } else {
                        showErrorDialog();
                    }
                } else {
                    attemptSendMoneyWithPinCheck();
                }
            }
        });

        if (!Utilities.isValueAvailable(SendMoneyActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT())
                && !Utilities.isValueAvailable(SendMoneyActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT()))
            attemptGetBusinessRuleWithServiceCharge(Constants.SERVICE_ID_SEND_MONEY);
        else
            attemptGetServiceCharge();
        return v;
    }

    private void attemptSendMoneyWithPinCheck() {
        if (mAddInContactsCheckBox.isChecked()) {
            addContact(mReceiverName, mReceiverMobileNumber, null);
        }
        if (SendMoneyActivity.mMandatoryBusinessRules.IS_PIN_REQUIRED()) {
            new CustomPinCheckerWithInputDialog(getActivity(), new CustomPinCheckerWithInputDialog.PinCheckAndSetListener() {
                @Override
                public void ifPinCheckedAndAdded(String pin) {
                    attemptSendMoney(pin);
                }
            });
        } else {
            attemptSendMoney(null);
        }
    }

    @ValidateAccess
    private void addContact(String name, String phoneNumber, String relationship) {
        AddContactRequestBuilder addContactRequestBuilder = new
                AddContactRequestBuilder(name, phoneNumber, relationship);

        new AddContactAsyncTask(Constants.COMMAND_ADD_CONTACTS,
                addContactRequestBuilder.generateUri(), addContactRequestBuilder.getAddContactRequest(),
                getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void attemptSendMoney(String pin) {
        if (mSendMoneyTask != null) {
            return;
        }

        mProgressDialog.setMessage(getString(R.string.progress_dialog_text_sending_money));
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        SendMoneyRequest mSendMoneyRequest = new SendMoneyRequest(
                mSenderMobileNumber, ContactEngine.formatMobileNumberBD(mReceiverMobileNumber),
                mAmount.toString(), mDescription, pin);
        Gson gson = new Gson();
        String json = gson.toJson(mSendMoneyRequest);
        mSendMoneyTask = new HttpRequestPostAsyncTask(Constants.COMMAND_SEND_MONEY,
                Constants.BASE_URL_SM + Constants.URL_SEND_MONEY, json, getActivity());
        mSendMoneyTask.mHttpResponseListener = this;
        mSendMoneyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage(mError_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public int getServiceID() {
        return Constants.SERVICE_ID_SEND_MONEY;
    }

    @Override
    public BigDecimal getAmount() {
        return mAmount;
    }

    @Override
    public void onServiceChargeLoadFinished(BigDecimal serviceCharge) {
        mServiceChargeView.setText(Utilities.formatTaka(serviceCharge));
        mNetAmountView.setText(Utilities.formatTaka(mAmount.subtract(serviceCharge)));
    }

    @Override
    public void onPinLoadFinished(boolean isPinRequired) {
        SendMoneyActivity.mMandatoryBusinessRules.setIS_PIN_REQUIRED(isPinRequired);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        super.httpResponseReceiver(result);

        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mProgressDialog.dismiss();
            mSendMoneyTask = null;
            if (getActivity() != null)
                Toaster.makeText(getActivity(), R.string.send_money_failed_due_to_server_down, Toast.LENGTH_SHORT);
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_SEND_MONEY)) {

            try {
                mSendMoneyResponse = gson.fromJson(result.getJsonString(), SendMoneyResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), mSendMoneyResponse.getMessage(), Toast.LENGTH_LONG);
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();

                    //Google Analytic event
                    Utilities.sendEventTracker(mTracker, "SendMoney", "Sent", mSendMoneyResponse.getMessage());
                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                    if (getActivity() != null)
                        ((MyApplication) getActivity().getApplication()).launchLoginPage(mSendMoneyResponse.getMessage());
                } else {
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), mSendMoneyResponse.getMessage(), Toast.LENGTH_LONG);

                    //Google Analytic event
                    Utilities.sendEventTracker(mTracker, "SendMoney", "Failed", mSendMoneyResponse.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mProgressDialog.dismiss();
            mSendMoneyTask = null;

        }
    }
}
