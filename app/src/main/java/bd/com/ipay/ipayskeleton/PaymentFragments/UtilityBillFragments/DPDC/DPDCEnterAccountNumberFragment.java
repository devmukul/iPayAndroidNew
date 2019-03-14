package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.DPDC;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.OTPVerificationForTwoFactorAuthenticationServicesDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.BusinessRuleV2;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.MandatoryBusinessRules;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.Rule;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.DPDCUserInfoGetRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.DescoBillPayRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.DescoBillPayResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.DescoCustomerInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.DpdcCustomerInfoResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleConstants;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;


public class DPDCEnterAccountNumberFragment extends BaseFragment implements HttpResponseListener, DatePickerDialog.OnDateSetListener {
    private EditText mAccountIDEditText;
    private EditText mLocationCodeEditText;
    private EditText mMonthEditText;
    private Button mContinueButton;
    private CustomProgressDialog mProgressDialog;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
    private OTPVerificationForTwoFactorAuthenticationServicesDialog mOTPVerificationForTwoFactorAuthenticationServicesDialog;

    private String mUri;
    private String mBillNumber;
    private String mLocationCode;
    private String mBillMonth;
    private String mBillYear;
    private String mAmount;

    private HttpRequestPostAsyncTask mDpdcCustomerInfoTask = null;
    private DpdcCustomerInfoResponse mDpdcCustomerInfoResponse;
    private DPDCUserInfoGetRequest mDpdcUserInfoGetRequest;
    private HttpRequestGetAsyncTask mGetBusinessRuleTask;
    private AnimatedProgressDialog mCustomProgressDialog;

