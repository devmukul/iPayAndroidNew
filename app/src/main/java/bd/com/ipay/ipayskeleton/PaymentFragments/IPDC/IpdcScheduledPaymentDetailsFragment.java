package bd.com.ipay.ipayskeleton.PaymentFragments.IPDC;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.SentReceivedRequestReviewActivity;
import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.BusinessRule;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RequestMoney.RequestMoneyAcceptRejectOrCancelResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.GetSchedulePaymentDetailsResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.InstallmentInfoList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.SchedulePaymentAcceptReject;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.SetSchedulePaymentDecisionRequest;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.BusinessRuleConstants;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ShedulePaymentConstant;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TwoFactorAuthConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IpdcScheduledPaymentDetailsFragment extends Fragment implements HttpResponseListener {
    private RecyclerView scheduledPaymentListRecyclerView;
    private long id;

    private HttpRequestGetAsyncTask getScheduledPaymentListTask;
    private HttpRequestPutAsyncTask setScheduledPaymentDecisionTask;
    private CustomProgressDialog progressDialog;
    List<InstallmentInfoList> installmentInfoList;
    private ScheduledPaymentListAdapter scheduledPaymentAdapter;
    private GetSchedulePaymentDetailsResponse getSchedulePaymentDetailsResponse;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    private SchedulePaymentAcceptReject schedulePaymentAcceptReject;


    private ProfileImageView productImageView;
    private TextView productNameTextView;
    private TextView createdAtTextView;
    private TextView mLoanAmountTextView;
    private TextView mNoOfInstallmentTextView;

    private View mButtonView;
    private Button mAcceptButtonView;
    private Button mRejectButtonView;


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

        mLoanAmountTextView = view.findViewById(R.id.loan_amount_view);
        mNoOfInstallmentTextView = view.findViewById(R.id.no_of_installment_view);
        mButtonView = view.findViewById(R.id.bottom_button);
        mAcceptButtonView = view.findViewById(R.id.button_accept);
        mRejectButtonView = view.findViewById(R.id.button_reject);

        getScheduledPaymentList();

        mAcceptButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptScheduledPayment(getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getId(), "ACCEPT");

            }
        });

        mRejectButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rejectScheduledPayment(getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getId(), "REJECT");
            }
        });
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

    private void acceptScheduledPayment(int id, String status) {

        if (setScheduledPaymentDecisionTask != null) {
            return;
        } else {
            progressDialog.showDialog();
            String json = new Gson().toJson(new SetSchedulePaymentDecisionRequest(id, status));
            String uri = Constants.BASE_URL_SCHEDULED_PAYMENT + Constants.URL_GET_SCHEDULED_PAYMENT_DETAILS;
            setScheduledPaymentDecisionTask = new HttpRequestPutAsyncTask
                    (Constants.COMMAND_ACCEPT_SCHEDULED_PAYMENT, uri, json, getContext(), this,
                            false);
            setScheduledPaymentDecisionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }

    }

    private void rejectScheduledPayment(int id, String status) {

        if (setScheduledPaymentDecisionTask != null) {
            return;
        } else {
            progressDialog.showDialog();
            String json = new Gson().toJson(new SetSchedulePaymentDecisionRequest(id, status));
            String uri = Constants.BASE_URL_SCHEDULED_PAYMENT + Constants.URL_GET_SCHEDULED_PAYMENT_DETAILS;
            setScheduledPaymentDecisionTask = new HttpRequestPutAsyncTask
                    (Constants.COMMAND_REJECT_SCHEDULED_PAYMENT, uri, json, getContext(), this,
                            false);
            setScheduledPaymentDecisionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
        }

        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_GET_SCHEDULED_PAYMENT_LIST:
                try {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        getSchedulePaymentDetailsResponse = new Gson().
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progressDialog.dismissDialog();
                getScheduledPaymentListTask = null;
                break;

            case Constants.COMMAND_ACCEPT_SCHEDULED_PAYMENT:
                try {
                    schedulePaymentAcceptReject = gson.fromJson(result.getJsonString(),
                            SchedulePaymentAcceptReject.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        getActivity().finish();
                    } else {
                        getActivity().finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                progressDialog.dismissDialog();
                setScheduledPaymentDecisionTask = null;

                break;
            case Constants.COMMAND_REJECT_SCHEDULED_PAYMENT:

                try {
                    schedulePaymentAcceptReject = gson.fromJson(result.getJsonString(),
                            SchedulePaymentAcceptReject.class);
                    getActivity().finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                progressDialog.dismissDialog();
                setScheduledPaymentDecisionTask = null;

                break;
        }

    }

    private void setData(GetSchedulePaymentDetailsResponse getSchedulePaymentDetailsResponse) {


        //productImageView;
        productNameTextView.setText(getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getProduct());
        String date = Utilities.formatDateWithoutTime(getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getCreatedAt());
        createdAtTextView.setText(date);
        mLoanAmountTextView.setText(getString(R.string.tk) + " " +numberFormat.format(getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getLoanAmount()));
        mNoOfInstallmentTextView.setText(numberFormat.format(getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getInstallmentNumber()));

        if(getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getStatus() == ShedulePaymentConstant.ScheduledPayment.WAITING_FOR_UPDATE_APPROVAL ||
                getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getStatus() == ShedulePaymentConstant.ScheduledPayment.WAITING_FOR_USER_APPROVAL){
            mButtonView.setVisibility(View.VISIBLE);
        }else{
            mButtonView.setVisibility(View.GONE);
        }

        try{
            productImageView.setProfilePicture(getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getReceiverInfo().getProfilePictures().get(0).getUrl(), true);
        }catch (Exception e){

        }

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
        public void onBindViewHolder(@NonNull ScheduledPaymentViewHolder scheduledPaymentViewHolder, final int i) {
            if(i==0) {
                scheduledPaymentViewHolder.installmentNoTextView.setText("1st Installment");
            }else if(i==1){
                scheduledPaymentViewHolder.installmentNoTextView.setText("2nd Installment");
            }else if(i==2){
                scheduledPaymentViewHolder.installmentNoTextView.setText("3rd Installment");
            }else{
                scheduledPaymentViewHolder.installmentNoTextView.setText((i+1)+"th Installment");
            }

            String totalAmountString = numberFormat.format(scheduledPaymentInfoList.get(i).getAmount());

            String date = Utilities.formatDateWithoutTime(scheduledPaymentInfoList.get(i).getTriggerDate());
            scheduledPaymentViewHolder.installmentDateTextView.setText(date);
            scheduledPaymentViewHolder.installmentAmountTextView.setText(getString(R.string.tk) + " " +totalAmountString);
            if(scheduledPaymentInfoList.get(i).getStatus()==200) {
                scheduledPaymentViewHolder.installmentAmountTextView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.transaction_tick_sign, 0, 0, 0);
            }else if(scheduledPaymentInfoList.get(i).getStatus()==104){
                scheduledPaymentViewHolder.installmentAmountTextView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.pending, 0, 0, 0);
            }else if(scheduledPaymentInfoList.get(i).getStatus()==400){
                scheduledPaymentViewHolder.installmentAmountTextView.setPaintFlags(scheduledPaymentViewHolder.installmentAmountTextView.getPaintFlags()
                        | Paint.STRIKE_THRU_TEXT_FLAG);
                scheduledPaymentViewHolder.installmentAmountTextView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.transaction_cross_sign, 0, 0, 0);
            }

            if(scheduledPaymentInfoList.get(i).getStatus()== ShedulePaymentConstant.ScheduledPayment.RUNNING &&
                    getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getStatus()== ShedulePaymentConstant.ScheduledPayment.RUNNING ) {
                scheduledPaymentViewHolder.payNowButton.setVisibility(View.VISIBLE);
            }else{
                scheduledPaymentViewHolder.payNowButton.setVisibility(View.GONE);
            }

            scheduledPaymentViewHolder.installmentIdTextView.setText("Installment ID: "+scheduledPaymentInfoList.get(i).getId());
            scheduledPaymentViewHolder.productImageView.setText(numberFormat.format(Double.valueOf(i+1)));

            scheduledPaymentViewHolder.payNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    try {
                        bundle.putSerializable(Constants.BILL_AMOUNT, numberFormat.parse(String.valueOf(scheduledPaymentInfoList.get(i).getAmount())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    bundle.putInt(Constants.INSTALLMENT_ID, scheduledPaymentInfoList.get(i).getId());
                    bundle.putString(Constants.IMAGE_URL, getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getReceiverInfo().getProfilePictures().get(0).getUrl());
                    bundle.putString(Constants.NAME, getSchedulePaymentDetailsResponse.getScheduledPaymentInfo().getReceiverInfo().getName());
                    ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new SchedulePaymentConfirmationFragment(), bundle, 3, true);

                }
            });
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

