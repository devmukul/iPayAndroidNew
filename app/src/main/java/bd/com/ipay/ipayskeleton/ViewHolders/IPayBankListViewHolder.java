package bd.com.ipay.ipayskeleton.ViewHolders;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import bd.com.ipay.ipayskeleton.Adapters.OnItemClickListener;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.Bank;
import bd.com.ipay.ipayskeleton.R;

public class IPayBankListViewHolder extends IPayViewHolder<Bank> {

	private final ImageView bankIconImageView;
	private final TextView bankNameTextView;

	private IPayBankListViewHolder(final View itemView, final OnItemClickListener onItemClickListener) {
		super(itemView);
		bankIconImageView = itemView.findViewById(R.id.bank_icon_image_view);
		bankNameTextView = itemView.findViewById(R.id.bank_name_text_view);
		itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onItemClickListener != null) {
					onItemClickListener.onItemClick(getAdapterPosition(), itemView);
				}
			}
		});
	}

	public static IPayBankListViewHolder create(View view, OnItemClickListener onItemClickListener) {
		return new IPayBankListViewHolder(view, onItemClickListener);
	}

	@Override
	public void bindTo(Bank bankAccountList) {
		Drawable icon = itemView.getContext().getResources().getDrawable(bankAccountList.getBankIcon(itemView.getContext()));
		bankIconImageView.setImageDrawable(icon);
		bankNameTextView.setText(bankAccountList.getName());
	}
}
