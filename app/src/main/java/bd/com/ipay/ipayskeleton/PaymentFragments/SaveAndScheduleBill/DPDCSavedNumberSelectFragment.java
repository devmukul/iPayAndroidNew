package bd.com.ipay.ipayskeleton.PaymentFragments.SaveAndScheduleBill;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestDeleteAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.DatabaseHelper.DataHelper;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.BillParam;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.GetSavedBillResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.MetaData;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.RecentBill;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.SavedBill;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.LinkThree.LinkThreeSubscriberIdInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;
import bd.com.ipay.ipayskeleton.Widget.View.SaveBillDialog;
import bd.com.ipay.ipayskeleton.Widget.View.ScheduleBillDialog;

public class DPDCSavedNumberSelectFragment extends Fragment implements HttpResponseListener{

	private RecyclerView mSavedListRecyclerView;
	private RecyclerView mRecentListRecyclerView;
	private View mRecentListView;
	private View mSavedListView;

	private List<SavedBill> savedBills;
    private List<RecentBill> recentBills;
	private SavedListAdapter mSavedAdapter;
    private RecentListAdapter mRecentAdapter;
    private String title;
    private Button payOtherAccountButton;

    private HttpRequestPostAsyncTask saveBillTask = null;
    private HttpRequestPutAsyncTask scheduleBillTask = null;
    private HttpRequestDeleteAsyncTask deleteBillTask = null;
    protected AnimatedProgressDialog customProgressDialog;