    private DatePickerDialog mDatePickerDialog;

    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dpdc_bill_payment, container, false);
        getActivity().setTitle(getString(R.string.dpdc));
        attemptGetBusinessRule(ServiceIdConstants.UTILITY_BILL_PAYMENT);
        mProgressDialog = new CustomProgressDialog(getContext());
        mCustomProgressDialog = new AnimatedProgressDialog(getContext());
        setUpView(view);
        return view;
    }

    private void setUpView(View view) {
        if (UtilityBillPaymentActivity.mMandatoryBusinessRules == null) {
            UtilityBillPaymentActivity.mMandatoryBusinessRules = new MandatoryBusinessRules(Constants.UTILITY_BILL_PAYMENT);
        }

        mAccountIDEditText = view.findViewById(R.id.customer_id_edit_text);
        mLocationCodeEditText = view.findViewById(R.id.location_code_edit_text);
        mMonthEditText = view.findViewById(R.id.month_edit_text);
        mContinueButton = view.findViewById(R.id.continue_button);
        mDatePickerDialog = initDatePickerDialog(getActivity(), this, false);
        UtilityBillPaymentActivity.mMandatoryBusinessRules = BusinessRuleCacheManager.getBusinessRules(Constants.UTILITY_BILL_PAYMENT);
        setUpButtonAction();
    }

    private void setUpButtonAction() {
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utilities.isConnectionAvailable(getContext())) {
                    if (verifyUserInput()) {
                        getCustomerInfo();
                    }

                } else {
                    Toast.makeText(getContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                }
            }
        });

        mMonthEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePickerDialog.show();
            }
        });
    }

    private void attemptGetBusinessRule(int serviceID) {
        if (mGetBusinessRuleTask != null) {
            return;
        }
        mGetBusinessRuleTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BUSINESS_RULE,
                Constants.BASE_URL_SM + Constants.URL_BUSINESS_RULE_V2 + "/" + serviceID, getActivity(), false);
        mGetBusinessRuleTask.mHttpResponseListener = this;
        mGetBusinessRuleTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getCustomerInfo() {
        if (mDpdcCustomerInfoTask != null) {
            return;
        } else {
            mUri = Constants.BASE_URL_UTILITY + Constants.URL_DPDC_CUSTOMER_INFO;
            mDpdcUserInfoGetRequest = new DPDCUserInfoGetRequest(mBillNumber, mLocationCode, mBillMonth, mBillYear);
            String mJsonString = new Gson().toJson(mDpdcUserInfoGetRequest);
            mDpdcCustomerInfoTask = new HttpRequestPostAsyncTask(Constants.COMMAND_GET_DPDC_CUSTOMER, mUri, mJsonString,
                    getActivity(), this, true);
            mDpdcCustomerInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mProgressDialog.show();
        }
    }

    private boolean verifyUserInput() {
        mBillNumber = mAccountIDEditText.getText().toString();
        mLocationCode = mLocationCodeEditText.getText().toString();
        String month = mMonthEditText.getText().toString();
        if (mBillNumber == null || mBillNumber.isEmpty()) {
            mAccountIDEditText.setError(getString(R.string.enter_account_number));
            return false;
        } else if(mLocationCode == null || mLocationCode.isEmpty()){
            mLocationCodeEditText.setError(getString(R.string.enter_location_code));
            return false;
        } else if(month == null || month.isEmpty()){
            mMonthEditText.setError(getString(R.string.select_bill_month));
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFoundWithout404(result, getContext(), mCustomProgressDialog)) {
            mProgressDialog.dismiss();
            mCustomProgressDialog.dismissDialog();
            mDpdcCustomerInfoTask = null;
            mGetBusinessRuleTask = null;
            if (result != null && result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
                try {
                    GenericResponseWithMessageOnly genericResponseWithMessageOnly = new Gson().
                            fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                    Utilities.showErrorDialog(getContext(), genericResponseWithMessageOnly.getMessage());
                } catch (Exception e) {
                    Utilities.showErrorDialog(getContext(), getString(R.string.not_found));
                }
            }
            return;
        } else {
            try {
                mProgressDialog.dismiss();
                Gson gson = new Gson();
                if (result.getApiCommand().equals(Constants.COMMAND_GET_DPDC_CUSTOMER)) {
                    mDpdcCustomerInfoResponse = gson.fromJson(result.getJsonString(), DpdcCustomerInfoResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        Utilities.hideKeyboard(getActivity());
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.BILL_MONTH, mBillMonth);
                        bundle.putString(Constants.BILL_YEAR, mBillYear);
                        bundle.putString(Constants.LOCATION_CODE, mLocationCode);

                        bundle.putString(Constants.BILL_NUMBER, mDpdcCustomerInfoResponse.getBillNumber());
                        bundle.putString(Constants.DUE_DATE, mDpdcCustomerInfoResponse.getDueDate());
                        bundle.putString(Constants.ACCOUNT_ID, mDpdcCustomerInfoResponse.getAccountNumber());
                        bundle.putSerializable(Constants.BILL_AMOUNT, numberFormat.parse(mDpdcCustomerInfoResponse.getBillAmount()));
                        if (mDpdcCustomerInfoResponse.getStampAmount() != null && Integer.parseInt(mDpdcCustomerInfoResponse.getStampAmount()) != 0) {
                            bundle.putSerializable(Constants.STAMP_AMOUNT, numberFormat.parse(mDpdcCustomerInfoResponse.getStampAmount()));
                        }
                        bundle.putSerializable(Constants.VAT_AMOUNT, numberFormat.parse(mDpdcCustomerInfoResponse.getVatAmount()));
                        bundle.putSerializable(Constants.TOTAL_AMOUNT, numberFormat.parse(mDpdcCustomerInfoResponse.getTotalAmount()));
                        ((UtilityBillPaymentActivity) getActivity()).switchToDPDCBillInfoFragment(bundle);
                    } else {
                        if (!TextUtils.isEmpty(mDpdcCustomerInfoResponse.getMessage())) {
                            Utilities.showErrorDialog(getContext(), mDpdcCustomerInfoResponse.getMessage());
                        } else {
                            Utilities.showErrorDialog(getContext(), getString(R.string.not_found));

                        }
                    }
                    mDpdcCustomerInfoTask = null;
                } else if (result.getApiCommand().equals(Constants.COMMAND_GET_BUSINESS_RULE)) {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        gson = new Gson();
                        BusinessRuleV2 businessRuleArray = gson.fromJson(result.getJsonString(), BusinessRuleV2.class);
                        List<Rule> rules = businessRuleArray.getRules();

                        for (Rule rule : rules) {
                            switch (rule.getRuleName()) {
                                case BusinessRuleConstants.SERVICE_RULE_UTILITY_BILL_PAYMENT_MAX_AMOUNT_PER_PAYMENT:
                                    UtilityBillPaymentActivity.mMandatoryBusinessRules.setMAX_AMOUNT_PER_PAYMENT(rule.getRuleValue());
                                    break;
                                case BusinessRuleConstants.SERVICE_RULE_UTILITY_BILL_PAYMENT_MIN_AMOUNT_PER_PAYMENT:
                                    UtilityBillPaymentActivity.mMandatoryBusinessRules.setMIN_AMOUNT_PER_PAYMENT(rule.getRuleValue());
                                    break;
                                case BusinessRuleConstants.SERVICE_RULE_UTILITY_BILL_PAYMENT_VERIFICATION_REQUIRED:
                                    UtilityBillPaymentActivity.mMandatoryBusinessRules.setVERIFICATION_REQUIRED(rule.getRuleValue());
                                    break;
                                case BusinessRuleConstants.SERVICE_RULE_UTILITY_BILL_PAYMENT_PIN_REQUIRED:
                                    UtilityBillPaymentActivity.mMandatoryBusinessRules.setPIN_REQUIRED(rule.getRuleValue());
                                    break;
                            }

                            BusinessRuleCacheManager.setBusinessRules(Constants.UTILITY_BILL_PAYMENT, UtilityBillPaymentActivity.mMandatoryBusinessRules);
                        }
                    }
                    mGetBusinessRuleTask = null;
                }
            } catch (Exception e) {
                mProgressDialog.dismiss();
                mDpdcCustomerInfoTask = null;
                mGetBusinessRuleTask = null;
                Utilities.showErrorDialog(getContext(), getString(R.string.request_failed));
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

        final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        mBillMonth = getFormattedDate(month+1);
        mBillYear = String.valueOf(year);
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM, yyyy", Locale.getDefault());
        mMonthEditText.setError(null);
        mMonthEditText.setText(simpleDateFormat.format(calendar.getTime()));
    }

    private String getFormattedDate(int value) {
        if(value<10) {
            return String.format(Locale.US,"0%d",value);
        } else {
            return Integer.toString(value);
        }
    }

    public static com.tsongkha.spinnerdatepicker.DatePickerDialog initDatePickerDialog(Context context, com.tsongkha.spinnerdatepicker.DatePickerDialog.OnDateSetListener onDateSetListener, boolean showDay) {
        final Calendar calendar = Calendar.getInstance();
        int year, month, day;
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        SpinnerDatePickerDialogBuilder spinnerDatePickerDialogBuilder = new SpinnerDatePickerDialogBuilder()
                .context(context)
                .spinnerTheme(R.style.NumberPickerStyle)
                .callback(onDateSetListener)
                .showTitle(true)
                .showDaySpinner(true)
                .defaultDate(year, month, day)
                .showDaySpinner(showDay)
                .maxDate(year, month, day);
        return spinnerDatePickerDialogBuilder.build();
    }


}

