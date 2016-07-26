package bd.com.ipay.ipayskeleton.ProfileFragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.devspark.progressfragment.ProgressFragment;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import bd.com.ipay.ipayskeleton.Api.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.HttpResponseObject;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomSelectorDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.Introducer.GetIntroducedListResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.Introducer.Introduced;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.IntroductionAndInvite.GetIntroductionRequestsResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.IntroductionAndInvite.IntroduceActionResponse;
import bd.com.ipay.ipayskeleton.Model.MMModule.Profile.IntroductionAndInvite.IntroductionRequestClass;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IntroducedFragment extends ProgressFragment implements HttpResponseListener {
    ;

    private GetIntroducedListResponse mIntroducedListResponse;
    private HttpRequestGetAsyncTask mGetIntroducedTask = null;

    private HttpRequestGetAsyncTask mGetRecommendationRequestsTask = null;
    private GetIntroductionRequestsResponse mRecommendationRequestsResponse;

    private HttpRequestPostAsyncTask mRecommendActionTask = null;
    private IntroduceActionResponse mIntroduceActionResponse;

    private List<IntroductionRequestClass> mRecommendationRequestList;
    private List<Introduced> mIntroducedList;

    private ProgressDialog mProgressDialog;
    private RecyclerView mRecyclerView;
    private TextView mEmptyListTextView;
    private IntroducdAdapter mIntroduceAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_introduced_requests, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.list_introduced_requests);
        mEmptyListTextView = (TextView) v.findViewById(R.id.empty_list_text);

        mProgressDialog = new ProgressDialog(getActivity());

        if (Utilities.isConnectionAvailable(getActivity())) {
            getIntroducedList();
            getRecommendationRequestsList();
        }

        mIntroduceAdapter = new IntroducdAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mIntroduceAdapter);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentShown(false);
    }

    private void getIntroducedList() {
        if (mGetIntroducedTask != null) {
            return;
        }

        mGetIntroducedTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_INTRODUCED_LIST,
                Constants.BASE_URL_MM + Constants.URL_GET_DOWNSTREAM_APPROVED_INTRODUCTION_REQUESTS, getActivity());
        mGetIntroducedTask.mHttpResponseListener = this;
        mGetIntroducedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getRecommendationRequestsList() {
        if (mGetRecommendationRequestsTask != null) {
            return;
        }

        mGetRecommendationRequestsTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_RECOMMENDATION_REQUESTS,
                Constants.BASE_URL_MM + Constants.URL_GET_DOWNSTREAM_NOT_APPROVED_INTRODUCTION_REQUESTS, getActivity());
        mGetRecommendationRequestsTask.mHttpResponseListener = this;
        mGetRecommendationRequestsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void attemptSetRecommendationStatus(long requestID, String recommendationStatus) {
        if (requestID == 0) {
            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG).show();
            return;
        }

        mProgressDialog.setMessage(getString(R.string.verifying_user));
        mProgressDialog.show();
        mRecommendActionTask = new HttpRequestPostAsyncTask(Constants.COMMAND_INTRODUCE_ACTION,
                Constants.BASE_URL_MM + Constants.URL_INTRODUCE_ACTION + requestID + "/" + recommendationStatus, null, getActivity());
        mRecommendActionTask.mHttpResponseListener = this;
        mRecommendActionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void refreshIntroductionRequestList() {
        if (Utilities.isConnectionAvailable(getActivity())) {
            getRecommendationRequestsList();
        }
    }

    @Override
    public void httpResponseReceiver(HttpResponseObject result) throws RuntimeException {


        mGetRecommendationRequestsTask = null;
        if (result == null || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_INTERNAL_ERROR
                || result.getStatus() == Constants.HTTP_RESPONSE_STATUS_NOT_FOUND) {
            mProgressDialog.dismiss();

            if (getActivity() != null)
                Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_SHORT).show();
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
                        //mBaseList.addAll(mIntroducedList);
                        mIntroduceAdapter.notifyDataSetChanged();

                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), R.string.pending_get_failed, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), R.string.pending_get_failed, Toast.LENGTH_LONG).show();
                }

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
                mGetRecommendationRequestsTask = null;

                break;
            case Constants.COMMAND_INTRODUCE_ACTION:

                try {
                    mIntroduceActionResponse = gson.fromJson(result.getJsonString(), IntroduceActionResponse.class);
                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), mIntroduceActionResponse.getMessage(), Toast.LENGTH_LONG).show();

                        // Refresh recommendation requests list
                        if (mRecommendationRequestList != null)
                            mRecommendationRequestList.clear();
                        mRecommendationRequestList = null;
                        refreshIntroductionRequestList();
                    } else {
                        if (getActivity() != null)
                            Toast.makeText(getActivity(), mIntroduceActionResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), R.string.service_not_available, Toast.LENGTH_LONG).show();
                }

                mProgressDialog.dismiss();
                mRecommendActionTask = null;
                break;
        }

        try {
            if (isAdded())
                setContentShown(true);
            if (mIntroducedList != null && mIntroducedList.size() == 0 && mRecommendationRequestList != null && mRecommendationRequestList.size() == 0)
                mEmptyListTextView.setVisibility(View.VISIBLE);
            else mEmptyListTextView.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private class IntroducdAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int RECOMMENDATION_ITEM_VIEW = 1;
        private static final int INTRODUCED_LIST_ITEM_VIEW = 2;
        private static final int INTRODUCED_LIST_HEADER_VIEW = 3;

        private final int ACTION_VERIFY = 0;
        private final int ACTION_REJECT = 1;
        private final int ACTION_SPAM = 2;

        public IntroducdAdapter() {
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView mIntroducedName;
            private final TextView mIntroducedMobileNumber;
            private final ProfileImageView mIntroducedProfilePictureView;

            private List<String> mReceivedRequestActionList;
            private CustomSelectorDialog mCustomSelectorDialog;

            private final TextView mSenderName;
            private final TextView mSenderMobileNumber;
            private final TextView mDate;

            private final View divider;

            public ViewHolder(final View itemView) {
                super(itemView);

                mIntroducedName = (TextView) itemView.findViewById(R.id.introduced_name);
                mIntroducedMobileNumber = (TextView) itemView.findViewById(R.id.introduced_mobile_number);
                mIntroducedProfilePictureView = (ProfileImageView) itemView.findViewById(R.id.introduced_profile_picture);

                mSenderName = (TextView) itemView.findViewById(R.id.textview_title);
                mSenderMobileNumber = (TextView) itemView.findViewById(R.id.textview_description);
                mDate = (TextView) itemView.findViewById(R.id.textview_time);

                divider = itemView.findViewById(R.id.divider);

            }

            public void bindViewRecommendationList(int pos) {

                final String imageUrl = mRecommendationRequestList.get(pos).getProfilePictureUrl();
                final long requestID = mRecommendationRequestList.get(pos).getId();
                final String senderName = mRecommendationRequestList.get(pos).getSenderName();
                final String senderMobileNumber = mRecommendationRequestList.get(pos).getSenderMobileNumber();
                final String recommendationStatus = mRecommendationRequestList.get(pos).getStatus();
                final String time = new SimpleDateFormat("EEE, MMM d, ''yy, h:mm a").format(mRecommendationRequestList.get(pos).getDate());

                mSenderName.setText(senderName);
                mSenderMobileNumber.setText(senderMobileNumber);
                mDate.setText(time);


                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (recommendationStatus.equalsIgnoreCase(Constants.INTRODUCTION_REQUEST_STATUS_PENDING)) {
                            mReceivedRequestActionList = Arrays.asList(getResources().getStringArray(R.array.introduce_action));
                            mCustomSelectorDialog = new CustomSelectorDialog(getActivity(), senderName, mReceivedRequestActionList);
                            mCustomSelectorDialog.setOnResourceSelectedListener(new CustomSelectorDialog.OnResourceSelectedListener() {
                                @Override
                                public void onResourceSelected(int selectedIndex, String name) {
                                    if (selectedIndex == ACTION_VERIFY) {
                                        new android.app.AlertDialog.Builder(getActivity())
                                                .setTitle(R.string.are_you_sure)
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        attemptSetRecommendationStatus(requestID, Constants.INTRODUCTION_REQUEST_ACTION_APPROVE);
                                                    }
                                                })
                                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // Do nothing
                                                    }
                                                })
                                                .show();

                                    } else if (selectedIndex == ACTION_REJECT) {
                                        new android.app.AlertDialog.Builder(getActivity())
                                                .setTitle(R.string.are_you_sure)
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        attemptSetRecommendationStatus(requestID, Constants.INTRODUCTION_REQUEST_ACTION_REJECT);
                                                    }
                                                })
                                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // Do nothing
                                                    }
                                                })
                                                .show();
                                    } else if (selectedIndex == ACTION_SPAM) {
                                        new android.app.AlertDialog.Builder(getActivity())
                                                .setTitle(R.string.are_you_sure)
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        attemptSetRecommendationStatus(requestID, Constants.INTRODUCTION_REQUEST_ACTION_MARK_AS_SPAM);
                                                    }
                                                })
                                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // Do nothing
                                                    }
                                                })
                                                .show();
                                    }
                                }
                            });
                            mCustomSelectorDialog.show();
                        }
                    }
                });
            }

            public void bindViewForIntroducedList(int pos) {

                if (mRecommendationRequestList != null && mRecommendationRequestList.size() != 0 )  pos = pos - mRecommendationRequestList.size() - 1;

                final String introducedName = mIntroducedList.get(pos).getName();
                final String introducedMobileNumber = mIntroducedList.get(pos).getMobileNumber();
                String imageUrl = mIntroducedList.get(pos).getProfilePictureUrl();
                mIntroducedProfilePictureView.setProfilePicture(Constants.BASE_URL_FTP_SERVER + imageUrl, false);
                mIntroducedName.setText(introducedName);
                mIntroducedMobileNumber.setText(introducedMobileNumber);
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

            }  else if (viewType == RECOMMENDATION_ITEM_VIEW) {
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

                } else if (holder instanceof IntroducedListHeaderViewHolder) {
                    IntroducedListHeaderViewHolder vh = (IntroducedListHeaderViewHolder) holder;

                }else if (holder instanceof RecommendationRequestViewHolder) {
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

            if (mRecommendationRequestList == null && mIntroducedList == null )
                return 0;

            if (mRecommendationRequestList != null)
                recommendationRequestListSize = mRecommendationRequestList.size();
            if(mIntroducedList != null)
                introducedListSize = mIntroducedList.size();

            if (recommendationRequestListSize > 0 && introducedListSize > 0)
                return recommendationRequestListSize + 1 + introducedListSize ;
            else if (introducedListSize > 0 && recommendationRequestListSize == 0)
                return introducedListSize ;
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
            if(mIntroducedList != null)
                introducedListSize = mIntroducedList.size();

            if (recommendationRequestListSize > 0 && introducedListSize > 0) {
                if (position == recommendationRequestListSize )
                    return INTRODUCED_LIST_HEADER_VIEW;
                else if (position > recommendationRequestListSize )
                    return INTRODUCED_LIST_ITEM_VIEW;
                else return RECOMMENDATION_ITEM_VIEW;

            } else if (introducedListSize > 0 && recommendationRequestListSize == 0) {
               return INTRODUCED_LIST_ITEM_VIEW;

            } else if (introducedListSize == 0 && recommendationRequestListSize > 0) {
                return RECOMMENDATION_ITEM_VIEW;
            }
            else return RECOMMENDATION_ITEM_VIEW;
        }
    }

}
