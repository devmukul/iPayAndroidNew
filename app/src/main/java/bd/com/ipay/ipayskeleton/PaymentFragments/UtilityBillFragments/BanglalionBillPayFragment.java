package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.AddMoneyActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.AddMoneyReviewActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.CardPaymentWebViewActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.TransactionDetailsActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.ResourceApi.GetAvailableBankAsyncTask;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.CustomView.AbstractSelectorView;
import bd.com.ipay.ipayskeleton.CustomView.BankSelectorView;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.BanglalionPackageSelectorDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomSelectorDialogWithIcon;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.MerchantBranchSelectorDialog;
import bd.com.ipay.ipayskeleton.CustomView.SelectorView;
import bd.com.ipay.ipayskeleton.CustomView.ServiceSelectorView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.GetBankListResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.BusinessRule;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.GetBusinessRuleRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.AllowablePackage;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GetCustomerInfoResponse;
import bd.com.ipay.ipayskeleton.Model.Service.IpayService;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleConstants;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Common.CommonData;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DecimalDigitsInputFilter;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class BanglalionBillPayFragment extends Fragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetCustomerInfoTask = null;
    private HttpRequestGetAsyncTask mGetBusinessRuleTask = null;
    private HttpRequestPostAsyncTask mBanglalionBillPayTask = null;

    private View mCustomerIdView;
    private View mBillPayOptionView;
    private View mUserInfoView;
    private View mPostPaidBillPayView;
    private View mPrepaidBillPayView;
    private View mPrepaidAmmountView;

    private EditText mCustomerIdEditText;
    private EditText mPostpaidAmountEditText;
    private EditText mPrepaidAmountEditText;
    private EditText mPrepaidPackageSelectEditText;

    private TextView mCustomerNameTextView;
    private TextView mPackageTypeTextView;
    private TextView mCustomerIdTextView;

    private Button mPayBillButton;

    private List<AllowablePackage> mAllowedPackage;
    private ProgressDialog mProgressDialog;

    private BanglalionPackageSelectorDialog mBanglalionPackageSelectorDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.progress_dialog_add_money_in_progress));


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_banglalion_bill_pay, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


