package bd.com.ipay.ipayskeleton.PaymentFragments.RequestMoneyFragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressFragment;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import bd.com.ipay.ipayskeleton.Api.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.HttpResponseObject;
import bd.com.ipay.ipayskeleton.CustomView.CustomSwipeRefreshLayout;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomSelectorDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.Model.MMModule.RequestMoney.GetPendingMoneyRequest;
import bd.com.ipay.ipayskeleton.Model.MMModule.RequestMoney.GetPendingRequestResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.RequestMoney.RequestMoneyAcceptRejectOrCancelRequest;
import bd.com.ipay.ipayskeleton.Model.MMModule.RequestMoney.RequestMoneyAcceptRejectOrCancelResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.RequestMoney.RequestsSentClass;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class SentMoneyRequestsFragment extends ProgressFragment implements HttpResponseListener {

    private final int ACTION_CANCEL_REQUEST = 0;

    private HttpRequestPostAsyncTask mPendingRequestTask = null;
    private GetPendingRequestResponse mGetPendingRequestResponse;

    private HttpRequestPostAsyncTask mCancelRequestTask = null;
    private RequestMoneyAcceptRejectOrCancelResponse mRequestMoneyAcceptRejectOrCancelResponse;

    private ProgressDialog mProgressDialog;
    private RecyclerView mPendingListRecyclerView;
    private SentMoneyRequestListAdapter mPendingRequestsAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<RequestsSentClass> pendingMoneyRequestClasses;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyListTextView;

    private int pageCount = 0;
    private boolean hasNext = false;
    private boolean clearListAfterLoading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sent_money_requests, container, false);
        mProgressDialog = new ProgressDialog(getActivity());
        mPendingListRecyclerView = (RecyclerView) v.findViewById(R.id.list_my_requests);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);

        mEmptyListTextView = (TextView) v.findViewById(R.id.empty_list_text);
        mPendingRequestsAdapter = new SentMoneyRequestListAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mPendingListRecyclerView.setLayoutManager(mLayoutManager);
        mPendingListRecyclerView.setAdapter(mPendingRequestsAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    refreshPendingList();
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentShown(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utilities.isConnectionAvailable(getActivity())) {
            getPendingRequests();
        }
    }

    private void refreshPendingList() {
        if (Utilities.isConnectionAvailable(getActivity())) {
            pageCount = 0;
            clearListAfterLoading = true;
            getPendingRequests();
        }
    }

    private void getPendingRequests() {
        if (mPendingRequestTask != null) {
            return;
        }

        GetPendingMoneyRequest mUserActivityRequest = new GetPendingMoneyRequest(pageCount, Constants.SERVICE_ID_REQUEST_MONEY);
        Gson gson = new Gson();
        String json = gson.toJson(mUserActivityRequest);
        mPendingRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_GET_PENDING_REQUESTS_ME,
                Constants.BASE_URL_SM + Constants.URL_GET_SENT_REQUESTS, json, getActivity());
        mPendingRequestTask.mHttpResponseListener = this;
        mPendingRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void cancelRequest(Long id) {
        if (mCancelRequestTask != null) {
            return;
        }

        mProgressDialog.setMessage(getString(R.string.progress_dialog_cancelling));
        mProgressDialog.show();
        // No PIN needed for now to place a request from me
        RequestMoneyAcceptRejectOrCancelRequest requestMoneyAcceptRejectOrCancelRequest =
                new RequestMoneyAcceptRejectOrCancelRequest(id, null);
        Gson gson = new Gson();
        String json = gson.toJson(requestMoneyAcceptRejectOrCancelRequest);
        mCancelRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_CANCEL_REQUESTS_MONEY,
                Constants.BASE_URL_SM + Constants.URL_CANCEL_NOTIFICATION_REQUEST, json, getActivity());
        mCancelRequestTask.mHttpResponseListener = this;
        mCancelRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(HttpResponseObject result) {

        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
					|| result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mProgressDialog.dismiss();
            mPendingRequestTask = null;
            mSwipeRefreshLayout.setRefreshing(false);
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.fetch_info_failed, Toast.LENGTH_LONG).show();
            return;
        }


        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_GET_PENDING_REQUESTS_ME)) {

            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                try {

                    mGetPendingRequestResponse = gson.fromJson(result.getJsonString(), GetPendingRequestResponse.class);

                    if (clearListAfterLoading || pendingMoneyRequestClasses == null) {
                        pendingMoneyRequestClasses = mGetPendingRequestResponse.getAllNotifications();
                        clearListAfterLoading = false;
                    } else {
                        List<RequestsSentClass> tempPendingMoneyRequestClasses;
                        tempPendingMoneyRequestClasses = mGetPendingRequestResponse.getAllNotifications();
                        pendingMoneyRequestClasses.addAll(tempPendingMoneyRequestClasses);
                    }

                    hasNext = mGetPendingRequestResponse.isHasNext();
                    mPendingRequestsAdapter.notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.pending_get_failed, Toast.LENGTH_LONG).show();
                }

            } else {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.pending_get_failed, Toast.LENGTH_LONG).show();
            }


            if (this.isAdded()) setContentShown(true);
            mSwipeRefreshLayout.setRefreshing(false);
            mPendingRequestTask = null;

        } else if (result.getApiCommand().equals(Constants.COMMAND_CANCEL_REQUESTS_MONEY)) {

            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                try {
                    mRequestMoneyAcceptRejectOrCancelResponse = gson.fromJson(result.getJsonString(),
                            RequestMoneyAcceptRejectOrCancelResponse.class);
                    String message = mRequestMoneyAcceptRejectOrCancelResponse.getMessage();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

                    // Refresh the pending list
                    if (pendingMoneyRequestClasses != null)
                        pendingMoneyRequestClasses.clear();
                    pendingMoneyRequestClasses = null;
                    pageCount = 0;
                    getPendingRequests();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.could_not_cancel_money_request, Toast.LENGTH_LONG).show();
                }

            } else {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.could_not_cancel_money_request, Toast.LENGTH_LONG).show();
            }

            mProgressDialog.dismiss();
            mCancelRequestTask = null;
        }

        if (pendingMoneyRequestClasses != null && pendingMoneyRequestClasses.size() == 0 ) {
            mEmptyListTextView.setVisibility(View.VISIBLE);
        } else mEmptyListTextView.setVisibility(View.GONE);
    }

    public class SentMoneyRequestListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int FOOTER_VIEW = 1;
        private static final int MONEY_REQUEST_ITEM_VIEW = 4;

        public class MoneyRequestViewHolder extends RecyclerView.ViewHolder {
            private final TextView mSenderNumber;
            private final TextView mTime;
            private final TextView mDescription;
            private final ProfileImageView mProfileImageView;
            private final View divider;

            private final int ACTION_CANCEL = 0;

            private CustomSelectorDialog mCustomSelectorDialog;
            private List<String> mMyRequestActionList;

            public MoneyRequestViewHolder(final View itemView) {
                super(itemView);

                mSenderNumber = (TextView) itemView.findViewById(R.id.request_number);
                mTime = (TextView) itemView.findViewById(R.id.time);
                mDescription = (TextView) itemView.findViewById(R.id.description);
                mProfileImageView = (ProfileImageView) itemView.findViewById(R.id.profile_picture);
                divider = itemView.findViewById(R.id.divider);
            }

            public void bindView(int pos) {

                if (pos == pendingMoneyRequestClasses.size() - 1) divider.setVisibility(View.GONE);

                final long id = pendingMoneyRequestClasses.get(pos).getId();
                String time = new SimpleDateFormat("EEE, MMM d, ''yy, h:mm a").format(pendingMoneyRequestClasses.get(pos).getRequestTime());
                final String name = pendingMoneyRequestClasses.get(pos).getReceiverProfile().getUserName();
                String imageUrl = pendingMoneyRequestClasses.get(pos).getReceiverProfile().getUserProfilePicture();
                String mobileNumber = pendingMoneyRequestClasses.get(pos).getReceiverProfile().getUserMobileNumber();
                mTime.setText(time);
                mSenderNumber.setText(name);
                mDescription.setText(pendingMoneyRequestClasses.get(pos).getDescription());

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMyRequestActionList = Arrays.asList(getResources().getStringArray(R.array.my_request_action));
                        mCustomSelectorDialog = new CustomSelectorDialog(getActivity(), name, mMyRequestActionList);
                        mCustomSelectorDialog.setOnResourceSelectedListener(new CustomSelectorDialog.OnResourceSelectedListener() {
                            @Override
                            public void onResourceSelected(int selectedIndex,String name) {
                                if (selectedIndex == ACTION_CANCEL) {
                                    showAlertDialogue(getString(R.string.cancel_money_request_confirm), ACTION_CANCEL_REQUEST, id);
                                }
                            }
                        });
                        mCustomSelectorDialog.show();
                    }
                });

                mProfileImageView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + imageUrl, false);

            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            private TextView mLoadMoreTextView;

            public FooterViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (hasNext) {
                            pageCount = pageCount + 1;
                            getPendingRequests();
                        }
                    }
                });

                mLoadMoreTextView = (TextView) itemView.findViewById(R.id.load_more);
            }

            public void bindView() {

                if (hasNext)
                    mLoadMoreTextView.setText(R.string.load_more);
                else
                    mLoadMoreTextView.setText(R.string.no_more_results);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v;
            if (viewType == MONEY_REQUEST_ITEM_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_pending_request_money_me, parent, false);
                return new MoneyRequestViewHolder(v);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_load_more_footer, parent, false);
                return new FooterViewHolder(v);
            }

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            try {
                if (holder instanceof  MoneyRequestViewHolder) {
                    MoneyRequestViewHolder vh = (MoneyRequestViewHolder) holder;
                    vh.bindView(position);
                } else if (holder instanceof FooterViewHolder) {
                    FooterViewHolder vh = (FooterViewHolder) holder;
                    vh.bindView();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (pendingMoneyRequestClasses == null || pendingMoneyRequestClasses.isEmpty())
                return 0;
            else
                return pendingMoneyRequestClasses.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1)
                return FOOTER_VIEW;
            else
                return MONEY_REQUEST_ITEM_VIEW;
        }
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

        alertDialogue.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
            }
        });

        alertDialogue.show();
    }
}