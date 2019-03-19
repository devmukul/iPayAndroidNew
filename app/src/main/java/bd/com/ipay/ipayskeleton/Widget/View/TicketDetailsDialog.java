package bd.com.ipay.ipayskeleton.Widget.View;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.R;

public class TicketDetailsDialog {
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
    private AlertDialog alertDialog;
    private TextView titleTextView;

    private TextView dateTextView;
    private TextView tarinNameTextView;
    private TextView classNameTextView;
    private TextView adultChildTextView;
    private TextView fareAmountTextView;
    private TextView vatAmountTextView;
    private TextView netAmountTextView;
    private final Button buyTicketButton;
    private final ImageButton closeButton;

    public TicketDetailsDialog(Context context) {
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);

        @SuppressLint("InflateParams") final View customTitleView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_custom_title, null, false);
        @SuppressLint("InflateParams") final View customView = LayoutInflater.from(context).inflate(R.layout.layout_ticket_info_bill, null, false);

        closeButton = customTitleView.findViewById(R.id.close_button);
        titleTextView = customTitleView.findViewById(R.id.title_text_view);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        dateTextView = customView.findViewById(R.id.date);
        tarinNameTextView = customView.findViewById(R.id.train_name);
        classNameTextView = customView.findViewById(R.id.ticket_class);
        adultChildTextView = customView.findViewById(R.id.adult_child);
        fareAmountTextView = customView.findViewById(R.id.fare_enter_text_view);
        vatAmountTextView = customView.findViewById(R.id.vat_enter_text_view);
        buyTicketButton = customView.findViewById(R.id.buy_ticket_button);
        netAmountTextView = customView.findViewById(R.id.net_amount_enter_text_view);
        alertDialog = new AlertDialog.Builder(context)
                .setCustomTitle(customTitleView)
                .setView(customView)
                .setCancelable(false)
                .create();
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setDate(String date) {
        dateTextView.setText(date);
    }

    public void setTrainName(CharSequence trainName) {
        tarinNameTextView.setText(trainName);
    }

    public void setAdultChild(CharSequence trainName) {
        adultChildTextView.setText(trainName);
    }

    public void setClassName(CharSequence className) {
        classNameTextView.setText(className);
    }

    public void setFareAmount(double fareAmount) {
        fareAmountTextView.setText(String.format("Tk. %s", numberFormat.format(BigDecimal.valueOf(fareAmount))));
    }

    public void setVatAmount(double vatAmount) {
        vatAmountTextView.setText(String.format("Tk. %s", numberFormat.format(BigDecimal.valueOf(vatAmount))));
    }

    public void setNetAmount(double netAmount) {
        netAmountTextView.setText(String.format("Tk. %s", numberFormat.format(BigDecimal.valueOf(netAmount))));
    }

    public void setBuyTicketButtonAction(final View.OnClickListener onClickListener) {
        buyTicketButton.setOnClickListener(onClickListener);
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
