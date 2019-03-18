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

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GetScheduledPaymentInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GroupedScheduledPaymentInfo;
import bd.com.ipay.ipayskeleton.PaymentFragments.IPDC.IpdcScheduledPaymentFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class GlobalScheduledPaymentListFragment extends Fragment implements HttpResponseListener {
    private RecyclerView scheduledPaymentListRecyclerView;
    private HttpRequestGetAsyncTask getScheduledPaymentListTask;
    private ScheduledPaymentListAdapter scheduledPaymentAdapter;
    private CustomProgressDialog progressDialog;
    private List<GroupedScheduledPaymentInfo> groupedScheduledPaymentInfoList;


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
        ((TextView) view.findViewById(R.id.title)).setText(getString(R.string.schedule_payment_service_list));
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
        private List<GroupedScheduledPaymentInfo> scheduledPaymentInfoList;

        public ScheduledPaymentListAdapter(List<GroupedScheduledPaymentInfo> scheduledPaymentInfoList) {
            this.scheduledPaymentInfoList = scheduledPaymentInfoList;
        }

        @NonNull
        @Override
        public ScheduledPaymentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ScheduledPaymentViewHolder(LayoutInflater.from(getContext()).
                    inflate(R.layout.list_item_scheduled_payment, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduledPaymentViewHolder scheduledPaymentViewHolder, int i) {
            scheduledPaymentViewHolder.productNameTextView.setText(scheduledPaymentInfoList.get(i).getReceiverInfo().getName());
            scheduledPaymentViewHolder.totalInstallmentCountTextView.setText(scheduledPaymentInfoList.get(i).getScheduledPaymentInfos().size() + " running");
            try {
                Glide.with(getContext())
                        .load(scheduledPaymentInfoList.get(i).getReceiverInfo().getProfilePictures().get(0).getUrl())
                        .error(R.drawable.ic_profile)
                        .into(scheduledPaymentViewHolder.productImageView);
            } catch (Exception e) {
                Glide.with(getContext())
                        .load(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .into(scheduledPaymentViewHolder.productImageView);
            }
            scheduledPaymentViewHolder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("scheduledPaymentList", (Serializable) groupedScheduledPaymentInfoList);
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
            private RoundedImageView productImageView;
            private TextView productNameTextView;
            private TextView totalInstallmentCountTextView;
            private View parentView;

            public ScheduledPaymentViewHolder(@NonNull View itemView) {
                super(itemView);
                productImageView = (RoundedImageView) itemView.findViewById(R.id.product_image);
                productNameTextView = (TextView) itemView.findViewById(R.id.product_name);
                totalInstallmentCountTextView = (TextView) itemView.findViewById(R.id.installment_count);
                parentView = itemView;
            }
        }
    }
}

