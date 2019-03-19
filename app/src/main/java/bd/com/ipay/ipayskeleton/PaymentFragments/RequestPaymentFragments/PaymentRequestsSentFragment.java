package bd.com.ipay.ipayskeleton.PaymentFragments.RequestPaymentFragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.RequestPaymentActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.SentReceivedRequestPaymentReviewActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.CustomSwipeRefreshLayout;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.GetPendingPaymentsRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.GetPendingPaymentsResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.PendingPaymentClass;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.ContactSearchHelper;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class PaymentRequestsSentFragment extends ProgressFragment implements HttpResponseListener {
    private final int REQUEST_PAYMENT_REVIEW_REQUEST = 101;
    private HttpRequestPostAsyncTask mPendingInvoicesTask = null;
    private GetPendingPaymentsResponse mGetPendingPaymentsResponse;

    private CustomProgressDialog mProgressDialog;
    private RecyclerView mPendingListRecyclerView;
    private PendingListAdapter mInvoicesSentAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<PendingPaymentClass> pendingPaymentClasses;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int historyPageCount = 0;
    private boolean hasNext = false;
    private boolean isLoading = false;
    private boolean clearListAfterLoading;
    private TextView mEmptyListTextView;
    private Tracker mTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_money_request_sent));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sent_payment_request, container, false);
        getActivity().setTitle(R.string.request_payment);

        ((RequestPaymentActivity) getActivity()).mFabNewRequestPayment.setVisibility(View.VISIBLE);

        mEmptyListTextView = (TextView) view.findViewById(R.id.empty_list_text);
        mProgressDialog = new CustomProgressDialog(getActivity());
        mPendingListRecyclerView = (RecyclerView) view.findViewById(R.id.list_invoice_sent);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

        mInvoicesSentAdapter = new PendingListAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mPendingListRecyclerView.setLayoutManager(mLayoutManager);
        mPendingListRecyclerView.setAdapter(mInvoicesSentAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    refreshPaymentRequestsPendingList();
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (pendingPaymentClasses == null) {
            setContentShown(false);
            getPendingPaymentRequests();
        } else
            setContentShown(true);
    }

    private void refreshPaymentRequestsPendingList() {
        if (Utilities.isConnectionAvailable(getActivity())) {
            historyPageCount = 0;
            clearListAfterLoading = true;
            getPendingPaymentRequests();

        } else if (getActivity() != null)
            Toaster.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_LONG);
    }

    private void getPendingPaymentRequests() {
        if (mPendingInvoicesTask != null)
            return;

        GetPendingPaymentsRequest mGetPendingPaymentsRequest = new GetPendingPaymentsRequest(historyPageCount, Constants.SERVICE_ID_REQUEST_PAYMENT);
        Gson gson = new Gson();
        String json = gson.toJson(mGetPendingPaymentsRequest);
        mPendingInvoicesTask = new HttpRequestPostAsyncTask(Constants.COMMAND_GET_PENDING_PAYMENT_REQUESTS_SENT,
                Constants.BASE_URL_SM + Constants.URL_GET_SENT_REQUESTS, json, getActivity(), false);
        mPendingInvoicesTask.mHttpResponseListener = this;
        mPendingInvoicesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mPendingInvoicesTask = null;
            mSwipeRefreshLayout.setRefreshing(false);

            return;
        }

        if (this.isAdded()) setContentShown(true);

        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_GET_PENDING_PAYMENT_REQUESTS_SENT)) {


            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                try {
                    mGetPendingPaymentsResponse = gson.fromJson(result.getJsonString(), GetPendingPaymentsResponse.class);

                    if (pendingPaymentClasses == null || clearListAfterLoading) {
                        clearListAfterLoading = false;
                        pendingPaymentClasses = mGetPendingPaymentsResponse.getRequests();

                    } else {
                        List<PendingPaymentClass> tempPaymentList;
                        tempPaymentList = mGetPendingPaymentsResponse.getRequests();
                        pendingPaymentClasses.addAll(tempPaymentList);
                    }

                    hasNext = mGetPendingPaymentsResponse.isHasNext();
                    if (isLoading) isLoading = false;
                    mInvoicesSentAdapter.notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), R.string.pending_get_failed, Toast.LENGTH_LONG);
                }

            } else {
                if (getActivity() != null)
                    Toaster.makeText(getActivity(), R.string.pending_get_failed, Toast.LENGTH_LONG);
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mPendingInvoicesTask = null;
        }

        if (pendingPaymentClasses != null && pendingPaymentClasses.size() == 0)
            mEmptyListTextView.setVisibility(View.VISIBLE);
        else mEmptyListTextView.setVisibility(View.GONE);

    }

    public class PendingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int FOOTER_VIEW = 1;

        public PendingListAdapter() {
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView mSenderNameTextView;
            private final TextView mAmountTextView;
            private final TextView mTimeTextView;
            private final TextView statusView;
            private final ProfileImageView mProfileImageView;

            private String mTime;
            private String mDescription;
            private int mStatus;
            private BigDecimal mAmount;
            private long mRequestID;
            private String mReceiverName;
            private String mReceiverMobileNumber;
            private String mPhotoUri;

            public ViewHolder(final View itemView) {
                super(itemView);

                mSenderNameTextView = (TextView) itemView.findViewById(R.id.request_name);
                mAmountTextView = (TextView) itemView.findViewById(R.id.amount);
                mTimeTextView = (TextView) itemView.findViewById(R.id.time);
                mProfileImageView = (ProfileImageView) itemView.findViewById(R.id.profile_picture);
                statusView = (TextView) itemView.findViewById(R.id.status);
            }

            public void bindView(int pos) {

                mPhotoUri = pendingPaymentClasses.get(pos).getReceiverProfile().getUserProfilePicture();
                mTime = Utilities.formatDateWithTime(pendingPaymentClasses.get(pos).getRequestTime());
                mReceiverName = pendingPaymentClasses.get(pos).getReceiverProfile().getUserName();
                mReceiverMobileNumber = pendingPaymentClasses.get(pos).getReceiverProfile().getUserMobileNumber();
                mStatus = pendingPaymentClasses.get(pos).getStatus();
                mAmount = pendingPaymentClasses.get(pos).getAmount();
                mDescription = pendingPaymentClasses.get(pos).getDescriptionOfRequest();
                mRequestID = pendingPaymentClasses.get(pos).getId();

                mProfileImageView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + mPhotoUri, false);

                mSenderNameTextView.setText(mReceiverName);

                switch (mStatus) {
                    case Constants.INVOICE_STATUS_ACCEPTED:
                        statusView.setTextColor(getResources().getColor(R.color.bottle_green));
                        statusView.setText(R.string.accepted);
                        break;
                    case Constants.INVOICE_STATUS_PROCESSING:
                        statusView.setTextColor(getResources().getColor(R.color.background_yellow));
                        statusView.setText(R.string.processing);
                        break;
                    case Constants.INVOICE_STATUS_REJECTED:
                        statusView.setTextColor(Color.RED);
                        statusView.setText(R.string.rejected);
                        break;
                    case Constants.INVOICE_STATUS_CANCELED:
                        statusView.setTextColor(Color.GRAY);
                        statusView.setText(R.string.cancelled);
                        break;
                    case Constants.INVOICE_STATUS_DRAFT:
                        statusView.setTextColor(Color.RED);
                        statusView.setText(R.string.draft);
                        break;
                    default:
                        statusView.setTextColor(Color.RED);
                        statusView.setText(R.string.not_applicable);
                        break;
                }

                mAmountTextView.setText(Utilities.formatTaka(pendingPaymentClasses.get(pos).getAmount()));
                mTimeTextView.setText(mTime);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!mSwipeRefreshLayout.isRefreshing()) {
                            Intent intent = new Intent(getActivity(), SentReceivedRequestPaymentReviewActivity.class);
                            intent.putExtra(Constants.REQUEST_TYPE, Constants.REQUEST_TYPE_SENT_REQUEST);
                            intent.putExtra(Constants.AMOUNT, mAmount);
                            intent.putExtra(Constants.RECEIVER_MOBILE_NUMBER, ContactEngine.formatMobileNumberBD(mReceiverMobileNumber));
                            intent.putExtra(Constants.DESCRIPTION_TAG, mDescription);
                            intent.putExtra(Constants.MONEY_REQUEST_ID, mRequestID);
                            intent.putExtra(Constants.STATUS, mStatus);
                            intent.putExtra(Constants.NAME, mReceiverName);
                            intent.putExtra(Constants.PHOTO_URI, mPhotoUri);
                            intent.putExtra(Constants.IS_IN_CONTACTS, new ContactSearchHelper(getActivity()).searchMobileNumber(mReceiverMobileNumber));

                            startActivityForResult(intent, REQUEST_PAYMENT_REVIEW_REQUEST);
                        }
                    }
                });
            }
        }

        public class FooterViewHolder extends ViewHolder {
            private TextView mLoadMoreTextView;
            private ProgressBar mLoadMoreProgressBar;

            public FooterViewHolder(View itemView) {
                super(itemView);

                mLoadMoreTextView = (TextView) itemView.findViewById(R.id.load_more);
                mLoadMoreProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            }

            public void bindViewFooter() {
                setItemVisibilityOfFooterView();

                mLoadMoreTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (hasNext) {
                            historyPageCount = historyPageCount + 1;
                            showLoadingInFooter();
                            getPendingPaymentRequests();
                        }
                    }
                });
            }

            private void setItemVisibilityOfFooterView() {
                if (isLoading) {
                    mLoadMoreProgressBar.setVisibility(View.VISIBLE);
                    mLoadMoreTextView.setVisibility(View.GONE);
                } else {
                    mLoadMoreProgressBar.setVisibility(View.GONE);
                    mLoadMoreTextView.setVisibility(View.VISIBLE);

                    if (hasNext)
                        mLoadMoreTextView.setText(R.string.load_more);
                    else
                        mLoadMoreTextView.setText(R.string.no_more_results);
                }
            }

            private void showLoadingInFooter() {
                isLoading = true;
                notifyDataSetChanged();
            }
        }

        // Now define the viewholder for Normal list item
        public class NormalViewHolder extends ViewHolder {
            public NormalViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Do whatever you want on clicking the normal items
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v;
            if (viewType == FOOTER_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_load_more_footer, parent, false);

                return new FooterViewHolder(v);
            }

            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_pending_invoices_sent, parent, false);

            return new NormalViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            try {
                if (holder instanceof NormalViewHolder) {
                    NormalViewHolder vh = (NormalViewHolder) holder;
                    vh.bindView(position);

                } else if (holder instanceof FooterViewHolder) {
                    FooterViewHolder vh = (FooterViewHolder) holder;
                    vh.bindViewFooter();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {

            if (pendingPaymentClasses == null || pendingPaymentClasses.size() == 0)
                return 0;
            else {
                // Count 1 is added for load more footer
                return 1 + pendingPaymentClasses.size();
            }

        }

        @Override
        public int getItemViewType(int position) {
            if (position == pendingPaymentClasses.size()) {
                // This is where we'll add footer.
                return FOOTER_VIEW;
            }
            return super.getItemViewType(position);
        }
    }
}
