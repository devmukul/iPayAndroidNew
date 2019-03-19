package bd.com.ipay.ipayskeleton.PaymentFragments.RequestMoneyFragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.SentReceivedRequestReviewActivity;
import bd.com.ipay.ipayskeleton.Api.ContactApi.AddContactAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomPinCheckerWithInputDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationForTwoFactorAuthenticationServicesDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.BusinessRule;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.GetBusinessRuleRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RequestMoney.RequestMoneyAcceptRejectOrCancelRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RequestMoney.RequestMoneyAcceptRejectOrCancelResponse;
import bd.com.ipay.ipayskeleton.Model.Contact.AddContactRequestBuilder;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleConstants;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class SentReceivedRequestReviewFragment extends BaseFragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mAcceptRequestTask = null;

    private HttpRequestPostAsyncTask mCancelRequestTask = null;

    private HttpRequestPostAsyncTask mRejectRequestTask = null;

    private HttpRequestGetAsyncTask mGetBusinessRuleTask = null;

    private OTPVerificationForTwoFactorAuthenticationServicesDialog mOTPVerificationForTwoFactorAuthenticationServicesDialog;

    private RequestMoneyAcceptRejectOrCancelRequest mRequestMoneyAcceptRejectOrCancelRequest;
    private RequestMoneyAcceptRejectOrCancelResponse mRequestMoneyAcceptRejectOrCancelResponse;

    private CustomProgressDialog mProgressDialog;

    private int mRequestType;
    private BigDecimal mAmount;
    private String mReceiverName;
    private String mReceiverMobileNumber;
    private String mPhotoUri;
    private String mDescription;
    private long mRequestID;
    private String mTransactionID;

    private ProfileImageView mProfileImageView;
    private TextView mNameView;
    private TextView mMobileNumberView;
    private TextView mDescriptionTagView;
    private TextView mDescriptionView;
    private TextView mAmountView;
    private TextView mNetAmountTitleView;
    private Button mRejectButton;
    private Button mAcceptButton;
    private Button mCancelButton;
    private CheckBox mAddInContactsCheckBox;

    private boolean isInContacts;
    private boolean switchedFromTransactionHistory = false;
    private Tracker mTracker;

    private AnimatedProgressDialog mCustomProgressDialog;

    private Context mContext;

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

        mAmount = (BigDecimal) getActivity().getIntent().getSerializableExtra(Constants.AMOUNT);
        mReceiverMobileNumber = getActivity().getIntent().getStringExtra(Constants.RECEIVER_MOBILE_NUMBER);
        mDescription = getActivity().getIntent().getStringExtra(Constants.DESCRIPTION_TAG);
        mReceiverName = getActivity().getIntent().getStringExtra(Constants.NAME);
        mPhotoUri = getActivity().getIntent().getStringExtra(Constants.PHOTO_URI);
        mContext = getContext();
        mCustomProgressDialog = new AnimatedProgressDialog(mContext);
        mRequestType = getActivity().getIntent()
                .getIntExtra(Constants.REQUEST_TYPE, Constants.REQUEST_TYPE_RECEIVED_REQUEST);

        isInContacts = getActivity().getIntent().getBooleanExtra(Constants.IS_IN_CONTACTS, false);
        switchedFromTransactionHistory = getActivity().getIntent()
                .getBooleanExtra(Constants.SWITCHED_FROM_TRANSACTION_HISTORY, false);

        if (switchedFromTransactionHistory)
            mTransactionID = getActivity().getIntent().getStringExtra(Constants.TRANSACTION_ID);
        else
            mRequestID = (long) getActivity().getIntent().getSerializableExtra(Constants.MONEY_REQUEST_ID);

        mProfileImageView = v.findViewById(R.id.image_view_profile);
        mNameView = v.findViewById(R.id.textview_name);
        mMobileNumberView = v.findViewById(R.id.textview_mobile_number);
        mDescriptionTagView = v.findViewById(R.id.description);
        mDescriptionView = v.findViewById(R.id.textview_description);
        mAmountView = v.findViewById(R.id.textview_amount);
        mNetAmountTitleView = v.findViewById(R.id.net_amount_title);
        mAddInContactsCheckBox = v.findViewById(R.id.add_in_contacts);

        mAcceptButton = v.findViewById(R.id.button_accept);
        mRejectButton = v.findViewById(R.id.button_reject);
        mCancelButton = v.findViewById(R.id.button_cancel);


        mProgressDialog = new CustomProgressDialog(getActivity());

        if (mRequestType == Constants.REQUEST_TYPE_RECEIVED_REQUEST) {
            getActivity().setTitle(R.string.request_money);
            mNetAmountTitleView.setText(getString(R.string.recipient_net_amount));
        } else
            getActivity().setTitle(R.string.request_money);

        SentReceivedRequestReviewActivity.mMandatoryBusinessRules = BusinessRuleCacheManager.getBusinessRules(Constants.REQUEST_MONEY);

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

        if (mRequestType == Constants.REQUEST_TYPE_RECEIVED_REQUEST) {
            mAcceptButton.setVisibility(View.VISIBLE);
            mRejectButton.setVisibility(View.VISIBLE);
            mCancelButton.setVisibility(View.GONE);
        } else {
            mAcceptButton.setVisibility(View.GONE);
            mRejectButton.setVisibility(View.GONE);
            mCancelButton.setVisibility(View.VISIBLE);
        }


        if (!isInContacts) {
            mAddInContactsCheckBox.setVisibility(View.VISIBLE);
            mAddInContactsCheckBox.setChecked(true);
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
                        rejectRequestMoney();
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

        // Get business rule
        attemptGetBusinessRule(Constants.SERVICE_ID_REQUEST_MONEY);

        return v;
    }

    private void attemptAcceptRequestWithPinCheck() {
        if (mAddInContactsCheckBox.isChecked()) {
            addContact(mReceiverName, mReceiverMobileNumber, null);
        }

        if (SentReceivedRequestReviewActivity.mMandatoryBusinessRules.IS_PIN_REQUIRED()) {
            new CustomPinCheckerWithInputDialog(getActivity(), new CustomPinCheckerWithInputDialog.PinCheckAndSetListener() {
                @Override
                public void ifPinCheckedAndAdded(String pin) {
                    acceptRequestMoney(pin);
                }
            });
        } else {
            acceptRequestMoney(null);
        }

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
            attemptAcceptRequestWithPinCheck();
        }
    }

    private void showAlertDialogue(String msg, final long id) {
        AlertDialog.Builder alertDialogue = new AlertDialog.Builder(getActivity());
        alertDialogue.setTitle(R.string.confirm_query);
        alertDialogue.setMessage(msg);

        alertDialogue.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                cancelRequest();
            }
        });

        alertDialogue.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        alertDialogue.show();
    }

    private void cancelRequest() {
        if (mAddInContactsCheckBox.isChecked()) {
            addContact(mReceiverName, mReceiverMobileNumber, null);
        }

        if (mCancelRequestTask != null) {
            return;
        }

        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        // No PIN needed for now to place a request from me
        if (!switchedFromTransactionHistory) {
            mRequestMoneyAcceptRejectOrCancelRequest =
                    new RequestMoneyAcceptRejectOrCancelRequest(mRequestID, null);
        } else {
            mRequestMoneyAcceptRejectOrCancelRequest =
                    new RequestMoneyAcceptRejectOrCancelRequest(mTransactionID, null);
        }

        Gson gson = new Gson();
        String json = gson.toJson(mRequestMoneyAcceptRejectOrCancelRequest);
        mCancelRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_CANCEL_REQUESTS_MONEY,
                Constants.BASE_URL_SM + Constants.URL_CANCEL_NOTIFICATION_REQUEST, json, getActivity(), false);
        mCancelRequestTask.mHttpResponseListener = this;
        mCancelRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void rejectRequestMoney() {
        if (mAddInContactsCheckBox.isChecked()) {
            addContact(mReceiverName, mReceiverMobileNumber, null);
        }

        if (mRejectRequestTask != null) {
            return;
        }
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        if (!switchedFromTransactionHistory) {
            mRequestMoneyAcceptRejectOrCancelRequest =
                    new RequestMoneyAcceptRejectOrCancelRequest(mRequestID);
        } else mRequestMoneyAcceptRejectOrCancelRequest =
                new RequestMoneyAcceptRejectOrCancelRequest(mTransactionID);

        Gson gson = new Gson();
        String json = gson.toJson(mRequestMoneyAcceptRejectOrCancelRequest);
        mRejectRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_REJECT_REQUESTS_MONEY,
                Constants.BASE_URL_SM + Constants.URL_REJECT_NOTIFICATION_REQUEST, json, getActivity(), false);
        mRejectRequestTask.mHttpResponseListener = this;
        mRejectRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void launchOTPVerification() {
        String jsonString = new Gson().toJson(mRequestMoneyAcceptRejectOrCancelRequest);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog = new OTPVerificationForTwoFactorAuthenticationServicesDialog(getActivity(), jsonString,
                Constants.COMMAND_ACCEPT_REQUESTS_MONEY,
                Constants.BASE_URL_SM + Constants.URL_ACCEPT_NOTIFICATION_REQUEST, Constants.METHOD_POST);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog.mParentHttpResponseListener = this;
    }

    private void acceptRequestMoney(String pin) {
        if (mAcceptRequestTask != null) {
            return;
        }
        mCustomProgressDialog.showDialog();

        if (!switchedFromTransactionHistory) {
            mRequestMoneyAcceptRejectOrCancelRequest =
                    new RequestMoneyAcceptRejectOrCancelRequest(mRequestID, pin);
        } else {
            mRequestMoneyAcceptRejectOrCancelRequest =
                    new RequestMoneyAcceptRejectOrCancelRequest(mTransactionID, pin);
        }

        Gson gson = new Gson();
        String json = gson.toJson(mRequestMoneyAcceptRejectOrCancelRequest);
        mAcceptRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ACCEPT_REQUESTS_MONEY,
                Constants.BASE_URL_SM + Constants.URL_ACCEPT_NOTIFICATION_REQUEST, json, getActivity(), false);
        mAcceptRequestTask.mHttpResponseListener = this;
        mAcceptRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @ValidateAccess
    private void addContact(String name, String phoneNumber, String relationship) {
        AddContactRequestBuilder addContactRequestBuilder = new
                AddContactRequestBuilder(name, phoneNumber, relationship);

        new AddContactAsyncTask(Constants.COMMAND_ADD_CONTACTS,
                addContactRequestBuilder.generateUri(), addContactRequestBuilder.getAddContactRequest(),
                getActivity()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void attemptGetBusinessRule(int serviceID) {
        if (mGetBusinessRuleTask != null)
            return;

        String mUri = new GetBusinessRuleRequestBuilder(serviceID).getGeneratedUri();
        mGetBusinessRuleTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BUSINESS_RULE,
                mUri, getActivity(), this, true);

        mGetBusinessRuleTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        mProgressDialog.dismiss();

        if (HttpErrorHandler.isErrorFound(result, getContext(), mCustomProgressDialog)) {
            mAcceptRequestTask = null;
            mRejectRequestTask = null;
            return;
        }

        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_GET_BUSINESS_RULE:
                try {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                        BusinessRule[] businessRuleArray = gson.fromJson(result.getJsonString(), BusinessRule[].class);

                        if (businessRuleArray != null) {
                            for (BusinessRule rule : businessRuleArray) {
                                if (rule.getRuleID().equals(BusinessRuleConstants.SERVICE_RULE_REQUEST_MONEY_MAX_AMOUNT_PER_PAYMENT)) {
                                    SentReceivedRequestReviewActivity.mMandatoryBusinessRules.setMAX_AMOUNT_PER_PAYMENT(rule.getRuleValue());
                                } else if (rule.getRuleID().equals(BusinessRuleConstants.SERVICE_RULE_REQUEST_MONEY_MIN_AMOUNT_PER_PAYMENT)) {
                                    SentReceivedRequestReviewActivity.mMandatoryBusinessRules.setMIN_AMOUNT_PER_PAYMENT(rule.getRuleValue());
                                } else if (rule.getRuleID().equals(BusinessRuleConstants.SERVICE_RULE_REQUEST_MONEY_VERIFICATION_REQUIRED)) {
                                    SentReceivedRequestReviewActivity.mMandatoryBusinessRules.setVERIFICATION_REQUIRED(rule.getRuleValue());
                                } else if (rule.getRuleID().equals(BusinessRuleConstants.SERVICE_RULE_REQUEST_MONEY_PIN_REQUIRED)) {
                                    SentReceivedRequestReviewActivity.mMandatoryBusinessRules.setPIN_REQUIRED(rule.getRuleValue());
                                }
                            }
                            BusinessRuleCacheManager.setBusinessRules(Constants.REQUEST_MONEY, SentReceivedRequestReviewActivity.mMandatoryBusinessRules);
                        }
                    } else {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mGetBusinessRuleTask = null;
                break;

            case Constants.COMMAND_ACCEPT_REQUESTS_MONEY:
                try {
                    mRequestMoneyAcceptRejectOrCancelResponse = gson.fromJson(result.getJsonString(),
                            RequestMoneyAcceptRejectOrCancelResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                            mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                        } else {
                            mCustomProgressDialog.showSuccessAnimationAndMessage
                                    (mRequestMoneyAcceptRejectOrCancelResponse.getMessage());
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mCustomProgressDialog.dismissDialog();
                                if (switchedFromTransactionHistory) {
                                    Intent intent = new Intent();
                                    getActivity().setResult(Activity.RESULT_OK, intent);
                                    getActivity().finish();
                                } else
                                    getActivity().onBackPressed();
                            }
                        }, 2000);
                        Utilities.sendSuccessEventTracker(mTracker, "Money Request", ProfileInfoCacheManager.getAccountId(), mAmount.longValue());
                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                        if (getActivity() != null) {
                            mCustomProgressDialog.showFailureAnimationAndMessage
                                    (mRequestMoneyAcceptRejectOrCancelResponse.getMessage());
                            ((MyApplication) getActivity().getApplication()).launchLoginPage("");
                        }
                        Utilities.sendBlockedEventTracker(mTracker, "Money Request", ProfileInfoCacheManager.getAccountId(), mAmount.longValue());
                    } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED) {
                        mCustomProgressDialog.dismissDialog();
                        Toaster.makeText(getActivity(), mRequestMoneyAcceptRejectOrCancelResponse.getMessage(), Toast.LENGTH_LONG);
                        SecuritySettingsActivity.otpDuration = mRequestMoneyAcceptRejectOrCancelResponse.getOtpValidFor();
                        launchOTPVerification();
                    } else {
                        if (getActivity() != null) {
                            if (mOTPVerificationForTwoFactorAuthenticationServicesDialog == null) {
                                mCustomProgressDialog.showFailureAnimationAndMessage(
                                        mRequestMoneyAcceptRejectOrCancelResponse.getMessage());
                            } else {
                                if (mCustomProgressDialog != null) {
                                    mCustomProgressDialog.showFailureAnimationAndMessage(mRequestMoneyAcceptRejectOrCancelResponse.getMessage());
                                } else {
                                    Toast.makeText(mContext, mRequestMoneyAcceptRejectOrCancelResponse.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }

                            if (mRequestMoneyAcceptRejectOrCancelResponse.getMessage().toLowerCase().contains(TwoFactorAuthConstants.WRONG_OTP)) {
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
                        Utilities.sendFailedEventTracker(mTracker, "Money Request", ProfileInfoCacheManager.getAccountId(), mRequestMoneyAcceptRejectOrCancelResponse.getMessage(), mAmount.longValue());
                    }
                } catch (Exception e) {
                    if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                        mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                    }
                    e.printStackTrace();
                    mCustomProgressDialog.showFailureAnimationAndMessage(getString(R.string.service_not_available));
                    Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage());
                }

                mProgressDialog.dismiss();
                mAcceptRequestTask = null;

                break;
            case Constants.COMMAND_REJECT_REQUESTS_MONEY:

                try {
                    mRequestMoneyAcceptRejectOrCancelResponse = gson.fromJson(result.getJsonString(),
                            RequestMoneyAcceptRejectOrCancelResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        String message = mRequestMoneyAcceptRejectOrCancelResponse.getMessage();
                        if (getActivity() != null) {
                            Toaster.makeText(getActivity(), message, Toast.LENGTH_LONG);

                            if (switchedFromTransactionHistory) {
                                Utilities.finishLauncherActivity(getActivity());
                            } else
                                getActivity().onBackPressed();
                        }

                    } else {
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), mRequestMoneyAcceptRejectOrCancelResponse.getMessage(), Toast.LENGTH_LONG);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), R.string.could_not_reject_money_request, Toast.LENGTH_LONG);
                }

                mProgressDialog.dismiss();
                mRejectRequestTask = null;

                break;
            case Constants.COMMAND_CANCEL_REQUESTS_MONEY:

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    try {
                        mRequestMoneyAcceptRejectOrCancelResponse = gson.fromJson(result.getJsonString(),
                                RequestMoneyAcceptRejectOrCancelResponse.class);
                        String message = mRequestMoneyAcceptRejectOrCancelResponse.getMessage();
                        if (getActivity() != null) {
                            Toaster.makeText(getActivity(), message, Toast.LENGTH_LONG);

                            if (switchedFromTransactionHistory) {
                                Utilities.finishLauncherActivity(getActivity());
                            } else
                                getActivity().onBackPressed();
                        }

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
        }

    }
}
