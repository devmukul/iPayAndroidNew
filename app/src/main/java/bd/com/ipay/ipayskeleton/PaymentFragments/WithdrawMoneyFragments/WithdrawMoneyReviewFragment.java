package bd.com.ipay.ipayskeleton.PaymentFragments.WithdrawMoneyFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.WithdrawMoneyActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomPinCheckerWithInputDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationForTwoFactorAuthenticationServicesDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.WithdrawMoneyRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.WithdrawMoneyResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.PaymentFragments.CommonFragments.ReviewFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class WithdrawMoneyReviewFragment extends ReviewFragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mWithdrawMoneyTask = null;
    private WithdrawMoneyRequest mWithdrawMoneyRequest;

    private CustomProgressDialog mProgressDialog;
    private AnimatedProgressDialog mCustomProgressDialog;
    private OTPVerificationForTwoFactorAuthenticationServicesDialog mOTPVerificationForTwoFactorAuthenticationServicesDialog;

    private double mAmount;
    private String mDescription;
    private BankAccountList mSelectedBank;

    private TextView mServiceChargeTextView;
    private TextView mNetAmountTextView;
    private View mNetAmountViewHolder;
    private View mServiceChargeViewHolder;

    private Tracker mTracker;

    private Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAmount = getActivity().getIntent().getDoubleExtra(Constants.AMOUNT, 0);

        mDescription = getActivity().getIntent().getStringExtra(Constants.DESCRIPTION_TAG);
        mSelectedBank = getActivity().getIntent().getParcelableExtra(Constants.SELECTED_BANK_ACCOUNT);

        mProgressDialog = new CustomProgressDialog(getActivity());
        mContext = getContext();
        mCustomProgressDialog = new AnimatedProgressDialog(mContext);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_withdraw_money_review));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_withdraw_money_review, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ImageView bankIconImageView = findViewById(R.id.bank_icon_image_view);
        final TextView bankNameTextView = findViewById(R.id.bank_name_text_view);
        final TextView bankAccountNumberTextView = findViewById(R.id.bank_account_number_text_view);
        final TextView amountTextView = findViewById(R.id.amount_text_view);
        mServiceChargeTextView = findViewById(R.id.service_charge_text_view);
        mNetAmountTextView = findViewById(R.id.net_amount_text_view);
        mNetAmountViewHolder = findViewById(R.id.netAmountViewHolder);
        mServiceChargeViewHolder = findViewById(R.id.serviceChargeViewHolder);
        final LinearLayout descriptionViewHolder = findViewById(R.id.description_view_holder);
        final TextView descriptionTextView = findViewById(R.id.description_text_view);
        final Button withdrawMoneyButton = findViewById(R.id.withdraw_money_button);

        bankIconImageView.setImageResource(mSelectedBank.getBankIcon(getContext()));
        bankNameTextView.setText(mSelectedBank.getBankName());
        bankAccountNumberTextView.setText(mSelectedBank.getAccountNumber());

        amountTextView.setText(Utilities.formatTaka(getAmount()));
        mServiceChargeTextView.setText(Utilities.formatTaka(new BigDecimal(0.0)));
        mNetAmountTextView.setText(Utilities.formatTaka(getAmount().add(new BigDecimal(0.0))));

        if (mDescription == null || mDescription.isEmpty()) {
            descriptionViewHolder.setVisibility(View.GONE);
        } else {
            descriptionViewHolder.setVisibility(View.VISIBLE);
            descriptionTextView.setText(mDescription);
        }

        withdrawMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utilities.isValueAvailable(WithdrawMoneyActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT())
                        && Utilities.isValueAvailable(WithdrawMoneyActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT())) {
                    String errorMessage = InputValidator.isValidAmount(getActivity(), new BigDecimal(mAmount),
                            WithdrawMoneyActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT(),
                            WithdrawMoneyActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT());

                    if (errorMessage == null) {
                        attemptWithdrawMoneyWithPinCheck();

                    } else {
                        showErrorDialog(errorMessage);
                    }
                } else
                    attemptWithdrawMoneyWithPinCheck();
                ;

            }
        });

        attemptGetServiceCharge();
    }

    public <T extends View> T findViewById(@IdRes int id) {
        //noinspection unchecked,ConstantConditions
        return (T) getView().findViewById(id);
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

    private void attemptWithdrawMoneyWithPinCheck() {
        if (WithdrawMoneyActivity.mMandatoryBusinessRules.IS_PIN_REQUIRED()) {
            new CustomPinCheckerWithInputDialog(getActivity(), new CustomPinCheckerWithInputDialog.PinCheckAndSetListener() {
                @Override
                public void ifPinCheckedAndAdded(String pin) {
                    attemptWithdrawMoney(pin);
                }
            });
        } else {
            attemptWithdrawMoney(null);
        }
    }

    private void attemptWithdrawMoney(String pin) {
        if (mWithdrawMoneyTask != null) {
            return;
        }
        mCustomProgressDialog.showDialog();
        mWithdrawMoneyRequest = new WithdrawMoneyRequest(mSelectedBank.getBankAccountId(), mAmount, mDescription, pin);
        Gson gson = new Gson();
        String json = gson.toJson(mWithdrawMoneyRequest);
        mWithdrawMoneyTask = new HttpRequestPostAsyncTask(Constants.COMMAND_WITHDRAW_MONEY,
                Constants.BASE_URL_SM + Constants.URL_WITHDRAW_MONEY, json, getActivity(), false);
        mWithdrawMoneyTask.mHttpResponseListener = this;

        mWithdrawMoneyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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


    @Override
    public int getServiceID() {
        return Constants.SERVICE_ID_WITHDRAW_MONEY;
    }

    @Override
    public BigDecimal getAmount() {
        return new BigDecimal(mAmount);
    }

    @Override
    public void onServiceChargeLoadFinished(BigDecimal serviceCharge) {
        mServiceChargeViewHolder.setVisibility(View.VISIBLE);
        mNetAmountViewHolder.setVisibility(View.VISIBLE);
        mServiceChargeTextView.setText(Utilities.formatTaka(serviceCharge));
        mNetAmountTextView.setText(Utilities.formatTaka(getAmount().add(serviceCharge)));
    }

    private void launchOTPVerification() {
        String jsonString = new Gson().toJson(mWithdrawMoneyRequest);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog = new OTPVerificationForTwoFactorAuthenticationServicesDialog(getActivity(), jsonString, Constants.COMMAND_WITHDRAW_MONEY,
                Constants.BASE_URL_SM + Constants.URL_WITHDRAW_MONEY, Constants.METHOD_POST);
        mOTPVerificationForTwoFactorAuthenticationServicesDialog.mParentHttpResponseListener = this;
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        super.httpResponseReceiver(result);
        mProgressDialog.dismiss();

        if (HttpErrorHandler.isErrorFound(result, getContext(), mCustomProgressDialog)) {
            mWithdrawMoneyTask = null;
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_WITHDRAW_MONEY)) {

            try {
                WithdrawMoneyResponse mWithdrawMoneyResponse = gson.fromJson(result.getJsonString(), WithdrawMoneyResponse.class);

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                        mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                    } else {
                        mCustomProgressDialog.showSuccessAnimationAndMessage(mWithdrawMoneyResponse.getMessage());
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mCustomProgressDialog.dismissDialog();
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                        }
                    }, 2000);

                    //Google Analytic event
                    Utilities.sendSuccessEventTracker(mTracker, "Withdraw Money", ProfileInfoCacheManager.getAccountId(), Double.valueOf(mAmount).longValue());
                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_BLOCKED) {
                    if (getActivity() != null) {
                        mCustomProgressDialog.showFailureAnimationAndMessage(mWithdrawMoneyResponse.getMessage());
                        ((MyApplication) getActivity().getApplication()).launchLoginPage("");
                    }
                    Utilities.sendBlockedEventTracker(mTracker, "Withdraw Money", ProfileInfoCacheManager.getAccountId(), Double.valueOf(mAmount).longValue());


                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_ACCEPTED) {
                    mCustomProgressDialog.dismissDialog();
                    Toast.makeText(getActivity(), mWithdrawMoneyResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    SecuritySettingsActivity.otpDuration = mWithdrawMoneyResponse.getOtpValidFor();
                    launchOTPVerification();

                } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_EXPIRED) {
                    mCustomProgressDialog.dismissDialog();
                    Toast.makeText(getActivity(), mWithdrawMoneyResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    SecuritySettingsActivity.otpDuration = mWithdrawMoneyResponse.getOtpValidFor();
                    launchOTPVerification();
                } else {
                    if (getActivity() != null) {
                        if (mOTPVerificationForTwoFactorAuthenticationServicesDialog == null) {
                            mCustomProgressDialog.showFailureAnimationAndMessage(mWithdrawMoneyResponse.getMessage());
                        } else {
                            if (mCustomProgressDialog != null) {
                                mCustomProgressDialog.showFailureAnimationAndMessage(mWithdrawMoneyResponse.getMessage());
                            } else {
                                Toast.makeText(mContext, mWithdrawMoneyResponse.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }

                        if (mWithdrawMoneyResponse.getMessage().toLowerCase().contains(TwoFactorAuthConstants.WRONG_OTP)) {
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

                    //Google Analytic event
                    Utilities.sendFailedEventTracker(mTracker, "Withdraw Money", ProfileInfoCacheManager.getAccountId(), mWithdrawMoneyResponse.getMessage(), Double.valueOf(mAmount).longValue());
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (mOTPVerificationForTwoFactorAuthenticationServicesDialog != null) {
                    mOTPVerificationForTwoFactorAuthenticationServicesDialog.dismissDialog();
                }
                Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage());
                mCustomProgressDialog.showFailureAnimationAndMessage(getResources().getString(R.string.service_not_available));
            }

            mProgressDialog.dismiss();
            mWithdrawMoneyTask = null;

        }
    }
}