//        if (getActivity().getIntent().getStringExtra(Constants.TAG) != null && getActivity().getIntent().getStringExtra(Constants.TAG).equalsIgnoreCase("CARD"))
//            isOnlyByCard = true;
//        else
//            isOnlyByCard = false;
//        final List<IpayService> availableAddMoneyOptions = Utilities.getAvailableAddMoneyOptions(isOnlyByCard);
//
//        mAmountEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter()});
//
//        mAddMoneyProceedButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (Utilities.isConnectionAvailable(getActivity())) {
//                    if (verifyUserInputs()) {
//                        launchReviewPage(mAddMoneyOptionSelectorView.getSelectedItem().getServiceId());
//                    }
//                } else
//                    Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
//            }
//        });
//
//        mAddMoneyOptionSelectorView.setSelectorDialogTitle(getString(R.string.add_money_from));
//        mAddMoneyOptionSelectorView.setOnItemAccessValidation(new SelectorView.OnItemAccessValidation() {
//            @Override
//            public boolean hasItemAccessAbility(int id, String name) {
//                switch (name) {
//                    case Constants.ADD_MONEY_BY_BANK_TITLE:
//                        return ACLManager.hasServicesAccessibility(ServiceIdConstants.ADD_MONEY_BY_BANK);
//                    case Constants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD_TITLE:
//                        return true;
//                    default:
//                        return false;
//                }
//            }
//        });
//
//        mAddMoneyOptionSelectorView.setItems(availableAddMoneyOptions);
//        mAddMoneyOptionSelectorView.setOnItemSelectListener(new AbstractSelectorView.OnItemSelectListener() {
//
//            @Override
//            public boolean onItemSelected(int selectedItemPosition) {
//                switch (availableAddMoneyOptions.get(selectedItemPosition).getServiceId()) {
//                    case ServiceIdConstants.ADD_MONEY_BY_BANK:
//                        setupAddMoneyFromBank();
//                        break;
//                    case ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD:
//                        setupAddMoneyFromCreditOrDebitCard();
//                        break;
//                }
//                return true;
//            }
//        });
//        if (availableAddMoneyOptions.size() == 1) {
//            mAddMoneyOptionSelectorView.selectedItem(0);
//            mAddMoneyOptionSelectorViewHolder.setVisibility(View.GONE);
//        } else {
//            mAddMoneyOptionSelectorViewHolder.setVisibility(View.VISIBLE);
//        }

        initView(view);

        mPayBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBankInformation();
            }
        });

        mPrepaidPackageSelectEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Test "+mAllowedPackage.size());
                if (mAllowedPackage.size() > 0) {
                    mBanglalionPackageSelectorDialog = new BanglalionPackageSelectorDialog(getContext(), mAllowedPackage);
                    mBanglalionPackageSelectorDialog.setOnResourceSelectedListener(new BanglalionPackageSelectorDialog.OnResourceSelectedListener() {
                        @Override
                        public void onResourceSelected(AllowablePackage allowablePackage) {
                            mPrepaidPackageSelectEditText.setText(allowablePackage.getPackageName());
                            mPrepaidAmmountView.setVisibility(View.VISIBLE);
                            mPrepaidAmountEditText.setText(allowablePackage.getAmount().toString());
                        }
                    });
                    mBanglalionPackageSelectorDialog.showDialog();
                }
            }
        });


    }

    private <T extends View> T findViewById(@IdRes int viewId) {
        //noinspection unchecked,ConstantConditions
        return (T) getView().findViewById(viewId);
    }

    private void initView(View v){
        mCustomerIdView = findViewById(R.id.customer_id_view);
        mBillPayOptionView= findViewById(R.id.bill_pay_option_selector_view_holder);
        mUserInfoView= findViewById(R.id.user_info_view_holder);
        mPostPaidBillPayView= findViewById(R.id.postpaid_bill_view_holder);
        mPrepaidBillPayView= findViewById(R.id.prepaid_package_selector_view_holder);
        mPrepaidAmmountView= findViewById(R.id.package_amount_view);

        mCustomerIdEditText= findViewById(R.id.customer_id_edit_text);
        mPostpaidAmountEditText= findViewById(R.id.postpaid_amount_edit_text);
        mPrepaidAmountEditText= findViewById(R.id.prepaid_amount_edit_text);
        mPrepaidPackageSelectEditText= findViewById(R.id.package_selector_edit_text);

        mCustomerNameTextView= findViewById(R.id.name_text_view);
        mPackageTypeTextView= findViewById(R.id.package_type_text_view);
        mCustomerIdTextView= findViewById(R.id.acount_id_text_view);

        mPayBillButton= findViewById(R.id.bill_pay_button);
    }

    private void showGetVerifiedDialog() {
        MaterialDialog.Builder dialog = new MaterialDialog.Builder(getActivity());
        dialog
                .content(R.string.get_verified)
                .cancelable(false)
                .content(getString(R.string.can_not_add_money_from_bank_if_not_verified))
                .positiveText(R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });

        dialog.show();
    }

    private void getBankInformation() {
        getBankList();
    }

    private void getBankList() {
        if (mGetCustomerInfoTask != null) {
            return;
        }

        mProgressDialog.setMessage(getString(R.string.progress_dialog_fetching_bank_info));
        mProgressDialog.show();
        mGetCustomerInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BANK_LIST,
                Constants.BASE_URL_UTILITY + Constants.URL_GET_CUSTOMER_INFO +mCustomerIdEditText.getText().toString(), getActivity(), false);
        mGetCustomerInfoTask.mHttpResponseListener = this;

        mGetCustomerInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void attemptRefreshAvailableBankNames() {
        GetAvailableBankAsyncTask mGetAvailableBankAsyncTask = new GetAvailableBankAsyncTask(getActivity(),
                new GetAvailableBankAsyncTask.BankLoadListener() {
                    @Override
                    public void onLoadSuccess() {
                        mProgressDialog.dismiss();
                        getBankList();
                    }

                    @Override
                    public void onLoadFailed() {
                        if (getActivity() != null) {
                            Toaster.makeText(getActivity(), R.string.failed_available_bank_list_loading, Toast.LENGTH_LONG);
                            getActivity().finish();
                        }
                    }
                });
        mProgressDialog.setMessage(getActivity().getString(R.string.progress_dialog_fetching_bank_list));
        mProgressDialog.show();
        mGetAvailableBankAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void attemptGetBusinessRule(int serviceID) {
        if (mGetBusinessRuleTask != null) {
            return;
        }

        String mUri = new GetBusinessRuleRequestBuilder(serviceID).getGeneratedUri();
        mGetBusinessRuleTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BUSINESS_RULE,
                mUri, getActivity(), this, false);

        mGetBusinessRuleTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

//    private boolean verifyUserInputs() {
//        final boolean shouldProceed;
//        final View focusView;
//        clearAllErrorMessage();
//
//        if (mAddMoneyOptionSelectorView.getSelectedItemPosition() == -1) {
//            focusView = null;
//            mAddMoneyOptionSelectorView.setError(R.string.choose_add_money_option);
//            shouldProceed = false;
//        } else if (!Utilities.isValueAvailable(AddMoneyActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT())
//                || !Utilities.isValueAvailable(AddMoneyActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT())) {
//            DialogUtils.showDialogForBusinessRuleNotAvailable(getActivity());
//            return false;
//        } else if (AddMoneyActivity.mMandatoryBusinessRules.isVERIFICATION_REQUIRED() && !ProfileInfoCacheManager.isAccountVerified()) {
//            DialogUtils.showDialogVerificationRequired(getActivity());
//            return false;
//        } else if (mAddMoneyOptionSelectorView.getSelectedItem().getServiceId() == ServiceIdConstants.ADD_MONEY_BY_BANK && mBankSelectorView.getSelectedItemPosition() == -1) {
//            focusView = null;
//            mBankSelectorView.setError(R.string.select_a_bank);
//            shouldProceed = false;
//        } else if (!isValidAmount()) {
//            focusView = mAmountEditText;
//            shouldProceed = false;
//        } else {
//            focusView = null;
//            shouldProceed = true;
//        }
//
//        if (focusView != null) {
//            focusView.requestFocus();
//        }
//        return shouldProceed;
//    }

//    private boolean isValidAmount() {
//        boolean isValidAmount;
//        String errorMessage;
//
//        if (TextUtils.isEmpty(mAmountEditText.getText())) {
//            errorMessage = getString(R.string.please_enter_amount);
//        } else if (!InputValidator.isValidDigit(mAmountEditText.getText().toString().trim())) {
//            errorMessage = getString(R.string.please_enter_amount);
//        } else {
//            errorMessage = InputValidator.isValidAmount(getActivity(), new BigDecimal(mAmountEditText.getText().toString()),
//                    AddMoneyActivity.mMandatoryBusinessRules.getMIN_AMOUNT_PER_PAYMENT(),
//                    AddMoneyActivity.mMandatoryBusinessRules.getMAX_AMOUNT_PER_PAYMENT());
//        }
//
//        if (errorMessage != null) {
//            isValidAmount = false;
//            mAmountEditText.setError(errorMessage);
//        } else {
//            isValidAmount = true;
//        }
//        return isValidAmount;
//    }

//    private void clearAllErrorMessage() {
//        mAmountEditText.setError(null);
//        mAddMoneyOptionSelectorView.setError(null);
//        mBankSelectorView.setError(null);
//        mNoteEditText.setError(null);
//    }

//    private void launchReviewPage(final int addMonerServiceId) {
//        final String amount = mAmountEditText.getText().toString().trim();
//        final String description = mNoteEditText.getText().toString().trim();
//
//        Intent intent = new Intent(getActivity(), AddMoneyReviewActivity.class);
//        intent.putExtra(Constants.AMOUNT, Double.parseDouble(amount));
//        intent.putExtra(Constants.DESCRIPTION_TAG, description);
//
//
//        switch (addMonerServiceId) {
//            case ServiceIdConstants.ADD_MONEY_BY_BANK:
//                // Adding the type is by bank
//                intent.putExtra(Constants.ADD_MONEY_TYPE, Constants.ADD_MONEY_TYPE_BY_BANK);
//
//                // Adding the info of the selected bank
//                BankAccountList selectedBankAccount = mListUserBankClasses.get(mBankSelectorView.getSelectedItemPosition());
//                intent.putExtra(Constants.SELECTED_BANK_ACCOUNT, selectedBankAccount);
//                break;
//            case ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD:
//                // Adding the type is by credit/debit card
//                intent.putExtra(Constants.ADD_MONEY_TYPE, Constants.ADD_MONEY_TYPE_BY_CREDIT_OR_DEBIT_CARD);
//                break;
//        }
//        startActivityForResult(intent, AddMoneyActivity.ADD_MONEY_REVIEW_REQUEST);
//    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == Activity.RESULT_OK && requestCode == AddMoneyActivity.ADD_MONEY_REVIEW_REQUEST) {
//            if (data != null && data.hasExtra(Constants.CARD_TRANSACTION_DATA)) {
//                final int addMoneyByCreditOrDebitCardStatus = data.getBundleExtra(Constants.CARD_TRANSACTION_DATA).getInt(Constants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD_STATUS, 0);
//                final Intent intent;
//                if (addMoneyByCreditOrDebitCardStatus == CardPaymentWebViewActivity.CARD_TRANSACTION_SUCCESSFUL) {
//                    if (((AddMoneyActivity) getActivity()).FROM_ON_BOARD) {
//                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.money_added_successfully), Toast.LENGTH_LONG).show();
//                        intent = new Intent(getActivity(), HomeActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
//                    } else {
//                        final String transactionId = data.getBundleExtra(Constants.CARD_TRANSACTION_DATA).getString(Constants.TRANSACTION_ID);
//                        intent = new Intent(getActivity(), TransactionDetailsActivity.class);
//                        intent.putExtra(Constants.STATUS, Constants.PAYMENT_REQUEST_STATUS_ALL);
//                        intent.putExtra(Constants.MONEY_REQUEST_ID, transactionId);
//                        startActivity(intent);
//                    }
//                }
//
//            }
//            if (getActivity() != null)
//                getActivity().finish();
//        }
//    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mGetCustomerInfoTask = null;
            mGetBusinessRuleTask = null;

            return;
        }

        if (isAdded()) mProgressDialog.dismiss();

        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_GET_BANK_LIST:
                GetCustomerInfoResponse mCustomerInfoResponse;
                switch (result.getStatus()) {
                    case Constants.HTTP_RESPONSE_STATUS_OK:
                        try {
                            mCustomerInfoResponse = gson.fromJson(result.getJsonString(), GetCustomerInfoResponse.class);
                            mAllowedPackage = mCustomerInfoResponse.getAllowablePackages();
                            System.out.println("Test "+mAllowedPackage.size());

                            mCustomerIdView.setVisibility(View.GONE);
                            mBillPayOptionView.setVisibility(View.VISIBLE);
                            mUserInfoView.setVisibility(View.VISIBLE);
                            if(mCustomerInfoResponse.getUserType().equals("POSTPAID")){
                                mPostPaidBillPayView.setVisibility(View.VISIBLE);
                                mPrepaidBillPayView.setVisibility(View.GONE);
                            }else {
                                mPostPaidBillPayView.setVisibility(View.GONE);
                                mPrepaidBillPayView.setVisibility(View.VISIBLE);
                            }

//                            mListUserBankClasses = new ArrayList<>();
//
//                            for (BankAccountList bank : mBankListResponse.getBankAccountList()) {
//                                if (bank.getVerificationStatus().equals(Constants.BANK_ACCOUNT_STATUS_VERIFIED)) {
//                                    mListUserBankClasses.add(bank);
//                                }
//                            }
//                            mBankSelectorView.setItems(mListUserBankClasses);
//                            mBankSelectorView.setSelectable(true);
//                            if (mBankSelectorView.isBankAdded() && mBankSelectorView.isVerifiedBankAdded()) {
//                                attemptGetBusinessRule(Constants.SERVICE_ID_ADD_MONEY_BY_BANK);
//                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (getActivity() != null)
                                Toaster.makeText(getActivity(), R.string.fetch_info_failed, Toast.LENGTH_LONG);
                        }
                        break;
                    default:
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), R.string.fetch_info_failed, Toast.LENGTH_LONG);
                        break;
                }
                break;
            case Constants.COMMAND_GET_BUSINESS_RULE:
                mGetBusinessRuleTask = null;
                switch (result.getStatus()) {
                    case Constants.HTTP_RESPONSE_STATUS_OK:
                        try {
                            gson = new Gson();

                            BusinessRule[] businessRuleArray = gson.fromJson(result.getJsonString(), BusinessRule[].class);

                            for (BusinessRule rule : businessRuleArray) {
                                switch (rule.getRuleID()) {
                                    case BusinessRuleConstants.SERVICE_RULE_ADD_MONEY_MAX_AMOUNT_PER_PAYMENT:
                                        AddMoneyActivity.mMandatoryBusinessRules.setMAX_AMOUNT_PER_PAYMENT(rule.getRuleValue());
                                        break;
                                    case BusinessRuleConstants.SERVICE_RULE_ADD_MONEY_MIN_AMOUNT_PER_PAYMENT:
                                        AddMoneyActivity.mMandatoryBusinessRules.setMIN_AMOUNT_PER_PAYMENT(rule.getRuleValue());
                                        break;
                                    case BusinessRuleConstants.SERVICE_RULE_ADD_MONEY_VERIFICATION_REQUIRED:
                                        AddMoneyActivity.mMandatoryBusinessRules.setVERIFICATION_REQUIRED(rule.getRuleValue());
                                        break;
                                    // For add money by card
                                    case BusinessRuleConstants.SERVICE_RULE_ADD_CARDMONEY_MAX_AMOUNT_SINGLE:
                                        AddMoneyActivity.mMandatoryBusinessRules.setMAX_AMOUNT_PER_PAYMENT(rule.getRuleValue());
                                        break;
                                    case BusinessRuleConstants.SERVICE_RULE_ADD_CARDMONEY_MIN_AMOUNT_SINGLE:
                                        AddMoneyActivity.mMandatoryBusinessRules.setMIN_AMOUNT_PER_PAYMENT(rule.getRuleValue());
                                        break;
                                    case BusinessRuleConstants.SERVICE_RULE_ADD_CARDMONEY_VERIFICATION_REQUIRED:
                                        AddMoneyActivity.mMandatoryBusinessRules.setVERIFICATION_REQUIRED(rule.getRuleValue());
                                        break;
                                    // For all types of add money
                                    case BusinessRuleConstants.SERVICE_RULE_ADD_MONEY_PIN_REQUIRED:
                                        AddMoneyActivity.mMandatoryBusinessRules.setPIN_REQUIRED(rule.getRuleValue());
                                        break;
                                }
                            }

//                            switch (mAddMoneyOptionSelectorView.getSelectedItem().getServiceId()) {
//                                case ServiceIdConstants.ADD_MONEY_BY_BANK:
//                                    BusinessRuleCacheManager.setBusinessRules(Constants.ADD_MONEY_BY_BANK, AddMoneyActivity.mMandatoryBusinessRules);
//                                    break;
//                                case ServiceIdConstants.ADD_MONEY_BY_CREDIT_OR_DEBIT_CARD:
//                                    BusinessRuleCacheManager.setBusinessRules(Constants.ADD_MONEY_BY_CARD, AddMoneyActivity.mMandatoryBusinessRules);
//                                    break;
//                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                if (getActivity() != null)
                    Toaster.makeText(getActivity(), R.string.fetch_info_failed, Toast.LENGTH_LONG);
                break;
        }
    }
}