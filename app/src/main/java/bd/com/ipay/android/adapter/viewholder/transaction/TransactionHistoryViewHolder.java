package bd.com.ipay.android.adapter.viewholder.transaction;

import android.content.Context;
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
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionMetaData;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class TransactionHistoryViewHolder extends PagedListViewHolder<TransactionHistory> {

    private final TextView transactionDescriptionTextView;
    private final TextView transactionTimeTextView;
    private final TextView receiverNameTextView;
    private final TextView balanceTextView;
    private final TextView netAmountTextView;
    private final RoundedImageView transactionImageView;
    private RoundedImageView sponsorOrBeneficiaryImageView;
    private TextView sponsorOrBeneficiaryNameTextView;
    private final CircleTransform circleTransform;
    private Context context;

    private TransactionHistoryViewHolder(@NonNull View itemView,
                                         @NonNull final OnItemClickListener onItemClickListener) {
        super(itemView);
        circleTransform = new CircleTransform(itemView.getContext());
        context = itemView.getContext();
        transactionDescriptionTextView = itemView
                .findViewById(R.id.transaction_description_text_view);
        transactionTimeTextView = itemView.findViewById(R.id.transaction_time_text_view);
        receiverNameTextView = itemView.findViewById(R.id.receiver_name_text_view);
        balanceTextView = itemView.findViewById(R.id.balance_text_view);
        netAmountTextView = itemView.findViewById(R.id.net_amount_text_view);
        transactionImageView = itemView.findViewById(R.id.transaction_image_view);
        sponsorOrBeneficiaryImageView = itemView.findViewById(R.id.sponsor_or_beneficiary_image);
        sponsorOrBeneficiaryNameTextView = itemView.findViewById(R.id.sponsor_or_beneficiary_name);

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

        // showing account balance
        if (transactionHistory.getAccountBalance() != null &&
                transactionHistory.getStatusCode() != Constants.TRANSACTION_STATUS_PROCESSING) {
            balanceTextView.setVisibility(View.VISIBLE);
            balanceTextView.setText(Utilities
                    .formatTakaWithComma(transactionHistory.getAccountBalance()));
            netAmountTextView.setText(Utilities
                    .formatTakaFromString(transactionHistory.getNetAmountFormatted()));
        } else {
            balanceTextView.setVisibility(View.GONE);
            if (transactionHistory.getNetAmount() == 0.0) {
                netAmountTextView.setText(Utilities.formatTakaFromString(Double.toString(transactionHistory.getAmount())));
            } else {
                netAmountTextView.setText(Utilities
                        .formatTakaFromString(String.valueOf(transactionHistory.getNetAmount())));
            }
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

			transactionTimeTextView.setText(itemView.getContext()
					.getString(R.string.today_date,
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
            try {
                Glide.with(itemView.getContext())
                        .load(transactionHistory.getAdditionalInfo().
                                getImageWithType(itemView.getContext()))
                        .transform(circleTransform)
                        .crossFade()
                        .into(transactionImageView);
            } catch (Exception e) {

            }

        }
        //showing source_of_fund_related_information
        TransactionMetaData metaData = transactionHistory.getMetaData();
        if (!ProfileInfoCacheManager.isBusinessAccount()) {
            if (metaData != null) {
                if (metaData.isSponsoredByOther()) {
                    sponsorOrBeneficiaryImageView.setVisibility(View.VISIBLE);
                    sponsorOrBeneficiaryNameTextView.setVisibility(View.VISIBLE);
                    String mobileNumber = ProfileInfoCacheManager.getMobileNumber();
                    if (metaData.getSponsorMobileNumber().equals(ContactEngine.formatMobileNumberBD(
                            ProfileInfoCacheManager.getMobileNumber()))) {

                        sponsorOrBeneficiaryNameTextView.setText(context.getString(R.string.paid_for,metaData.getBeneficiaryName()));

                        if (metaData.getBeneficiaryProfilePictures() != null) {
                            if (metaData.getBeneficiaryProfilePictures().size() != 0) {
                                Glide.with(context)
                                        .load(Constants.BASE_URL_FTP_SERVER + metaData.getBeneficiaryProfilePictures().get(0).getUrl())
                                        .centerCrop()
                                        .error(R.drawable.user_brand_bg)
                                        .into(sponsorOrBeneficiaryImageView);
                                sponsorOrBeneficiaryImageView.setVisibility(View.VISIBLE);
                            } else {
                                Glide.with(context)
                                        .load(R.drawable.user_brand_bg)
                                        .centerCrop()
                                        .into(sponsorOrBeneficiaryImageView);
                            }
                        } else {
                            Glide.with(context)
                                    .load(R.drawable.user_brand_bg)
                                    .centerCrop()
                                    .into(sponsorOrBeneficiaryImageView);
                        }

                    } else {

                        if (metaData.getSponsorProfilePictures() != null) {
                            if (metaData.getSponsorProfilePictures().size() != 0) {
                                Glide.with(context)
                                        .load(Constants.BASE_URL_FTP_SERVER +
                                                metaData.getSponsorProfilePictures().get(0).getUrl())
                                        .centerCrop()
                                        .error(R.drawable.user_brand_bg)
                                        .into(sponsorOrBeneficiaryImageView);
                            }
                        }

                        sponsorOrBeneficiaryNameTextView.setText(context.getString(R.string.paid_by,metaData.getSponsorName()));

                    }

                } else {
                    sponsorOrBeneficiaryNameTextView.setVisibility(View.GONE);
                    sponsorOrBeneficiaryImageView.setVisibility(View.GONE);
                }
            } else {

            }
        } else {
            sponsorOrBeneficiaryNameTextView.setVisibility(View.GONE);
            sponsorOrBeneficiaryImageView.setVisibility(View.GONE);
        }
    }

}