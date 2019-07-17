package bd.com.ipay.ipayskeleton.CustomView.Dialogs;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.AddMoneyByBracBank;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TwoFA.TwoFactorAuthSettingsSaveResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.Bank.IPayAddMoneyFromBankSuccessFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BracBankAddMoneyServicesAsynctaskMap;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class BracBankPinDialog extends DialogFragment implements HttpResponseListener {
    private HttpRequestPostAsyncTask mHttpPostAsyncTask;

    private String transactionId;
    private PinView mOTPEditText;
    private Button mActivateButton;
    private AnimatedProgressDialog mCustomProgressDialog;
    protected BankAccountList bankAccountList;


    protected Number transactionAmount;

    public static String TAG = "FullScreenDialog";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);

        if (getArguments() != null) {
            transactionId = getArguments().getString(Constants.TRANSACTION_ID);
            transactionAmount = (Number) getArguments().getSerializable(Constants.TRANSACTION_AMOUNT_KEY);
            bankAccountList = getArguments().getParcelable(Constants.SELECTED_BANK_ACCOUNT);
        }
        mCustomProgressDialog = new AnimatedProgressDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_barc_bank_otp, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        initializeView(view);
        return view;
    }

    private void initializeView(View view) {

        mOTPEditText = view.findViewById(R.id.otp_edittext);
        mActivateButton = view.findViewById(R.id.confirm_pin);

        mCustomProgressDialog = new AnimatedProgressDialog(getContext());
        setButtonActions();

    }

    private void setButtonActions() {
        mActivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiding the keyboard after verifying OTP
                Utilities.hideKeyboard(getContext(), v);
                if (Utilities.isConnectionAvailable(getContext())) {
                    verifyInput();
                }
                else if (getContext() != null)
                    Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void verifyInput() {
        boolean cancel = false;
        View focusView = null;

        String mOTP = mOTPEditText.getText().toString().trim();

        String errorMessage = InputValidator.isValidOTP(getContext(), mOTP);
        if (errorMessage != null) {
            mOTPEditText.setError(errorMessage);
            focusView = mOTPEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mOTP = mOTPEditText.getText().toString().trim();
            attemptDesiredRequestWithOTP(mOTP);
        }
    }

    private void attemptDesiredRequestWithOTP(String otp) {

        if (mHttpPostAsyncTask != null) {
            return;
        }
        if (mCustomProgressDialog != null) {
            mCustomProgressDialog.setTitle(R.string.please_wait_no_ellipsis);
        }
        mCustomProgressDialog.showDialog();
        AddMoneyByBracBank addMoneyByBracBank = new AddMoneyByBracBank(transactionId, otp);
        String jsons = new Gson().toJson(addMoneyByBracBank);
        mHttpPostAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_MONEY_FROM_BANK, Constants.BASE_URL_SM +"add-money/brac/confirm", jsons, getContext(), false);
        mHttpPostAsyncTask.mHttpResponseListener = this;
        mHttpPostAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void hideOtpDialog() {
        if (getDialog().isShowing()) {
            getDialog().cancel();
        }
    }

    public void showOtpDialog() {
        if (!getDialog().isShowing()) {
            getDialog().show();
        }
    }

    public void dismissDialog() {
        getDialog().dismiss();
    }



    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), mCustomProgressDialog)) {
            mHttpPostAsyncTask = null;
            getDialog().dismiss();
            return;
        } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
            TwoFactorAuthSettingsSaveResponse twoFactorAuthSettingsSaveResponse =
                    new Gson().fromJson(result.getJsonString(), TwoFactorAuthSettingsSaveResponse.class);
            mCustomProgressDialog.setTitle(R.string.success);
            mCustomProgressDialog.showSuccessAnimationAndMessage(twoFactorAuthSettingsSaveResponse.getMessage());

        } else if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_PROCESSING) {
            TwoFactorAuthSettingsSaveResponse twoFactorAuthSettingsSaveResponse =
                    new Gson().fromJson(result.getJsonString(), TwoFactorAuthSettingsSaveResponse.class);
            mCustomProgressDialog.setTitle(R.string.success);
            mCustomProgressDialog.showSuccessAnimationAndMessage(twoFactorAuthSettingsSaveResponse.getMessage());
        }

        mCustomProgressDialog.dismissDialog();
        mHttpPostAsyncTask = null;
        if (getActivity() != null)
            Utilities.hideKeyboard(getActivity());
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.TRANSACTION_AMOUNT_KEY, transactionAmount);
        bundle.putParcelable(Constants.SELECTED_BANK_ACCOUNT, bankAccountList);
        if (getActivity() instanceof IPayTransactionActionActivity)
            ((IPayTransactionActionActivity) getActivity()).switchFragment(new IPayAddMoneyFromBankSuccessFragment(), bundle, 3, true);
        dismissDialog();
    }

}