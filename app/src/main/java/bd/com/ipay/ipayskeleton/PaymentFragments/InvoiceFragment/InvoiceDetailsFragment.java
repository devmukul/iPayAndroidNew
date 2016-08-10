package bd.com.ipay.ipayskeleton.PaymentFragments.InvoiceFragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;

import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.Model.MMModule.MakePayment.ItemList;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class InvoiceDetailsFragment extends Fragment {

    private RecyclerView mReviewRecyclerView;
    private InvoiceReviewAdapter invoiceReviewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ItemList[] mItemList;
    private BigDecimal mAmount;
    private BigDecimal mNetAmount;
    private BigDecimal mVat;

    private String mDescription;
    private String mTime;
    private long id;
    private int status;
    private String mReceiverName;
    private String mReceiverMobileNumber;
    private String mPhotoUri;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_make_payment_notification_review, container, false);
        getActivity().setTitle(R.string.invoice_details);

        Bundle bundle = getArguments();
        context = getContext();

        this.mVat = new BigDecimal(bundle.getString(Constants.VAT));
        this.mAmount = new BigDecimal(bundle.getString(Constants.AMOUNT));
        this.mDescription = bundle.getString(Constants.DESCRIPTION);
        this.mTime = bundle.getString(Constants.TIME);
        this.id = bundle.getLong(Constants.MONEY_REQUEST_ID);
        this.status = bundle.getInt(Constants.STATUS);
        this.mReceiverMobileNumber = bundle.getString(Constants.MOBILE_NUMBER);
        this.mReceiverName = bundle.getString(Constants.NAME);
        this.mPhotoUri = bundle.getString(Constants.PHOTO_URI);

        List<ItemList> temporaryItemList;
        temporaryItemList = bundle.getParcelableArrayList(Constants.INVOICE_ITEM_NAME_TAG);
        this.mItemList = temporaryItemList.toArray(new ItemList[temporaryItemList.size()]);

        mReviewRecyclerView = (RecyclerView) v.findViewById(R.id.list_invoice);
        invoiceReviewAdapter = new InvoiceReviewAdapter();
        mLayoutManager = new LinearLayoutManager(this.getContext());
        mReviewRecyclerView.setLayoutManager(mLayoutManager);
        mReviewRecyclerView.setAdapter(invoiceReviewAdapter);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

