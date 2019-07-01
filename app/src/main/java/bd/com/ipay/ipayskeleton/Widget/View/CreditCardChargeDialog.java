package bd.com.ipay.ipayskeleton.Widget.View;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CardNumberValidator;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;

public class CreditCardChargeDialog {
	private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
	private AlertDialog alertDialog;
	private final TextView titleTextView;
	private final TextView billTitleTextView;
	private final ImageView billClientLogoImageView;
	private final TextView billSubTitleTextView;

	private final TextView requestAmmountTextView;
	private final TextView chargeTextView;
	private final TextView totalTextView;

	private final Button payBillButton;
	private final ImageButton closeButton;

	private final RequestManager requestManager;
	private final CircleTransform circleTransform;
	Context context;

	public CreditCardChargeDialog(Context context) {
		this.context = context;
		numberFormat.setMinimumFractionDigits(2);
		numberFormat.setMaximumFractionDigits(2);

		@SuppressLint("InflateParams") final View customTitleView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_custom_title, null, false);
		@SuppressLint("InflateParams") final View customView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_credit_card_charge_details, null, false);

		closeButton = customTitleView.findViewById(R.id.close_button);
		titleTextView = customTitleView.findViewById(R.id.title_text_view);

		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.cancel();
			}
		});

		billTitleTextView = customView.findViewById(R.id.bill_title_text_view);
		billSubTitleTextView = customView.findViewById(R.id.bill_sub_title_text_view);
		billClientLogoImageView = customView.findViewById(R.id.bill_client_logo_image_view);

		requestAmmountTextView = customView.findViewById(R.id.request_amount_text_view);
		chargeTextView = customView.findViewById(R.id.charge_text_view);
		totalTextView = customView.findViewById(R.id.total_text_view);

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

	public void setClientLogoImageResource(int imageResource) {
		billClientLogoImageView.setImageResource(imageResource);
	}

	@SuppressWarnings("unused")
	public void setClientLogoImage(String imageUrl) {

		requestManager.load(imageUrl)
				.transform(circleTransform)
				.crossFade()
				.into(billClientLogoImageView);
	}

	public void setBillTitleInfo(String billValue) {
		billTitleTextView.setText(CardNumberValidator.deSanitizeEntry(billValue, ' '));
	}

	public void setBillSubTitleInfo(CharSequence billValue) {
		billSubTitleTextView.setText(billValue, TextView.BufferType.SPANNABLE);
	}

	public void setTotalInfo(double billValue) {
		totalTextView.setText(String.format("Tk. %s", numberFormat.format(new BigDecimal(billValue))));
	}

	public void setRequestAmountInfo(double billValue) {
		requestAmmountTextView.setText(String.format("Tk. %s", numberFormat.format(new BigDecimal(billValue))));
	}

	public void setChargeInfo(double billValue) {
		chargeTextView.setText(String.format("Tk. %s", numberFormat.format(new BigDecimal(billValue))));
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
