package bd.com.ipay.ipayskeleton.ManageBanksFragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ManageBanksActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestDeleteAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.ResourceApi.GetAvailableBankAsyncTask;
import bd.com.ipay.ipayskeleton.CustomView.CustomSwipeRefreshLayout;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomSelectorDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.GetBankListResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.RemoveBankAccountResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.VerifyBankWithAmountRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.VerifyBankWithAmountRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.VerifyBankWithAmountResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DecimalDigitsInputFilter;
import bd.com.ipay.ipayskeleton.Utilities.InputValidator;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class BankAccountsFragment extends ProgressFragment implements HttpResponseListener {

    private HttpRequestDeleteAsyncTask mRemoveBankAccountTask = null;
    private RemoveBankAccountResponse mRemoveBankAccountResponse;

    private HttpRequestPostAsyncTask mSendForVerificationWithAmountTask = null;
    private VerifyBankWithAmountResponse mVerifyBankWithAmountResponse;

    private HttpRequestGetAsyncTask mGetBankTask = null;
    private GetBankListResponse mBankListResponse;

    private CustomProgressDialog mProgressDialog;
    private RecyclerView mBankListRecyclerView;
    private TextView mEmptyListTextView;
    private UserBankListAdapter mUserBankListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<BankAccountList> mListUserBankClasses;

    private CustomSwipeRefreshLayout mSwipeRefreshLayout;
    private Tracker mTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        attemptRefreshAvailableBankNames();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_bank_account));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_bank_accounts, container, false);
        getActivity().setTitle(R.string.bank_list);

        mSwipeRefreshLayout =  v.findViewById(R.id.swipe_refresh_layout);
        mBankListRecyclerView =  v.findViewById(R.id.list_bank);
        mEmptyListTextView =  v.findViewById(R.id.empty_list_text);
        mProgressDialog = new CustomProgressDialog(getActivity());

        mUserBankListAdapter = new UserBankListAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mBankListRecyclerView.setLayoutManager(mLayoutManager);
        mBankListRecyclerView.setAdapter(mUserBankListAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    getBankList();

                } else
                    mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentShown(false);

        // Block from adding bank if an user is not verified
        if (ProfileInfoCacheManager.getVerificationStatus().equals(Constants.ACCOUNT_VERIFICATION_STATUS_NOT_VERIFIED)) {
            mEmptyListTextView.setText(R.string.bank_list_empty_text);
            mEmptyListTextView.setVisibility(View.VISIBLE);
            return;
        }

        getBankList();
    }

    private void attemptRefreshAvailableBankNames() {
        GetAvailableBankAsyncTask mGetAvailableBankAsyncTask = new GetAvailableBankAsyncTask(getActivity(),
                new GetAvailableBankAsyncTask.BankLoadListener() {
                    @Override
                    public void onLoadSuccess() {
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

        mGetAvailableBankAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getBankList() {
        if (mGetBankTask != null) {
            return;
        }

        mGetBankTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BANK_LIST,
                Constants.BASE_URL_MM + Constants.URL_GET_BANK, getActivity(), true);
        mGetBankTask.mHttpResponseListener = this;
        mGetBankTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void attemptVerificationWithAmount(Long userBankID, double amount) {
        if (userBankID == 0) {
            if (getActivity() != null)
                Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG);
            return;
        }
        mProgressDialog.show();
        VerifyBankWithAmountRequest mVerifyBankWithAmountRequest = new VerifyBankWithAmountRequest(amount);
        Gson gson = new Gson();
        String json = gson.toJson(mVerifyBankWithAmountRequest);

        VerifyBankWithAmountRequestBuilder mVerifyBankWithAmountRequestBuilder = new VerifyBankWithAmountRequestBuilder(userBankID);
        String mUrl = mVerifyBankWithAmountRequestBuilder.getGeneratedUrl();

        mSendForVerificationWithAmountTask = new HttpRequestPostAsyncTask(Constants.COMMAND_VERIFICATION_BANK_WITH_AMOUNT, mUrl, json, getActivity(), false);
        mSendForVerificationWithAmountTask.mHttpResponseListener = this;
        mSendForVerificationWithAmountTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void attemptRemoveBank(long bankAccountID) {
        if (bankAccountID == 0) {
            if (getActivity() != null)
                Toaster.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG);
            return;
        }

        mProgressDialog.show();
        mRemoveBankAccountTask = new HttpRequestDeleteAsyncTask(Constants.COMMAND_REMOVE_A_BANK,
                Constants.BASE_URL_MM + Constants.URL_REMOVE_A_BANK + bankAccountID, getActivity(), false);
        mRemoveBankAccountTask.mHttpResponseListener = this;
        mRemoveBankAccountTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void processGetBankListResponse(String json) {

        try {
            Gson gson = new Gson();
            mBankListResponse = gson.fromJson(json, GetBankListResponse.class);

            if (mListUserBankClasses == null) {
                mListUserBankClasses = mBankListResponse.getBankAccountList();
            } else {
                List<BankAccountList> tempBankClasses;
                tempBankClasses = mBankListResponse.getBankAccountList();
                mListUserBankClasses.clear();
                mListUserBankClasses.addAll(tempBankClasses);
            }

            if (mListUserBankClasses != null && mListUserBankClasses.size() > 0)
                mEmptyListTextView.setVisibility(View.GONE);
            else mEmptyListTextView.setVisibility(View.VISIBLE);

            mUserBankListAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshBankList() {
        if (mListUserBankClasses != null)
            mListUserBankClasses.clear();
        mListUserBankClasses = null;
        getBankList();
    }

    private void showRemoveBankAccountDialog(final long bankAccountID) {
        MaterialDialog.Builder mRemoveBankDialogBuilder;
        MaterialDialog mRemoveBankDialog;
        mRemoveBankDialogBuilder = new MaterialDialog.Builder(getActivity())
                .title(R.string.remove_bank_title)
                .content(R.string.are_you_sure_to_remove_bank_account)
                .cancelable(true)
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (Utilities.isConnectionAvailable(getActivity())) {
                            attemptRemoveBank(bankAccountID);
                        }
                    }
                });
        mRemoveBankDialog = mRemoveBankDialogBuilder.build();
        mRemoveBankDialog.show();
    }

    private void showBlockedBankAccountDialog(String bankName, String bankAccountNumber, String branchName,final long bankAccountId) {
        String content = bankName + "\n" + branchName + "\n" + bankAccountNumber + "\n" + getString(R.string.blocked_bank_message);

        MaterialDialog.Builder mRemoveBankDialogBuilder;
        MaterialDialog mRemoveBankDialog;
        mRemoveBankDialogBuilder = new MaterialDialog.Builder(getActivity())
                .title(R.string.blocked_bank_title)
                .content(content)
                .cancelable(true)
                .positiveText(R.string.remove_bank)
		        .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
	                @Override
	                public void onClick(@NonNull MaterialDialog dialog,
	                                    @NonNull DialogAction which) {
	                	showRemoveBankAccountDialog(bankAccountId);
	                }
                });

        mRemoveBankDialog = mRemoveBankDialogBuilder.build();
        mRemoveBankDialog.show();
    }

    private void showVerifyBankWithAmountDialog(final long bankAccountID) {
        final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.enter_the_amount_we_sent)
                .customView(R.layout.dialog_verify_bank_with_amount, true)
                .positiveText(R.string.submit)
                .negativeText(R.string.cancel)
                .autoDismiss(false)
                .show();

        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        View view = dialog.getCustomView();
        final EditText mAmountEditText =  view.findViewById(R.id.amount);

        // Allow user to write not more than two digits after decimal point for an input of an amount
        mAmountEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(true)});

        dialog.getBuilder().onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                if (mAmountEditText.getText().toString().trim().length() == 0) {
                    mAmountEditText.setError(getString(R.string.please_enter_amount));
                    mAmountEditText.requestFocus();

                } else {
                    String amount = mAmountEditText.getText().toString().trim();
                    if (InputValidator.isValidDigit(amount)) {
                        if (Double.parseDouble(amount) <= 0) {
                            mAmountEditText.setError(getString(R.string.please_enter_amount));
                            mAmountEditText.requestFocus();
                        } else if (Double.parseDouble(amount) > 0) {
                            if (Utilities.isConnectionAvailable(getActivity())) {
                                imm.hideSoftInputFromWindow(mAmountEditText.getWindowToken(), 0);
                                attemptVerificationWithAmount(bankAccountID, Double.parseDouble(amount));
                            } else
                                Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    } else {
                        mAmountEditText.setError(getString(R.string.please_enter_amount));
                        mAmountEditText.requestFocus();
                    }

                }
            }
        });

        dialog.getBuilder().onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                imm.hideSoftInputFromWindow(mAmountEditText.getWindowToken(), 0);
                dialog.dismiss();
            }
        });
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mGetBankTask = null;
            mRemoveBankAccountTask = null;
            return;
        }

        Gson gson = new Gson();
        if (this.isAdded())
            setContentShown(true);


        switch (result.getApiCommand()) {
            case Constants.COMMAND_GET_BANK_LIST:
                try {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        processGetBankListResponse(result.getJsonString());
                    } else {
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), R.string.pending_get_failed, Toast.LENGTH_LONG);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mSwipeRefreshLayout.setRefreshing(false);
                mGetBankTask = null;

                break;
            case Constants.COMMAND_REMOVE_A_BANK:

                try {
                    mRemoveBankAccountResponse = gson.fromJson(result.getJsonString(), RemoveBankAccountResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), mRemoveBankAccountResponse.getMessage(), Toast.LENGTH_LONG);

                        // Refresh bank list
                        refreshBankList();

                    } else {
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), mRemoveBankAccountResponse.getMessage(), Toast.LENGTH_LONG);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), R.string.failed_remove_bank, Toast.LENGTH_LONG);
                }

                mProgressDialog.dismiss();
                mRemoveBankAccountTask = null;

                break;
            case Constants.COMMAND_VERIFICATION_BANK_WITH_AMOUNT:

                try {
                    mVerifyBankWithAmountResponse = gson.fromJson(result.getJsonString(), VerifyBankWithAmountResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), mVerifyBankWithAmountResponse.getMessage(), Toast.LENGTH_LONG);
                    } else {
                        if (getActivity() != null)
                            Toaster.makeText(getActivity(), mVerifyBankWithAmountResponse.getMessage(), Toast.LENGTH_LONG);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), R.string.failed_to_bank_verification, Toast.LENGTH_LONG);
                }

                // Refresh the bank list for updated bank status
                refreshBankList();

                mProgressDialog.dismiss();
                mSendForVerificationWithAmountTask = null;
                break;
        }
    }

    public class UserBankListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int FOOTER_VIEW = 1;
        private static final int BANK_LIST_ITEM_VIEW = 2;

        public UserBankListAdapter() {
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView mBankName;
            private final TextView mBankAccountNumber;
            private final ImageView mAttachmentChequebook;
            private final ImageView mBankVerifiedStatus;
            private final Button mBankPending;
            private final Button mBankUnconsented;
            private final TextView mBranchName;
            private final View divider;
            private final ImageView bankIcon;

            private CustomSelectorDialog mCustomSelectorDialog;
            private List<String> mBankActionList;

            public ViewHolder(final View itemView) {
                super(itemView);

                mBankAccountNumber =  itemView.findViewById(R.id.bank_account_number);
                mBankName =  itemView.findViewById(R.id.bank_name);
                mBankVerifiedStatus =  itemView.findViewById(R.id.bank_account_verify_status);
                mAttachmentChequebook =  itemView.findViewById(R.id.chequebook_attachment);
                mBankPending =  itemView.findViewById(R.id.bank_account_pending_button);
                mBankUnconsented =  itemView.findViewById(R.id.bank_account_unconsented_button);
                mBranchName =  itemView.findViewById(R.id.bank_branch_name);
                divider = itemView.findViewById(R.id.divider);
                bankIcon =  itemView.findViewById(R.id.portrait);
            }

            public void bindView(final int pos) {

                final long bankAccountID = mListUserBankClasses.get(pos).getBankAccountId();
                final String bankName = mListUserBankClasses.get(pos).getBankName();
                final String branchName = mListUserBankClasses.get(pos).getBranchName();
                final String verificationStatus = mListUserBankClasses.get(pos).getVerificationStatus();
                final String bankAccountNumber = mListUserBankClasses.get(pos).getAccountNumber();
                try {
                    Drawable icon = getResources().getDrawable(mListUserBankClasses.get(pos).getBankIcon(getContext()));
                    bankIcon.setImageDrawable(icon);
                }catch (Exception e){
                    e.printStackTrace();
                }

                if (pos == mListUserBankClasses.size() - 1)
                    divider.setVisibility(View.GONE);

                mBankAccountNumber.setText(bankAccountNumber);
                mBankName.setText(bankName);
                mBranchName.setText(branchName);

                switch (verificationStatus) {
                    case Constants.BANK_ACCOUNT_STATUS_VERIFIED:
                        mBankVerifiedStatus.setVisibility(View.VISIBLE);
                        mBankVerifiedStatus.setImageResource(R.drawable.ic_verified);
                        mBankVerifiedStatus.clearColorFilter();
                        mBankPending.setVisibility(View.GONE);
                        mBankActionList = Arrays.asList(getResources().getStringArray(R.array.verified_bank_action));
                        if (mListUserBankClasses.get(pos).getBankDocuments() == null || mListUserBankClasses.get(pos).getBankDocuments().size() < 1)
                            mAttachmentChequebook.setVisibility(View.GONE);
                        else
                            mAttachmentChequebook.setVisibility(View.VISIBLE);
                        break;

                    case Constants.BANK_ACCOUNT_STATUS_NOT_VERIFIED:
                        mBankVerifiedStatus.setVisibility(View.VISIBLE);
                        mBankVerifiedStatus.setImageResource(R.drawable.ic_notverified);
                        mBankVerifiedStatus.setColorFilter(Color.RED);
                        mBankPending.setVisibility(View.GONE);
                        mBankActionList = Arrays.asList(getResources().getStringArray(R.array.not_verified_bank_action));
                        break;

                    case Constants.BANK_ACCOUNT_STATUS_BLOCKED:
                        mBankVerifiedStatus.setVisibility(View.VISIBLE);
                        mBankVerifiedStatus.setImageResource(R.drawable.ic_notverified);
                        mBankVerifiedStatus.setColorFilter(Color.RED);
                        mBankPending.setVisibility(View.GONE);
                        mAttachmentChequebook.setVisibility(View.GONE);
                        break;

                    case Constants.BANK_ACCOUNT_STATUS_UNCONSENTED:
                        mBankUnconsented.setVisibility(View.VISIBLE);
                        mBankPending.setVisibility(View.GONE);
                        mBankVerifiedStatus.setVisibility(View.GONE);
                        mBankActionList = Arrays.asList(getResources().getStringArray(R.array.not_verified_bank_action));
                        break;

                    default:
                        // Bank verification status pending
                        mBankPending.setVisibility(View.VISIBLE);
                        mBankVerifiedStatus.setVisibility(View.GONE);
                        mBankActionList = Arrays.asList(getResources().getStringArray(R.array.not_verified_bank_action));
                        break;
                }

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (verificationStatus.equals(Constants.BANK_ACCOUNT_STATUS_BLOCKED)) {
                            showBlockedBankAccountDialog(bankName, bankAccountNumber, branchName, bankAccountID);
                        } else if (verificationStatus.equals(Constants.BANK_ACCOUNT_STATUS_UNCONSENTED)) {
                            launchAddBankAgreementPage(mListUserBankClasses.get(pos));
                        }else {
                            mCustomSelectorDialog = new CustomSelectorDialog(getActivity(), bankName, mBankActionList);
                            mCustomSelectorDialog.setOnResourceSelectedListener(new CustomSelectorDialog.OnResourceSelectedListener() {
                                @Override
                                public void onResourceSelected(int selectedIndex, String action) {
                                    if (getContext().getString(R.string.remove).equalsIgnoreCase(action)) {
                                        mCustomSelectorDialog.dismiss();
                                        showRemoveBankAccountDialog(bankAccountID);

                                    } else if (getContext().getString(R.string.verify).equalsIgnoreCase(action)) {
                                        if (!verificationStatus.equals(Constants.BANK_ACCOUNT_STATUS_VERIFIED)) {
                                            mCustomSelectorDialog.dismiss();
                                            showVerifyBankWithAmountDialog(bankAccountID);
                                        }
                                    }
                                }
                            });
                            mCustomSelectorDialog.show();
                        }
                    }
                });

                mAttachmentChequebook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Constants.SELECTED_CHEQUEBOOK_COVER, mListUserBankClasses.get(pos));
                        ((ManageBanksActivity) getActivity()).switchToPreviewChequebookCoverFragment(bundle);
                    }
                });
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            public FooterViewHolder(View itemView) {
                super(itemView);
            }

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;

            if (viewType == FOOTER_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_bank_footer, parent, false);
                return new FooterViewHolder(v);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_bank_accounts, parent, false);
                return new ViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                if (holder instanceof ViewHolder) {
                    ViewHolder vh = (ViewHolder) holder;
                    vh.bindView(position);
                } else if (holder instanceof FooterViewHolder) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (mListUserBankClasses != null && mListUserBankClasses.size() > 0)
                return mListUserBankClasses.size() + 1;
            else return 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return FOOTER_VIEW;
            } else {
                return BANK_LIST_ITEM_VIEW;
            }

        }
    }

    private void launchAddBankAgreementPage(BankAccountList bankAccountList) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.BANK_NAME, bankAccountList.getBankName());
        bundle.putString(Constants.BANK_BRANCH_NAME, bankAccountList.getBranchName());
        bundle.putString(Constants.BANK_BRANCH_ROUTE_NO, bankAccountList.getBranchRoutingNumber());
        bundle.putBoolean(Constants.FROM_ON_BOARD, false);
        bundle.putString(Constants.BANK_ACCOUNT_NAME, bankAccountList.getAccountName());
        bundle.putString(Constants.BANK_ACCOUNT_NUMBER, bankAccountList.getAccountNumber());
        bundle.putBoolean(Constants.IS_STARTED_FROM_PROFILE_COMPLETION, false);
        bundle.putBoolean(Constants.IS_STARTED_FROM_UNCONCENTED_LIST, true);
        bundle.putLong(Constants.BANK_ACCOUNT_ID, bankAccountList.getBankAccountId());

        ((ManageBanksActivity) getActivity()).switchToAddBankAgreementFragment(bundle);
    }
}
