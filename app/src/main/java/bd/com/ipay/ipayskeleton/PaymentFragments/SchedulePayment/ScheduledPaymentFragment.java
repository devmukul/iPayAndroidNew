package bd.com.ipay.ipayskeleton.PaymentFragments.SchedulePayment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.GroupedScheduledPaymentList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.ScheduledPaymentInfo;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ShedulePaymentConstant;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class ScheduledPaymentFragment extends Fragment {
    private RecyclerView scheduledPaymentListRecyclerView;
    private ScheduledPaymentListAdapter scheduledPaymentAdapter;
    private GroupedScheduledPaymentList groupedScheduledPaymentInfoList;
    private List<ScheduledPaymentInfo> scheduledPaymentInfoList;

    private TextView emptyTextView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_global_scheduled_payment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        groupedScheduledPaymentInfoList = (GroupedScheduledPaymentList) getArguments().
                getSerializable(IPayUtilityBillPayActionActivity.SCHEDULE_PAYMENT_LIST);
        scheduledPaymentInfoList = new ArrayList<>();
        for (ScheduledPaymentInfo scheduledPaymentInfo : groupedScheduledPaymentInfoList.getScheduledPaymentInfos()){
            if(scheduledPaymentInfo.getStatus() == ShedulePaymentConstant.ScheduledPayment.RUNNING ||
                    scheduledPaymentInfo.getStatus() == ShedulePaymentConstant.ScheduledPayment.WAITING_FOR_USER_APPROVAL ||
                    scheduledPaymentInfo.getStatus() == ShedulePaymentConstant.ScheduledPayment.WAITING_FOR_UPDATE_APPROVAL ||
                    scheduledPaymentInfo.getStatus() == ShedulePaymentConstant.ScheduledPayment.COMPLETED){
                scheduledPaymentInfoList.add(scheduledPaymentInfo);
            }
        }

        scheduledPaymentListRecyclerView = view.findViewById(R.id.scheduled_payment_list);
        emptyTextView = view.findViewById(R.id.empty_text);

        ((TextView) view.findViewById(R.id.title)).setText(groupedScheduledPaymentInfoList.getReceiverInfo().getName());
        ((ImageView) view.findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        if(scheduledPaymentInfoList.size()>0){
            emptyTextView.setVisibility(View.GONE);
        }else {
            emptyTextView.setVisibility(View.VISIBLE);
        }

        scheduledPaymentAdapter = new ScheduledPaymentListAdapter(scheduledPaymentInfoList);
        scheduledPaymentListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduledPaymentListRecyclerView.setAdapter(scheduledPaymentAdapter);
        scheduledPaymentAdapter.notifyDataSetChanged();
    }

    public class ScheduledPaymentListAdapter extends RecyclerView.Adapter<ScheduledPaymentListAdapter.ScheduledPaymentViewHolder> {
        private List<ScheduledPaymentInfo> scheduledPaymentInfoList;

        public ScheduledPaymentListAdapter(List<ScheduledPaymentInfo> scheduledPaymentInfoList) {
            this.scheduledPaymentInfoList = scheduledPaymentInfoList;
        }


        @NonNull
        @Override
        public ScheduledPaymentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ScheduledPaymentViewHolder(LayoutInflater.from(getContext()).
                    inflate(R.layout.list_item_scheduled_payment, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduledPaymentViewHolder scheduledPaymentViewHolder, final int i) {
            scheduledPaymentViewHolder.productImageView.setText(String.valueOf(i+1));

            if(scheduledPaymentInfoList.get(i).getStatus() == ShedulePaymentConstant.ScheduledPayment.RUNNING){
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setText
                    (Long.toString(scheduledPaymentInfoList.get(i).getInstallmentNumber()) +
                            " "+ getString(R.string.intallment_text));
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setTextColor(Color.parseColor("#888888"));
            }else if(scheduledPaymentInfoList.get(i).getStatus() == ShedulePaymentConstant.ScheduledPayment.WAITING_FOR_USER_APPROVAL){
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setText
                        (getString(R.string.waiting_for_approval));
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setTextColor(Color.parseColor("#00b2a2"));
            }else if(scheduledPaymentInfoList.get(i).getStatus() == ShedulePaymentConstant.ScheduledPayment.WAITING_FOR_UPDATE_APPROVAL){
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setText
                        (getString(R.string.waiting_for_ammedment));
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setTextColor(Color.parseColor("#00b2a2"));
            }else {
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setText(getString(R.string.installment_complete));
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setTextColor(Color.parseColor("#FF0000"));
            }

            scheduledPaymentViewHolder.productNameTextView.setText(scheduledPaymentInfoList.get(i).getProduct());
            String date = Utilities.formatDateWithoutTime(scheduledPaymentInfoList.get(i).getStartDate());
            scheduledPaymentViewHolder.createdAtTextView.setText(date);

            scheduledPaymentViewHolder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putLong(Constants.ID,  scheduledPaymentInfoList.get(i).getId());
                    ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new ScheduledPaymentDetailsFragment(), bundle, 2, true);
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
            private TextView productNameTextView;
            private TextView totalInstallmentCountTextView;
            private View parentView;
            private TextView createdAtTextView;

            public ScheduledPaymentViewHolder(@NonNull View itemView) {
                super(itemView);
                productImageView = itemView.findViewById(R.id.product_image);
                productNameTextView = itemView.findViewById(R.id.product_name);
                totalInstallmentCountTextView = itemView.findViewById(R.id.installment_count);
                createdAtTextView = itemView.findViewById(R.id.created_at);
                parentView = itemView;
            }
        }
    }
}

