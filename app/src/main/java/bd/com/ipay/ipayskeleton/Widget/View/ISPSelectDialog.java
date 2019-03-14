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
import java.util.List;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.UtilityBillPaymentActivity;
import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney.CardType;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.ISP;
import bd.com.ipay.ipayskeleton.PaymentFragments.UtilityBillFragments.CreditCard.Bank;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.PinChecker;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;

public class ISPSelectDialog {
	private AlertDialog alertDialog;
	private final TextView titleTextView;
	private final ImageButton closeButton;
	private RecyclerView cardTypeRecyclerView;
	private BankListAdapter mBankListAdapter;
	Context context;
    private int selectedBankIconId;
    private String selectedBankCode;

    List<ISP> cardTypes;

	public ISPSelectDialog(Context context) {
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

        genarateCardType();
        mBankListAdapter = new BankListAdapter(context, cardTypes);
        cardTypeRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        cardTypeRecyclerView.setAdapter(mBankListAdapter);

	}

    private void genarateCardType() {
        cardTypes = new ArrayList<>();
        ISP cardType = new ISP(context.getString(R.string.amberIT), Constants.AMBERIT , R.drawable.ic_amber_it);
        cardTypes.add(cardType);
        cardType = new ISP(context.getString(R.string.banglalion), Constants.BLION , R.drawable.banglalion);
        cardTypes.add(cardType);
        cardType = new ISP(context.getString(R.string.brilliant), Constants.BRILLIANT , R.drawable.brilliant_logo);
        cardTypes.add(cardType);
        cardType = new ISP(context.getString(R.string.carnival), Constants.CARNIVAL ,  R.drawable.ic_carnival);
        cardTypes.add(cardType);
        cardType = new ISP(context.getString(R.string.link_three), Constants.LINK3 , R.drawable.link_three_logo);
        cardTypes.add(cardType);
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

        private List<ISP> mBankList;
        Context context;


        public BankListAdapter(Context context, List<ISP> mBankList) {
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
            holder.bankNameTextView.setText(mBankList.get(position).getIspName());
            holder.bankIconImageView.setImageResource(mBankList.get(position).getIspIconDrawable());

            holder.bankIconImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    payBill(mBankList.get(position).getIspCode(), null);
                    alertDialog.cancel();
                }
            });
            holder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    payBill(mBankList.get(position).getIspCode(), null);
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

    private void payBill(final String provider, final String type) {
        if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.UTILITY_BILL_PAYMENT)) {
            DialogUtils.showServiceNotAllowedDialog(context);
            return;
        }
        PinChecker pinChecker = new PinChecker(context, new PinChecker.PinCheckerListener() {
            @Override
            public void ifPinAdded() {
                Intent intent;
                switch (provider) {
                    case Constants.BRILLIANT:
                    case Constants.AMBERIT:
                        intent = new Intent(context, UtilityBillPaymentActivity.class);
                        intent.putExtra(Constants.SERVICE, provider);
                        context.startActivity(intent);
                        break;
                    case Constants.LINK3:
                        intent = new Intent(context, IPayUtilityBillPayActionActivity.class);
                        intent.putExtra(IPayUtilityBillPayActionActivity.BILL_PAY_PARTY_NAME_KEY, IPayUtilityBillPayActionActivity.BILL_PAY_LINK_THREE);
                        context.startActivity(intent);
                        break;
                    case Constants.CARNIVAL:
                        intent = new Intent(context, IPayUtilityBillPayActionActivity.class);
                        intent.putExtra(IPayUtilityBillPayActionActivity.BILL_PAY_PARTY_NAME_KEY, IPayUtilityBillPayActionActivity.BILL_PAY_CARNIVAL);
                        context.startActivity(intent);
                        break;
                    case Constants.BLION:
                        intent = new Intent(context, UtilityBillPaymentActivity.class);
                        intent.putExtra(Constants.SERVICE, Constants.BANGLALION);
                        context.startActivity(intent);
                        break;
                }
            }
        });
        pinChecker.execute();
    }
}
