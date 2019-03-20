package bd.com.ipay.ipayskeleton.PaymentFragments.IPDC;

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

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GroupedScheduledPaymentInfo;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPDC.ScheduledPaymentInfo;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.ShedulePaymentConstant;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IpdcScheduledPaymentFragment extends Fragment {
    private RecyclerView scheduledPaymentListRecyclerView;
    private GlobalScheduledPaymentListFragment.ScheduledPaymentListAdapter scheduledPaymentAdapter;
    private GroupedScheduledPaymentInfo groupedScheduledPaymentInfoList;
    private List<ScheduledPaymentInfo> scheduledPaymentInfoList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_global_scheduled_payment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        groupedScheduledPaymentInfoList = (GroupedScheduledPaymentInfo) getArguments().
                getSerializable("scheduledPaymentList");
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
        ((TextView) view.findViewById(R.id.title)).setText(getString(R.string.my_schedule_list));
        ((ImageView) view.findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        scheduledPaymentListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduledPaymentListRecyclerView.setAdapter(new ScheduledPaymentListAdapter(scheduledPaymentInfoList));
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
                            " Installments");
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setTextColor(Color.parseColor("#888888"));
            }else if(scheduledPaymentInfoList.get(i).getStatus() == ShedulePaymentConstant.ScheduledPayment.WAITING_FOR_USER_APPROVAL){
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setText
                        ("Waiting for approval *");
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setTextColor(Color.parseColor("#00b2a2"));
            }else if(scheduledPaymentInfoList.get(i).getStatus() == ShedulePaymentConstant.ScheduledPayment.WAITING_FOR_UPDATE_APPROVAL){
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setText
                        ("Waiting for amendment approval *");
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setTextColor(Color.parseColor("#00b2a2"));
            }else {
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setText("Completed");
                scheduledPaymentViewHolder.totalInstallmentCountTextView.setTextColor(Color.parseColor("#FF0000"));
            }

            scheduledPaymentViewHolder.productNameTextView.setText(scheduledPaymentInfoList.get(i).getProduct());
            String date = Utilities.formatDateWithoutTime(scheduledPaymentInfoList.get(i).getStartDate());
            scheduledPaymentViewHolder.createdAtTextView.setText(date);

            scheduledPaymentViewHolder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putLong("ID",  scheduledPaymentInfoList.get(i).getId());
                    ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new IpdcScheduledPaymentDetailsFragment(), bundle, 2, true);
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

