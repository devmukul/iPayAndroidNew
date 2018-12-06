package bd.com.ipay.android.adapter.viewholder.transaction;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import bd.com.ipay.android.adapter.viewholder.OnItemClickListener;
import bd.com.ipay.android.adapter.viewholder.PagedListViewHolder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionHistory;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class TransactionHistoryViewHolder extends PagedListViewHolder<TransactionHistory> {

	private final TextView transactionDescriptionTextView;
	private final TextView transactionTimeTextView;
	private final TextView receiverNameTextView;
	private final TextView balanceTextView;
	private final TextView netAmountTextView;
	private final RoundedImageView transactionImageView;
	private final CircleTransform circleTransform;

	private TransactionHistoryViewHolder(@NonNull View itemView,
	                                     @NonNull final OnItemClickListener onItemClickListener) {
		super(itemView);
		circleTransform = new CircleTransform(itemView.getContext());
		transactionDescriptionTextView = itemView
				.findViewById(R.id.transaction_description_text_view);
		transactionTimeTextView = itemView.findViewById(R.id.transaction_time_text_view);
		receiverNameTextView = itemView.findViewById(R.id.receiver_name_text_view);
		balanceTextView = itemView.findViewById(R.id.balance_text_view);
		netAmountTextView = itemView.findViewById(R.id.net_amount_text_view);
		transactionImageView = itemView.findViewById(R.id.transaction_image_view);

		itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onItemClickListener.onItemClick(getAdapterPosition(), view);
			}
		});
	}

	public static TransactionHistoryViewHolder create(@NonNull ViewGroup parent,
	                                                  OnItemClickListener onItemClickListener) {
		final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
		return new TransactionHistoryViewHolder(
				layoutInflater.inflate(R.layout.list_item_transaction_history_new,
						parent, false), onItemClickListener);
	}

	@Override
	public void bindTo(TransactionHistory transactionHistory) {

		transactionDescriptionTextView.setText(transactionHistory.getShortDescription());
		transactionTimeTextView.setText(Utilities.formatDayMonthYear(transactionHistory.getTime()));
		netAmountTextView.setText(Utilities
				.formatTakaFromString(transactionHistory.getNetAmountFormatted()));

		// showing account balance
		if (transactionHistory.getAccountBalance() != null &&
				transactionHistory.getStatusCode() != Constants.TRANSACTION_STATUS_PROCESSING) {
			balanceTextView.setVisibility(View.VISIBLE);
			balanceTextView.setText(Utilities
					.formatTakaWithComma(transactionHistory.getAccountBalance()));
		} else {
			balanceTextView.setVisibility(View.GONE);
		}

		// showing receiver name
		if (!TextUtils.isEmpty(transactionHistory.getReceiver())) {
			receiverNameTextView.setVisibility(View.VISIBLE);
			if (!TextUtils.isEmpty(transactionHistory.getOutletName())) {
				receiverNameTextView.setText(String.format("%s (%s)",
						transactionHistory.getReceiver(), transactionHistory.getOutletName()));
			} else {
				receiverNameTextView.setText(transactionHistory.getReceiver());
			}
		} else {
			receiverNameTextView.setVisibility(View.VISIBLE);
		}

		// showing time
		if (DateUtils.isToday(transactionHistory.getTime())) {
			transactionTimeTextView.setText(String.format("Today, %s",
					Utilities.formatTimeOnly(transactionHistory.getTime())));
		} else {
			transactionTimeTextView.setText(Utilities
					.formatDayMonthYear(transactionHistory.getTime()));
		}

		// showing status icon
		switch (transactionHistory.getStatusCode()) {
			case Constants.TRANSACTION_STATUS_ACCEPTED:
				netAmountTextView.setPaintFlags(netAmountTextView.getPaintFlags()
						& (~Paint.STRIKE_THRU_TEXT_FLAG));
				transactionDescriptionTextView.setCompoundDrawablesWithIntrinsicBounds(
						0, 0, R.drawable.transaction_tick_sign, 0);

				break;
			case Constants.TRANSACTION_STATUS_PROCESSING:
				netAmountTextView.setPaintFlags(netAmountTextView.getPaintFlags()
						& (~Paint.STRIKE_THRU_TEXT_FLAG));
				transactionDescriptionTextView.setCompoundDrawablesWithIntrinsicBounds(
						0, 0, R.drawable.pending, 0);
				break;
			case Constants.TRANSACTION_STATUS_CANCELLED:
			case Constants.TRANSACTION_STATUS_REJECTED:
			case Constants.TRANSACTION_STATUS_FAILED:
				netAmountTextView.setPaintFlags(netAmountTextView.getPaintFlags()
						| Paint.STRIKE_THRU_TEXT_FLAG);
				transactionDescriptionTextView.setCompoundDrawablesWithIntrinsicBounds(
						0, 0, R.drawable.transaction_cross_sign, 0);
				break;
		}

		// showing transaction image
		if (transactionHistory.getAdditionalInfo().getType()
				.equalsIgnoreCase(Constants.TRANSACTION_TYPE_USER)) {
			Glide.with(itemView.getContext())
					.load(Constants.BASE_URL_FTP_SERVER
							+ transactionHistory.getAdditionalInfo()
							.getUserProfilePic())
					.transform(circleTransform)
					.crossFade()
					.into(transactionImageView);
		} else {
			Glide.with(itemView.getContext())
					.load(transactionHistory.getAdditionalInfo().
							getImageWithType(itemView.getContext()))
					.transform(circleTransform)
					.crossFade()
					.into(transactionImageView);
		}
	}
}