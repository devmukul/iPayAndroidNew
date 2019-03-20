package bd.com.ipay.ipayskeleton.PaymentFragments.IPDC;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPDC.GetSchedulePaymentDetailsResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPDC.GetScheduledPaymentInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPDC.InstallmentInfoList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPDC.ScheduledPaymentInfo;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IpdcScheduledPaymentDetailsFragment extends Fragment implements HttpResponseListener {
    private RecyclerView scheduledPaymentListRecyclerView;
    private long id;

    private HttpRequestGetAsyncTask getScheduledPaymentListTask;
    private CustomProgressDialog progressDialog;
    List<InstallmentInfoList> installmentInfoList;
    private ScheduledPaymentListAdapter scheduledPaymentAdapter;


    private TextView productImageView;
    private TextView productNameTextView;
    private TextView createdAtTextView;
    private TextView mPaymentDateTextView;
    private TextView mLoanAmountTextView;
    private TextView mInstallmentAmountTextView;
    private TextView mPaidAmountTextView;
    private TextView mNoOfInstallmentTextView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scheduled_payment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        id = getArguments().getLong("ID");
        progressDialog = new CustomProgressDialog(getContext());
        scheduledPaymentListRecyclerView = view.findViewById(R.id.scheduled_payment_list);
        ((TextView) view.findViewById(R.id.title)).setText(getString(R.string.my_schedule_list));
        ((ImageView) view.findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        productImageView = view.findViewById(R.id.product_image);
        productNameTextView = view.findViewById(R.id.product_name);
        createdAtTextView = view.findViewById(R.id.created_at);

        mPaymentDateTextView = view.findViewById(R.id.schedule_date_view);
        mLoanAmountTextView = view.findViewById(R.id.loan_amount_view);
        mInstallmentAmountTextView = view.findViewById(R.id.installment_amount_view);
        mPaidAmountTextView = view.findViewById(R.id.paid_amount_view);
        mNoOfInstallmentTextView = view.findViewById(R.id.no_of_installment_view);

        getScheduledPaymentList();
    }

    private void getScheduledPaymentList() {

        if (getScheduledPaymentListTask != null) {
            return;
        } else {
            progressDialog.showDialog();
            String uri = Constants.BASE_URL_SCHEDULED_PAYMENT + Constants.URL_GET_SCHEDULED_PAYMENT_DETAILS+id;
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
                GetSchedulePaymentDetailsResponse getSchedulePaymentDetailsResponse = new Gson().
                        fromJson(result.getJsonString(), GetSchedulePaymentDetailsResponse.class);
                setData(getSchedulePaymentDetailsResponse);

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

    private void setData(GetSchedulePaymentDetailsResponse getSchedulePaymentDetailsResponse) {

        //productImageView;
        productNameTextView.setText(getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getProduct());
        String date = Utilities.formatDateWithoutTime(getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getCreatedAt());
        createdAtTextView.setText(date);
        mLoanAmountTextView.setText(String.valueOf(getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getTotalAmount()));
        mInstallmentAmountTextView.setText(String.valueOf(getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getInstallmentAmount()));
        mPaidAmountTextView.setText(String.valueOf(getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getAmountPaid()));
        mNoOfInstallmentTextView.setText(String.valueOf(getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getInstallmentNumber()));


        installmentInfoList = getSchedulePaymentDetailsResponse.getInstallmentInfoList();
        scheduledPaymentAdapter = new ScheduledPaymentListAdapter(installmentInfoList);
        scheduledPaymentListRecyclerView.setAdapter(scheduledPaymentAdapter);
        scheduledPaymentListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduledPaymentAdapter.notifyDataSetChanged();

    }

    public class ScheduledPaymentListAdapter extends RecyclerView.Adapter<ScheduledPaymentListAdapter.ScheduledPaymentViewHolder> {
        private List<InstallmentInfoList> scheduledPaymentInfoList;

        public ScheduledPaymentListAdapter(List<InstallmentInfoList> scheduledPaymentInfoList) {
            this.scheduledPaymentInfoList = scheduledPaymentInfoList;
        }

        @NonNull
        @Override
        public ScheduledPaymentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ScheduledPaymentViewHolder(LayoutInflater.from(getContext()).
                    inflate(R.layout.list_item_installment_details, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduledPaymentViewHolder scheduledPaymentViewHolder, int i) {
            if(i==0) {
                scheduledPaymentViewHolder.installmentNoTextView.setText("1st Installment");
            }else if(i==1){
                scheduledPaymentViewHolder.installmentNoTextView.setText("2nd Installment");
            }else if(i==2){
                scheduledPaymentViewHolder.installmentNoTextView.setText("3rd Installment");
            }else{
                scheduledPaymentViewHolder.installmentNoTextView.setText(i+"th Installment");
            }

            String date = Utilities.formatDateWithoutTime(scheduledPaymentInfoList.get(i).getTriggerDate());
            scheduledPaymentViewHolder.installmentDateTextView.setText(date);
            scheduledPaymentViewHolder.installmentAmountTextView.setText(String.valueOf(scheduledPaymentInfoList.get(i).getAmount()));
            if(scheduledPaymentInfoList.get(i).getStatus()==200) {
                Drawable img = getContext().getResources().getDrawable( R.drawable.transaction_tick_sign );
                img.setBounds( 0, 0, 60, 60 );
                scheduledPaymentViewHolder.installmentAmountTextView.setCompoundDrawables( img, null, null, null );
            }

            if(scheduledPaymentInfoList.get(i).getStatus()==103) {
                scheduledPaymentViewHolder.payNowButton.setVisibility(View.VISIBLE);
            }else{
                scheduledPaymentViewHolder.payNowButton.setVisibility(View.GONE);
            }

            scheduledPaymentViewHolder.installmentIdTextView.setText("Installment ID: "+scheduledPaymentInfoList.get(i).getId());
        }

        @Override
        public int getItemCount() {
            if (this.scheduledPaymentInfoList == null) return 0;
            else return this.scheduledPaymentInfoList.size();
        }

        public class ScheduledPaymentViewHolder extends RecyclerView.ViewHolder {
            private TextView productImageView;
            private TextView installmentNoTextView;
            private TextView installmentDateTextView;
            private TextView installmentAmountTextView;
            private View parentView;
            private TextView installmentIdTextView;
            private Button payNowButton;

            public ScheduledPaymentViewHolder(@NonNull View itemView) {
                super(itemView);
                productImageView = itemView.findViewById(R.id.transaction_image_view);
                installmentNoTextView = itemView.findViewById(R.id.installment_no_text_view);
                installmentAmountTextView = itemView.findViewById(R.id.installment_amount_text_view);
                installmentDateTextView = itemView.findViewById(R.id.installment_date_text_view);
                installmentIdTextView = itemView.findViewById(R.id.installment_id_text_view);
                payNowButton = itemView.findViewById(R.id.pay_now_button);
                parentView = itemView;
            }
        }
    }
}

