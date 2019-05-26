package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.CreditCard;


import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.GetSavedBillResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.RecentBill;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.SavedBill;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GetAvailableCreditCardBanks;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GetProviderResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.Provider;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.ProviderCategory;
import bd.com.ipay.ipayskeleton.PaymentFragments.SaveAndScheduleBill.BillPaySavedNumberSelectFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.SaveAndScheduleBill.LankaBanglaCARDSavedNumberSelectFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Card.LankaBanglaAmountInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LankaBangla.Card.LankaBanglaCardNumberInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LinkThree.LinkThreeSubscriberIdInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;

public class CreditCardBankSelectionFragment extends Fragment implements HttpResponseListener {
    private RecyclerView mBankListRecyclerView;
    private ArrayList<Bank> mBankList;
    private HttpRequestGetAsyncTask mGetBankListAsyncTask;
    private LinearLayout mProgressLayout;
    private BankListAdapter bankListAdapter;
    //private int clickedPosition;
    private int selectedBankIconId;
    private String selectedBankCode;

    private boolean isFromDashboard;
    private HttpRequestGetAsyncTask mGetUtilityProviderListTask;
    private GetProviderResponse mUtilityProviderResponse;
    private List<ProviderCategory> mUtilityProviderTypeList;
    private HashMap<String, String> mProviderAvailabilityMap;

    private HttpRequestGetAsyncTask mGetSavedBillListTask;
    private GetSavedBillResponse mSavedBillResponse;
    private List<SavedBill> mSavedBills = new ArrayList<>();
    List<RecentBill> mRecentBill = new ArrayList<>();
    DataHelper dataHelper ;

