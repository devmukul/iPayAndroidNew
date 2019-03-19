package bd.com.ipay.ipayskeleton.ProfileFragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ProfileActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Address.AddressClass;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Introducer.GetIntroducedListResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Introducer.Introduced;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.IntroductionAndInvite.GetIntroductionRequestsResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.IntroductionAndInvite.IntroductionRequestClass;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactSearchHelper;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IntroducedFragment extends ProgressFragment implements HttpResponseListener {
    private GetIntroducedListResponse mIntroducedListResponse;
    private HttpRequestGetAsyncTask mGetIntroducedTask = null;

    private HttpRequestGetAsyncTask mGetRecommendationRequestsTask = null;
    private GetIntroductionRequestsResponse mRecommendationRequestsResponse;

    private List<IntroductionRequestClass> mRecommendationRequestList;
    private List<Introduced> mIntroducedList;

    private CustomProgressDialog mProgressDialog;
    private RecyclerView mRecyclerView;
    private TextView mEmptyListTextView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private IntroducedAdapter mIntroduceAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Tracker mTracker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_introduces));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_introduced_requests, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.list_introduced_requests);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
        mEmptyListTextView = (TextView) v.findViewById(R.id.empty_list_text);

        mProgressDialog = new CustomProgressDialog(getActivity());

        mIntroduceAdapter = new IntroducedAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mIntroduceAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getIntroducedList();
                getRecommendationRequestsList();
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentShown(false);
        getIntroducedList();
        getRecommendationRequestsList();
    }

    private void getIntroducedList() {
        if (mGetIntroducedTask != null) return;

        mGetIntroducedTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_INTRODUCED_LIST,
                Constants.BASE_URL_MM + Constants.URL_GET_DOWNSTREAM_APPROVED_INTRODUCTION_REQUESTS, getActivity(), false);
        mGetIntroducedTask.mHttpResponseListener = this;
        mGetIntroducedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getRecommendationRequestsList() {
        if (mGetRecommendationRequestsTask != null) return;

        mGetRecommendationRequestsTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_RECOMMENDATION_REQUESTS,
                Constants.BASE_URL_MM + Constants.URL_GET_DOWNSTREAM_NOT_APPROVED_INTRODUCTION_REQUESTS, getActivity(), false);
        mGetRecommendationRequestsTask.mHttpResponseListener = this;
        mGetRecommendationRequestsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public void httpResponseReceiver(GenericHttpResponse result) throws RuntimeException {
        mGetRecommendationRequestsTask = null;
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            setContentShown(true);
            mGetIntroducedTask = null;
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_GET_INTRODUCED_LIST:
                try {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mIntroducedListResponse = gson.fromJson(result.getJsonString(), GetIntroducedListResponse.class);

                        if (mIntroducedList == null) {
                            mIntroducedList = mIntroducedListResponse.getIntroducedList();
                        } else {
                            List<Introduced> tempIntroducedClasses;
                            tempIntroducedClasses = mIntroducedListResponse.getIntroducedList();
                            mIntroducedList.clear();
                            mIntroducedList.addAll(tempIntroducedClasses);
                        }
                        mIntroduceAdapter.notifyDataSetChanged();

                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), R.string.pending_get_failed, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.pending_get_failed, Toast.LENGTH_LONG).show();
                }

                mGetIntroducedTask = null;
                break;
            case Constants.COMMAND_GET_RECOMMENDATION_REQUESTS:

                if (this.isAdded()) setContentShown(true);
                try {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mRecommendationRequestsResponse = gson.fromJson(result.getJsonString(), GetIntroductionRequestsResponse.class);

                        if (mRecommendationRequestList == null) {
                            mRecommendationRequestList = mRecommendationRequestsResponse.getVerificationRequestList();
                        } else {
                            List<IntroductionRequestClass> tempRecommendationRequestsClasses;
                            tempRecommendationRequestsClasses = mRecommendationRequestsResponse.getVerificationRequestList();
                            mRecommendationRequestList.clear();
                            mRecommendationRequestList.addAll(tempRecommendationRequestsClasses);
                        }

                        if (mRecommendationRequestList != null)
                            mIntroduceAdapter.notifyDataSetChanged();

                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), R.string.pending_get_failed, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT).show();
                }

                mProgressDialog.dismiss();
                mSwipeRefreshLayout.setRefreshing(false);
                mGetRecommendationRequestsTask = null;
                break;
        }
        try {
            if (isAdded()) setContentShown(true);
            if (mIntroducedList != null && mIntroducedList.size() == 0 && mRecommendationRequestList != null && mRecommendationRequestList.size() == 0)
                mEmptyListTextView.setVisibility(View.VISIBLE);
            else mEmptyListTextView.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class IntroducedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int RECOMMENDATION_ITEM_VIEW = 1;
        private static final int INTRODUCED_LIST_ITEM_VIEW = 2;
        private static final int INTRODUCED_LIST_HEADER_VIEW = 3;

        public IntroducedAdapter() {
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView mIntroducedNameView;
            private final TextView mIntroducedMobileNumberView;
            private final ProfileImageView mIntroducedProfilePictureView;
            private final TextView mSenderNameView;
            private final TextView mSenderMobileNumberView;
            private final TextView mTimeView;
            private final ProfileImageView mRecommendationProfilePictureView;
            private final View divider;


            public ViewHolder(final View itemView) {
                super(itemView);

                mIntroducedNameView = (TextView) itemView.findViewById(R.id.introduced_name);
                mIntroducedMobileNumberView = (TextView) itemView.findViewById(R.id.introduced_mobile_number);
                mIntroducedProfilePictureView = (ProfileImageView) itemView.findViewById(R.id.introduced_profile_picture);

                mSenderNameView = (TextView) itemView.findViewById(R.id.sender_name);
                mSenderMobileNumberView = (TextView) itemView.findViewById(R.id.sender_mobile_number);
                mTimeView = (TextView) itemView.findViewById(R.id.time);
                mRecommendationProfilePictureView = (ProfileImageView) itemView.findViewById(R.id.profile_picture);
                divider = itemView.findViewById(R.id.divider);
            }

            public void bindViewRecommendationList(int pos) {
                final long requestID = mRecommendationRequestList.get(pos).getId();
                final String senderName = mRecommendationRequestList.get(pos).getName();
                final String senderMobileNumber = mRecommendationRequestList.get(pos).getSenderMobileNumber();
                final String photoUri = mRecommendationRequestList.get(pos).getImageUrl();
                final AddressClass mAddress = mRecommendationRequestList.get(pos).getPresentAddress();
                final String fathersName = mRecommendationRequestList.get(pos).getFather();
                final String mothersName = mRecommendationRequestList.get(pos).getMother();

                mSenderNameView.setText(senderName);
                mSenderMobileNumberView.setText(senderMobileNumber);

                final long requestTime = mRecommendationRequestList.get(pos).getDate();
                if (requestTime == 0) mTimeView.setVisibility(View.GONE);
                else {
                    mTimeView.setVisibility(View.VISIBLE);
                    final String time = Utilities.formatDateWithTime(mRecommendationRequestList.get(pos).getDate());
                    mTimeView.setText(time);
                }
                mRecommendationProfilePictureView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + photoUri, false);

                if (pos == mRecommendationRequestList.size() - 1)
                    divider.setVisibility(View.GONE);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Bundle bundle = new Bundle();
                        bundle.putLong(Constants.REQUEST_ID, requestID);
                        bundle.putString(Constants.NAME, senderName);
                        bundle.putString(Constants.PHOTO_URI, Constants.BASE_URL_FTP_SERVER + photoUri);
                        bundle.putString(Constants.MOBILE_NUMBER, senderMobileNumber);
                        bundle.putString(Constants.FATHERS_NAME, fathersName);
                        bundle.putString(Constants.MOTHERS_NAME, mothersName);
                        bundle.putSerializable(Constants.ADDRESS, mAddress);
                        bundle.putBoolean(Constants.IS_IN_CONTACTS,
                                new ContactSearchHelper(getActivity()).searchMobileNumber(senderMobileNumber));

                        ((ProfileActivity) getActivity()).switchToRecommendationReviewFragment(bundle);
                    }
                });
            }

            public void bindViewForIntroducedList(int pos) {
                if (mRecommendationRequestList != null && mRecommendationRequestList.size() != 0)
                    pos = pos - mRecommendationRequestList.size() - 1;

                String introducedName = mIntroducedList.get(pos).getName();
                String introducedMobileNumber = mIntroducedList.get(pos).getMobileNumber();
                String imageUrl = mIntroducedList.get(pos).getProfilePictureUrl();
                mIntroducedProfilePictureView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + imageUrl, false);
                mIntroducedNameView.setText(introducedName);
                mIntroducedMobileNumberView.setText(introducedMobileNumber);
            }
        }

        private class IntroducedListHeaderViewHolder extends ViewHolder {
            public IntroducedListHeaderViewHolder(View itemView) {
                super(itemView);
            }
        }

        private class IntroducedListItemViewHolder extends ViewHolder {
            public IntroducedListItemViewHolder(View itemView) {
                super(itemView);
            }
        }

        private class RecommendationRequestViewHolder extends ViewHolder {
            public RecommendationRequestViewHolder(View itemView) {
                super(itemView);
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;

            if (viewType == INTRODUCED_LIST_ITEM_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_introduced_list, parent, false);
                return new IntroducedListItemViewHolder(v);

            } else if (viewType == RECOMMENDATION_ITEM_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_introduction_requests, parent, false);
                return new RecommendationRequestViewHolder(v);

            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_introduced_list_header, parent, false);
                return new IntroducedListHeaderViewHolder(v);

            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                if (holder instanceof IntroducedListItemViewHolder) {
                    IntroducedListItemViewHolder vh = (IntroducedListItemViewHolder) holder;
                    vh.bindViewForIntroducedList(position);

                } else if (holder instanceof RecommendationRequestViewHolder) {
                    RecommendationRequestViewHolder vh = (RecommendationRequestViewHolder) holder;
                    vh.bindViewRecommendationList(position);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            int introducedListSize = 0;
            int recommendationRequestListSize = 0;

            if (mRecommendationRequestList == null && mIntroducedList == null)
                return 0;

            if (mRecommendationRequestList != null)
                recommendationRequestListSize = mRecommendationRequestList.size();
            if (mIntroducedList != null)
                introducedListSize = mIntroducedList.size();

            if (recommendationRequestListSize > 0 && introducedListSize > 0)
                return recommendationRequestListSize + 1 + introducedListSize;
            else if (introducedListSize > 0 && recommendationRequestListSize == 0)
                return introducedListSize;
            else if (introducedListSize == 0 && recommendationRequestListSize > 0)
                return recommendationRequestListSize;
            else return 0;

        }

        @Override
        public int getItemViewType(int position) {
            int recommendationRequestListSize = 0;
            int introducedListSize = 0;

            if (mRecommendationRequestList == null && mIntroducedList == null)
                return super.getItemViewType(position);

            if (mRecommendationRequestList != null)
                recommendationRequestListSize = mRecommendationRequestList.size();
            if (mIntroducedList != null)
                introducedListSize = mIntroducedList.size();

            if (recommendationRequestListSize > 0 && introducedListSize > 0) {
                if (position == recommendationRequestListSize)
                    return INTRODUCED_LIST_HEADER_VIEW;
                else if (position > recommendationRequestListSize)
                    return INTRODUCED_LIST_ITEM_VIEW;
                else return RECOMMENDATION_ITEM_VIEW;

            } else if (introducedListSize > 0 && recommendationRequestListSize == 0) {
                return INTRODUCED_LIST_ITEM_VIEW;

            } else if (introducedListSize == 0 && recommendationRequestListSize > 0) {
                return RECOMMENDATION_ITEM_VIEW;
            } else return RECOMMENDATION_ITEM_VIEW;
        }
    }
}
