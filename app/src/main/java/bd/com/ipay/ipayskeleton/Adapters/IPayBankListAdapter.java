package bd.com.ipay.ipayskeleton.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank.BankAccountList;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.Bank;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.ViewHolders.IPayBankListViewHolder;
import bd.com.ipay.ipayskeleton.ViewHolders.UserBankListViewHolder;

public class IPayBankListAdapter extends ListAdapter<Bank, IPayBankListViewHolder> implements Filterable {

	private final OnItemClickListener onItemClickListener;
	private final LayoutInflater layoutInflater;

	private static final DiffUtil.ItemCallback<Bank> BANK_ACCOUNT_LIST_DIFF = new DiffUtil.ItemCallback<Bank>() {
		@Override
		public boolean areItemsTheSame(Bank oldItem, Bank newItem) {
			return oldItem.getBankCode().equals(newItem.getBankCode());
		}

		@Override
		public boolean areContentsTheSame(Bank oldItem, Bank newItem) {
			return oldItem.getBankCode().equals(newItem.getBankCode());
		}
	};

	public IPayBankListAdapter(@NonNull final Context context, @Nullable final OnItemClickListener onItemClickListener) {
		super(BANK_ACCOUNT_LIST_DIFF);
		this.onItemClickListener = onItemClickListener;
		this.layoutInflater = LayoutInflater.from(context);
	}

	@NonNull
	@Override
	public IPayBankListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return IPayBankListViewHolder.create(layoutInflater.inflate(R.layout.list_item_bank_list, parent, false), onItemClickListener);
	}

	@Override
	public void onBindViewHolder(@NonNull IPayBankListViewHolder holder, int position) {
		final Bank bankAccountList = getItem(position);
		holder.bindTo(bankAccountList);
	}

	@Override
	public Filter getFilter() {
		return null;
	}
}
