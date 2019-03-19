package bd.com.ipay.ipayskeleton.Widget.View;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.text.NumberFormat;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;

public class CardChargeDialog {
	private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
	private AlertDialog alertDialog;
	private final TextView titleTextView;
	private final TextView totalBillTextView;
	private final Button payBillButton;
	private final ImageButton closeButton;

	private final RequestManager requestManager;
	private final CircleTransform circleTransform;

	public CardChargeDialog(Context context) {
		numberFormat.setMinimumFractionDigits(2);
		numberFormat.setMaximumFractionDigits(2);

		@SuppressLint("InflateParams") final View customTitleView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_custom_title, null, false);
		@SuppressLint("InflateParams") final View customView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_credit_card_charge, null, false);

		closeButton = customTitleView.findViewById(R.id.close_button);
		titleTextView = customTitleView.findViewById(R.id.title_text_view);

		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.cancel();
			}
		});
		totalBillTextView = customView.findViewById(R.id.total_bill_text_view);
		payBillButton = customView.findViewById(R.id.pay_bill_button);

		alertDialog = new AlertDialog.Builder(context)
				.setCustomTitle(customTitleView)
				.setView(customView)
				.setCancelable(false)
				.create();

		requestManager = Glide.with(context);
		circleTransform = new CircleTransform(context);
	}

	public void setTitle(CharSequence title) {
		titleTextView.setText(title, TextView.BufferType.SPANNABLE);
	}

	public void setPayBillButtonAction(final View.OnClickListener onClickListener) {
		payBillButton.setOnClickListener(onClickListener);
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
}
