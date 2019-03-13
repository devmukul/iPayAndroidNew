package bd.com.ipay.ipayskeleton.PaymentFragments;

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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GetScheduledPaymentInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.ScheduledPaymentInfo;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class IpdcScheduledPaymentListFragment extends Fragment {
    private RecyclerView scheduledPaymentListRecyclerView;
    private List<ScheduledPaymentInfo> groupedScheduledPaymentList;
    private HttpRequestGetAsyncTask getScheduledPaymentListTask;
    private ScheduledPaymentListAdapter scheduledPaymentAdapter;
    private CustomProgressDialog progressDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ipdc_scheduled_payment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scheduledPaymentAdapter = new ScheduledPaymentListAdapter();
        scheduledPaymentListRecyclerView = view.findViewById(R.id.scheduled_payment_list);
        scheduledPaymentListRecyclerView.setAdapter(new ScheduledPaymentListAdapter());
        progressDialog = new CustomProgressDialog(getContext());

        scheduledPaymentListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getScheduledPaymentList();
    }

    private void getScheduledPaymentList() {

        if (getScheduledPaymentListTask != null) {

        } else {
            progressDialog.showDialog();
            String uri = Constants.BASE_URL_SCHEDULED_PAYMENT + Constants.URL_GET_SCHEDULED_PAYMENT_LIST;
            getScheduledPaymentListTask = new HttpRequestGetAsyncTask
                    (Constants.COMMAND_GET_SCHEDULED_PAYMENT_LIST, uri, getContext(),
                            new HttpResponseListener() {
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
                                            groupedScheduledPaymentList = getScheduledPaymentInfoResponse.getGroupedScheduledPaymentList();
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
                            },
                            false);
            getScheduledPaymentListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }

    }

    public class ScheduledPaymentListAdapter extends RecyclerView.Adapter<ScheduledPaymentListAdapter.ScheduledPaymentViewHolder> {

        @NonNull
        @Override
        public ScheduledPaymentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ScheduledPaymentViewHolder(LayoutInflater.from(getContext()).
                    inflate(R.layout.list_item_scheduled_payment, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduledPaymentViewHolder scheduledPaymentViewHolder, int i) {
            scheduledPaymentViewHolder.productNameTextView.setText(groupedScheduledPaymentList.get(i).getProduct());
            Glide.with(getContext())
                    .load(groupedScheduledPaymentList.get(i).getReceiverInfo().getPhotoUrl())
                    .error(R.layout.profile_image_view)
                    .into(scheduledPaymentViewHolder.productImageView);
        }

        @Override
        public int getItemCount() {
            if (groupedScheduledPaymentList == null) return 0;
            else return groupedScheduledPaymentList.size();
        }

        public class ScheduledPaymentViewHolder extends RecyclerView.ViewHolder {
            private RoundedImageView productImageView;
            private TextView productNameTextView;

            public ScheduledPaymentViewHolder(@NonNull View itemView) {
                super(itemView);
                productImageView = (RoundedImageView) itemView.findViewById(R.id.product_image);
                productNameTextView = (TextView) itemView.findViewById(R.id.product_name);
            }
        }
    }
}

