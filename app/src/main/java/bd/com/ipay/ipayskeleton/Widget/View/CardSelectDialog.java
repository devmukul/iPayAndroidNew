package bd.com.ipay.ipayskeleton.Widget.View;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.CardType;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class CardSelectDialog {
	private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
	private AlertDialog alertDialog;
	private final TextView titleTextView;
	private final Button continueButton;
	private final ImageButton closeButton;
	private RecyclerView cardTypeRecyclerView;
	private CardTypeAdapter mCardTypeAdapter;
	List<CardType> cardTypes;
	private String selectedCard = null;
    private int selectedPos = -1;

	public CardSelectDialog(Context context) {
		numberFormat.setMinimumFractionDigits(2);
		numberFormat.setMaximumFractionDigits(2);

		@SuppressLint("InflateParams") final View customTitleView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_custom_title, null, false);
		@SuppressLint("InflateParams") final View customView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_credit_card_type, null, false);

		closeButton = customTitleView.findViewById(R.id.close_button);
		titleTextView = customTitleView.findViewById(R.id.title_text_view);

		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.cancel();
			}
		});
		continueButton = customView.findViewById(R.id.button_send_money);

        cardTypeRecyclerView = customView.findViewById(R.id.card_type_recycler_view);

		alertDialog = new AlertDialog.Builder(context)
				.setCustomTitle(customTitleView)
				.setView(customView)
				.setCancelable(false)
				.create();

		genarateCardType();
        mCardTypeAdapter = new CardTypeAdapter(context,cardTypes);
        cardTypeRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        cardTypeRecyclerView.setAdapter(mCardTypeAdapter);

	}

	private void genarateCardType() {
	    cardTypes = new ArrayList<>();
		CardType cardType = new CardType(Constants.VISA_CARD, Constants.VISA, R.drawable.visa);
		cardTypes.add(cardType);
		cardType = new CardType(Constants.MASTER_CARD, Constants.MASTERCARD, R.drawable.mastercard);
        cardTypes.add(cardType);
        cardType = new CardType(Constants.AMEX_CARD, Constants.AMEX, R.drawable.amex);
        cardTypes.add(cardType);
	}

	public void setTitle(CharSequence title) {
		titleTextView.setText(title, TextView.BufferType.SPANNABLE);
	}

    public String getSelectedCardType() {
        return selectedCard;
    }

	public void setPayBillButtonAction(final View.OnClickListener onClickListener) {
        continueButton.setOnClickListener(onClickListener);
	}

	public void setCloseButtonAction(final View.OnClickListener onClickListener) {
		closeButton.setOnClickListener(onClickListener);
	}

	public void show() {
		if (!alertDialog.isShowing())
			alertDialog.show();
	}

	public void cancel() {
		if (alertDialog.isShowing())
			alertDialog.cancel();
	}


    public class CardTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<CardType> mCardType;
        Context context;


        public CardTypeAdapter(Context context, List<CardType> mCardType) {
            this.context = context;
            this.mCardType = mCardType;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_card_type, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            try {
                ViewHolder vh = (ViewHolder) holder;
                vh.bindView(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            if (mCardType == null)
                return 0;
            else
                return mCardType.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final View itemView;
            private ProfileImageView cardIcon;
            private TextView mNameTextView;

            public ViewHolder(View itemView) {

                super(itemView);

                this.itemView = itemView;
                cardIcon = (ProfileImageView) itemView.findViewById(R.id.card_icon);
                mNameTextView = (TextView) itemView.findViewById(R.id.card_name);
                cardIcon.setBusinessLogoPlaceHolder();
            }

            public void bindView(final int position) {
                mNameTextView.setText(mCardType.get(position).getCardName());
                cardIcon.setProfilePicture(mCardType.get(position).getCardIconDrawable());
                if(position != selectedPos) {
                    itemView.setBackgroundColor(context.getResources().getColor(R.color.colorTransparent));
                }else{
                    itemView.setBackgroundColor(context.getResources().getColor(R.color.colorSelectorBack));
                }

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedCard = mCardType.get(position).getCardKey();
                        selectedPos = position;
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }
}
