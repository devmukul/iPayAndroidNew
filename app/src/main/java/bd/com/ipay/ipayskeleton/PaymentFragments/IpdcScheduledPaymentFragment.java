package bd.com.ipay.ipayskeleton.PaymentFragments;

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

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GroupedScheduledPaymentInfo;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.ScheduledPaymentInfo;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IpdcScheduledPaymentFragment extends Fragment {
    private RecyclerView scheduledPaymentListRecyclerView;
    private GlobalScheduledPaymentListFragment.ScheduledPaymentListAdapter scheduledPaymentAdapter;
    private List<GroupedScheduledPaymentInfo> groupedScheduledPaymentInfoList;
    private List<ScheduledPaymentInfo> scheduledPaymentInfoList;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_global_scheduled_payment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        groupedScheduledPaymentInfoList = (List<GroupedScheduledPaymentInfo>) getArguments().
                getSerializable("scheduledPaymentList");
        scheduledPaymentInfoList = groupedScheduledPaymentInfoList.get(0).getScheduledPaymentInfos();
        scheduledPaymentListRecyclerView = view.findViewById(R.id.scheduled_payment_list);
        groupedScheduledPaymentInfoList = new ArrayList<>();
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
        public void onBindViewHolder(@NonNull ScheduledPaymentViewHolder scheduledPaymentViewHolder, int i) {
            scheduledPaymentViewHolder.totalInstallmentCountTextView.setText
                    (Long.toString(scheduledPaymentInfoList.get(i).getInstallmentNumber()) +
                            " installment remaining");
            scheduledPaymentViewHolder.productNameTextView.setText(scheduledPaymentInfoList.get(i).getProduct());
            String date = Utilities.formatDayMonthYear(scheduledPaymentInfoList.get(i).getStartDate());
            scheduledPaymentViewHolder.createdAtTextView.setText(date);
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
            private TextView createdAtTextView;

            public ScheduledPaymentViewHolder(@NonNull View itemView) {
                super(itemView);
                productImageView = (RoundedImageView) itemView.findViewById(R.id.product_image);
                productNameTextView = (TextView) itemView.findViewById(R.id.product_name);
                totalInstallmentCountTextView = (TextView) itemView.findViewById(R.id.installment_count);
                createdAtTextView = (TextView) itemView.findViewById(R.id.created_at);
                createdAtTextView.setVisibility(View.VISIBLE);
                parentView = itemView;
            }
        }
    }
}