    private SavedBill mSavedBill = new SavedBill();
    DataHelper dataHelper ;
    private HttpRequestGetAsyncTask mGetSavedBillListTask;
    private GetSavedBillResponse mSavedBillResponse;
    SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        dataHelper = DataHelper.getInstance(getContext());
        if (getArguments() != null) {
            title = getArguments().getString(Constants.NAME);
            savedBills = (List<SavedBill>) getArguments().getSerializable("SAVED_DATA");
            recentBills = (List<RecentBill>) getArguments().getSerializable("RECENT_DATA");
        }
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_desco_bill_payment_saved_number_select, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.dpdc));

        customProgressDialog = new AnimatedProgressDialog(getActivity());
        customProgressDialog.setCancelable(false);

        mSavedListView = view.findViewById(R.id.saved_bill_view);
        mRecentListView = view.findViewById(R.id.recent_bill_view);
        payOtherAccountButton = view.findViewById(R.id.pay_bill_other_account);

        if(savedBills == null || savedBills.size()<1)
            mSavedListView.setVisibility(View.GONE);
        if(recentBills == null || recentBills.size()<1)
            mRecentListView.setVisibility(View.GONE);

        mSavedListRecyclerView = view.findViewById(R.id.saved_list_recycler_view);
        mSavedListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSavedAdapter = new SavedListAdapter();
        mSavedListRecyclerView.setAdapter(mSavedAdapter);

        mRecentListRecyclerView = view.findViewById(R.id.recent_list_recycler_view);
        mRecentListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecentAdapter = new RecentListAdapter();
        mRecentListRecyclerView.setAdapter(mRecentAdapter);

        payOtherAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((UtilityBillPaymentActivity) getActivity()).
                        switchToDpdcBillPaymentFragment();
            }
        });
	}

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (getActivity() == null)
            return;

        if (HttpErrorHandler.isErrorFound(result, getContext(), customProgressDialog)) {
            customProgressDialog.dismissDialog();

            saveBillTask = null;
            scheduleBillTask = null;
            deleteBillTask = null;
            mGetSavedBillListTask = null;
        } else {
            try {
                switch (result.getApiCommand()) {
                    case Constants.COMMAND_LINK_THREE_BILL_PAY_SAVE:
                        GetSavedBillResponse savedBillResponse = new Gson().fromJson(result.getJsonString(), GetSavedBillResponse.class);
                        switch (result.getStatus()) {
                            case Constants.HTTP_RESPONSE_STATUS_OK:
                                getSavedList("DPDC");
                                break;
                            default:
                                if (getActivity() != null) {
                                    customProgressDialog.showFailureAnimationAndMessage(savedBillResponse.getMessage());
                                    break;
                                }
                                break;
                        }
                        saveBillTask = null;
                        break;
                    case Constants.COMMAND_LINK_THREE_BILL_PAY_DELETE:
                        GetSavedBillResponse deleteBillResponse = new Gson().fromJson(result.getJsonString(), GetSavedBillResponse.class);
                        switch (result.getStatus()) {
                            case Constants.HTTP_RESPONSE_STATUS_OK:
                                getSavedList("DPDC");
                                break;
                            default:
                                if (getActivity() != null) {
                                    customProgressDialog.showFailureAnimationAndMessage(deleteBillResponse.getMessage());
                                    break;
                                }
                                break;
                        }
                        deleteBillTask = null;
                        break;
                    case Constants.COMMAND_GET_SAVED_BILL_LIST:
                        if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                            mSavedBillResponse = new Gson().fromJson(result.getJsonString(), GetSavedBillResponse.class);
                            savedBills = mSavedBillResponse.getSavedBills();
                            recentBills = dataHelper.getBills("DPDC");
                            if(savedBills!=null && savedBills.size()>0) {

                                List<RecentBill> tempRecentBills = new ArrayList<>();

                                for(RecentBill recentBill: recentBills) {
                                    String providerCode = recentBill.getParamValue();
                                    boolean isFound = false;
                                    for (SavedBill savedBill : savedBills) {
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
                                mSavedListView.setVisibility(View.VISIBLE);
                            }else {
                                mSavedListView.setVisibility(View.GONE);
                                List<RecentBill> tempRecentBills = new ArrayList<>();

                                for(RecentBill recentBill: recentBills) {
                                    recentBill.setSaved(false);
                                    tempRecentBills.add(recentBill);
                                }
                                dataHelper.createBills(tempRecentBills);
                            }
                        }

                        recentBills = dataHelper.getBills("DPDC");



                        if(recentBills==null || recentBills.size()<1) {
                            mRecentListView.setVisibility(View.GONE);
                        }else {

                            mRecentListView.setVisibility(View.VISIBLE);
                        }
                        mSavedAdapter.notifyDataSetChanged();
                        mRecentAdapter.notifyDataSetChanged();

                        customProgressDialog.setTitle(R.string.success);
                        customProgressDialog.showSuccessAnimationAndMessage(getString(R.string.success));
                        mGetSavedBillListTask = null;
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();

                saveBillTask = null;
                scheduleBillTask = null;
                deleteBillTask = null;
                mGetSavedBillListTask = null;
                customProgressDialog.showFailureAnimationAndMessage(getString(R.string.failed));
            }
        }

    }

    public class SavedListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final LayoutInflater layoutInflater;

        SavedListAdapter() {
            layoutInflater = LayoutInflater.from(getContext());
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SavedListAdapter.ViewHolder(layoutInflater.inflate(R.layout.list_item_saved_bill, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            try {
                SavedListAdapter.ViewHolder vh = (SavedListAdapter.ViewHolder) holder;
                vh.bindView(position);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (savedBills == null) {
                return 0;
            } else {
                return savedBills.size();
            }
        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.list_item_contact;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final View itemView;

            private final TextView billImageView;
            private final TextView accountNo;
            private final TextView nameTextView;
            private final TextView dateTextView;
            //private final ImageView scheduleImage;
            private final ImageView deleteScheduleImage;

            public ViewHolder(View itemView) {
                super(itemView);

                this.itemView = itemView;

                billImageView = itemView.findViewById(R.id.bill_image);
                accountNo = itemView.findViewById(R.id.account_no);
                nameTextView = itemView.findViewById(R.id.name_text);
                dateTextView = itemView.findViewById(R.id.last_payment_date);

                //scheduleImage = itemView.findViewById(R.id.button_schedule);
                deleteScheduleImage = itemView.findViewById(R.id.delete_schedule);
            }

            public void bindView(final int pos) {

                final SavedBill savedBill = savedBills.get(pos);

                accountNo.setText(savedBill.getBillParams().get(0).getParamLabel()+": "+savedBill.getBillParams().get(0).getParamValue());
                nameTextView.setText(savedBill.getShortName());
                billImageView.setText(String.valueOf(pos+1));
                deleteScheduleImage.setVisibility(View.VISIBLE);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("IS_FROM_HISTORY", true);
                        bundle.putString(Constants.ACCOUNT_ID, savedBill.getBillParams().get(0).getParamValue());
                        bundle.putString("META_DATA", new Gson().toJson(savedBill.getMetaData()));

                        bundle.putString(Constants.NAME, savedBill.getShortName());

                        ((UtilityBillPaymentActivity) getActivity()).switchToDpdcBillPaymentFragment(bundle);
                    }
                });

                deleteScheduleImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSavedBill = savedBill;
                        deleteBill(savedBill.getId());

                    }
                });
            }
        }

    }


    public class RecentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final LayoutInflater layoutInflater;

        RecentListAdapter() {
            layoutInflater = LayoutInflater.from(getContext());
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecentListAdapter.RecentViewHolder(layoutInflater.inflate(R.layout.list_item_saved_bill, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            try {
                RecentViewHolder vh = (RecentViewHolder) holder;
                vh.bindView(position);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (recentBills == null) {
                return 0;
            } else {
                return recentBills.size();
            }
        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.list_item_contact;
        }

        public class RecentViewHolder extends RecyclerView.ViewHolder {
            private final View itemView;

            private final TextView billImageView;
            private final TextView accountNo;
            private final TextView nameTextView;
            private final TextView dateTextView;
            private final ImageView saveImage;

            public RecentViewHolder(View itemView) {
                super(itemView);

                this.itemView = itemView;

                billImageView = itemView.findViewById(R.id.bill_image);
                accountNo = itemView.findViewById(R.id.account_no);
                nameTextView = itemView.findViewById(R.id.name_text);
                dateTextView = itemView.findViewById(R.id.last_payment_date);

                saveImage = itemView.findViewById(R.id.button_save);
            }

            public void bindView(final int pos) {
                /*
                 * We need to show original name on the top if exists
                 */

                final RecentBill recentBill = recentBills.get(pos);

                accountNo.setText(recentBill.getParamLabel()+": "+recentBill.getParamValue());
                if(TextUtils.isEmpty(recentBill.getShortName()))
                    nameTextView.setVisibility(View.GONE);
                else{
                    nameTextView.setVisibility(View.VISIBLE);
                    nameTextView.setText(recentBill.getShortName());
                }
                billImageView.setText(String.valueOf(pos+1));

                if(recentBill.getSaved()){
                    saveImage.setVisibility(View.GONE);
                }else{
                    saveImage.setVisibility(View.VISIBLE);
                }

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.ACCOUNT_ID, recentBill.getParamValue());
                        bundle.putString("META_DATA", recentBill.getMetaData());
                        bundle.putBoolean("IS_FROM_HISTORY", true);
                        bundle.putString(Constants.NAME, recentBill.getShortName());
                        ((UtilityBillPaymentActivity) getActivity()).switchToDpdcBillPaymentFragment(bundle);


                    }
                });

                saveImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showSaveInfoDialogue(recentBill);
                    }
                });
            }

        }

    }

    public void showSaveInfoDialogue(final RecentBill recentBill) {
        if (getActivity() == null)
            return;

        final SaveBillDialog saveBillDialog = new SaveBillDialog(getContext());
        saveBillDialog.setTitle(getString(R.string.save_bill_info));
        saveBillDialog.setAccountInfo(recentBill.getParamValue(), getString(R.string.customer_number));

        saveBillDialog.setCloseButtonAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.hideKeyboard(getContext(), v);
                saveBillDialog.cancel();
            }
        });
        saveBillDialog.setPayBillButtonAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBillDialog.cancel();
                saveAndSchedule(recentBill, saveBillDialog.getBillTitleInfo(), saveBillDialog.getIsScheduled(), saveBillDialog.getScheduledateInfo() );

            }
        });
        saveBillDialog.show();
    }

    public void saveAndSchedule(RecentBill recentBill, String shortname, boolean isSchedule, String date) {
        if (!Utilities.isConnectionAvailable(getContext())) {
            Toaster.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT);
        }
        if (saveBillTask == null) {

            Date c = new Date();
            SimpleDateFormat curFormater = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String formattedDate = curFormater.format(c);
            SavedBill savedBill = new SavedBill();
            MetaData metaData = new Gson().fromJson(recentBill.getMetaData(), MetaData.class);
            if(metaData!=null){
                savedBill.setPaidForOthers(true);
            }else{
                savedBill.setPaidForOthers(false);
            }

            List<BillParam> billParamList = new ArrayList<>();
            billParamList.add(new BillParam( getString(R.string.customer_number), "customerNumber", recentBill.getParamValue(), "String"));
            billParamList.add(new BillParam("Amount", "amount", recentBill.getAmount(), "Number"));
            billParamList.add(new BillParam(getString(R.string.location_code), "locationCode", recentBill.getLocationCode(), "Number"));

            savedBill.setShortName(shortname);
            if(isSchedule) {
                savedBill.setIsScheduledToo(true);
                savedBill.setDateOfBillPayment(Integer.valueOf(date));
                savedBill.setSkipNumberOfMonths(0);
            }else {
                savedBill.setIsScheduledToo(false);
                savedBill.setDateOfBillPayment(1);
                savedBill.setSkipNumberOfMonths(0);
            }
            savedBill.setProviderCode(recentBill.getProviderCode());
            savedBill.setLastPaid(formattedDate);
            savedBill.setBillParams(billParamList);
            savedBill.setMetaData(metaData);

            String json = new Gson().toJson(savedBill);
            String uri = Constants.BASE_URL_UTILITY + Constants.URL_SAVED_BILL;
            saveBillTask = new HttpRequestPostAsyncTask(Constants.COMMAND_LINK_THREE_BILL_PAY_SAVE,
                    uri, json, getActivity(), this, false);
            saveBillTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            customProgressDialog.showDialog();
        }
    }

    public void deleteBill(int id) {
        if (!Utilities.isConnectionAvailable(getContext())) {
            Toaster.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT);
        }
        if (deleteBillTask == null) {
            String uri = Constants.BASE_URL_UTILITY + Constants.URL_SAVED_BILL+"/"+id;
            deleteBillTask = new HttpRequestDeleteAsyncTask(Constants.COMMAND_LINK_THREE_BILL_PAY_DELETE,
                    uri, getActivity(), this, false);
            deleteBillTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            customProgressDialog.showDialog();
        }
    }

    private void getSavedList(String providerCode) {
        if (mGetSavedBillListTask != null) {
            return;
        }

        mGetSavedBillListTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SAVED_BILL_LIST,
                Constants.BASE_URL_UTILITY + "scheduled/saved-bills/?providerCodes="+providerCode, getActivity(), false);
        mGetSavedBillListTask.mHttpResponseListener = this;
        mGetSavedBillListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


}
