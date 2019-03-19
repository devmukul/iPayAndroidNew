package bd.com.ipay.ipayskeleton.Widget.View;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
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
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;

public class BillDetailsDialog {
	private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
	private AlertDialog alertDialog;
	private final TextView titleTextView;
	private final TextView totalBillTextView;
	private final View totalBillViewHolder;
	private final TextView totalBillTitleTextView;
	private final TextView billTitleTextView;
	private final ImageView billClientLogoImageView;
	private final ImageView billTitleRightDrawableImageView;
	private final View minimumBillViewHolder;
	private final TextView minimumBillTitleTextView;
	private final TextView minimumBillTextView;
	private final TextView billSubTitleTextView;
	private final Button payBillButton;
	private final ImageButton closeButton;

	private final RequestManager requestManager;
	private final CircleTransform circleTransform;

	public BillDetailsDialog(Context context) {
		numberFormat.setMinimumFractionDigits(2);
		numberFormat.setMaximumFractionDigits(2);

		@SuppressLint("InflateParams") final View customTitleView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_custom_title, null, false);
		@SuppressLint("InflateParams") final View customView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_card_customer_info_bill, null, false);

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
		billTitleRightDrawableImageView = customView.findViewById(R.id.bill_title_right_drawable_image_view);

		totalBillViewHolder = customView.findViewById(R.id.total_bill_view_holder);
		totalBillTitleTextView = customView.findViewById(R.id.total_bill_title_text_view);
		totalBillTextView = customView.findViewById(R.id.total_bill_text_view);

		minimumBillViewHolder = customView.findViewById(R.id.minimum_bill_view_holder);
		minimumBillTitleTextView = customView.findViewById(R.id.minimum_bill_title_text_view);
		minimumBillTextView = customView.findViewById(R.id.minimum_bill_text_view);

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

	public void setBillTitleInfo(CharSequence billValue) {
		setBillTitleInfo(billValue, -1);
	}

	@SuppressWarnings("WeakerAccess")
	public void setBillTitleInfo(CharSequence billValue, int rightDrawableResId) {
		billTitleTextView.setText(billValue, TextView.BufferType.SPANNABLE);
		if (rightDrawableResId != -1) {
			requestManager.load(rightDrawableResId)
					.crossFade()
					.into(billTitleRightDrawableImageView);
		}
	}

	@SuppressWarnings("unused")
	public void setBillTitleInfo(CharSequence billValue, String rightDrawableImageUrl) {
		billTitleTextView.setText(billValue, TextView.BufferType.SPANNABLE);
		if (!TextUtils.isEmpty(rightDrawableImageUrl)) {
			requestManager.load(rightDrawableImageUrl)
					.crossFade()
					.into(billTitleRightDrawableImageView);
		}
	}

	public void setBillSubTitleInfo(CharSequence billValue) {
		billSubTitleTextView.setText(billValue, TextView.BufferType.SPANNABLE);
	}

	public void setTotalBillInfo(CharSequence title, double billValue) {
		totalBillViewHolder.setVisibility(View.VISIBLE);
		totalBillTitleTextView.setText(title);
		totalBillTextView.setText(String.format("Tk. %s", numberFormat.format(new BigDecimal(billValue))));
	}

	public void setMinimumBillInfo(CharSequence title, double billValue) {
		minimumBillViewHolder.setVisibility(View.VISIBLE);
		minimumBillTitleTextView.setText(title);
		minimumBillTextView.setText(String.format("Tk. %s", numberFormat.format(new BigDecimal(billValue))));
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
