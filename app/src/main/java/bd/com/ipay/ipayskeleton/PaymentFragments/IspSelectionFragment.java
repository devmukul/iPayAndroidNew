package bd.com.ipay.ipayskeleton.PaymentFragments;


import android.content.Intent;
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

import com.google.gson.Gson;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.ISP;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.GetSavedBillResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.RecentBill;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.SavedBill;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GetProviderResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.Provider;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.ProviderCategory;
import bd.com.ipay.ipayskeleton.PaymentFragments.SaveAndScheduleBill.BillPaySavedNumberSelectFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.Carnival.CarnivalIdInputFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LinkThree.LinkThreeSubscriberIdInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.PinChecker;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;

public class IspSelectionFragment extends Fragment implements HttpResponseListener {
    private RecyclerView mIspListRecyclerView;
    private LinearLayout mProgressLayout;
    private IspListAdapter ispListAdapter;
    private HttpRequestGetAsyncTask mGetUtilityProviderListTask;
    private GetProviderResponse mUtilityProviderResponse;
    private List<ProviderCategory> mUtilityProviderTypeList;
    private HashMap<String, String> mProviderAvailabilityMap;

    private List<ISP> ispTypes;
    private PinChecker pinChecker;

    private HttpRequestGetAsyncTask mGetSavedBillListTask;
    private GetSavedBillResponse mSavedBillResponse;
    private List<SavedBill> mSavedBills = new ArrayList<>();
    List<RecentBill> mRecentBill = new ArrayList<>();
    DataHelper dataHelper ;

    CustomProgressDialog mCustomProgressDialog;
    SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private static final int REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_credit_card_bank_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dataHelper = DataHelper.getInstance(getContext());
        mProviderAvailabilityMap = new HashMap<>();

        mCustomProgressDialog = new CustomProgressDialog(getContext());

