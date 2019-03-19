package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.CustomBillProviderTitleView;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GetProviderResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.Provider;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.ProviderCategory;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.PinChecker;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class UtilityProviderListFragment extends BaseFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mGetUtilityProviderListTask = null;
    GetProviderResponse mUtilityProviderResponse;
    List<ProviderCategory> mUtilityProviderTypeList;
    private LinearLayout mScrollViewHolder;
    private SwipeRefreshLayout utilityProviderListRefreshLayout;

    private PinChecker pinChecker;

    private CustomProgressDialog mProgressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_utility_provider, container, false);
        mScrollViewHolder = (LinearLayout) v.findViewById(R.id.scrollViewHolder);
        utilityProviderListRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.trending_business_list_refresh_layout);
        getActivity().setTitle(R.string.bill_pay);
        getTrendingBusinessList();
        mProgressDialog = new CustomProgressDialog(getContext());
        mProgressDialog.show();

        utilityProviderListRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()

        {
            @Override
            public void onRefresh() {
                if (mScrollViewHolder.getVisibility() == View.VISIBLE) {
                    getTrendingBusinessList();
                } else {
                    utilityProviderListRefreshLayout.setRefreshing(false);
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void getTrendingBusinessList() {
        if (mGetUtilityProviderListTask != null) {
            return;
        }

        mGetUtilityProviderListTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_TRENDING_BUSINESS_LIST,
                Constants.BASE_URL_UTILITY + Constants.URL_GET_PROVIDER, getActivity(), false);
        mGetUtilityProviderListTask.mHttpResponseListener = this;
        mGetUtilityProviderListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            mGetUtilityProviderListTask = null;
            utilityProviderListRefreshLayout.setRefreshing(false);
            mProgressDialog.dismiss();
            return;
        }
        try {
            mProgressDialog.dismiss();
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                Gson gson = new Gson();

                mScrollViewHolder.removeAllViews();

                mUtilityProviderResponse = gson.fromJson(result.getJsonString(), GetProviderResponse.class);
                mUtilityProviderTypeList = mUtilityProviderResponse.getProviderCategories();
                for (ProviderCategory providerCategory : mUtilityProviderTypeList) {

                    String mBusinessType = providerCategory.getCategoryName();
                    CustomBillProviderTitleView customBillProviderTitleView = new CustomBillProviderTitleView(getContext());
                    customBillProviderTitleView.setTitleView(mBusinessType);
                    mScrollViewHolder.addView(customBillProviderTitleView);

                    RecyclerView recyclerView = new RecyclerView(getContext());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER;
                    RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(layoutParams);
                    recyclerView.setLayoutParams(params);
                    recyclerView.setNestedScrollingEnabled(false);

                    List<Provider> mBillProviderList = providerCategory.getProviders();
                    BillProviderItemAdapter billProviderItemAdapter = new BillProviderItemAdapter(mBillProviderList, getActivity());
                    recyclerView.setAdapter(billProviderItemAdapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    mScrollViewHolder.addView(recyclerView);
                    mScrollViewHolder.setVisibility(View.VISIBLE);
                }

            } else {
                if (getActivity() != null) {
                    Toaster.makeText(getActivity(), R.string.business_contacts_sync_failed, Toast.LENGTH_LONG);
                }
            }
            mGetUtilityProviderListTask = null;
            utilityProviderListRefreshLayout.setRefreshing(false);
        } catch (Exception e) {
            e.printStackTrace();
            mProgressDialog.dismiss();
            if (getActivity() != null) {
                Toaster.makeText(getActivity(), R.string.business_contacts_sync_failed, Toast.LENGTH_LONG);
            }
        }

    }


    private class BillProviderItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Provider> mBillProviderList;

        public BillProviderItemAdapter(List<Provider> mBillProviderList, Context context) {
            this.mBillProviderList = mBillProviderList;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ProfileImageView mImageView;
            private TextView mTextView;

            public ViewHolder(final View itemView) {
                super(itemView);
                mImageView = (ProfileImageView) itemView.findViewById(R.id.profile_picture);
                mTextView = (TextView) itemView.findViewById(R.id.provider_name);
            }

            public void bindView(final int pos) {
                final Provider provider = mBillProviderList.get(pos);
                final String name = provider.getName();
                mTextView.setText(name);

                if (getIconByName(provider.getName().toLowerCase()) != 0) {
                    mImageView.setProfilePicture(getIconByName(provider.getName().toLowerCase()));
                } else {
                    mImageView.setProfilePicture(R.drawable.utility_icon);
                }
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switchToDesiredFragment(provider.getName());
                    }
                });
            }

            private void switchToDesiredFragment(String name) {
                if (name.toLowerCase().equals(Constants.BANGLALION.toLowerCase())) {
                    ((UtilityBillPaymentActivity) getActivity()).switchToBanglalionBillPayFragment();
                } else if (name.toLowerCase().equals(Constants.WESTZONE.toLowerCase())) {
                    ((UtilityBillPaymentActivity) getActivity()).switchToWestZoneBillPayFragment();
                } else if (name.toLowerCase().equals(Constants.BRILLIANT.toLowerCase())) {
                    ((UtilityBillPaymentActivity) getActivity()).switchToBrilliantRechargeFragment();
                }
            }

            private int getIconByName(String name) {
                Resources resources = getContext().getResources();
                final int resourceId = resources.getIdentifier(name, "drawable",
                        getContext().getPackageName());
                return resourceId;
            }
        }

        class NormalViewHolder extends ViewHolder {
            NormalViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new NormalViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_bill_provider_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                NormalViewHolder vh = (NormalViewHolder) holder;
                vh.bindView(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mBillProviderList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }


    }

}