private class InvoiceReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int INVOICE_DETAILS_LIST_ITEM_VIEW = 1;
        private static final int INVOICE_DETAILS_LIST_HEADER_VIEW = 2;
        private static final int INVOICE_DETAILS_LIST_FOOTER_VIEW = 3;

        public InvoiceReviewAdapter() {
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            final TextView descriptionTextView;
            final TextView timeTextView;
            final TextView invoiceIDTextView;

            private final TextView mItemNameView;
            private final TextView mQuantityView;
            private final TextView mAmountView;

            private final View headerView;

            private final ProfileImageView mProfileImageView;
            private final TextView mNameView;
            private final TextView mMobileNumberView;

            private final TextView mNetAmountView;
            private final TextView mVatView;
            private final TextView mTotalView;
            private final TextView mStatusView;

            public ViewHolder(final View itemView) {
                super(itemView);

                mProfileImageView = (ProfileImageView) itemView.findViewById(R.id.profile_picture);
                mNameView = (TextView) itemView.findViewById(R.id.textview_name);
                mMobileNumberView = (TextView) itemView.findViewById(R.id.textview_mobile_number);

                descriptionTextView = (TextView) itemView.findViewById(R.id.description);
                timeTextView = (TextView) itemView.findViewById(R.id.time);
                invoiceIDTextView = (TextView) itemView.findViewById(R.id.invoice_id);

                mItemNameView = (TextView) itemView.findViewById(R.id.textview_item);
                mQuantityView = (TextView) itemView.findViewById(R.id.textview_quantity);
                mAmountView = (TextView) itemView.findViewById(R.id.textview_amount);

                headerView = itemView.findViewById(R.id.header);

                mNetAmountView = (TextView) itemView.findViewById(R.id.textview_net_amount);
                mVatView = (TextView) itemView.findViewById(R.id.textview_vat);
                mTotalView = (TextView) itemView.findViewById(R.id.textview_total);
                mStatusView = (TextView) itemView.findViewById(R.id.status);

            }

            public void bindViewForListItem(int pos) {
                // Decrease pos by 1 as there is a header view now.
                pos = pos - 1;

                mItemNameView.setText(mItemList[pos].getItem());
                mQuantityView.setText(mItemList[pos].getQuantity().toString());
                mAmountView.setText(Utilities.formatTaka(mItemList[pos].getAmount()));
            }

            public void bindViewForHeader() {

                if (mItemList == null || mItemList.length ==0) {
                    headerView.setVisibility(View.GONE);
                }

                if (mReceiverName == null || mReceiverName.isEmpty()) {
                    mNameView.setVisibility(View.GONE);
                } else {
                    mNameView.setText(mReceiverName);
                }

                mMobileNumberView.setText(mReceiverMobileNumber);
                mProfileImageView.setProfilePicture(mPhotoUri, false);

                descriptionTextView.setText(mDescription);
                timeTextView.setText(mTime);
                invoiceIDTextView.setText(String.valueOf(id));
            }

            public void bindViewForFooter() {
                mNetAmount = mAmount.subtract(mVat);
                mNetAmountView.setText(Utilities.formatTaka(mNetAmount));
                mVatView.setText(Utilities.formatTaka(mVat));
                mTotalView.setText(Utilities.formatTaka(mAmount));
                if (status == Constants.INVOICE_STATUS_ACCEPTED) {
                    mStatusView.setText(context.getString(R.string.transaction_successful));
                    mStatusView.setTextColor(context.getResources().getColor(R.color.bottle_green));

                } else if (status == Constants.INVOICE_STATUS_PROCESSING) {
                    mStatusView.setText(context.getString(R.string.in_progress));
                    mStatusView.setTextColor(context.getResources().getColor(R.color.background_yellow));

                } else if (status == Constants.INVOICE_STATUS_REJECTED) {
                    mStatusView.setText(context.getString(R.string.transaction_rejected));
                    mStatusView.setTextColor(context.getResources().getColor(R.color.background_red));
                } else if (status == Constants.INVOICE_STATUS_CANCELED) {
                    mStatusView.setText(context.getString(R.string.transaction_cancelled));
                    mStatusView.setTextColor(Color.GRAY);
                }
                else if (status == Constants.INVOICE_STATUS_DRAFT) {
                    mStatusView.setText(context.getString(R.string.draft));
                    mStatusView.setTextColor(Color.GRAY);
                }
            }

        }

        public class ListFooterViewHolder extends ViewHolder {
            public ListFooterViewHolder(View itemView) {
                super(itemView);
            }
        }

        public class ListHeaderViewHolder extends ViewHolder {
            public ListHeaderViewHolder(View itemView) {
                super(itemView);
            }
        }

        public class ListItemViewHolder extends ViewHolder {
            public ListItemViewHolder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v;
            if (viewType == INVOICE_DETAILS_LIST_HEADER_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_invoice_details_header, parent, false);
                return new ListHeaderViewHolder(v);

            } else if (viewType == INVOICE_DETAILS_LIST_FOOTER_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_invoice_details_footer, parent, false);
                return new ListFooterViewHolder(v);

            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_make_payment_notification_review, parent, false);
                return new ListItemViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                if (holder instanceof ListItemViewHolder) {
                    ListItemViewHolder vh = (ListItemViewHolder) holder;
                    vh.bindViewForListItem(position);

                } else if (holder instanceof ListHeaderViewHolder) {
                    ListHeaderViewHolder vh = (ListHeaderViewHolder) holder;
                    vh.bindViewForHeader();

                } else if (holder instanceof ListFooterViewHolder) {
                    ListFooterViewHolder vh = (ListFooterViewHolder) holder;
                    vh.bindViewForFooter();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (mItemList == null)
                return 0;
            if (mItemList.length >= 0)
                return 1 + mItemList.length + 1;
            else return 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (mItemList == null) return super.getItemViewType(position);

            if (mItemList.length > 0) {
                if (position == 0) return INVOICE_DETAILS_LIST_HEADER_VIEW;

                else if (position == mItemList.length + 1)
                    return INVOICE_DETAILS_LIST_FOOTER_VIEW;

                else return INVOICE_DETAILS_LIST_ITEM_VIEW;
            } else if (mItemList.length == 0) {
                if (position == 0) return INVOICE_DETAILS_LIST_HEADER_VIEW;
                else return INVOICE_DETAILS_LIST_FOOTER_VIEW;
            }
            return super.getItemViewType(position);
        }
    }
}
