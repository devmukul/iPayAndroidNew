package bd.com.ipay.ipayskeleton.PaymentFragments.IPDC;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.GetScheduledPaymentInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.GroupedScheduledPaymentList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.ScheduledPaymentInfo;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ShedulePaymentConstant;

public class GlobalScheduledPaymentListFragment extends Fragment implements HttpResponseListener {
    private RecyclerView scheduledPaymentListRecyclerView;
    private HttpRequestGetAsyncTask getScheduledPaymentListTask;
    private ScheduledPaymentListAdapter scheduledPaymentAdapter;
    private CustomProgressDialog progressDialog;
    private List<GroupedScheduledPaymentList> groupedScheduledPaymentInfoList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_global_scheduled_payment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scheduledPaymentListRecyclerView = view.findViewById(R.id.scheduled_payment_list);
        progressDialog = new CustomProgressDialog(getContext());
        groupedScheduledPaymentInfoList = new ArrayList<>();
        scheduledPaymentListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ((TextView) view.findViewById(R.id.title)).setText(getString(R.string.scheduled_payment));
        ((ImageView) view.findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        getScheduledPaymentList();
    }

    private void getScheduledPaymentList() {

        if (getScheduledPaymentListTask != null) {
            return;
        } else {
            progressDialog.showDialog();
            String uri = Constants.BASE_URL_SCHEDULED_PAYMENT + Constants.URL_GET_SCHEDULED_PAYMENT_LIST;
            getScheduledPaymentListTask = new HttpRequestGetAsyncTask
                    (Constants.COMMAND_GET_SCHEDULED_PAYMENT_LIST, uri, getContext(), this,
                            false);
            getScheduledPaymentListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }

    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        getScheduledPaymentListTask = null;
        progressDialog.dismissDialog();
        if (HttpErrorHandler.isErrorFoundWithout404(result, getContext(), null)) {
            getScheduledPaymentListTask = null;

            if (result != null && result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
                GenericResponseWithMessageOnly genericResponseWithMessageOnly =
                        new Gson().fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                new AlertDialog.Builder(getContext())
                        .setPositiveButton(genericResponseWithMessageOnly.getMessage(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
            return;
        } else {
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                GetScheduledPaymentInfoResponse getScheduledPaymentInfoResponse = new Gson().
                        fromJson(result.getJsonString(), GetScheduledPaymentInfoResponse.class);
                groupedScheduledPaymentInfoList = getScheduledPaymentInfoResponse.getGroupedScheduledPaymentList();
                scheduledPaymentAdapter = new ScheduledPaymentListAdapter(groupedScheduledPaymentInfoList);
                scheduledPaymentListRecyclerView.setAdapter(scheduledPaymentAdapter);
                scheduledPaymentListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                scheduledPaymentAdapter.notifyDataSetChanged();

            } else {
                GenericResponseWithMessageOnly genericResponseWithMessageOnly =
                        new Gson().fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                new AlertDialog.Builder(getContext())
                        .setPositiveButton(genericResponseWithMessageOnly.getMessage(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

            }

        }

    }

    public class ScheduledPaymentListAdapter extends RecyclerView.Adapter<ScheduledPaymentListAdapter.ScheduledPaymentViewHolder> {
        private List<GroupedScheduledPaymentList> scheduledPaymentInfoList;

        public ScheduledPaymentListAdapter(List<GroupedScheduledPaymentList> scheduledPaymentInfoList) {
            this.scheduledPaymentInfoList = scheduledPaymentInfoList;
        }

        @NonNull
        @Override
        public ScheduledPaymentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ScheduledPaymentViewHolder(LayoutInflater.from(getContext()).
                    inflate(R.layout.list_item_schedule_payment_service_list, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduledPaymentViewHolder scheduledPaymentViewHolder, final int i) {
            scheduledPaymentViewHolder.productNameTextView.setText(scheduledPaymentInfoList.get(i).getReceiverInfo().getName());
            int pending =0;
            int running =0;

            for(ScheduledPaymentInfo scheduledPaymentInfo : scheduledPaymentInfoList.get(i).getScheduledPaymentInfos()){
                if(scheduledPaymentInfo.getStatus() == ShedulePaymentConstant.ScheduledPayment.RUNNING){
                    running = running+1;
                }

                if(scheduledPaymentInfo.getStatus() == ShedulePaymentConstant.ScheduledPayment.WAITING_FOR_USER_APPROVAL ||
                        scheduledPaymentInfo.getStatus() == ShedulePaymentConstant.ScheduledPayment.WAITING_FOR_UPDATE_APPROVAL){
                    pending = pending+1;
                }
            }

            scheduledPaymentViewHolder.runningCountTextView.setText(running + " Running");
            scheduledPaymentViewHolder.pendingCountTextView.setText(pending + " Pending");
            try {
                scheduledPaymentViewHolder.productImageView.setProfilePicture(scheduledPaymentInfoList.get(i).getReceiverInfo().getProfilePictures().get(0).getUrl(),true);
            }catch (Exception e){
                e.printStackTrace();
            }


            scheduledPaymentViewHolder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("scheduledPaymentList", groupedScheduledPaymentInfoList.get(i));
                    ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new IpdcScheduledPaymentFragment(), bundle, 1, true);
                }
            });
        }

        @Override
        public int getItemCount() {
            if (this.scheduledPaymentInfoList == null) return 0;
            else return this.scheduledPaymentInfoList.size();
        }

        public class ScheduledPaymentViewHolder extends RecyclerView.ViewHolder {
            private ProfileImageView productImageView;
            private TextView productNameTextView;
            private TextView runningCountTextView;
            private TextView pendingCountTextView;
            private View parentView;

            public ScheduledPaymentViewHolder(@NonNull View itemView) {
                super(itemView);
                productImageView = (ProfileImageView) itemView.findViewById(R.id.product_image);
                productNameTextView = (TextView) itemView.findViewById(R.id.product_name);
                runningCountTextView = (TextView) itemView.findViewById(R.id.running_item_text);
                pendingCountTextView = (TextView) itemView.findViewById(R.id.pending_item_text);
                parentView = itemView;
            }
        }
    }
}

