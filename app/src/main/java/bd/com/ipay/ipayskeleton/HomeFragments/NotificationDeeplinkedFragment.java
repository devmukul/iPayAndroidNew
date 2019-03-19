package bd.com.ipay.ipayskeleton.HomeFragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.RichNotificationDetailsActivity;
import bd.com.ipay.ipayskeleton.Activities.WebViewActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.CustomSwipeRefreshLayout;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.DeepLinkedNotification;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GetDeepLinkedNotificationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UpdateNotificationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UpdateNotificationStateRequest;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class NotificationDeeplinkedFragment extends ProgressFragment implements HttpResponseListener {


    private RecyclerView mNotificationsRecyclerView;
    private NotificationListAdapter mNotificationListAdapter;
    private LinearLayoutManager mLayoutManager;
    private CustomSwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyListTextView;
    private List<DeepLinkedNotification> mDeepLinkedNotifications;

    private boolean hasNext = false;
    private boolean isLoading = false;
    private boolean clearListAfterLoading;
    private boolean mIsScrolled = false;
    private int mTotalItemCount = 0;
    private int mPastVisibleItems;
    private int mVisibleItem;
    private long lastTime;
    private int rowToDelete;

    private HttpRequestGetAsyncTask mGetNotificationAsyncTask;
    private HttpRequestPutAsyncTask mUpdateNotificationStateTask;

    private CustomProgressDialog mProgressDialog;

    private NotificationBroadcastReceiver notificationBroadcastReceiver;
    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        lastTime = 0;
        mTracker = Utilities.getTracker(getActivity());
        rowToDelete = -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notifications, container, false);

        mSwipeRefreshLayout = (CustomSwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
        mNotificationsRecyclerView = (RecyclerView) v.findViewById(R.id.list_notification);
        mEmptyListTextView = (TextView) v.findViewById(R.id.empty_list_text);
        if (mEmptyListTextView != null) {
            mEmptyListTextView.setText("You do not have any notifications");
        }
        mNotificationListAdapter = new NotificationListAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mNotificationsRecyclerView.setLayoutManager(mLayoutManager);
        mNotificationsRecyclerView.setAdapter(mNotificationListAdapter);
        mProgressDialog = new CustomProgressDialog(getContext());
        getNotifications();

        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                lastTime = 0;
                if (mDeepLinkedNotifications != null) {
                    mDeepLinkedNotifications.clear();
                    mNotificationListAdapter.notifyDataSetChanged();
                }
                getNotifications();
            }
        });
        implementScrollListener();
        return v;
    }

    public void onResume() {
        super.onResume();

        notificationBroadcastReceiver = new NotificationBroadcastReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(notificationBroadcastReceiver,
                new IntentFilter(Constants.NOTIFICATION_UPDATE_BROADCAST));

        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_notifications));
    }

    private void getNotifications() {
        if (mGetNotificationAsyncTask != null) {
            setContentShown(true);
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        } else {
            String url = Constants.BASE_URL_PUSH_NOTIFICATION + Constants.URL_PULL_NOTIFICATION;
            Uri.Builder uri = Uri.parse(url)
                    .buildUpon();
            uri.appendQueryParameter("limit", Integer.toString(10));
            if (lastTime != 0) {
                uri.appendQueryParameter("beforeTime", Long.toString(lastTime));
            }
            mGetNotificationAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_NOTIFICATION,
                    uri.build().toString(), getContext(), this, false);
            mGetNotificationAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private List<Long> getTimesFromNotificationList(List<DeepLinkedNotification> notifications) {
        List<Long> timeList = new ArrayList<>();
        for (int i = 0; i < notifications.size(); i++) {
            timeList.add(notifications.get(i).getTime());
        }
        return timeList;
    }

    private void checkNotificationStatusAndUpdate(List<DeepLinkedNotification> notifications) {
        List<DeepLinkedNotification> toUpdateNotifications = new ArrayList<>();
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getStatus().equals("NOT_SEEN")) {
                toUpdateNotifications.add(notifications.get(i));
            }
        }
        if (toUpdateNotifications.size() > 0) {
            updateNotificationState(getTimesFromNotificationList(toUpdateNotifications), "SEEN");
        }
    }

    private void loadNotifications(List<DeepLinkedNotification> notifications, boolean hasNext) {
        if (clearListAfterLoading || mDeepLinkedNotifications == null || mDeepLinkedNotifications.size() == 0) {
            mDeepLinkedNotifications = notifications;
            lastTime = notifications.get(notifications.size() - 1).getTime();
            clearListAfterLoading = false;
        } else {
            List<DeepLinkedNotification> tempNotifications;
            tempNotifications = notifications;
            mDeepLinkedNotifications.addAll(tempNotifications);
            lastTime = mDeepLinkedNotifications.get(mDeepLinkedNotifications.size() - 1).getTime();
        }

        this.hasNext = hasNext;
        if (mDeepLinkedNotifications != null && mDeepLinkedNotifications.size() > 0)
            mEmptyListTextView.setVisibility(View.GONE);
        else
            mEmptyListTextView.setVisibility(View.VISIBLE);

        if (isLoading)
            isLoading = false;
        mNotificationListAdapter.notifyDataSetChanged();
        setContentShown(true);
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(notificationBroadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        try {
            mProgressDialog.dismiss();
            if (HttpErrorHandler.isErrorFound(result, getActivity(), mProgressDialog)) {
                setContentShown(true);
                mSwipeRefreshLayout.setRefreshing(false);
                mGetNotificationAsyncTask = null;
                mUpdateNotificationStateTask = null;
                if (isAdded()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                return;
            }
            switch (result.getApiCommand()) {
                case Constants.COMMAND_GET_NOTIFICATION:
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        GetDeepLinkedNotificationResponse getDeepLinkedNotificationResponse = new Gson().
                                fromJson(result.getJsonString(), GetDeepLinkedNotificationResponse.class);
                        List<DeepLinkedNotification> deepLinkedNotifications = getDeepLinkedNotificationResponse.getNotificationList();
                        if (deepLinkedNotifications != null && deepLinkedNotifications.size() > 0) {
                            checkNotificationStatusAndUpdate(deepLinkedNotifications);
                            loadNotifications(deepLinkedNotifications, getDeepLinkedNotificationResponse.isHasNext());
                        } else {
                            if (mDeepLinkedNotifications == null || mDeepLinkedNotifications.size() == 0) {
                                mEmptyListTextView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    setContentShown(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                    mGetNotificationAsyncTask = null;
                    break;
                case Constants.COMMAND_UPDATE_NOTIFICATION_STATE:
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (rowToDelete != -1) {
                            mDeepLinkedNotifications.remove(rowToDelete);
                            mNotificationListAdapter.notifyDataSetChanged();
                            try {
                                lastTime = mDeepLinkedNotifications.get(mDeepLinkedNotifications.size() - 1).getTime();
                            } catch (Exception e) {

                            }
                            rowToDelete = -1;
                        }
                        if (!result.isSilent()) {
                            UpdateNotificationResponse updateNotificationResponse = new Gson().
                                    fromJson(result.getJsonString(), UpdateNotificationResponse.class);
                        }
                        Log.d("update notification", result.getJsonString());
                    } else {
                        rowToDelete = -1;
                    }
                    mUpdateNotificationStateTask = null;
            }
        } catch (Exception e) {
            setContentShown(true);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void implementScrollListener() {
        mNotificationsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    mIsScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mVisibleItem = recyclerView.getChildCount();
                mTotalItemCount = mLayoutManager.getItemCount();
                mPastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
                if (mIsScrolled
                        && (mVisibleItem + mPastVisibleItems) == mTotalItemCount && hasNext && mGetNotificationAsyncTask == null) {
                    isLoading = true;
                    mIsScrolled = false;
                    try {
                        mNotificationListAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    getNotifications();
                }

            }

        });

    }

    private void updateNotificationState(List<Long> timeList, String state) {
        if (mUpdateNotificationStateTask != null) {
            rowToDelete = -1;
            return;
        } else {
            boolean isSilent = true;
            if (state.toUpperCase().equals("CLEARED")) {
                isSilent = false;
                mProgressDialog.show();
            }
            UpdateNotificationStateRequest updateNotificationStateRequest = new UpdateNotificationStateRequest(timeList, state.toUpperCase());
            mUpdateNotificationStateTask = new HttpRequestPutAsyncTask(Constants.COMMAND_UPDATE_NOTIFICATION_STATE,
                    Constants.BASE_URL_PUSH_NOTIFICATION + "v2/update",
                    new Gson().toJson(updateNotificationStateRequest), getContext(), this, isSilent);
            mUpdateNotificationStateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.NotificationViewHolder> {

        public int FOOTER_VIEW = 1;

        public class NotificationViewHolder extends RecyclerView.ViewHolder {
            private final TextView mNameView;
            private final TextView mTimeView;
            private final ProfileImageView mProfileImageView;
            private TextView mLoadMoreTextView;
            private ProgressBar mLoadMoreProgressBar;
            private TextView titleView;
            private LinearLayout notificationHolderLayout;

            public NotificationViewHolder(final View itemView) {
                super(itemView);

                mNameView = (TextView) itemView.findViewById(R.id.textview_description);
                mTimeView = (TextView) itemView.findViewById(R.id.textview_time);
                mProfileImageView = (ProfileImageView) itemView.findViewById(R.id.profile_picture);
                mLoadMoreProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
                mLoadMoreTextView = (TextView) itemView.findViewById(R.id.load_more);
                titleView = (TextView) itemView.findViewById(R.id.textview_title);
                notificationHolderLayout = (LinearLayout) itemView.findViewById(R.id.notification_holder);
            }

            private void setItemVisibilityOfFooterView() {
                if (isLoading) {
                    mLoadMoreProgressBar.setVisibility(View.VISIBLE);
                    mLoadMoreTextView.setVisibility(View.GONE);
                } else {
                    mLoadMoreProgressBar.setVisibility(View.GONE);
                    mLoadMoreTextView.setVisibility(View.VISIBLE);

                    if (hasNext) {
                        mLoadMoreTextView.setText(R.string.load_more);
                    } else {
                        mLoadMoreTextView.setText(R.string.no_more_results);
                    }
                }
            }

            public void bindView(final int pos) {
                if (pos == mDeepLinkedNotifications.size()) {
                    mLoadMoreProgressBar.setVisibility(View.VISIBLE);
                    setItemVisibilityOfFooterView();
                } else {
                    DeepLinkedNotification notification = mDeepLinkedNotifications.get(pos);
                    if (!notification.getStatus().equals("VISITED")) {
                        notificationHolderLayout.setBackgroundColor(getResources().getColor(R.color.colorNotificationIPay));
                    } else {
                        notificationHolderLayout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    }
                    if (notification.getIcon() != null) {
                        mProfileImageView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + notification.getIcon(), false);
                    } else {
                        mProfileImageView.setProfilePicture(R.drawable.ic_ipay_verifiedmember);
                    }
                    mNameView.setText(notification.getBody());
                    mTimeView.setText(Utilities.formatDateWithTime(notification.getTime()));
                    titleView.setText(notification.getTitle());
                }
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri uri = Uri.parse(mDeepLinkedNotifications.get(pos).getDeepLink());
                        List<Long> timeList = new ArrayList<>();
                        timeList.clear();
                        timeList.add(mDeepLinkedNotifications.get(pos).getTime());
                        updateNotificationState(timeList, "VISITED");
                        try {
                            DeepLinkedNotification deepLinkedNotification = mDeepLinkedNotifications.get(pos);
                            if (deepLinkedNotification.getMeta() == null ||
                                    deepLinkedNotification.getMeta().getImageUrl() == null||
                                    deepLinkedNotification.getMeta().getImageUrl().equals("")) {
                                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                                intent.putExtra("url", mDeepLinkedNotifications.get(pos).getDeepLink());
                                intent.putExtra("sourceActivity", "Notification");
                                startActivity(intent);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        notificationHolderLayout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                                    }
                                }, 500);
                            } else {
                                Intent intent = new Intent(getContext(), RichNotificationDetailsActivity.class);
                                intent.putExtra(Constants.TITLE, deepLinkedNotification.getTitle());
                                intent.putExtra(Constants.DESCRIPTION, deepLinkedNotification.getMeta().getDescription());
                                intent.putExtra(Constants.DEEP_LINK, deepLinkedNotification.getDeepLink());
                                intent.putExtra(Constants.IMAGE_URL, deepLinkedNotification.getMeta().getImageUrl());
                                startActivity(intent);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        new AlertDialog.Builder(getContext())
                                .setMessage("Do you want to delete this notification permanently? ")
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        rowToDelete = -1;
                                        List<Long> timeList = new ArrayList<>();
                                        timeList.add(mDeepLinkedNotifications.get(pos).getTime());
                                        rowToDelete = pos;
                                        updateNotificationState(timeList, "CLEARED");
                                    }
                                }).show();
                        return false;
                    }
                });
            }
        }

        @Override
        public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == FOOTER_VIEW) {
                View v = LayoutInflater.from(getContext()).inflate(R.layout.list_item_load_more_footer, parent, false);
                return new NotificationViewHolder(v);
            } else {
                View v = LayoutInflater.from(getContext()).inflate(R.layout.view_notification_description, parent, false);
                return new NotificationViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(NotificationViewHolder holder, int position) {
            try {
                if (position == mDeepLinkedNotifications.size()) {
                    holder.bindView(position);
                } else {
                    holder.bindView(position);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == mDeepLinkedNotifications.size()) {
                return FOOTER_VIEW;
            }

            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            if (mDeepLinkedNotifications == null || mDeepLinkedNotifications.isEmpty())
                return 0;
            else {
                return mDeepLinkedNotifications.size() + 1;
            }
        }

    }

    private class NotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}
