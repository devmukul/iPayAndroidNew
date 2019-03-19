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
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.NumberFormat;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;

public class DpsBillDetailsDialog {
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
    private AlertDialog alertDialog;
    private TextView titleTextView;
    private TextView accountNameTextView;
    private TextView accountNumberTextView;
    private TextView branchIDTextView;
    private TextView installmentAmountTextView;
    private TextView accountMaturityDateTextView;

    private TextView billTitleTextView;
    private TextView billSubTitleTextView;
    private final Button payBillButton;
    private final ImageButton closeButton;
    private RoundedImageView billClientLogoImageView;

    private final RequestManager requestManager;
    private final CircleTransform circleTransform;

    public DpsBillDetailsDialog(Context context) {
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);

        @SuppressLint("InflateParams") final View customTitleView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_custom_title, null, false);
        @SuppressLint("InflateParams") final View customView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_dps_customer_info_bill, null, false);

        closeButton = customTitleView.findViewById(R.id.close_button);
        titleTextView = customTitleView.findViewById(R.id.title_text_view);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        accountNameTextView = customView.findViewById(R.id.account_title_enter_text_view);
        accountNumberTextView = customView.findViewById(R.id.account_number_enter_text_view);
        branchIDTextView = customView.findViewById(R.id.branch_id_enter_text_view);
        installmentAmountTextView = customView.findViewById(R.id.installment_amount_enter_text_view);
        accountMaturityDateTextView = customView.findViewById(R.id.account_maturity_date_enter_text_view);
        billClientLogoImageView = customView.findViewById(R.id.bill_client_logo_image_view);
        payBillButton = customView.findViewById(R.id.pay_bill_button);
        billTitleTextView = customView.findViewById(R.id.bill_title_text_view);
        billSubTitleTextView = customView.findViewById(R.id.bill_sub_title_text_view);
        alertDialog = new AlertDialog.Builder(context)
                .setCustomTitle(customTitleView)
                .setView(customView)
                .setCancelable(false)
                .create();

        requestManager = Glide.with(context);
        circleTransform = new CircleTransform(context);
    }

    public void setAccountName(String accountName) {
        this.accountNameTextView.setText(accountName);
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumberTextView.setText(accountNumber);
    }

    public void setMaturityDate(String maturityDate) {
        this.accountMaturityDateTextView.setText(maturityDate);
    }

    public void setBranchID(String branchID) {
        this.branchIDTextView.setText(branchID);
    }

    public void setInstallmentAmount(String amount) {
        this.installmentAmountTextView.setText(amount);
    }

    public void setTitle(CharSequence title) {
        titleTextView.setText(title, TextView.BufferType.SPANNABLE);
    }

    public void setClientLogoImageResource(int imageResource) {
        requestManager.load(imageResource)
                .transform(circleTransform)
                .crossFade()
                .into(billClientLogoImageView);
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
    }

    @SuppressWarnings("unused")
    public void setBillTitleInfo(CharSequence billValue, String rightDrawableImageUrl) {
        billTitleTextView.setText(billValue, TextView.BufferType.SPANNABLE);
    }

    public void setBillSubTitleInfo(CharSequence billValue) {
        billSubTitleTextView.setText(billValue, TextView.BufferType.SPANNABLE);
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