    CustomProgressDialog mCustomProgressDialog;
    SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_credit_card_bank_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dataHelper = DataHelper.getInstance(getContext());
        mCustomProgressDialog = new CustomProgressDialog(getContext());
        mProviderAvailabilityMap = new HashMap<>();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            isFromDashboard = bundle.getBoolean(Constants.FROM_DASHBOARD, false);
        }

        mBankListRecyclerView = view.findViewById(R.id.user_bank_list_recycler_view);
        mProgressLayout = view.findViewById(R.id.progress_layout);
        bankListAdapter = new BankListAdapter();
        mBankListRecyclerView.setAdapter(bankListAdapter);
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        ((IPayUtilityBillPayActionActivity) getActivity()).setSupportActionBar(toolbar);
        ((IPayUtilityBillPayActionActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().setTitle(R.string.credit_card_bill_title);
        if(isFromDashboard) {
            getServiceProviderList();
        }else{
            attemptGetBankList();
        }
    }

    public int getBankIcon(Bank bank) {
        Resources resources = getContext().getResources();
        int resourceId;
        if (bank.getBankCode() != null)
            resourceId = resources.getIdentifier("ic_bank" + bank.getBankCode(), "drawable",
                    getContext().getPackageName());
        else if(bank.getBankName().equalsIgnoreCase(getString(R.string.lanka_bangla_card)))
            resourceId = R.drawable.ic_lankabd2;
        else
            resourceId = resources.getIdentifier("ic_bank" + "111", "drawable",
                    getContext().getPackageName());
        return resourceId;
    }

    public void attemptGetBankList() {
        if (mGetBankListAsyncTask != null) {
            return;
        } else {
            mGetBankListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BANK_LIST,
                    Constants.BASE_URL_SM + Constants.URL_GET_BANK_LIST, getContext(), this, false);
            mGetBankListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void getServiceProviderList() {
        if (mGetUtilityProviderListTask != null) {
            return;
        }

        mGetUtilityProviderListTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SERVICE_PROVIDER_LIST,
                Constants.BASE_URL_UTILITY + Constants.URL_GET_PROVIDER, getActivity(), false);
        mGetUtilityProviderListTask.mHttpResponseListener = this;
        mGetUtilityProviderListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getSavedList(String providerCode1,  String providerCode2) {
        mCustomProgressDialog.show();
        if (mGetSavedBillListTask != null) {
            return;
        }

        mGetSavedBillListTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SAVED_BILL_LIST,
                Constants.BASE_URL_UTILITY + "scheduled/saved-bills/?providerCodes="+providerCode1+"&providerCodes="+providerCode2, getActivity(), false);
        mGetSavedBillListTask.mHttpResponseListener = this;
        mGetSavedBillListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            mGetBankListAsyncTask = null;
            mGetUtilityProviderListTask = null;
            return;
        }
        try {

            if (result.getApiCommand().equals(Constants.COMMAND_GET_BANK_LIST)) {

                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                    mProgressLayout.setVisibility(View.GONE);
                    mBankList = new Gson().fromJson(result.getJsonString(), GetAvailableCreditCardBanks.class).getBankList();
                    if(isFromDashboard){
                        mBankList.add(0, new Bank(getString(R.string.lanka_bangla_card),null));
                    }
                    mBankListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    mBankListRecyclerView.setAdapter(bankListAdapter);
                    bankListAdapter.notifyDataSetChanged();
                } else {
                    Toaster.makeText(getContext(), "Bank List Fetch Failed", Toast.LENGTH_LONG);
                }
                mGetBankListAsyncTask = null;
            }  else if (result.getApiCommand().equals(Constants.COMMAND_GET_SAVED_BILL_LIST)) {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    mSavedBillResponse = new Gson().fromJson(result.getJsonString(), GetSavedBillResponse.class);
                    mSavedBills = mSavedBillResponse.getSavedBills();
                    mRecentBill = dataHelper.getBills("LANKABANGLA-VISA", "LANKABANGLA-MASTERCARD");

                    if(mSavedBills!=null && mSavedBills.size()>0) {

                        List<RecentBill> tempRecentBills = new ArrayList<>();

                        for(RecentBill recentBill: mRecentBill) {
                            String providerCode = recentBill.getParamValue();
                            boolean isFound = false;
                            for (SavedBill savedBill : mSavedBills) {
                                if (savedBill.getBillParams().get(0).getParamValue().equalsIgnoreCase(providerCode)){
                                    isFound = true;
                                    recentBill.setShortName(savedBill.getShortName());
                                    recentBill.setScheduledToo(savedBill.getIsScheduledToo());
                                    recentBill.setSaved(true);
                                    recentBill.setProviderCode(savedBill.getProviderCode());
                                    recentBill.setDateOfBillPayment(savedBill.getDateOfBillPayment());
                                    Date date1 =null;
                                    Date date2 = null;
                                    try {
                                        date2 = dateFormater.parse(savedBill.getLastPaid());
                                        date1 = dateFormater.parse(recentBill.getLastPaid());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if (date1.compareTo(date2) <= 0) {
                                        recentBill.setLastPaid(savedBill.getLastPaid());
                                    }
                                    recentBill.setPaidForOthers(savedBill.getPaidForOthers());
                                    recentBill.setMetaData(new  Gson().toJson(savedBill.getMetaData()));

                                    recentBill.setParamId(savedBill.getBillParams().get(0).getParamId());
                                    recentBill.setParamLabel(savedBill.getBillParams().get(0).getParamLabel());
                                    recentBill.setParamValue(savedBill.getBillParams().get(0).getParamValue());
                                    if (savedBill.getBillParams().size() > 1) {
                                        for(int i=1; i<savedBill.getBillParams().size();i++){
                                            if(savedBill.getBillParams().get(i).getParamId().equalsIgnoreCase("amount"))
                                                recentBill.setAmount(savedBill.getBillParams().get(i).getParamValue());
                                            if(savedBill.getBillParams().get(i).getParamId().equalsIgnoreCase("amountType"))
                                                recentBill.setAmountType(savedBill.getBillParams().get(i).getParamValue());
                                            if(savedBill.getBillParams().get(i).getParamId().equalsIgnoreCase("locationCode"))
                                                recentBill.setAmountType(savedBill.getBillParams().get(i).getParamValue());

                                        }

                                    }
                                    tempRecentBills.add(recentBill);
                                    break;

                                }
                            }

                            if(!isFound) {
                                recentBill.setSaved(false);
                                tempRecentBills.add(recentBill);
                            }
                        }

                        dataHelper.createBills(tempRecentBills);
                    }else {
                        List<RecentBill> tempRecentBills = new ArrayList<>();

                        for(RecentBill recentBill: mRecentBill) {
                            recentBill.setSaved(false);
                            tempRecentBills.add(recentBill);
                        }
                        dataHelper.createBills(tempRecentBills);
                    }

                }

                mRecentBill = dataHelper.getBills("LANKABANGLA-VISA", "LANKABANGLA-MASTERCARD");

                if((mRecentBill !=null && mRecentBill.size()>0) || (mSavedBills != null&& mSavedBills.size()>0)){
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.NAME, getString(R.string.lanka_bangla_card));
                    bundle.putSerializable("SAVED_DATA", (Serializable) mSavedBills);
                    bundle.putSerializable("RECENT_DATA", (Serializable) mRecentBill);
                    ((IPayUtilityBillPayActionActivity) getActivity()).
                            switchFragment(new LankaBanglaCARDSavedNumberSelectFragment(), bundle, 1, true);
                }else{

                    ((IPayUtilityBillPayActionActivity) getActivity()).
                            switchFragment(new LankaBanglaCardNumberInputFragment(), null, 1, true);
                }
                mCustomProgressDialog.dismissDialogue();
                mGetSavedBillListTask = null;
            } else if (result.getApiCommand().equals(Constants.COMMAND_GET_SERVICE_PROVIDER_LIST)) {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    mUtilityProviderResponse = new Gson().fromJson(result.getJsonString(), GetProviderResponse.class);
                    mUtilityProviderTypeList = mUtilityProviderResponse.getProviderCategories();
                    if (mUtilityProviderTypeList != null && mUtilityProviderTypeList.size() != 0) {
                        for (int i = 0; i < mUtilityProviderTypeList.size(); i++) {
                            for (int j = 0; j < mUtilityProviderTypeList.get(i).getProviders().size(); j++) {
                                Provider provider = mUtilityProviderTypeList.get(i).getProviders().get(j);
                                if (!provider.isActive()) {
                                    if (provider.getStatusMessage() != null) {
                                        mProviderAvailabilityMap.put(provider.getCode().toUpperCase(), provider.getStatusMessage());
                                    } else {
                                        mProviderAvailabilityMap.put(provider.getCode().toUpperCase(), getString(R.string.you_cant_avail_this_service));
                                    }
                                } else {
                                    mProviderAvailabilityMap.put(provider.getCode().toUpperCase(), getString(R.string.active));
                                }
                            }
                        }
                    }
                    mGetUtilityProviderListTask = null;
                    attemptGetBankList();
                }
            }

        } catch (Exception e) {
            Toaster.makeText(getContext(), "Bank List Fetch Failed", Toast.LENGTH_LONG);
            mGetBankListAsyncTask = null;
            mGetUtilityProviderListTask = null;
        }
    }

    public class BankListAdapter extends RecyclerView.Adapter<BankListAdapter.BankViewHolder> {
        @NonNull
        @Override
        public BankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.list_bank_item, null, false);
            return new BankViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull final BankViewHolder holder, final int position) {
            holder.bankNameTextView.setText(mBankList.get(position).getBankName());
            holder.bankIconImageView.setImageResource(getBankIcon(mBankList.get(position)));

            holder.bankIconImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(isFromDashboard && position==0){
                        ((IPayUtilityBillPayActionActivity) getActivity()).
                                switchFragment(new LankaBanglaCardNumberInputFragment(), null, 1, true);
                    }else {
                        selectedBankIconId = getBankIcon(mBankList.get(position));
                        selectedBankCode = mBankList.get(position).getBankCode();
                        Bundle bundle = new Bundle();
                        bundle.putString(IPayUtilityBillPayActionActivity.BANK_CODE, selectedBankCode);
                        bundle.putInt(IPayUtilityBillPayActionActivity.BANK_ICON, selectedBankIconId);
                        ((IPayUtilityBillPayActionActivity) getActivity()).
                                switchFragment(new CreditCardInfoInputFragment(), bundle, 1, true);
                    }

                }
            });
            holder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isFromDashboard && position==0){
                        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.UTILITY_BILL_PAYMENT)) {
                            DialogUtils.showServiceNotAllowedDialog(getContext());
                            return;
                        } else if (mProviderAvailabilityMap.get(Constants.LANKABANGLA) != null) {
                            if (!mProviderAvailabilityMap.get(Constants.LANKABANGLA).
                                    equals(getString(R.string.active))) {
                                DialogUtils.showCancelableAlertDialog(getContext(), mProviderAvailabilityMap.get(Constants.LANKABANGLA));
                                return;
                            }
                        }


                        getSavedList("LANKABANGLA-VISA", "LANKABANGLA-MASTERCARD");
                    }else {
                        selectedBankIconId = getBankIcon(mBankList.get(position));
                        selectedBankCode = mBankList.get(position).getBankCode();
                        Bundle bundle = new Bundle();
                        bundle.putString(IPayUtilityBillPayActionActivity.BANK_CODE, selectedBankCode);
                        bundle.putInt(IPayUtilityBillPayActionActivity.BANK_ICON, selectedBankIconId);
                        ((IPayUtilityBillPayActionActivity) getActivity()).
                                switchFragment(new CreditCardInfoInputFragment(), bundle, 1, true);
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return mBankList.size();
        }

        public class BankViewHolder extends RecyclerView.ViewHolder {
            public TextView bankNameTextView;
            private ImageView bankIconImageView;
            private View parentView;


            public BankViewHolder(View itemView) {
                super(itemView);
                bankIconImageView = itemView.findViewById(R.id.bank_icon);
                bankNameTextView = itemView.findViewById(R.id.bank_name);
                parentView = itemView;
            }
        }
    }
}
