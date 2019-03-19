package bd.com.ipay.ipayskeleton.HomeFragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.devspark.progressfragment.ProgressFragment;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.WebViewActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.NewsList.GetNewsRoomFeedResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.NewsList.NewsList;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class NewsRoomFragment extends ProgressFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mNewsRoomTask = null;
    private RecyclerView mNewsRoomRecyclerView;
    private NestedScrollView mNewsRoomView;
    private NewsRoomAdapter mNewsRoomAdapter;
    private List<NewsList> mNewsRoomList = new ArrayList<>();
    private TextView mEmptyListTextView;
    private boolean isLoading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_offer, container, false);
        if (getActivity() != null)
            getActivity().setTitle(R.string.news_room);

        initializeViews(v);
        setupViewsAndActions();
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNewsRoomList();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null && isVisibleToUser) {
            refreshNewsRoomList();
        }
    }

    private void refreshNewsRoomList() {
        getNewsRoomList();
    }

    private void getNewsRoomList() {
        if (mNewsRoomTask != null) {
            return;
        }
        String url = Constants.BASE_URL_CMS + Constants.URL_NEWSROOM;
        mNewsRoomTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_NEWS,
                url, getActivity(), false);
        mNewsRoomTask.mHttpResponseListener = this;
        mNewsRoomTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadNewsRoomList(List<NewsList> newsRooms) {
        mNewsRoomList = new ArrayList<>();
        mNewsRoomList = newsRooms;

        if (mNewsRoomList != null && mNewsRoomList.size() > 0) {
            mNewsRoomView.setVisibility(View.VISIBLE);
            mEmptyListTextView.setVisibility(View.GONE);
            mNewsRoomAdapter.notifyDataSetChanged();
            setContentShown(true);
        } else {
            mNewsRoomView.setVisibility(View.INVISIBLE);
            mEmptyListTextView.setVisibility(View.VISIBLE);
        }

        if (isLoading)
            isLoading = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initializeViews(View v) {
        mEmptyListTextView = v.findViewById(R.id.empty);
        mNewsRoomRecyclerView = v.findViewById(R.id.list_transaction_history);
        mNewsRoomView = v.findViewById(R.id.parent_view);
    }

    private void setupViewsAndActions() {
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        mNewsRoomAdapter = new NewsRoomAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mNewsRoomRecyclerView.setLayoutManager(mLayoutManager);
        mNewsRoomRecyclerView.setAdapter(mNewsRoomAdapter);
        ViewCompat.setNestedScrollingEnabled(mNewsRoomRecyclerView, false);
    }


    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            mNewsRoomTask = null;
            setContentShown(true);
            mNewsRoomView.setVisibility(View.INVISIBLE);
            mEmptyListTextView.setVisibility(View.VISIBLE);
            return;
        }

        Gson gson = new Gson();
        if (result.getApiCommand().equals(Constants.COMMAND_GET_NEWS)) {
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                try {
                    GetNewsRoomFeedResponse mTransactionHistoryResponse = gson.fromJson(result.getJsonString(), GetNewsRoomFeedResponse.class);
                    loadNewsRoomList(mTransactionHistoryResponse.getNewsList());
                } catch (Exception e) {
                    e.printStackTrace();
                    mNewsRoomView.setVisibility(View.INVISIBLE);
                    mEmptyListTextView.setVisibility(View.VISIBLE);
                    if (isLoading)
                        isLoading = false;
                }
            } else {
                mNewsRoomView.setVisibility(View.INVISIBLE);
                mEmptyListTextView.setVisibility(View.VISIBLE);
                if (getActivity() != null) {
                    Log.d("News Room", "Could not fetch news");
                }
            }
            mNewsRoomTask = null;
            if (this.isAdded()) setContentShown(true);
        }
    }

    private class NewsRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TEMPLATE_1_VIEW = 1;
        private static final int TEMPLATE_2_VIEW = 2;
        private static final int TEMPLATE_3_VIEW = 3;
        private static final int TEMPLATE_4_VIEW = 4;


        public class TitleViewHolder extends RecyclerView.ViewHolder {

            private final TextView mNewsDescriptionView;
            private final TextView mTitleView;
            private final TextView mSubTitleView;
            private final Button mMoreView;
            private ImageView mNewsImageView;

            public TitleViewHolder(View itemView) {
                super(itemView);
                mNewsDescriptionView = itemView.findViewById(R.id.details_text_view);
                mTitleView = itemView.findViewById(R.id.title_text_view);
                mSubTitleView = itemView.findViewById(R.id.subtitle_text_view);
                mMoreView = itemView.findViewById(R.id.tap_to_see_more);
                mNewsImageView = itemView.findViewById(R.id.news_image_view);
            }

            public void bindView(int pos) {
                final NewsList newsList = mNewsRoomList.get(pos);
                final String title = newsList.getTitle();
                final String description = newsList.getBodyContent();
                final String subtitle = newsList.getSubTitle();
                final String imgUrl = newsList.getImageUrl();
                final String buttonText = newsList.getUrlPlaceholder();
                final String detailsURL = newsList.getUrlExtension();

                if (!TextUtils.isEmpty(title)) {
                    mTitleView.setText(title);
                }

                if (!TextUtils.isEmpty(description)) {
                    mNewsDescriptionView.setText(description);
                }

                if (!TextUtils.isEmpty(subtitle)) {
                    mSubTitleView.setText(subtitle);
                }

                if (!TextUtils.isEmpty(imgUrl)) {
                    Glide.with(itemView.getContext()).load(Constants.BASE_URL_FTP_SERVER + imgUrl).into(mNewsImageView);
                }

                if (!TextUtils.isEmpty(detailsURL)) {
                    mMoreView.setVisibility(View.VISIBLE);
                    mMoreView.setText(buttonText);
                    mMoreView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                                intent.putExtra("url", detailsURL);
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(getContext(), R.string.no_browser_found_error_message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    mMoreView.setVisibility(View.GONE);
                }


            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TEMPLATE_2_VIEW:
                    return new TitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_newsroom_template_2, parent, false));
                case TEMPLATE_3_VIEW:
                    return new TitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_newsroom_template_3, parent, false));
                case TEMPLATE_4_VIEW:
                    return new TitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_newsroom_template_4, parent, false));
                default:
                    return new TitleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_newsroom_template_1, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                TitleViewHolder vh = (TitleViewHolder) holder;
                vh.bindView(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public int getItemCount() {
            return mNewsRoomList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mNewsRoomList.get(position).getTemplateId();
        }

    }
}
