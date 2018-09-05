package bd.com.ipay.ipayskeleton.HomeFragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.devspark.progressfragment.ProgressFragment;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.WebViewActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.CustomSwipeRefreshLayout;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Offer.OfferResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Offer.Promotion;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class OfferFragment extends ProgressFragment implements HttpResponseListener {

    private HttpRequestGetAsyncTask mPromotionTask = null;
    private RecyclerView mPromotionsRecyclerView;
    private PromotionsAdapter mPromotionsAdapter;
    private LinearLayoutManager mLayoutManager;
    private List<Promotion> mPromotionList = new ArrayList<>();
//    private CustomSwipeRefreshLayout mSwipeRefreshLayout;
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
        getActivity().setTitle(R.string.offer);

        initializeViews(v);
        setupViewsAndActions();

    /*    mSwipeRefreshLayout.setOnRefreshListener(new CustomSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utilities.isConnectionAvailable(getActivity()) && mPromotionTask == null) {
                    refreshPromotions();
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });*/

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPromotions();
    }

    @Override
    public void onPause() {
        super.onPause();
       /* if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.destroyDrawingCache();
            mSwipeRefreshLayout.clearAnimation();
        }*/
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null) {
            if (isVisibleToUser) {
                refreshPromotions();
            }
        }
    }

    private void refreshPromotions() {
        getPromotions();
    }

    private void getPromotions() {
        if (mPromotionTask != null) {
            return;
        }
        //String url = "https://ipay-772e8.firebaseio.com/.json";
        String url = Constants.BASE_URL_MM+"bsp/contents";

        https://www.ipay.com.bd/api/v1/bsp/contents
        mPromotionTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROMOTIONS,
                url, getActivity(), false);
        mPromotionTask.mHttpResponseListener = this;
        mPromotionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadPromotions(List<Promotion> promotions) {
        mPromotionList = new ArrayList<>();
        for (int i = 0; i < promotions.size(); i++) {
            Promotion values = promotions.get(i);
            Calendar calendar = Calendar.getInstance();
            long currentTime = calendar.getTimeInMillis();

            if (currentTime < values.getExpire_date()) {
                mPromotionList.add(values);
            }
        }

        if (mPromotionList != null && mPromotionList.size() > 0) {
            mPromotionsRecyclerView.setVisibility(View.VISIBLE);
            mEmptyListTextView.setVisibility(View.GONE);
            mPromotionsAdapter.notifyDataSetChanged();
            setContentShown(true);
        } else {
            mPromotionsRecyclerView.setVisibility(View.GONE);
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
        mEmptyListTextView = (TextView) v.findViewById(R.id.empty_list_text);
        mPromotionsRecyclerView = (RecyclerView) v.findViewById(R.id.list_transaction_history);
        mPromotionsRecyclerView.setNestedScrollingEnabled(true);
        //mSwipeRefreshLayout = (CustomSwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
    }

    private void setupViewsAndActions() {
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        mPromotionsAdapter = new PromotionsAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mPromotionsRecyclerView.setLayoutManager(mLayoutManager);
        mPromotionsRecyclerView.setAdapter(mPromotionsAdapter);
    }


    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
            mPromotionTask = null;
            setContentShown(true);
            return;
        }

        Gson gson = new Gson();
        if (result.getApiCommand().equals(Constants.COMMAND_GET_PROMOTIONS)) {
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                try {
                    OfferResponse mTransactionHistoryResponse = gson.fromJson(result.getJsonString(), OfferResponse.class);
                    loadPromotions(mTransactionHistoryResponse.getPromotions());
                } catch (Exception e) {
                    e.printStackTrace();
                    mPromotionsRecyclerView.setVisibility(View.GONE);
                    mEmptyListTextView.setVisibility(View.VISIBLE);
                    if (isLoading)
                        isLoading = false;
                }
            } else {
                if (getActivity() != null)
                    Toast.makeText(getActivity(), R.string.promotions_get_failed, Toast.LENGTH_LONG).show();
            }
            //mSwipeRefreshLayout.setRefreshing(false);
            mPromotionTask = null;
            if (this.isAdded()) setContentShown(true);
        }
    }

    private class PromotionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final ImageView mPromoImageView;
            private final ProgressBar progressBar;

            public ViewHolder(final View itemView) {
                super(itemView);
                mPromoImageView = (ImageView) itemView.findViewById(R.id.offer_image);
                progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
            }

            public void bindView(int pos) {
                final Promotion promotionList = mPromotionList.get(pos);
                final String imageUrl = promotionList.getImage_url();
                final String offerUrl = promotionList.getUrl();
                final long expire = promotionList.getExpire_date();
                mPromoImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!TextUtils.isEmpty(offerUrl)) {
                            try {
                                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                                intent.putExtra("url", "https://www.ipay.com.bd/promotions?link=" + offerUrl);
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(getContext(), R.string.no_browser_found_error_message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                Glide.with(getContext())
                        .load(imageUrl)
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String s, Target<GlideDrawable> target, boolean b) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable glideDrawable, String s, Target<GlideDrawable> target, boolean b, boolean b1) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .crossFade()
                        .into(mPromoImageView);
            }


        }


        // Now define the view holder for Normal mPromotionList item
        class NormalViewHolder extends ViewHolder {
            NormalViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new NormalViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_offer, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                NormalViewHolder vh = (NormalViewHolder) holder;
                vh.bindView(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mPromotionList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

    }
}
