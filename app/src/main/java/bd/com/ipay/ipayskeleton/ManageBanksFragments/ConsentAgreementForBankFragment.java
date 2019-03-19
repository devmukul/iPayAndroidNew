package bd.com.ipay.ipayskeleton.ManageBanksFragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ManageBanksActivity;
import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.Api.DocumentUploadApi.UploadChequebookCoverAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.AddBankRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.AddBankResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Documents.UploadDocumentResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class ConsentAgreementForBankFragment extends BaseFragment implements HttpResponseListener {

    private HttpRequestPostAsyncTask mAddBankTask = null;
    private AddBankResponse mAddBankResponse;
    private HttpRequestPostAsyncTask mAddConcentTask = null;
    private UploadChequebookCoverAsyncTask mUploadCheckbookCovorAsyncTask;
    private CustomProgressDialog mProgressDialog;

    private TextView mAccountNameTextView;
    private TextView mBankNameTextView;
    private TextView mBranchNameTextView;
    private TextView mBankAccountNumberTextView;

    private Button mAgreeButton;
    private Button mDisagreeButton;

    private String mAccountName;
    private String mBankName;
    private String mBranchName;
    private String mBranchRouteNo;
    private String mBankAccountNumber;

    private String mDocType;
    private String[] mImageUrl;

    private Long mBankAccountId;

    private boolean startedFromProfileCompletion = false;
    private boolean isSwitchedFromOnBoard = false;
    private boolean isSwitchedUnconcentedList = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_bank_agreement, container, false);
        getActivity().setTitle(R.string.bank_consent_agreement);
        isSwitchedFromOnBoard = false;

        Bundle bundle = getArguments();
        if (bundle != null) {
            try {
                isSwitchedFromOnBoard = bundle.getBoolean(Constants.FROM_ON_BOARD);
            } catch (Exception e) {

            }
        }

        initializeViews(view);
        initializeBankInfo(getArguments());
        populateViews();
        setOnClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_consent_agreement_for_bank));
    }

    private void setOnClickListeners() {
        mAgreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSwitchedUnconcentedList) {
                    attemptAddBank(mBranchRouteNo, 0,
                            mAccountName, mBankAccountNumber);
                }else {
                    performConcentAgreement(mBankAccountId);
                }
            }
        });

        mDisagreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void populateViews() {
        mAccountNameTextView.setText(mAccountName);
        mBankNameTextView.setText(mBankName);
        mBranchNameTextView.setText(mBranchName);
        mBankAccountNumberTextView.setText(mBankAccountNumber);
    }

    private void initializeBankInfo(Bundle bundle) {
        mBankName = bundle.getString(Constants.BANK_NAME);
        mBranchName = bundle.getString(Constants.BANK_BRANCH_NAME);;
        mBranchRouteNo = bundle.getString(Constants.BANK_BRANCH_ROUTE_NO);
        mBankAccountNumber = bundle.getString(Constants.BANK_ACCOUNT_NUMBER);
        mAccountName = bundle.getString(Constants.BANK_ACCOUNT_NAME);
        mDocType = bundle.getString(Constants.DOCUMENT_TYPE);
        mImageUrl = bundle.getStringArray(Constants.PHOTO_URI);
        mBankAccountId = bundle.getLong(Constants.BANK_ACCOUNT_ID);

        isSwitchedUnconcentedList = bundle.getBoolean(Constants.IS_STARTED_FROM_UNCONCENTED_LIST);
        startedFromProfileCompletion = bundle.getBoolean(Constants.IS_STARTED_FROM_PROFILE_COMPLETION);
    }

    private void initializeViews(View view) {
        mProgressDialog = new CustomProgressDialog(getActivity());
        mAccountNameTextView = (TextView) view.findViewById(R.id.bank_account_name);
        mBankNameTextView = (TextView) view.findViewById(R.id.bank_name);
        mBranchNameTextView = (TextView) view.findViewById(R.id.bank_branch_name);
        mBankAccountNumberTextView = (TextView) view.findViewById(R.id.bank_account_number);
        mAgreeButton = (Button) view.findViewById(R.id.button_agree);
        mDisagreeButton = (Button) view.findViewById(R.id.button_disagree);
    }

    private void attemptAddBank(String branchRoutingNumber, int accountType, String accountName, String accountNumber) {
        mProgressDialog.show();
        AddBankRequest mAddBankRequest = new AddBankRequest(branchRoutingNumber, accountType, accountName, accountNumber);
        Gson gson = new Gson();
        String json = gson.toJson(mAddBankRequest);
        mAddBankTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ADD_A_BANK,
                Constants.BASE_URL_MM + Constants.URL_ADD_A_BANK, json, getActivity(), false);
        mAddBankTask.mHttpResponseListener = this;
        mAddBankTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void performIdentificationDocumentUpload(long bankId) {
        final String url;
        mProgressDialog.show();
        url = Constants.BASE_URL_MM + Constants.URL_CHECKBOOK_COVOR_UPLOAD;
        mUploadCheckbookCovorAsyncTask = new UploadChequebookCoverAsyncTask(Constants.COMMAND_UPLOAD_DOCUMENT, url, getContext(), mDocType, mImageUrl, ConsentAgreementForBankFragment.this, bankId);
        mUploadCheckbookCovorAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void performConcentAgreement(long bankId) {
        final String url;

        url = Constants.BASE_URL_MM + Constants.URL_CONCENT_UPLOAD+"/"+bankId+"/consent?consent=true";
        mAddConcentTask = new HttpRequestPostAsyncTask(Constants.COMMAND_UPDATE_CONCENT,
                url , null, getActivity(), false);
        mAddConcentTask.mHttpResponseListener = this;
        mAddConcentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mAddBankTask = null;
            return;
        }

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_ADD_A_BANK)) {

            try {
                mAddBankResponse = gson.fromJson(result.getJsonString(), AddBankResponse.class);
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                    ProfileInfoCacheManager.addSourceOfFund(true);
                    if (mImageUrl != null) {
                        performIdentificationDocumentUpload(mAddBankResponse.getId());
                    } else {
                        mProgressDialog.dismiss();
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), mAddBankResponse.getMessage(), Toast.LENGTH_LONG);
                        if (isSwitchedFromOnBoard) {
                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } else if (!isSwitchedFromOnBoard) {
                            ((ManageBanksActivity) getActivity()).switchToBankAccountsFragment();
                        } else
                            Toaster.makeText(getActivity(), R.string.bank_successfully_placed_for_verification, Toast.LENGTH_LONG);
                    }

                } else {
                    mProgressDialog.dismiss();
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), mAddBankResponse.getMessage(), Toast.LENGTH_SHORT);
                }

            } catch (Exception e) {
                mProgressDialog.dismiss();
                e.printStackTrace();
            }
            mAddBankTask = null;

        } else if (result.getApiCommand().equals(Constants.COMMAND_UPLOAD_DOCUMENT)) {

            mProgressDialog.dismiss();

            UploadDocumentResponse uploadDocumentResponse = gson.fromJson(result.getJsonString(), UploadDocumentResponse.class);
            if (getActivity() != null)
                Toaster.makeText(getActivity(), uploadDocumentResponse.getMessage(), Toast.LENGTH_LONG);
            if (isSwitchedFromOnBoard) {
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else if (!isSwitchedFromOnBoard) {
                ((ManageBanksActivity) getActivity()).switchToBankAccountsFragment();
            } else
                Toaster.makeText(getActivity(), R.string.bank_successfully_placed_for_verification, Toast.LENGTH_LONG);

        } else if (result.getApiCommand().equals(Constants.COMMAND_UPDATE_CONCENT)) {

            mProgressDialog.dismiss();

            UploadDocumentResponse uploadDocumentResponse = gson.fromJson(result.getJsonString(), UploadDocumentResponse.class);
            if (getActivity() != null)
                Toaster.makeText(getActivity(), uploadDocumentResponse.getMessage(), Toast.LENGTH_LONG);
            if (isSwitchedFromOnBoard) {
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else if (!isSwitchedFromOnBoard) {
                ((ManageBanksActivity) getActivity()).switchToBankAccountsFragment();
            } else
                Toaster.makeText(getActivity(), R.string.bank_successfully_placed_for_verification, Toast.LENGTH_LONG);
        }
    }
}