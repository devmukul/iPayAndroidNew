package bd.com.ipay.ipayskeleton.ProfileFragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devspark.progressfragment.ProgressFragment;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.DialogActivities.ContactPickerDialogActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Introducer.GetIntroducerListResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Introducer.GetRecommendationRequestsResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Introducer.Introducer;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.Introducer.RecommendationRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.IntroductionAndInvite.AskForIntroductionResponse;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IntroducerFragment extends ProgressFragment implements HttpResponseListener {
    private final int PICK_CONTACT_REQUEST = 100;
    private int MINIMUM_INTRODUCER_COUNT;           // Default value

    private GetIntroducerListResponse mIntroducerListResponse;
    private HttpRequestGetAsyncTask mGetIntroducersTask = null;

    private GetRecommendationRequestsResponse mSentRequestListResponse;
    private HttpRequestGetAsyncTask mGetSentRequestTask = null;

    private HttpRequestPostAsyncTask mAskForRecommendationTask = null;
    private AskForIntroductionResponse mAskForIntroductionResponse;

    private List<RecommendationRequest> mSentRequestList;
    private List<Introducer> mIntroducerList;

    private CustomProgressDialog mProgressDialog;
    private RecyclerView mRecyclerView;
    private TextView mEmptyListTextView;
    private RelativeLayout mCompleteIntroducerHeaderLayout;
    private TextView mIntroducerStatusTextView;
    private ImageView mAskForRecommendation;
    private IntroduceAdapter mIntroduceAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Tracker mTracker;

    @Override
    public void onResume() {
        super.onResume();
        Utilities.sendScreenTracker(mTracker, getString(R.string.screen_name_introducer));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mTracker = Utilities.getTracker(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_introducer_requests, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.list_introducer_requests);
        mEmptyListTextView = (TextView) v.findViewById(R.id.empty_list_text);
        mCompleteIntroducerHeaderLayout = (RelativeLayout) v.findViewById(R.id.complete_introduction_header);
        mIntroducerStatusTextView = (TextView) v.findViewById(R.id.intoduce_status);
        mAskForRecommendation = (ImageView) v.findViewById(R.id.ask_for_recommendation);

        mProgressDialog = new CustomProgressDialog(getActivity());

        mIntroduceAdapter = new IntroduceAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mIntroduceAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getIntroducerList();
                getSentRequestList();
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentShown(false);
        getIntroducerList();
        getSentRequestList();

    }

    private void getIntroducerList() {
        if (mGetIntroducersTask != null) return;

        mGetIntroducersTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_INTRODUCER_LIST,
                Constants.BASE_URL_MM + Constants.URL_GET_UPSTREAM_APPROVED_INTRODUCTION_REQUESTS, getActivity(), true);
        mGetIntroducersTask.mHttpResponseListener = this;
        mGetIntroducersTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getSentRequestList() {
        if (mGetSentRequestTask != null) return;

        mGetSentRequestTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SENT_REQUEST_LIST,
                Constants.BASE_URL_MM + Constants.URL_GET_UPSTREAM_NOT_APPROVED_INTRODUCTION_REQUESTS, getActivity(), true);
        mGetSentRequestTask.mHttpResponseListener = this;
        mGetSentRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void sendRecommendationRequest(String mobileNumber) {
        if (mAskForRecommendationTask != null) return;

        mProgressDialog.show();
        mAskForRecommendationTask = new HttpRequestPostAsyncTask(Constants.COMMAND_ASK_FOR_RECOMMENDATION,
                Constants.BASE_URL_MM + Constants.URL_ASK_FOR_INTRODUCTION + mobileNumber, null, getActivity(), false);
        mAskForRecommendationTask.mHttpResponseListener = this;
        mAskForRecommendationTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) throws RuntimeException {
        if (HttpErrorHandler.isErrorFound(result, getContext(), mProgressDialog)) {
            mProgressDialog.dismiss();
            mGetIntroducersTask = null;
            mSwipeRefreshLayout.setRefreshing(false);
            setContentShown(true);
            return;
        }

        Gson gson = new Gson();

        switch (result.getApiCommand()) {
            case Constants.COMMAND_GET_INTRODUCER_LIST:
                try {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mIntroducerListResponse = gson.fromJson(result.getJsonString(), GetIntroducerListResponse.class);

                        if (mIntroducerList == null) {
                            mIntroducerList = mIntroducerListResponse.getIntroducers();
                            MINIMUM_INTRODUCER_COUNT = mIntroducerListResponse.getRequiredForProfileCompletion();
                            mAskForRecommendation.setOnClickListener(new View.OnClickListener() {
                                @Override
                                @ValidateAccess(ServiceIdConstants.GET_CONTACTS)
                                public void onClick(View v) {
                                    Intent intent = new Intent(getActivity(), ContactPickerDialogActivity.class);
                                    intent.putExtra(Constants.VERIFIED_USERS_ONLY, true);                   // Get the verified iPay users only.
                                    startActivityForResult(intent, PICK_CONTACT_REQUEST);
                                }
                            });

                        } else {
                            List<Introducer> tempIntroducerClasses;
                            tempIntroducerClasses = mIntroducerListResponse.getIntroducers();
                            mIntroducerList.clear();
                            mIntroducerList.addAll(tempIntroducerClasses);
                        }
                        if (mIntroducerListResponse.getIntroducers().size() == 0) {
                            mCompleteIntroducerHeaderLayout.setVisibility(View.VISIBLE);
                            String mIntroducerMessage = getString(R.string.you_do_not_have_any_introducer);
                            mIntroducerStatusTextView.setText(mIntroducerMessage);
                        } else {
                            mCompleteIntroducerHeaderLayout.setVisibility(View.GONE);
                        }
                        mIntroduceAdapter.notifyDataSetChanged();

                        mSwipeRefreshLayout.setRefreshing(false);
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), R.string.pending_get_failed, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.pending_get_failed, Toast.LENGTH_LONG).show();
                }

                mGetIntroducersTask = null;
                break;
            case Constants.COMMAND_GET_SENT_REQUEST_LIST:
                if (this.isAdded()) setContentShown(true);
                try {
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        mSentRequestListResponse = gson.fromJson(result.getJsonString(), GetRecommendationRequestsResponse.class);

                        if (mSentRequestList == null) {
                            mSentRequestList = mSentRequestListResponse.getSentRequestList();
                        } else {
                            List<RecommendationRequest> tempIntroducerClasses;
                            tempIntroducerClasses = mSentRequestListResponse.getSentRequestList();
                            mSentRequestList.clear();
                            mSentRequestList.addAll(tempIntroducerClasses);
                        }
                        mIntroduceAdapter.notifyDataSetChanged();
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), R.string.pending_get_failed, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.pending_get_failed, Toast.LENGTH_LONG).show();
                }

                mSwipeRefreshLayout.setRefreshing(false);
                mGetSentRequestTask = null;
                break;
            case Constants.COMMAND_ASK_FOR_RECOMMENDATION:
                try {
                    mAskForIntroductionResponse = gson.fromJson(result.getJsonString(), AskForIntroductionResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), R.string.introduction_request_sent, Toast.LENGTH_LONG).show();
                        }
                    } else if (getActivity() != null) {
                        Toast.makeText(getActivity(), mAskForIntroductionResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), R.string.failed_asking_introduction, Toast.LENGTH_LONG).show();
                    }
                }
                mProgressDialog.dismiss();
                mAskForRecommendationTask = null;
                break;
        }
        try {
            if (isAdded()) setContentShown(true);

            if (mIntroducerList != null && mIntroducerList.size() == 0 && mSentRequestList != null && mSentRequestList.size() == 0)
                mEmptyListTextView.setVisibility(View.VISIBLE);
            else mEmptyListTextView.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class IntroduceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int INTRODUCER_LIST_ITEM_VIEW = 1;
        private static final int INTRODUCER_LIST_HEADER_VIEW = 2;
        private static final int SENT_REQUEST_LIST_ITEM_VIEW = 5;

        public IntroduceAdapter() {
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView mIntroducerName;
            private final TextView mIntroducerMobileNumber;
            private final ProfileImageView mIntroducerProfilePictureView;
            private final TextView mRequestedName;
            private final TextView mRequestedMobileNumber;
            private final ProfileImageView mRequestedProfilePictureView;
            private final ImageView mSentRequestStatus;
            private final TextView mTimeView;
            private final View divider;

            public ViewHolder(final View itemView) {
                super(itemView);

                mIntroducerName = (TextView) itemView.findViewById(R.id.introducer_name);
                mIntroducerMobileNumber = (TextView) itemView.findViewById(R.id.introducer_mobile_number);
                mIntroducerProfilePictureView = (ProfileImageView) itemView.findViewById(R.id.profile_picture);

                mRequestedName = (TextView) itemView.findViewById(R.id.requested_name);
                mRequestedMobileNumber = (TextView) itemView.findViewById(R.id.requested_mobile_number);
                mRequestedProfilePictureView = (ProfileImageView) itemView.findViewById(R.id.requested_profile_picture);
                mSentRequestStatus = (ImageView) itemView.findViewById(R.id.request_status);
                mTimeView = (TextView) itemView.findViewById(R.id.time);
                divider = itemView.findViewById(R.id.divider);
            }


            public void bindViewForSentRequestList(int pos) {
                String RequestedName = mSentRequestList.get(pos).getName();
                String RequestedMobileNumber = mSentRequestList.get(pos).getMobileNumber();
                String requestStatus = mSentRequestList.get(pos).getStatus();
                String imageUrl = mSentRequestList.get(pos).getProfilePictureUrl();
                mRequestedProfilePictureView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + imageUrl, false);
                mRequestedName.setText(RequestedName);
                mRequestedMobileNumber.setText(RequestedMobileNumber);

                if (pos == mSentRequestList.size() - 1)
                    divider.setVisibility(View.GONE);

                switch (requestStatus) {
                    case Constants.INTRODUCTION_REQUEST_STATUS_PENDING:
                        mSentRequestStatus.setImageResource(R.drawable.ic_workinprogress);
                        break;
                    case Constants.INTRODUCTION_REQUEST_STATUS_APPROVED:
                        mSentRequestStatus.setImageResource(R.drawable.ic_verified);
                        break;
                    case Constants.INTRODUCTION_REQUEST_STATUS_SPAM:
                        mSentRequestStatus.setImageResource(R.drawable.ic_introducer_warning);
                        break;
                    default:
                        // INTRODUCTION_REQUEST_STATUS_REJECTED
                        mSentRequestStatus.setImageResource(R.drawable.ic_introducer_notverified);
                        break;
                }
            }

            public void bindViewForIntroducerList(int pos) {
                if (mSentRequestList != null && mSentRequestList.size() != 0)
                    pos = pos - mSentRequestList.size() - 1;

                String introducerName = mIntroducerList.get(pos).getName();
                String introducerMobileNumber = mIntroducerList.get(pos).getMobileNumber();
                String imageUrl = mIntroducerList.get(pos).getProfilePictureUrl();
                mIntroducerProfilePictureView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + imageUrl, false);
                mIntroducerName.setText(introducerName);
                mIntroducerMobileNumber.setText(introducerMobileNumber);
                long introducedTime = mIntroducerList.get(pos).getIntroducedDate();

                if (introducedTime == 0) mTimeView.setVisibility(View.GONE);
                else {
                    mTimeView.setVisibility(View.VISIBLE);
                    String time = Utilities.formatDateWithoutTime(mIntroducerList.get(pos).getIntroducedDate());
                    mTimeView.setText(getString(R.string.introduced_on) + " " + time);
                }
            }
        }

        private class IntroducerListHeaderViewHolder extends ViewHolder {
            public IntroducerListHeaderViewHolder(View itemView) {
                super(itemView);
            }
        }

        private class IntroducerListItemViewHolder extends ViewHolder {
            public IntroducerListItemViewHolder(View itemView) {
                super(itemView);
            }
        }

        private class SentRequestListHeaderViewHolder extends ViewHolder {
            public SentRequestListHeaderViewHolder(View itemView) {
                super(itemView);
            }
        }

        private class SentRequestListItemViewHolder extends ViewHolder {
            public SentRequestListItemViewHolder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;

            if (viewType == INTRODUCER_LIST_ITEM_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_introducer_list, parent, false);
                return new IntroducerListItemViewHolder(v);

            } else if (viewType == INTRODUCER_LIST_HEADER_VIEW) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_introducer_list_header, parent, false);
                return new IntroducerListHeaderViewHolder(v);

            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_introduction_request_from_me, parent, false);
                return new SentRequestListItemViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                if (holder instanceof SentRequestListItemViewHolder) {
                    SentRequestListItemViewHolder vh = (SentRequestListItemViewHolder) holder;
                    vh.bindViewForSentRequestList(position);

                } else if (holder instanceof IntroducerListItemViewHolder) {
                    IntroducerListItemViewHolder vh = (IntroducerListItemViewHolder) holder;
                    vh.bindViewForIntroducerList(position);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            int introducerListSize = 0;
            int sentRequestsListSize = 0;

            if (mIntroducerList == null && mSentRequestList == null) return 0;

            // Get the sizes of the lists
            if (mSentRequestList != null)
                sentRequestsListSize = mSentRequestList.size();
            if (mIntroducerList != null)
                introducerListSize = mIntroducerList.size();

            if (sentRequestsListSize > 0 && introducerListSize > 0)
                return sentRequestsListSize + 1 + introducerListSize;
            else if (introducerListSize > 0 && sentRequestsListSize == 0)
                return introducerListSize;
            else if (introducerListSize == 0 && sentRequestsListSize > 0)
                return sentRequestsListSize;
            else return 0;
        }

        @Override
        public int getItemViewType(int position) {
            int sentRequestListSize = 0;
            int introducerListSize = 0;

            if (mSentRequestList == null && mIntroducerList == null)
                return super.getItemViewType(position);

            if (mSentRequestList != null)
                sentRequestListSize = mSentRequestList.size();
            if (mIntroducerList != null)
                introducerListSize = mIntroducerList.size();

            if (sentRequestListSize > 0 && introducerListSize > 0) {
                if (position == sentRequestListSize)
                    return INTRODUCER_LIST_HEADER_VIEW;
                else if (position > sentRequestListSize)
                    return INTRODUCER_LIST_ITEM_VIEW;
                else return SENT_REQUEST_LIST_ITEM_VIEW;

            } else if (introducerListSize > 0 && sentRequestListSize == 0) {
                return INTRODUCER_LIST_ITEM_VIEW;

            } else if (introducerListSize == 0 && sentRequestListSize > 0) {
                return SENT_REQUEST_LIST_ITEM_VIEW;
            } else return SENT_REQUEST_LIST_ITEM_VIEW;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {

            String mobileNumber = data.getStringExtra(Constants.MOBILE_NUMBER);

            if (mobileNumber != null) sendRecommendationRequest(mobileNumber);
            else
                Toast.makeText(getActivity(), R.string.could_not_fetch_the_number, Toast.LENGTH_SHORT).show();

        }
    }
}