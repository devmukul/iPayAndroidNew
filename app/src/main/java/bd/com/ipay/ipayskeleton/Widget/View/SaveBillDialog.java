package bd.com.ipay.ipayskeleton.Widget.View;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.CustomView.Dialogs.SelectorDialog;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CircleTransform;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class SaveBillDialog {
	private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
	private AlertDialog alertDialog;
	private final TextView titleTextView;
	private final EditText accountIdTextView;
	private final EditText billTitleTextView;
	private final EditText scheduleDateTextView;
	private final Button saveButton;
	private final ImageButton closeButton;

	private final View scheduleDateView;
	private final CheckBox isScheduleChecked;

	private final RequestManager requestManager;
	private final CircleTransform circleTransform;

    private SelectorDialog locationSelectorDialog;

    private List<String> mLocationList;
    private Context context;

	public SaveBillDialog(Context context) {
		numberFormat.setMinimumFractionDigits(2);
		numberFormat.setMaximumFractionDigits(2);

		@SuppressLint("InflateParams") final View customTitleView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_custom_title, null, false);
		@SuppressLint("InflateParams") final View customView = LayoutInflater.from(context).inflate(R.layout.layout_dialog_save_bill, null, false);

		closeButton = customTitleView.findViewById(R.id.close_button);
		titleTextView = customTitleView.findViewById(R.id.title_text_view);

		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.cancel();
			}
		});
		billTitleTextView = customView.findViewById(R.id.title);
        accountIdTextView = customView.findViewById(R.id.account_id);
        scheduleDateTextView = customView.findViewById(R.id.schedule_date);
        saveButton = customView.findViewById(R.id.save_button);

        scheduleDateView = customView.findViewById(R.id.schedule_date_view);
        isScheduleChecked = customView.findViewById(R.id.is_schedule);

        isScheduleChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if(b){
					scheduleDateView.setVisibility(View.VISIBLE);
				}else {
					scheduleDateView.setVisibility(View.GONE);
				}
			}
		});

		alertDialog = new AlertDialog.Builder(context)
				.setCustomTitle(customTitleView)
				.setView(customView)
				.setCancelable(false)
				.create();


        getDateLIst();
        setDateAdapter(context, mLocationList);

		requestManager = Glide.with(context);
		circleTransform = new CircleTransform(context);
	}

    private void setDateAdapter(Context context, List<String> classList) {
        locationSelectorDialog = new SelectorDialog(context, context.getString(R.string.select_schedule_date), classList);
        locationSelectorDialog.setOnResourceSelectedListener(new SelectorDialog.OnResourceSelectedListener() {
            @Override
            public void onResourceSelected(String name) {
                scheduleDateTextView.setError(null);
                scheduleDateTextView.setText(name);
            }
        });

        scheduleDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationSelectorDialog.show();
            }
        });
    }

	public void setTitle(CharSequence title) {
		titleTextView.setText(title, TextView.BufferType.SPANNABLE);
	}

	public void setBillTitleInfo(CharSequence billValue) {
		billTitleTextView.setText(billValue, TextView.BufferType.SPANNABLE);
	}

    public void setScheduleDateInfo(CharSequence billValue) {
        scheduleDateTextView.setText(billValue, TextView.BufferType.SPANNABLE);
    }

    public void setAccountInfo(CharSequence billValue, CharSequence hints) {
	    accountIdTextView.setHint(hints);
        accountIdTextView.setText(billValue, TextView.BufferType.SPANNABLE);
    }

    public String getBillTitleInfo() {
        return billTitleTextView.getText().toString();
    }

    public String getScheduledateInfo() {
        return scheduleDateTextView.getText().toString();
    }

    public boolean getIsScheduled() {
        return isScheduleChecked.isChecked();
    }

	public void setPayBillButtonAction(final View.OnClickListener onClickListener) {
        saveButton.setOnClickListener(onClickListener);
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

    public void getDateLIst(){
        mLocationList = new ArrayList<>();
        for(int i=0;i<28; i++){
            if(i<9)
                mLocationList.add("0"+(i+1));
            else
                mLocationList.add(""+(i+1));
        }
    }
}
