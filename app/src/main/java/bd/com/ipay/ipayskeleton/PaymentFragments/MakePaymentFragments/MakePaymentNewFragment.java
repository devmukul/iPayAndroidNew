package bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.Activities.IPayTransactionActionActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.QRCodePaymentActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.Activities.RailwayTicketActionActivity;
import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.ResourceApi.GetAllBusinessListAsyncTask;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.TrendingBusinessOutletSelectorDialog;
import bd.com.ipay.ipayskeleton.CustomView.MakePaymentContactsSearchView;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.BusinessContact.GetAllBusinessContactRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Business.Merchants.BusinessList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Business.Merchants.GetAllTrendingBusinessResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Business.Merchants.TrendingBusinessList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.GetSavedBillResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.RecentBill;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.SavedBill;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GetProviderResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.Provider;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.ProviderCategory;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.GetSponsorListResponse;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.Sponsor;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.PinChecker;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class MakePaymentNewFragment extends BaseFragment implements HttpResponseListener {

    private static final int REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH = 100;
    private HttpRequestGetAsyncTask mGetTrendingBusinessListTask = null;
    GetAllTrendingBusinessResponse mTrendingBusinessResponse;
    List<TrendingBusinessList> mTrendingBusinessList;

    private HttpRequestGetAsyncTask mGetUtilityProviderListTask;
    private GetProviderResponse mUtilityProviderResponse;
    private List<ProviderCategory> mUtilityProviderTypeList;

    private HttpRequestGetAsyncTask getSponsorListAsyncTask;
    private GetSponsorListResponse getSponsorListResponse = null;
    private ArrayList<Sponsor> sponsorArrayList;
    private ArrayList<Sponsor> approvedSponsorArrayList;

    private View mBillPayView;
    private View mLink3BillPayView;

    private View mAkashBillPayView;
    private View mBrilliantRechargeView;
    private View mWestZoneBillPayView;
    private View mDescoBillPayView;
    private View mDpdcBillPayView;
    private View mDozeBillPayView;
    private View mLankaBanglaView;
    private View mLankaBanglaDpsView;
    private View mAmberITBillPayView;
    private View mCreditCardBillPayView;
    private View mRailwayTicketPurchaseView;
    private HashMap<String, String> mProviderAvailabilityMap;
    private SwipeRefreshLayout trendingBusinessListRefreshLayout;

    private PinChecker pinChecker;
    private RecyclerView mTrendingListRecyclerView;
    private TrendingListAdapter mTrendingListAdapter;
    private MakePaymentContactsSearchView mMobileNumberEditText;
    private String trendingJson;
    private ProgressBar mProgressBar;

    private int transactionType;

    private HttpRequestGetAsyncTask mGetSavedBillListTask;
    private GetSavedBillResponse mSavedBillResponse;
    private List<SavedBill> mSavedBills = new ArrayList<>();
    List<RecentBill> mRecentBill = new ArrayList<>();
    DataHelper dataHelper ;

    CustomProgressDialog mCustomProgressDialog;

    String providerCode1;
    String providerCode2;
    String typeText;
    SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = Utilities.getTracker(getActivity());
        dataHelper = DataHelper.getInstance(getContext());

        mCustomProgressDialog = new CustomProgressDialog(getContext());
        approvedSponsorArrayList = new ArrayList<>();
        if (getArguments() != null) {
            transactionType = getArguments().getInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ipay_make_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProviderAvailabilityMap = new HashMap<>();
        mBillPayView = view.findViewById(R.id.billPayView);
        mLink3BillPayView = view.findViewById(R.id.linkThreeBill);
        mAkashBillPayView = view.findViewById(R.id.akash_dth);

        mDescoBillPayView = view.findViewById(R.id.desco);
        if (ACLManager.hasServicesAccessibility(ServiceIdConstants.DESCO)) {
            mDescoBillPayView.setVisibility(View.VISIBLE);
        } else {
            mDescoBillPayView.setVisibility(View.GONE);
        }
        mWestZoneBillPayView = view.findViewById(R.id.west_zone);
        mDozeBillPayView = view.findViewById(R.id.carnival);
        mDpdcBillPayView = view.findViewById(R.id.dpdc);
        mAmberITBillPayView = view.findViewById(R.id.amberit);
        mLankaBanglaView = view.findViewById(R.id.lankaBanglaViewCard);
        mLankaBanglaDpsView = view.findViewById(R.id.lankaBanglaViewDps);
        mBrilliantRechargeView = view.findViewById(R.id.brilliant_recharge_view);
        mCreditCardBillPayView = view.findViewById(R.id.credit_card_bill);
        mRailwayTicketPurchaseView = view.findViewById(R.id.railway_ticket);
        trendingBusinessListRefreshLayout = view.findViewById(R.id.trending_business_list_refresh_layout);
        mMobileNumberEditText = view.findViewById(R.id.searchView);
        mProgressBar = view.findViewById(R.id.progess_bar);

        mTrendingListRecyclerView = view.findViewById(R.id.trending_business_recycler_view_parent);
        mTrendingListRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mTrendingListRecyclerView.setLayoutManager(mLayoutManager);

        trendingJson = SharedPrefManager.getTrendingBusiness(null);
        if (!TextUtils.isEmpty(trendingJson)) {
            mProgressBar.setVisibility(View.GONE);
            Gson gson = new Gson();
            mTrendingBusinessResponse = gson.fromJson(trendingJson, GetAllTrendingBusinessResponse.class);
            mTrendingBusinessList = mTrendingBusinessResponse.getTrendingBusinessList();
            mTrendingListAdapter = new TrendingListAdapter(mTrendingBusinessList);
            mTrendingListRecyclerView.setAdapter(mTrendingListAdapter);
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        if (ACLManager.hasServicesAccessibility(ServiceIdConstants.GET_SOURCE_OF_FUND)) {
            attemptGetSponsorList();
        }


        getTrendingBusinessList();
        getServiceProviderList();

        mBillPayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payBill(Constants.BLION, null);
            }
        });

        mAkashBillPayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payBill(Constants.BEXCOM, null);
            }
        });

        mLink3BillPayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payBill(Constants.LINK3, null);
            }
        });

        mBrilliantRechargeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payBill(Constants.BRILLIANT, null);
            }
        });

        mAmberITBillPayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payBill(Constants.AMBERIT, null);
            }
        });

        mLankaBanglaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payBill(Constants.LANKABANGLA, "CARD");
            }
        });

        mLankaBanglaDpsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payBill(Constants.LANKABANGLA, "DPS");
            }
        });

        mWestZoneBillPayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payBill(Constants.WESTZONE, null);
            }
        });

        mDescoBillPayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.DESCO)) {
                    DialogUtils.showServiceNotAllowedDialog(getContext());
                    return;
                }
                pinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                    @Override
                    public void ifPinAdded() {
                        Intent intent = new Intent(getActivity(), UtilityBillPaymentActivity.class);
                        intent.putExtra(Constants.SERVICE, Constants.DESCO);
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
                pinChecker.execute();
            }
        });

        mDozeBillPayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payBill(Constants.CARNIVAL, null);
            }
        });

        mDpdcBillPayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.DPDC)) {
                    DialogUtils.showServiceNotAllowedDialog(getContext());
                    return;
                }
                pinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                    @Override
                    public void ifPinAdded() {
                        Intent intent = new Intent(getActivity(), UtilityBillPaymentActivity.class);
                        intent.putExtra(Constants.SERVICE, Constants.DPDC);
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
                pinChecker.execute();
            }
        });

        mCreditCardBillPayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payBill(Constants.CREDIT_CARD, null);
            }
        });

        mRailwayTicketPurchaseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.RAILWAY_TICKET)) {
                    DialogUtils.showServiceNotAllowedDialog(getContext());
                    return;
                }
                pinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                    @Override
                    public void ifPinAdded() {
                        Intent intent = new Intent(getActivity(), RailwayTicketActionActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH);
                    }
                });
                pinChecker.execute();
            }
        });

        trendingBusinessListRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!trendingBusinessListRefreshLayout.isRefreshing()) {
                    getTrendingBusinessList();
                } else {
                    trendingBusinessListRefreshLayout.setRefreshing(false);
                }
            }
        });

        mMobileNumberEditText.setCustomItemClickListener(new MakePaymentContactsSearchView.CustomItemClickListener() {
            @Override
            public void onItemClick(String name, String mobileNumber, String imageURL, String address, Long outletId) {

                Bundle bundle = new Bundle();
                bundle.putString(Constants.NAME, name);
                bundle.putString(Constants.PHOTO_URI, imageURL);
                bundle.putString(Constants.MOBILE_NUMBER, mobileNumber);
                bundle.putString(Constants.ADDRESS, address);
                if (outletId != null)
                    bundle.putLong(Constants.OUTLET_ID, outletId);

                if (approvedSponsorArrayList != null) {
                    if (approvedSponsorArrayList.size() > 0) {
                        bundle.putSerializable(Constants.SPONSOR_LIST, approvedSponsorArrayList);
                    }
                }

                bundle.putInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, transactionType);
                if (getActivity() instanceof IPayTransactionActionActivity) {
                    ((IPayTransactionActionActivity) getActivity()).switchToMakePaymentAmountInputFragment(bundle);
                }
            }
        });

        mMobileNumberEditText.setCustomBillPaymentClickListener(new MakePaymentContactsSearchView.CustomBillPaymentClickListener() {
            @Override
            public void onItemClick(String name, String id) {
                if (name.equals(getContext().getString(R.string.lanka_bangla_card)))
                    payBill(id, "CARD");
                else if (name.equals(getContext().getString(R.string.lanka_bangla_dps)))
                    payBill(id, "DPS");
                else if (name.equals(getContext().getString(R.string.railway_ticket))) {
                    if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.RAILWAY_TICKET)) {
                        DialogUtils.showServiceNotAllowedDialog(getContext());
                        return;
                    }
                    pinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                        @Override
                        public void ifPinAdded() {
                            Intent intent = new Intent(getActivity(), RailwayTicketActionActivity.class);
                            startActivityForResult(intent, REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH);
                        }
                    });
                    pinChecker.execute();
                } else if (name.equals(getContext().getString(R.string.desco))) {
                    if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.DESCO)) {
                        DialogUtils.showServiceNotAllowedDialog(getContext());
                        return;
                    }
                    pinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                        @Override
                        public void ifPinAdded() {
                            Intent intent = new Intent(getActivity(), UtilityBillPaymentActivity.class);
                            intent.putExtra(Constants.SERVICE, Constants.DESCO);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    });
                    pinChecker.execute();
                }else if (name.equals(getContext().getString(R.string.dpdc))) {
                    if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.DPDC)) {
                        DialogUtils.showServiceNotAllowedDialog(getContext());
                        return;
                    }
                    pinChecker = new PinChecker(getActivity(), new PinChecker.PinCheckerListener() {
                        @Override
                        public void ifPinAdded() {
                            Intent intent = new Intent(getActivity(), UtilityBillPaymentActivity.class);
                            intent.putExtra(Constants.SERVICE, Constants.DPDC);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    });
                    pinChecker.execute();
                }else
                    payBill(id, null);
            }
        });
    }

    private void getAllBusinessAccountsList() {
        GetAllBusinessContactRequestBuilder mGetAllBusinessContactRequestBuilder = new GetAllBusinessContactRequestBuilder(0);
        new GetAllBusinessListAsyncTask(getContext(), mGetAllBusinessContactRequestBuilder.getGeneratedUri()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                    case Constants.WESTZONE:
                    case Constants.WASA:
                        intent = new Intent(getActivity(), UtilityBillPaymentActivity.class);
                        intent.putExtra(Constants.SERVICE, provider);
                        startActivity(intent);
                        getActivity().finish();
                        break;
                    case Constants.DESCO:
                        providerCode1 = "DESCO";
                        getSavedList(providerCode1);
                        break;
                    case Constants.DPDC:
                        providerCode1 = "DESCO";
                        getSavedList(providerCode1);
                        break;
                    case Constants.LINK3:
                        providerCode1 = "LINK3";
                        getSavedList(providerCode1);
                        break;
                    case Constants.CARNIVAL:
                        intent = new Intent(getActivity(), IPayUtilityBillPayActionActivity.class);
                        intent.putExtra(IPayUtilityBillPayActionActivity.BILL_PAY_PARTY_NAME_KEY, IPayUtilityBillPayActionActivity.BILL_PAY_AKASH);
                        startActivityForResult(intent, REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH);
                        getActivity().finish();
                        break;
                    case Constants.BEXCOM:
                        intent = new Intent(getActivity(), IPayUtilityBillPayActionActivity.class);
                        intent.putExtra(IPayUtilityBillPayActionActivity.BILL_PAY_PARTY_NAME_KEY, IPayUtilityBillPayActionActivity.BILL_PAY_CARNIVAL);
                        startActivityForResult(intent, REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH);
                        getActivity().finish();
                        break;
                    case Constants.BLION:
                        intent = new Intent(getActivity(), UtilityBillPaymentActivity.class);
                        intent.putExtra(Constants.SERVICE, Constants.BANGLALION);
                        startActivity(intent);
                        getActivity().finish();
                        break;
                    case Constants.CREDIT_CARD:
                        intent = new Intent(getActivity(), IPayUtilityBillPayActionActivity.class);
                        intent.putExtra(IPayUtilityBillPayActionActivity.BILL_PAY_PARTY_NAME_KEY, IPayUtilityBillPayActionActivity.CREDIT_CARD);
                        startActivityForResult(intent, REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH);
                        getActivity().finish();
                        break;
                    case Constants.LANKABANGLA:
                        if (type.equals("CARD")) {
                            typeText  = "CARD";
                            providerCode1 = "LANKABANGLA-VISA";
                            providerCode2 = "LANKABANGLA-MASTERCARD";
                            getSavedList(providerCode1,providerCode2);
                        }
                        else {
                            typeText  = "DPS";
                            providerCode1 = "LANKABANGLA-DPS";
                            getSavedList(providerCode1);
                        }
                        break;
                }
            }
        });
        pinChecker.execute();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void getTrendingBusinessList() {
        if (mGetTrendingBusinessListTask != null) {
            return;
        }

        mGetTrendingBusinessListTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_TRENDING_BUSINESS_LIST,
                Constants.BASE_URL_MM + Constants.URL_GET_BUSINESS_LIST_TRENDING, getActivity(), false);
        mGetTrendingBusinessListTask.mHttpResponseListener = this;
        mGetTrendingBusinessListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            mGetTrendingBusinessListTask = null;
            mGetUtilityProviderListTask = null;
            getSponsorListResponse = null;
            trendingBusinessListRefreshLayout.setRefreshing(false);
            return;
        }
        try {
            if (result.getApiCommand().equals(Constants.COMMAND_GET_TRENDING_BUSINESS_LIST)) {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    SharedPrefManager.setTrendingBusiness(result.getJsonString());
                    Gson gson = new Gson();
                    mTrendingBusinessResponse = gson.fromJson(result.getJsonString(), GetAllTrendingBusinessResponse.class);
                    mTrendingBusinessList = mTrendingBusinessResponse.getTrendingBusinessList();
                    mTrendingListAdapter = new TrendingListAdapter(mTrendingBusinessList);
                    mTrendingListRecyclerView.setAdapter(mTrendingListAdapter);

                }
                mGetTrendingBusinessListTask = null;
                trendingBusinessListRefreshLayout.setRefreshing(false);
                if (mProgressBar.getVisibility() == View.VISIBLE) {
                    mProgressBar.setVisibility(View.GONE);
                }
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
                }
            }else if (result.getApiCommand().equals(Constants.COMMAND_GET_SPONSOR_LIST)) {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    getSponsorListResponse = new Gson().fromJson(result.getJsonString(), GetSponsorListResponse.class);
                    sponsorArrayList = getSponsorListResponse.getSponsor();
                    getOnlyApprovedSponsors();

                } else {
                    Toast.makeText(getContext(), getSponsorListResponse.getMessage(), Toast.LENGTH_LONG).show();
                }
                getSponsorListAsyncTask = null;

            }else if (result.getApiCommand().equals(Constants.COMMAND_GET_SAVED_BILL_LIST)) {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    mSavedBillResponse = new Gson().fromJson(result.getJsonString(), GetSavedBillResponse.class);
                    mSavedBills = mSavedBillResponse.getSavedBills();
                    if(TextUtils.isEmpty(providerCode2))
                        mRecentBill = dataHelper.getBills(providerCode1);
                    else
                        mRecentBill = dataHelper.getBills(providerCode1, providerCode2);

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

                if(TextUtils.isEmpty(providerCode2))
                    mRecentBill = dataHelper.getBills(providerCode1);
                else
                    mRecentBill = dataHelper.getBills(providerCode1, providerCode2);

                switch (providerCode1){
                    case Constants.DESCO:
                    case Constants.DPDC:
                        Intent intent = new Intent(getActivity(), UtilityBillPaymentActivity.class);
                        intent.putExtra(Constants.SERVICE, providerCode1);

                        if((mRecentBill !=null && mRecentBill.size()>0) || (mSavedBills != null&& mSavedBills.size()>0)){
                            intent.putExtra("SAVED_DATA", (Serializable) mSavedBills);
                            intent.putExtra("RECENT_DATA", (Serializable) mRecentBill);
                        }
                        startActivityForResult(intent, REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH);
                        getActivity().finish();
                        break;
                    case Constants.LINK3:
                        intent = new Intent(getActivity(), IPayUtilityBillPayActionActivity.class);
                        intent.putExtra(IPayUtilityBillPayActionActivity.BILL_PAY_PARTY_NAME_KEY, IPayUtilityBillPayActionActivity.BILL_PAY_LINK_THREE);

                        if((mRecentBill !=null && mRecentBill.size()>0) || (mSavedBills != null&& mSavedBills.size()>0)){
                            intent.putExtra("SAVED_DATA", (Serializable) mSavedBills);
                            intent.putExtra("RECENT_DATA", (Serializable) mRecentBill);
                        }
                        startActivityForResult(intent, REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH);
                        getActivity().finish();
                        break;
                    case "LANKABANGLA-VISA":
                    case "LANKABANGLA-MASTERCARD":
                        intent = new Intent(getActivity(), IPayUtilityBillPayActionActivity.class);
                        if((mRecentBill !=null && mRecentBill.size()>0) || (mSavedBills != null&& mSavedBills.size()>0)){
                            intent.putExtra("SAVED_DATA", (Serializable) mSavedBills);
                            intent.putExtra("RECENT_DATA", (Serializable) mRecentBill);
                        }
                        intent.putExtra(IPayUtilityBillPayActionActivity.BILL_PAY_PARTY_NAME_KEY, IPayUtilityBillPayActionActivity.BILL_PAY_LANKABANGLA_CARD);
                        startActivityForResult(intent, REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH);
                        getActivity().finish();

                        break;

                    case "LANKABANGLA-DPS":
                        intent = new Intent(getActivity(), IPayUtilityBillPayActionActivity.class);
                        if((mRecentBill !=null && mRecentBill.size()>0) || (mSavedBills != null&& mSavedBills.size()>0)){
                            intent.putExtra("SAVED_DATA", (Serializable) mSavedBills);
                            intent.putExtra("RECENT_DATA", (Serializable) mRecentBill);
                        }
                        intent.putExtra(IPayUtilityBillPayActionActivity.BILL_PAY_PARTY_NAME_KEY, IPayUtilityBillPayActionActivity.BILL_PAY_LANKABANGLA_DPS);
                        startActivityForResult(intent, REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH);
                        getActivity().finish();

                        break;

                }
                mCustomProgressDialog.dismissDialogue();
                mGetSavedBillListTask = null;
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (getActivity() != null) {
                Toaster.makeText(getActivity(), R.string.business_contacts_sync_failed, Toast.LENGTH_LONG);
            }
        }
    }

    private void getOnlyApprovedSponsors() {
        for (int i = 0; i < sponsorArrayList.size(); i++) {
            if (sponsorArrayList.get(i).getStatus().equals("APPROVED")) {
                approvedSponsorArrayList.add(sponsorArrayList.get(i));
            }
        }
        QRCodePaymentActivity.sponsorList = approvedSponsorArrayList;
        HomeActivity.mSponsorList = approvedSponsorArrayList;
    }


    public class TrendingListAdapter extends RecyclerView.Adapter<TrendingListAdapter.MyViewHolder> {
        private List<TrendingBusinessList> trendingBusinessList;

        public TrendingListAdapter(List<TrendingBusinessList> trendingBusinessList) {
            this.trendingBusinessList = trendingBusinessList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_trending_business_category, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.titleView.setText(trendingBusinessList.get(position).getBusinessType());
            holder.trendingBusinessCAtegory.setNestedScrollingEnabled(false);
            List<BusinessList> mBusinessAccountEntryList = trendingBusinessList.get(position).getBusinessList();
            PayDashBoardItemAdapter payDashBoardItemAdapter = new PayDashBoardItemAdapter(mBusinessAccountEntryList, getActivity());
            holder.trendingBusinessCAtegory.setAdapter(payDashBoardItemAdapter);
            holder.trendingBusinessCAtegory.setLayoutManager(new GridLayoutManager(getContext(), 4));
        }

        @Override
        public int getItemCount() {
            return trendingBusinessList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView titleView;
            public RecyclerView trendingBusinessCAtegory;

            public MyViewHolder(View view) {
                super(view);
                titleView = view.findViewById(R.id.trending_business_category_title);
                trendingBusinessCAtegory = view.findViewById(R.id.trending_business_recycler_view_category);
            }
        }
    }

    private void getSavedList(String providerCode) {
        mCustomProgressDialog.show();
        if (mGetSavedBillListTask != null) {
            return;
        }

        mGetSavedBillListTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SAVED_BILL_LIST,
                Constants.BASE_URL_UTILITY + "scheduled/saved-bills/?providerCodes="+providerCode, getActivity(), false);
        mGetSavedBillListTask.mHttpResponseListener = this;
        mGetSavedBillListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getSavedList(String providerCode1, String providerCode2) {
        mCustomProgressDialog.show();
        if (mGetSavedBillListTask != null) {
            return;
        }

        mGetSavedBillListTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SAVED_BILL_LIST,
                Constants.BASE_URL_UTILITY + "scheduled/saved-bills/?providerCodes="+providerCode1+"&providerCodes="+providerCode2, getActivity(), false);
        mGetSavedBillListTask.mHttpResponseListener = this;
        mGetSavedBillListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void attemptGetSponsorList() {
        if (getSponsorListAsyncTask != null) {
            return;
        } else {
            getSponsorListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SPONSOR_LIST, Constants.BASE_URL_MM + Constants.URL_GET_SPONSOR,
                    getContext(), this, true);
            getSponsorListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public class PayDashBoardItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<BusinessList> mBusinessAccountEntryList;
        private TrendingBusinessOutletSelectorDialog mMerchantBranchSelectorDialog;
        Context context;

        public PayDashBoardItemAdapter(List<BusinessList> mBusinessAccountEntryList, Context context) {
            this.mBusinessAccountEntryList = mBusinessAccountEntryList;
            this.context = context;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView mImageView;
            private TextView mTextView;

            public ViewHolder(final View itemView) {
                super(itemView);
                mImageView = itemView.findViewById(R.id.imageView);
                mTextView = itemView.findViewById(R.id.nameView);
            }

            public void bindView(final int pos) {
                final BusinessList merchantDetails = mBusinessAccountEntryList.get(pos);
                final String name = merchantDetails.getMerchantName();
                final String imageUrl = Constants.BASE_URL_FTP_SERVER + merchantDetails.getBusinessLogo();
                mTextView.setText(name);

                try {

                    final DrawableTypeRequest<String> glide = Glide.with(context).load(imageUrl);

                    glide.diskCacheStrategy(DiskCacheStrategy.ALL);

                    glide.placeholder(R.drawable.ic_business_logo_round)
                            .error(R.drawable.ic_business_logo_round)
                            .crossFade()
                            .dontAnimate()
                            .into(mImageView);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.MAKE_PAYMENT)) {
                            DialogUtils.showServiceNotAllowedDialog(context);
                        } else {

                            PinChecker payByQCPinChecker = new PinChecker(context, new PinChecker.PinCheckerListener() {
                                @Override
                                public void ifPinAdded() {
                                    if (mBusinessAccountEntryList.get(pos).getOutlets() != null && mBusinessAccountEntryList.get(pos).getOutlets().size() > 0) {
                                        if (mBusinessAccountEntryList.get(pos).getOutlets().size() > 1) {
                                            mMerchantBranchSelectorDialog = new TrendingBusinessOutletSelectorDialog(context, mBusinessAccountEntryList.get(pos));
                                            mMerchantBranchSelectorDialog.showDialog();
                                            mMerchantBranchSelectorDialog.setCustomItemClickListener(new TrendingBusinessOutletSelectorDialog.CustomItemClickListener() {
                                                @Override
                                                public void onItemClick(String name, String mobileNumber, String imageURL, String address, Long outletId) {
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString(Constants.NAME, name);
                                                    bundle.putString(Constants.PHOTO_URI, Constants.BASE_URL_FTP_SERVER + imageURL);
                                                    bundle.putString(Constants.MOBILE_NUMBER, mobileNumber);
                                                    bundle.putString(Constants.ADDRESS, address);
                                                    bundle.putLong(Constants.OUTLET_ID, outletId);
                                                    if (!(approvedSponsorArrayList == null || approvedSponsorArrayList.size() == 0)) {
                                                        bundle.putSerializable(Constants.SPONSOR_LIST, approvedSponsorArrayList);
                                                    }
                                                    bundle.putInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, transactionType);
                                                    if (getActivity() instanceof IPayTransactionActionActivity) {
                                                        ((IPayTransactionActionActivity) getActivity()).switchToMakePaymentAmountInputFragment(bundle);
                                                    }
                                                }
                                            });
                                        } else {
                                            Bundle bundle = new Bundle();
                                            bundle.putString(Constants.NAME, merchantDetails.getMerchantName());
                                            bundle.putString(Constants.PHOTO_URI, Constants.BASE_URL_FTP_SERVER + merchantDetails.getBusinessLogo());
                                            bundle.putString(Constants.MOBILE_NUMBER, merchantDetails.getMerchantMobileNumber());
                                            bundle.putString(Constants.ADDRESS, merchantDetails.getOutlets().get(0).getAddressString());
                                            bundle.putLong(Constants.OUTLET_ID, merchantDetails.getOutlets().get(0).getOutletId());

                                            if (!(approvedSponsorArrayList == null || approvedSponsorArrayList.size() == 0)) {
                                                bundle.putSerializable(Constants.SPONSOR_LIST, approvedSponsorArrayList);
                                            }
                                            bundle.putInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, transactionType);
                                            if (getActivity() instanceof IPayTransactionActionActivity) {
                                                ((IPayTransactionActionActivity) getActivity()).switchToMakePaymentAmountInputFragment(bundle);
                                            }
                                        }
                                    } else {
                                        Bundle bundle = new Bundle();
                                        bundle.putString(Constants.NAME, merchantDetails.getMerchantName());
                                        bundle.putString(Constants.PHOTO_URI, Constants.BASE_URL_FTP_SERVER + merchantDetails.getBusinessLogo());
                                        bundle.putString(Constants.MOBILE_NUMBER, merchantDetails.getMerchantMobileNumber());
                                        bundle.putString(Constants.ADDRESS, merchantDetails.getAddressString());
                                        bundle.putString(Constants.TYPE, merchantDetails.getBusinessType());

                                        if (!(approvedSponsorArrayList == null || approvedSponsorArrayList.size() == 0)) {
                                            bundle.putSerializable(Constants.SPONSOR_LIST, approvedSponsorArrayList);
                                        }
                                        bundle.putInt(IPayTransactionActionActivity.TRANSACTION_TYPE_KEY, transactionType);
                                        if (getActivity() instanceof IPayTransactionActionActivity) {
                                            ((IPayTransactionActionActivity) getActivity()).switchToMakePaymentAmountInputFragment(bundle);
                                        }
                                    }
                                }
                            });
                            payByQCPinChecker.execute();
                        }
                    }
                });
            }
        }

        class NormalViewHolder extends ViewHolder {
            NormalViewHolder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new NormalViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_trending_business, parent, false));
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
            return mBusinessAccountEntryList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SUCCESSFUL_ACTIVITY_FINISH)
            getActivity().finish();
    }
}
