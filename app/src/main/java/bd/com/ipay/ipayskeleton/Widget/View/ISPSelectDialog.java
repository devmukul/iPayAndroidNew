package bd.com.ipay.ipayskeleton.Widget.View;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.CreditCard.Bank;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class ISPSelectDialog {
	private AlertDialog alertDialog;
	private final TextView titleTextView;
	private final ImageButton closeButton;
	private RecyclerView cardTypeRecyclerView;
	private BankListAdapter mBankListAdapter;
	Context context;
    private int selectedBankIconId;
    private String selectedBankCode;

	public ISPSelectDialog(Context context, ArrayList<Bank> mBankList) {
	    this.context = context;

		@SuppressLint("InflateParams") final View customTitleView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_custom_title, null, false);
		@SuppressLint("InflateParams") final View customView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_credit_card, null, false);

		closeButton = customTitleView.findViewById(R.id.close_button);
		titleTextView = customTitleView.findViewById(R.id.title_text_view);

		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.cancel();
			}
		});

        cardTypeRecyclerView = customView.findViewById(R.id.card_type_recycler_view);

		alertDialog = new AlertDialog.Builder(context)
				.setCustomTitle(customTitleView)
				.setView(customView)
				.setCancelable(false)
				.create();

        mBankListAdapter = new BankListAdapter(context, mBankList);
        cardTypeRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        cardTypeRecyclerView.setAdapter(mBankListAdapter);

	}

	public void setTitle(CharSequence title) {
		titleTextView.setText(title, TextView.BufferType.SPANNABLE);
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


    public class BankListAdapter extends RecyclerView.Adapter<BankListAdapter.BankViewHolder> {

        private ArrayList<Bank> mBankList;
        Context context;


        public BankListAdapter(Context context, ArrayList<Bank> mBankList) {
            this.context = context;
            this.mBankList = mBankList;
        }


        @NonNull
        @Override
        public BankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_bank_item, null, false);
            return new BankListAdapter.BankViewHolder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull final BankViewHolder holder, final int position) {
            holder.bankNameTextView.setText(mBankList.get(position).getBankName());

            if(mBankList.get(position).getBankName().equalsIgnoreCase("Lanka Bangla")){
                holder.bankIconImageView.setImageResource(R.drawable.ic_lankabd2);
            }else {
                holder.bankIconImageView.setImageResource(getBankIcon(mBankList.get(position)));
            }

            holder.bankIconImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mBankList.get(position).getBankName().equalsIgnoreCase("Lanka Bangla")){
                        Intent intent = new Intent(context, IPayUtilityBillPayActionActivity.class);
                        intent.putExtra(IPayUtilityBillPayActionActivity.BILL_PAY_PARTY_NAME_KEY, IPayUtilityBillPayActionActivity.BILL_PAY_LANKABANGLA_CARD);
                        context.startActivity(intent);
                    }else {
                        selectedBankIconId = getBankIcon(mBankList.get(position));
                        selectedBankCode = mBankList.get(position).getBankCode();
                        Bundle bundle = new Bundle();
                        bundle.putString(IPayUtilityBillPayActionActivity.BANK_CODE, selectedBankCode);
                        bundle.putInt(IPayUtilityBillPayActionActivity.BANK_ICON, selectedBankIconId);
                        Intent intent = new Intent(context, IPayUtilityBillPayActionActivity.class);
                        intent.putExtra(Constants.FROM_DASHBOARD, true);
                        intent.putExtra(Constants.BUNDLE, bundle);
                        context.startActivity(intent);
                    }
                    alertDialog.cancel();
                }
            });
            holder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mBankList.get(position).getBankName().equalsIgnoreCase("Lanka Bangla")){
                        Intent intent = new Intent(context, IPayUtilityBillPayActionActivity.class);
                        intent.putExtra(IPayUtilityBillPayActionActivity.BILL_PAY_PARTY_NAME_KEY, IPayUtilityBillPayActionActivity.BILL_PAY_LANKABANGLA_CARD);
                        context.startActivity(intent);
                    }else {
                        selectedBankIconId = getBankIcon(mBankList.get(position));
                        selectedBankCode = mBankList.get(position).getBankCode();
                        Bundle bundle = new Bundle();
                        bundle.putString(IPayUtilityBillPayActionActivity.BANK_CODE, selectedBankCode);
                        bundle.putInt(IPayUtilityBillPayActionActivity.BANK_ICON, selectedBankIconId);
                        Intent intent = new Intent(context, IPayUtilityBillPayActionActivity.class);
                        intent.putExtra(Constants.FROM_DASHBOARD, true);
                        intent.putExtra(Constants.BUNDLE, bundle);
                        context.startActivity(intent);
                    }
                    alertDialog.cancel();
                }
            });

        }

        @Override
        public int getItemCount() {
            return mBankList.size();
        }

        public class BankViewHolder extends RecyclerView.ViewHolder {
            public TextView bankNameTextView;
            private ImageView bankIconImageView;
            private View parentView;


            public BankViewHolder(View itemView) {
                super(itemView);
                bankIconImageView = itemView.findViewById(R.id.bank_icon);
                bankNameTextView = itemView.findViewById(R.id.bank_name);
                parentView = itemView;
            }
        }
    }

    public int getBankIcon(Bank bank) {
        Resources resources = context.getResources();
        int resourceId;
        if (bank.getBankCode() != null)
            resourceId = resources.getIdentifier("ic_bank" + bank.getBankCode(), "drawable",
                    context.getPackageName());
        else
            resourceId = resources.getIdentifier("ic_bank" + "111", "drawable",
                    context.getPackageName());
        return resourceId;
    }
}
