package bd.com.ipay.ipayskeleton.PaymentFragments.RequestPaymentFragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.RequestPaymentActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.InvoiceItem;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PaymentAcceptRejectOrCancelResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RequestMoney.RequestMoneyAcceptRejectOrCancelRequest;
import bd.com.ipay.ipayskeleton.PaymentFragments.CommonFragments.ReviewFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class PaymentRequestSentDetailsFragment extends ReviewFragment implements HttpResponseListener {

    private final int ACTION_CANCEL_REQUEST = 0;

    private HttpRequestPostAsyncTask mCancelPaymentRequestTask = null;
    private PaymentAcceptRejectOrCancelResponse mPaymentCancelResponse;

    private ProgressDialog mProgressDialog;
    private RecyclerView mReviewRecyclerView;
    private InvoiceReviewAdapter invoiceReviewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private InvoiceItem[] mInvoiceItemArray;
    private BigDecimal mTotal;
    private BigDecimal mAmount;
    private BigDecimal mNetAmount;
    private BigDecimal mVat;
    public BigDecimal mServiceCharge = new BigDecimal(-1);

    private String mDescription;
    private String mTime;
    private long mID;
    private String mTransactionID;
    private int status;
    private String mReceiverName;
    private String mReceiverMobileNumber;
    private String mPhotoUri;
    private Context context;
    private boolean isPinRequired = true;

    private boolean switchedFromTransactionHistory = false;

    private final int HEADER_FOOTER_VIEW_COUNT = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_make_payment_notification_review, container, false);
        getActivity().setTitle(R.string.payment_details);

        Bundle bundle = getArguments();
        context = getContext();

        this.mVat = new BigDecimal(bundle.getString(Constants.VAT));
        this.mTotal = new BigDecimal(bundle.getString(Constants.AMOUNT));
        this.mDescription = bundle.getString(Constants.DESCRIPTION);
        this.mTime = bundle.getString(Constants.TIME);
        this.mID = bundle.getLong(Constants.MONEY_REQUEST_ID);
        this.mTransactionID = bundle.getString(Constants.TRANSACTION_ID);
        this.status = bundle.getInt(Constants.STATUS);
        this.mReceiverMobileNumber = bundle.getString(Constants.MOBILE_NUMBER);
        this.mReceiverName = bundle.getString(Constants.NAME);
        this.mPhotoUri = bundle.getString(Constants.PHOTO_URI);


        switchedFromTransactionHistory = getActivity().getIntent()
                .getBooleanExtra(Constants.SWITCHED_FROM_TRANSACTION_HISTORY, false);

        List<InvoiceItem> temporaryItemList;
        temporaryItemList = bundle.getParcelableArrayList(Constants.INVOICE_ITEM_NAME_TAG);

        if (mInvoiceItemArray != null)
            this.mInvoiceItemArray = temporaryItemList.toArray(new InvoiceItem[temporaryItemList.size()]);

        mProgressDialog = new ProgressDialog(getActivity());
        mReviewRecyclerView = (RecyclerView) v.findViewById(R.id.list_invoice);
        invoiceReviewAdapter = new InvoiceReviewAdapter();
        mLayoutManager = new LinearLayoutManager(this.getContext());
        mReviewRecyclerView.setLayoutManager(mLayoutManager);
        mReviewRecyclerView.setAdapter(invoiceReviewAdapter);

        attemptGetServiceCharge();
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void cancelRequest(Long id) {
        if (mCancelPaymentRequestTask != null) {
            return;
        }

        mProgressDialog.setMessage(getString(R.string.progress_dialog_cancelling));
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        RequestMoneyAcceptRejectOrCancelRequest requestMoneyAcceptRejectOrCancelRequest =
                new RequestMoneyAcceptRejectOrCancelRequest(id, null);
        Gson gson = new Gson();
        String json = gson.toJson(requestMoneyAcceptRejectOrCancelRequest);
        mCancelPaymentRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_CANCEL_PAYMENT_REQUEST,
                Constants.BASE_URL_SM + Constants.URL_CANCEL_NOTIFICATION_REQUEST, json, getActivity());
        mCancelPaymentRequestTask.mHttpResponseListener = this;
        mCancelPaymentRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected int getServiceID() {
        return Constants.SERVICE_ID_REQUEST_PAYMENT;
    }

    @Override
    protected BigDecimal getAmount() {
        return mTotal;
    }

    @Override
    protected void onServiceChargeLoadFinished(BigDecimal serviceCharge) {

        this.mServiceCharge = serviceCharge;
        invoiceReviewAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPinLoadFinished(boolean isPinRequired) {
        this.isPinRequired = isPinRequired;
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        super.httpResponseReceiver(result);

        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mProgressDialog.dismiss();
            if (getActivity() != null)
                 Toaster.makeText(getActivity(), R.string.fetch_info_failed,  Toast.LENGTH_LONG);
            return;
        }
        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_CANCEL_PAYMENT_REQUEST)) {

            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                try {
                    mPaymentCancelResponse = gson.fromJson(result.getJsonString(),
                            PaymentAcceptRejectOrCancelResponse.class);
                    String message = mPaymentCancelResponse.getMessage();
                    if (getActivity() != null)
                         Toaster.makeText(getActivity(), message,  Toast.LENGTH_LONG);

                    if (switchedFromTransactionHistory) {
                        Utilities.finishLauncherActivity(getActivity());
                    } else
                        ((RequestPaymentActivity) getActivity()).switchToSentPaymentRequestsFragment();

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                         Toaster.makeText(getActivity(), R.string.could_not_cancel_money_request,  Toast.LENGTH_LONG);
                }

            } else {
                if (getActivity() != null)
                     Toaster.makeText(getActivity(), R.string.could_not_cancel_money_request,  Toast.LENGTH_LONG);
            }

            mProgressDialog.dismiss();
            mCancelPaymentRequestTask = null;
        }
    }

    private class InvoiceReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int INVOICE_DETAILS_LIST_ITEM_VIEW = 1;
        private static final int INVOICE_DETAILS_LIST_HEADER_VIEW = 2;
        private static final int INVOICE_DETAILS_LIST_FOOTER_VIEW = 3;

        public InvoiceReviewAdapter() {
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            final TextView mDescriptionTextView;
            final TextView mTimeTextView;
            final TextView mTransactionIDTextView;

            private final TextView mItemNameView;
            private final TextView mQuantityView;
            private final TextView mAmountView;

            private final View mHeaderView;

            private final ProfileImageView mProfileImageView;
            private final TextView mNameView;
            private final TextView mMobileNumberView;

            private final TextView mNetAmountView;
            private final TextView mVatView;
            private final TextView mTotalView;
            private final TextView mServiceChargeView;
            private final TextView mStatusView;
            private final Button mCancelButton;

            public ViewHolder(final View itemView) {
                super(itemView);

                mProfileImageView = (ProfileImageView) itemView.findViewById(R.id.profile_picture);
                mNameView = (TextView) itemView.findViewById(R.id.textview_name);
                mMobileNumberView = (TextView) itemView.findViewById(R.id.textview_mobile_number);

                mDescriptionTextView = (TextView) itemView.findViewById(R.id.description);
                mTimeTextView = (TextView) itemView.findViewById(R.id.time);
                mTransactionIDTextView = (TextView) itemView.findViewById(R.id.invoice_id);

                mItemNameView = (TextView) itemView.findViewById(R.id.textview_item);
                mQuantityView = (TextView) itemView.findViewById(R.id.textview_quantity);
                mAmountView = (TextView) itemView.findViewById(R.id.textview_amount);
                mServiceChargeView = (TextView) itemView.findViewById(R.id.textview_service_charge);
                mHeaderView = itemView.findViewById(R.id.header);

                mNetAmountView = (TextView) itemView.findViewById(R.id.textview_net_amount);
                mVatView = (TextView) itemView.findViewById(R.id.textview_vat);
                mTotalView = (TextView) itemView.findViewById(R.id.textview_total);
                mStatusView = (TextView) itemView.findViewById(R.id.status);
                mCancelButton = (Button) itemView.findViewById(R.id.button_cancel);

            }

            public void bindViewForListItem(int pos) {
                // Decrease pos by 1 as there is a header view now.
                pos = pos - 1;

                mItemNameView.setText(mInvoiceItemArray[pos].getItem());
                mQuantityView.setText(mInvoiceItemArray[pos].getQuantity().toString());
                mAmountView.setText(Utilities.formatTaka(mInvoiceItemArray[pos].getAmount()));
            }

            public void bindViewForHeader() {

                if (mInvoiceItemArray == null || mInvoiceItemArray.length == 0) {
                    mHeaderView.setVisibility(View.GONE);
                }

                if (mReceiverName == null || mReceiverName.isEmpty()) {
                    mNameView.setVisibility(View.GONE);
                } else {
                    mNameView.setText(mReceiverName);
                }

                mMobileNumberView.setText(mReceiverMobileNumber);
                mProfileImageView.setProfilePicture(mPhotoUri, false);

                mDescriptionTextView.setText(mDescription);
                mTimeTextView.setText(mTime);
                mTransactionIDTextView.setText(String.valueOf(mTransactionID));
            }

            public void bindViewForFooter() {
                mAmount = mTotal.subtract(mVat);
                mNetAmount = mTotal.subtract(mServiceCharge);

                mAmountView.setText(Utilities.formatTaka(mAmount));
                mNetAmountView.setText(Utilities.formatTaka(mNetAmount));
                mVatView.setText(Utilities.formatTaka(mVat));
                mServiceChargeView.setText(Utilities.formatTaka(mServiceCharge));
                mTotalView.setText(Utilities.formatTaka(mTotal));

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
                } else if (status == Constants.INVOICE_STATUS_DRAFT) {
                    mStatusView.setText(context.getString(R.string.save_not_send));
                    mStatusView.setTextColor(Color.GRAY);
                }

                if (status == Constants.INVOICE_STATUS_PROCESSING || status == Constants.INVOICE_STATUS_DRAFT)
                    mCancelButton.setVisibility(View.VISIBLE);

                mCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showAlertDialogue(getString(R.string.cancel_payment_request_confirm), ACTION_CANCEL_REQUEST, mID);
                    }
                });
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
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_payment_request_details_header, parent, false);
                return new ListHeaderViewHolder(v);

            } else if (viewType == INVOICE_DETAILS_LIST_FOOTER_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sent_payment_request_details_footer, parent, false);
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
            if (mInvoiceItemArray == null || mInvoiceItemArray.length == 0)
                return HEADER_FOOTER_VIEW_COUNT;
            if (mInvoiceItemArray.length > 0)
                return 1 + mInvoiceItemArray.length + 1;
            else return 0;
        }

        @Override
        public int getItemViewType(int position) {
            if (mInvoiceItemArray == null || mInvoiceItemArray.length == 0) {
                if (position == 0) return INVOICE_DETAILS_LIST_HEADER_VIEW;
                else return INVOICE_DETAILS_LIST_FOOTER_VIEW;
            }
            if (mInvoiceItemArray.length > 0) {
                if (position == 0) return INVOICE_DETAILS_LIST_HEADER_VIEW;

                else if (position == mInvoiceItemArray.length + 1)
                    return INVOICE_DETAILS_LIST_FOOTER_VIEW;

                else return INVOICE_DETAILS_LIST_ITEM_VIEW;
            }
            return super.getItemViewType(position);
        }

        private void showAlertDialogue(String msg, final int action, final long id) {
            AlertDialog.Builder alertDialogue = new AlertDialog.Builder(getActivity());
            alertDialogue.setTitle(R.string.confirm_query);
            alertDialogue.setMessage(msg);

            alertDialogue.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    if (action == ACTION_CANCEL_REQUEST)
                        cancelRequest(id);
                }
            });

            alertDialogue.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                }
            });

            alertDialogue.show();
        }
    }
}
