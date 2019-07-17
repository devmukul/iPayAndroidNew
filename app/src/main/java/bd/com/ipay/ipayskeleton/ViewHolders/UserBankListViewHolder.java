package bd.com.ipay.ipayskeleton.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import bd.com.ipay.ipayskeleton.Adapters.OnItemClickListener;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.R;

public class UserBankListViewHolder extends IPayViewHolder<BankAccountList> {

	private final ImageView bankIconImageView;
	private final TextView bankNameTextView;
	private final TextView bankBranchNameTextView;
	private final TextView bankAccountIdTextView;

	private UserBankListViewHolder(final View itemView, final OnItemClickListener onItemClickListener) {
		super(itemView);
		bankIconImageView = itemView.findViewById(R.id.bank_icon_image_view);
		bankNameTextView = itemView.findViewById(R.id.bank_name_text_view);
		bankAccountIdTextView = itemView.findViewById(R.id.bank_account_id_text_view);
		bankBranchNameTextView = itemView.findViewById(R.id.bank_branch_name_text_view);

		itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onItemClickListener != null) {
					onItemClickListener.onItemClick(getAdapterPosition(), itemView);
				}
			}
		});
	}

	public static UserBankListViewHolder create(View view, OnItemClickListener onItemClickListener) {
		return new UserBankListViewHolder(view, onItemClickListener);
	}

	@Override
	public void bindTo(BankAccountList bankAccountList) {
		bankIconImageView.setImageResource(bankAccountList.getBankIcon(itemView.getContext()));
		bankNameTextView.setText(bankAccountList.getBankName());
		bankBranchNameTextView.setText(bankAccountList.getBranchName());
		bankAccountIdTextView.setText(bankAccountList.getAccountNumber());
	}
}