        mIspListRecyclerView = view.findViewById(R.id.user_bank_list_recycler_view);
        mProgressLayout = view.findViewById(R.id.progress_layout);
        mProgressLayout.setVisibility(View.GONE);
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        ((IPayUtilityBillPayActionActivity) getActivity()).setSupportActionBar(toolbar);
        ((IPayUtilityBillPayActionActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().setTitle(R.string.isp);

        genarateCardType();
        ispListAdapter = new IspListAdapter();
        mIspListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mIspListRecyclerView.setAdapter(ispListAdapter);

        getServiceProviderList();
    }

    private void genarateCardType() {
        ispTypes = new ArrayList<>();
        ISP cardType = new ISP(getActivity().getString(R.string.amberIT), Constants.AMBERIT , R.drawable.ic_amber_it);
        ispTypes.add(cardType);
        cardType = new ISP(getActivity().getString(R.string.banglalion), Constants.BLION , R.drawable.banglalion);
        ispTypes.add(cardType);
        cardType = new ISP(getActivity().getString(R.string.carnival), Constants.CARNIVAL ,  R.drawable.ic_carnival);
        ispTypes.add(cardType);
        cardType = new ISP(getActivity().getString(R.string.link_three), Constants.LINK3 , R.drawable.link_three_logo);
        ispTypes.add(cardType);
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

    private void getSavedList(String providerCode) {
        mCustomProgressDialog.show();
        if (mGetSavedBillListTask != null) {
            return;
        }

        mGetSavedBillListTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SAVED_BILL_LIST,
                Constants.BASE_URL_UTILITY + "/scheduled/saved-bills/?providerCodes="+providerCode, getActivity(), false);
        mGetSavedBillListTask.mHttpResponseListener = this;
        mGetSavedBillListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {

            mGetUtilityProviderListTask = null;
            mGetSavedBillListTask = null;
            mCustomProgressDialog.dismissDialogue();
            return;
        }
        try {

            if (result.getApiCommand().equals(Constants.COMMAND_GET_SERVICE_PROVIDER_LIST)) {
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
                }
            } else if (result.getApiCommand().equals(Constants.COMMAND_GET_SAVED_BILL_LIST)) {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    mSavedBillResponse = new Gson().fromJson(result.getJsonString(), GetSavedBillResponse.class);
                    mSavedBills = mSavedBillResponse.getSavedBills();
                    mRecentBill = dataHelper.getBills("LINK3");

                    if (mSavedBills != null && mSavedBills.size() > 0) {

                        List<RecentBill> tempRecentBills = new ArrayList<>();

                        for (RecentBill recentBill : mRecentBill) {
                            String providerCode = recentBill.getParamValue();
                            boolean isFound = false;
                            for (SavedBill savedBill : mSavedBills) {
                                if (savedBill.getBillParams().get(0).getParamValue().equalsIgnoreCase(providerCode)) {
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
                                    if (date1.compareTo(date2) <= 0) {
                                        recentBill.setLastPaid(savedBill.getLastPaid());
                                    }
                                    recentBill.setPaidForOthers(savedBill.getPaidForOthers());
                                    recentBill.setMetaData(new Gson().toJson(savedBill.getMetaData()));

                                    recentBill.setParamId(savedBill.getBillParams().get(0).getParamId());
                                    recentBill.setParamLabel(savedBill.getBillParams().get(0).getParamLabel());
                                    recentBill.setParamValue(savedBill.getBillParams().get(0).getParamValue());
                                    if (savedBill.getBillParams().size() > 1) {
                                        for (int i = 1; i < savedBill.getBillParams().size(); i++) {
                                            if (savedBill.getBillParams().get(i).getParamId().equalsIgnoreCase("amount"))
                                                recentBill.setAmount(savedBill.getBillParams().get(i).getParamValue());
                                            if (savedBill.getBillParams().get(i).getParamId().equalsIgnoreCase("amountType"))
                                                recentBill.setAmountType(savedBill.getBillParams().get(i).getParamValue());
                                            if (savedBill.getBillParams().get(i).getParamId().equalsIgnoreCase("locationCode"))
                                                recentBill.setAmountType(savedBill.getBillParams().get(i).getParamValue());

                                        }

                                    }
                                    tempRecentBills.add(recentBill);
                                    break;

                                }
                            }

                            if (!isFound) {
                                recentBill.setSaved(false);
                                tempRecentBills.add(recentBill);
                            }
                        }

                        dataHelper.createBills(tempRecentBills);
                    } else {
                        List<RecentBill> tempRecentBills = new ArrayList<>();

                        for (RecentBill recentBill : mRecentBill) {
                            recentBill.setSaved(false);
                            tempRecentBills.add(recentBill);
                        }
                        dataHelper.createBills(tempRecentBills);
                    }
                }

                mRecentBill = dataHelper.getBills("LINK3");

                if((mRecentBill !=null && mRecentBill.size()>0) || (mSavedBills != null&& mSavedBills.size()>0)){
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.NAME, getString(R.string.link_three));
                    bundle.putSerializable("SAVED_DATA", (Serializable) mSavedBills);
                    bundle.putSerializable("RECENT_DATA", (Serializable) mRecentBill);
                    ((IPayUtilityBillPayActionActivity) getActivity()).
                            switchFragment(new BillPaySavedNumberSelectFragment(), bundle, 1, true);
                }else{
                    ((IPayUtilityBillPayActionActivity) getActivity()).
                            switchFragment(new LinkThreeSubscriberIdInputFragment(), null, 1, true);
                }
                mCustomProgressDialog.dismissDialogue();
                mGetSavedBillListTask = null;
            }

        } catch (Exception e) {
            mCustomProgressDialog.dismissDialogue();
            ((IPayUtilityBillPayActionActivity) getActivity()).
                    switchFragment(new LinkThreeSubscriberIdInputFragment(), null, 1, true);

            mGetSavedBillListTask = null;
        }
    }

    public class IspListAdapter extends RecyclerView.Adapter<IspListAdapter.IspViewHolder> {
        @NonNull
        @Override
        public IspViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.list_bank_item, null, false);
            return new IspViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull final IspViewHolder holder, final int position) {
            holder.ispNameTextView.setText(ispTypes.get(position).getIspName());
            holder.ispIconImageView.setImageResource(ispTypes.get(position).getIspIconDrawable());

            holder.ispIconImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    payBill(ispTypes.get(position).getIspCode(),null);
                }
            });
            holder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    payBill(ispTypes.get(position).getIspCode(),null);
                }
            });

        }

        @Override
        public int getItemCount() {
            return ispTypes.size();
        }

        public class IspViewHolder extends RecyclerView.ViewHolder {
            public TextView ispNameTextView;
            private ImageView ispIconImageView;
            private View parentView;


            public IspViewHolder(View itemView) {
                super(itemView);
                ispIconImageView = itemView.findViewById(R.id.bank_icon);
                ispNameTextView = itemView.findViewById(R.id.bank_name);
                parentView = itemView;
            }
        }
    }

    private void payBill(final String provider, final String type) {
        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.UTILITY_BILL_PAYMENT)) {
            DialogUtils.showServiceNotAllowedDialog(getContext());
            return;
        } else if (mProviderAvailabilityMap.get(provider) != null) {
            if (!mProviderAvailabilityMap.get(provider).
                    equals(getString(R.string.active))) {
                DialogUtils.showCancelableAlertDialog(getContext(), mProviderAvailabilityMap.get(provider));
                return;
            }
        }
        pinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
            @Override
            public void ifPinAdded() {
                Intent intent;
                switch (provider) {
                    case Constants.BRILLIANT:
                    case Constants.AMBERIT:
                        intent = new Intent(getActivity(), UtilityBillPaymentActivity.class);
                        intent.putExtra(Constants.SERVICE, provider);
                        startActivity(intent);
                        getActivity().finish();
                        break;
                    case Constants.LINK3:
                        getSavedList("LINK3");
                        break;
                    case Constants.CARNIVAL:
                        ((IPayUtilityBillPayActionActivity) getActivity()).
                                switchFragment(new CarnivalIdInputFragment(), null, 1, true);
                        break;
                    case Constants.BLION:
                        intent = new Intent(getActivity(), UtilityBillPaymentActivity.class);
                        intent.putExtra(Constants.SERVICE, Constants.BANGLALION);
                        startActivity(intent);
                        getActivity().finish();
                        break;
                }
            }
        });
        pinChecker.execute();
    }
}
