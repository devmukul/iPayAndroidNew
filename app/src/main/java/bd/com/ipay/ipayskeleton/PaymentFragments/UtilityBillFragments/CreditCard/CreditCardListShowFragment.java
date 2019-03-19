package bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.CreditCard;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.GetSavedCardsList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill.MyCard;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.CreditCardAmountInputFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class CreditCardListShowFragment extends Fragment implements HttpResponseListener {
    private RecyclerView mCardListRecyclerView;
    private ArrayList<MyCard> cardList;
    private View progressView;
    private TextView progressTextView;
    private TextView noCardTextView;
    private Button newBillButton;
    private TextView savedCardTextView;

    private HttpRequestGetAsyncTask mGetSavedCardsAsyncTask;
    private CardListAdapter cardListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_show_my_card_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCardListRecyclerView = view.findViewById(R.id.card_list);
        progressView = view.findViewById(R.id.progress_layout);
        progressView.setVisibility(View.VISIBLE);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((IPayUtilityBillPayActionActivity) getActivity()).setSupportActionBar(toolbar);
        getActivity().setTitle(R.string.credit_card_bill_title);
        getActivity().setTitleColor(R.color.colorWhite);
        ((IPayUtilityBillPayActionActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressTextView = progressView.findViewById(R.id.progress_text_view);
        progressTextView.setText(getString(R.string.fetching_cards));
        noCardTextView = view.findViewById(R.id.no_card_text_view);
        savedCardTextView = view.findViewById(R.id.saved_card);
        newBillButton = view.findViewById(R.id.new_bill_button);
        newBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((IPayUtilityBillPayActionActivity) getActivity()).switchFragment(new CreditCardBankSelectionFragment(),
                        null, 1, true);
            }
        });
        attemptGetSavedCards();
        cardListAdapter = new CardListAdapter();
        mCardListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mCardListRecyclerView.setAdapter(cardListAdapter);
    }

    private void attemptGetSavedCards() {
        if (mGetSavedCardsAsyncTask != null) {
            return;
        } else {
            String uri = Constants.BASE_URL_SM + Constants.URL_GET_SAVED_CARDS;
            mGetSavedCardsAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SAVED_CARDS,
                    uri, getContext(), this, false);
            mGetSavedCardsAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    public int getBankIcon(String bankCode) {
        Resources resources = getContext().getResources();
        int resourceId;
        if (bankCode != null)
            resourceId = resources.getIdentifier("ic_bank" + bankCode, "drawable",
                    getContext().getPackageName());
        else
            resourceId = resources.getIdentifier("ic_bank" + "111", "drawable",
                    getContext().getPackageName());
        return resourceId;

    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        try {
            mGetSavedCardsAsyncTask = null;
            if (HttpErrorHandler.isErrorFound(result, getContext(), null)) {
                progressView.setVisibility(View.GONE);
            } else {
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    String jsonString = result.getJsonString();
                    GetSavedCardsList getSavedCardsList = new Gson().fromJson(jsonString, GetSavedCardsList.class);
                    cardList = getSavedCardsList.getCardList();
                    cardListAdapter.notifyDataSetChanged();
                    if (cardList.size() == 0) {
                        progressView.setVisibility(View.GONE);
                        mCardListRecyclerView.setVisibility(View.GONE);
                        noCardTextView.setVisibility(View.VISIBLE);
                        savedCardTextView.setVisibility(View.GONE);
                    } else {
                        progressView.setVisibility(View.GONE);
                        mCardListRecyclerView.setVisibility(View.VISIBLE);
                        noCardTextView.setVisibility(View.GONE);
                        savedCardTextView.setVisibility(View.VISIBLE);
                    }

                } else {
                    progressView.setVisibility(View.GONE);
                    savedCardTextView.setVisibility(View.GONE);
                    Toast.makeText(getContext(), getString(R.string.cant_fetch_details), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), getString(R.string.service_not_available), Toast.LENGTH_LONG).show();
        }

    }

    class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardListViewHolder> {
        @NonNull
        @Override
        public CardListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_my_cards, parent, false);
            return new CardListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CardListViewHolder holder, final int position) {
            holder.cardNumberTextView.setText(cardList.get(position).getCardNumber());
            holder.cardHolderNameTextView.setText(cardList.get(position).getCardHolderName());
            holder.bankIconView.setImageResource(getBankIcon(cardList.get(position).getBankCode()));
            holder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString(IPayUtilityBillPayActionActivity.CARD_NUMBER_KEY,
                            cardList.get(position).getCardNumber());
                    bundle.putString(IPayUtilityBillPayActionActivity.CARD_USER_NAME_KEY,
                            cardList.get(position).getCardHolderName());
                    bundle.putString(IPayUtilityBillPayActionActivity.BANK_CODE, cardList.get(position).getBankCode());
                    bundle.putBoolean(IPayUtilityBillPayActionActivity.SAVE_CARD_INFO, false);
                    bundle.putInt(IPayUtilityBillPayActionActivity.BANK_ICON, getBankIcon(cardList.get(position).getBankCode()));
                    ((IPayUtilityBillPayActionActivity) getActivity()).
                            switchFragment(new CreditCardAmountInputFragment(), bundle, 2, true);
                }
            });
        }

        @NonNull
        @Override
        public int getItemCount() {
            if (cardList != null) {
                return cardList.size();
            } else {
                return 0;
            }
        }

        class CardListViewHolder extends RecyclerView.ViewHolder {
            private RoundedImageView bankIconView;
            private TextView cardNumberTextView;
            private TextView cardHolderNameTextView;
            private View parentView;

            public CardListViewHolder(View itemView) {
                super(itemView);
                cardHolderNameTextView = itemView.findViewById(R.id.card_holder_name);
                cardNumberTextView = itemView.findViewById(R.id.card_number);
                bankIconView = itemView.findViewById(R.id.bank_icon);
                parentView = itemView;
            }
        }
    }
}
