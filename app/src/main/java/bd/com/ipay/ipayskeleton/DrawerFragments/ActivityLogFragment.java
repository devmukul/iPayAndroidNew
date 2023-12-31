package bd.com.ipay.ipayskeleton.DrawerFragments;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.CustomSwipeRefreshLayout;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UserActivity.GetActivityRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UserActivity.UserActivityClass;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UserActivity.UserActivityResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class ActivityLogFragment extends ProgressFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mUserActivityTask = null;
    private UserActivityResponse mUserActivityResponse;

    private RecyclerView mActivityLogRecyclerView;
    private ActivityLogAdapter mActivityLogAdapter;
    private LinearLayoutManager mLayoutManager;
    private List<UserActivityClass> userActivityResponsesList;
    private CustomSwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyListTextView;

    private LinearLayout eventFilterLayout;
    private LinearLayout dateFilterLayout;

    private CheckBox mChangeProfileCheckBox;
    private CheckBox mSystemEventCheckBox;
    private CheckBox mSecurityChangeCheckBox;
    private CheckBox mVerificationCheckBox;
    private Button mClearEventFilterButton;

    private Button mFromDateButton;
    private Button mToDateButton;
    private Button clearDateFilterButton;
    private Button filterByDateButton;

    private int historyPageCount = 0;
    private Integer type = null;
    private Calendar fromDate = null;
    private Calendar toDate = null;

    private boolean hasNext = false;
    private boolean isLoading = false;
    private boolean clearListAfterLoading;
    private boolean mIsScrolled = false;
    private int mTotalItemCount = 0;
    private int mPastVisiblesItems;
    private int mVisibleItem;

    private Menu menu;
    private Tracker mTracker;

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_activity_log));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.activity_log, menu);
        menuInflater.inflate(R.menu.clear_filter, menu);
        this.menu = menu;
        menu.findItem(R.id.action_clear_filter).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter_by_date:
                if (eventFilterLayout.getVisibility() == View.VISIBLE)
                    eventFilterLayout.setVisibility(View.GONE);
                dateFilterLayout.setVisibility(View.VISIBLE);
                return true;
            case R.id.action_filter_by_event:
                if (dateFilterLayout.getVisibility() == View.VISIBLE)
                    dateFilterLayout.setVisibility(View.GONE);
                eventFilterLayout.setVisibility(View.VISIBLE);
                return true;
            case R.id.action_clear_filter:
                clearDateFilter();
                clearEventFilter();
                historyPageCount = 0;
                if (userActivityResponsesList != null) userActivityResponsesList.clear();
                setContentShown(false);
                getUserActivities();
                menu.findItem(R.id.action_clear_filter).setVisible(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_activity_log, container, false);

        setTitle();
        mActivityLogRecyclerView = (RecyclerView) v.findViewById(R.id.list_recent_activity_logs);
        mSwipeRefreshLayout = (CustomSwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
        mEmptyListTextView = (TextView) v.findViewById(R.id.empty_list_text);

        mActivityLogAdapter = new ActivityLogAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mActivityLogRecyclerView.setLayoutManager(mLayoutManager);
        mActivityLogRecyclerView.setAdapter(mActivityLogAdapter);

        eventFilterLayout = (LinearLayout) v.findViewById(R.id.event_filters_layout);
        dateFilterLayout = (LinearLayout) v.findViewById(R.id.date_filter_layout);
        mClearEventFilterButton = (Button) v.findViewById(R.id.button_clear_filter_event);
        mChangeProfileCheckBox = (CheckBox) v.findViewById(R.id.filter_profile_changes);
        mSecurityChangeCheckBox = (CheckBox) v.findViewById(R.id.filter_security_changes);
        mVerificationCheckBox = (CheckBox) v.findViewById(R.id.filter_verification_changes);
        mSystemEventCheckBox = (CheckBox) v.findViewById(R.id.filter_system_event);

        mFromDateButton = (Button) v.findViewById(R.id.fromButton);
        mToDateButton = (Button) v.findViewById(R.id.toButton);
        clearDateFilterButton = (Button) v.findViewById(R.id.button_clear_filter_date);
        filterByDateButton = (Button) v.findViewById(R.id.button_filter_date);

        mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isConnectionAvailable(getActivity())) {
                    clearListAfterLoading = true;
                    historyPageCount = 0;
                    getUserActivities();
                }
            }
        });
        getUserActivities();


        implementScrollListener();
        setActionsForEventTypeFilter();
        setActionsForDateFilter();

        // Handle back press action when action mode is on.
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (dateFilterLayout.getVisibility() == View.VISIBLE)
                        dateFilterLayout.setVisibility(View.GONE);
                    else if (eventFilterLayout.getVisibility() == View.VISIBLE)
                        eventFilterLayout.setVisibility(View.GONE);
                    else return false;
                }
                return true;
            }
        });

        return v;
    }

    private void setTitle() {
        getActivity().setTitle(R.string.activity_log);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentShown(false);
    }

    private void clearDateFilter() {
        fromDate = null;
        toDate = null;
        mFromDateButton.setText("");
        mToDateButton.setText("");
    }

    private void clearEventFilter() {
        type = null;
        mChangeProfileCheckBox.setChecked(false);
        mVerificationCheckBox.setChecked(false);
        mSecurityChangeCheckBox.setChecked(false);
        mSystemEventCheckBox.setChecked(false);
    }

    private void setActionsForDateFilter() {

        clearDateFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFilterLayout.setVisibility(View.GONE);
                clearDateFilter();
                clearEventFilter();
                historyPageCount = 0;
                if (userActivityResponsesList != null) userActivityResponsesList.clear();
                setContentShown(false);
                getUserActivities();
            }
        });

        filterByDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.findItem(R.id.action_clear_filter).setVisible(true);
                dateFilterLayout.setVisibility(View.GONE);
                clearEventFilter();
                historyPageCount = 0;
                if (userActivityResponsesList != null) userActivityResponsesList.clear();
                setContentShown(false);
                getUserActivities();
            }
        });

        mFromDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                if (!mFromDateButton.getText().toString().equals("")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    final Date fromDate;
                    try {
                        fromDate = sdf.parse(mFromDateButton.getText().toString().trim());
                        calendar.setTime(fromDate);
                        DatePickerDialog dpd = new DatePickerDialog(getActivity(), mFromDateSetListener, calendar.get(Calendar.YEAR)
                                , calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                        dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
                        dpd.show();

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    DatePickerDialog dpd = new DatePickerDialog(getActivity(), mFromDateSetListener, calendar.get(Calendar.YEAR)
                            , calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    dpd.show();
                }
            }
        });


        mToDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                final Date fromDate;
                try {
                    fromDate = sdf.parse(mFromDateButton.getText().toString().trim());

                    DatePickerDialog dpd = new DatePickerDialog(getActivity(), mToDateSetListener, Constants.STARTING_YEAR
                            , Constants.STARTING_MONTH, Constants.STARTING_DATE);
                    dpd.getDatePicker().setMinDate(fromDate.getTime());
                    dpd.getDatePicker().setMaxDate(System.currentTimeMillis());
                    dpd.show();

                } catch (ParseException e) {
                    Toaster.makeText(getActivity(), R.string.select_from_date_first, Toast.LENGTH_LONG);
                }
            }
        });

    }

    private void setActionsForEventTypeFilter() {


        mClearEventFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.findItem(R.id.action_clear_filter).setVisible(true);
                clearDateFilter();
                clearEventFilter();

                historyPageCount = 0;
                if (userActivityResponsesList != null) userActivityResponsesList.clear();
                setContentShown(false);
                getUserActivities();
                eventFilterLayout.setVisibility(View.GONE);
            }
        });

        mChangeProfileCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.findItem(R.id.action_clear_filter).setVisible(true);
                if (mChangeProfileCheckBox.isChecked()) {
                    type = Constants.ACTIVITY_TYPE_CHANGE_PROFILE;
                    mVerificationCheckBox.setChecked(false);
                    mSecurityChangeCheckBox.setChecked(false);
                    mSystemEventCheckBox.setChecked(false);
                } else type = null;

                clearDateFilter();
                historyPageCount = 0;
                if (userActivityResponsesList != null) userActivityResponsesList.clear();
                setContentShown(false);
                getUserActivities();
                eventFilterLayout.setVisibility(View.GONE);
            }
        });

        mSecurityChangeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.findItem(R.id.action_clear_filter).setVisible(true);
                if (mSecurityChangeCheckBox.isChecked()) {
                    type = Constants.ACTIVITY_TYPE_CHANGE_SECURITY;
                    mVerificationCheckBox.setChecked(false);
                    mChangeProfileCheckBox.setChecked(false);
                    mSystemEventCheckBox.setChecked(false);
                } else type = null;

                clearDateFilter();
                historyPageCount = 0;
                if (userActivityResponsesList != null) userActivityResponsesList.clear();
                setContentShown(false);
                getUserActivities();
                eventFilterLayout.setVisibility(View.GONE);
            }
        });

        mVerificationCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.findItem(R.id.action_clear_filter).setVisible(true);
                if (mVerificationCheckBox.isChecked()) {
                    type = Constants.ACTIVITY_TYPE_VERIFICATION;
                    mChangeProfileCheckBox.setChecked(false);
                    mSecurityChangeCheckBox.setChecked(false);
                    mSystemEventCheckBox.setChecked(false);
                } else type = null;

                clearDateFilter();
                historyPageCount = 0;
                if (userActivityResponsesList != null) userActivityResponsesList.clear();
                setContentShown(false);
                getUserActivities();
                eventFilterLayout.setVisibility(View.GONE);
            }
        });

        mSystemEventCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.findItem(R.id.action_clear_filter).setVisible(true);
                if (mSystemEventCheckBox.isChecked()) {
                    type = Constants.ACTIVITY_TYPE_SYSTEM_EVENT;
                    mChangeProfileCheckBox.setChecked(false);
                    mSecurityChangeCheckBox.setChecked(false);
                    mChangeProfileCheckBox.setChecked(false);
                } else type = null;

                clearDateFilter();
                historyPageCount = 0;
                if (userActivityResponsesList != null) userActivityResponsesList.clear();
                setContentShown(false);
                getUserActivities();
                eventFilterLayout.setVisibility(View.GONE);
            }
        });

    }

    private final DatePickerDialog.OnDateSetListener mFromDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    fromDate = Calendar.getInstance();
                    fromDate.clear();
                    fromDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    fromDate.set(Calendar.MONTH, monthOfYear);
                    fromDate.set(Calendar.YEAR, year);

                    toDate = Calendar.getInstance();
                    toDate.setTime(fromDate.getTime());
                    toDate.add(Calendar.DATE, 1);

                    String fromDateStr = String.format(Constants.DATE_FORMAT, dayOfMonth, monthOfYear + 1, year);

                    mFromDateButton.setText(fromDateStr);
                    mToDateButton.setText(fromDateStr);
                }
            };

    private final DatePickerDialog.OnDateSetListener mToDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {

                    toDate = Calendar.getInstance();
                    toDate.clear();
                    toDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    toDate.set(Calendar.MONTH, monthOfYear);
                    toDate.set(Calendar.YEAR, year);

                    // If we want to filter activities until August 1, 2016, we actually need to set toDate to
                    // August 2, 2016 while sending request to server. Why? Because August 1, 2016 means
                    // 12:00:00 am at August 1, whereas we need to show all activities until 11:59:59 pm.
                    // Simplest way to do this is to just show all activities until 12:00 am in the next day.
                    toDate.add(Calendar.DATE, 1);

                    String toDateStr = String.format(Constants.DATE_FORMAT, dayOfMonth, monthOfYear + 1, year);

                    mToDateButton.setText(toDateStr);
                }
            };

    private void getUserActivities() {
        if (mUserActivityTask != null) {
            return;
        }

        String url = GetActivityRequestBuilder.generateUri(type,
                fromDate, toDate, historyPageCount, Constants.ACTIVITY_LOG_COUNT);
        mUserActivityTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_USER_ACTIVITIES,
                url, getActivity(), false);
        mUserActivityTask.mHttpResponseListener = this;
        mUserActivityTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void implementScrollListener() {
        mActivityLogRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

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
                mPastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                if (mIsScrolled
                        && (mVisibleItem + mPastVisiblesItems) == mTotalItemCount && hasNext) {
                    isLoading = true;
                    mIsScrolled = false;
                    historyPageCount = historyPageCount + 1;
                    mActivityLogAdapter.notifyDataSetChanged();
                    getUserActivities();
                }

            }

        });

    }


    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {

        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            mUserActivityTask = null;
            setContentShown(true);
            return;
        }


        Gson gson = new Gson();

        if (result.getApiCommand().equals(Constants.COMMAND_GET_USER_ACTIVITIES)) {
            if (this.isAdded()) setContentShown(true);
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

                try {
                    mUserActivityResponse = gson.fromJson(result.getJsonString(), UserActivityResponse.class);

                    if (clearListAfterLoading || userActivityResponsesList == null || userActivityResponsesList.size() == 0) {
                        userActivityResponsesList = mUserActivityResponse.getActivities();
                        clearListAfterLoading = false;
                    } else {
                        List<UserActivityClass> tempUserActivityResponsesList;
                        tempUserActivityResponsesList = mUserActivityResponse.getActivities();
                        userActivityResponsesList.addAll(tempUserActivityResponsesList);
                    }

                    hasNext = mUserActivityResponse.isHasNext();
                    if (isLoading)
                        isLoading = false;
                    mActivityLogAdapter.notifyDataSetChanged();
                    setContentShown(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toaster.makeText(getActivity(), R.string.user_activity_get_failed, Toast.LENGTH_LONG);
                }

            } else {
                if (getActivity() != null)
                    Toaster.makeText(getActivity(), R.string.user_activity_get_failed, Toast.LENGTH_LONG);
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mUserActivityTask = null;

        }

        if (userActivityResponsesList != null && userActivityResponsesList.size() == 0)
            mEmptyListTextView.setVisibility(View.VISIBLE);
        else mEmptyListTextView.setVisibility(View.GONE);
    }

    public class ActivityLogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int FOOTER_VIEW = 1;
        private static final int ACTIVITY_LOG_LIST_ITEM_VIEW = 2;


        public class ActivityLogViewHolder extends RecyclerView.ViewHolder {
            private final ImageView mPortrait;
            private final TextView mTransactionDescription;
            private final TextView mTime;

            public ActivityLogViewHolder(final View itemView) {
                super(itemView);

                mTransactionDescription = (TextView) itemView.findViewById(R.id.activity_description);
                mTime = (TextView) itemView.findViewById(R.id.time);
                mPortrait = (ImageView) itemView.findViewById(R.id.portrait);
            }

            public void bindView(int pos) {

                String description = userActivityResponsesList.get(pos).getDescription();

                String time = DateUtils.getRelativeTimeSpanString(userActivityResponsesList.get(pos).getTime()).toString();
                mTransactionDescription.setText(description);
                mTime.setText(time);

                // Set icon for activity type
                if (userActivityResponsesList.get(pos).getType() == Constants.ACTIVITY_TYPE_CHANGE_PROFILE) {
                    mPortrait.setImageResource(R.drawable.ic_change);
                } else if (userActivityResponsesList.get(pos).getType() == Constants.ACTIVITY_TYPE_MONEY_IN) {
                    mPortrait.setImageResource(R.drawable.ic_activity_cash_in);

                } else if (userActivityResponsesList.get(pos).getType() == Constants.ACTIVITY_TYPE_MONEY_OUT) {
                    mPortrait.setImageResource(R.drawable.ic_activity_cash_out);
                } else if (userActivityResponsesList.get(pos).getType() == Constants.ACTIVITY_TYPE_VERIFICATION) {
                    mPortrait.setImageResource(R.drawable.ic_verified_log);
                } else if (userActivityResponsesList.get(pos).getType() == Constants.ACTIVITY_TYPE_SYSTEM_EVENT) {
                    if (userActivityResponsesList.get(pos).getDescription().equalsIgnoreCase(Constants.SIGNED_IN)) {
                        mPortrait.setImageResource(R.drawable.ic_signin);
                    } else {
                        mPortrait.setImageResource(R.drawable.ic_signout);
                    }
                } else if (userActivityResponsesList.get(pos).getType() == Constants.ACTIVITY_TYPE_CHANGE_SECURITY) {
                    mPortrait.setImageResource(R.drawable.ic_security);
                }
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            private TextView mLoadMoreTextView;
            private ProgressBar mLoadMoreProgressBar;

            public FooterViewHolder(final View itemView) {
                super(itemView);

                mLoadMoreTextView = (TextView) itemView.findViewById(R.id.load_more);
                mLoadMoreProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            }

            public void bindView() {
                setItemVisibilityOfFooterView();
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

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v;

            if (viewType == FOOTER_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_load_more_footer, parent, false);
                return new FooterViewHolder(v);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_activity_log, parent, false);
                return new ActivityLogViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                if (holder instanceof ActivityLogViewHolder) {
                    ActivityLogViewHolder vh = (ActivityLogViewHolder) holder;
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
            if (userActivityResponsesList == null || userActivityResponsesList.isEmpty())
                return 0;
            else
                return userActivityResponsesList.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {

            if (position == getItemCount() - 1) {
                return FOOTER_VIEW;
            } else {
                return ACTIVITY_LOG_LIST_ITEM_VIEW;
            }
        }
    }
}
