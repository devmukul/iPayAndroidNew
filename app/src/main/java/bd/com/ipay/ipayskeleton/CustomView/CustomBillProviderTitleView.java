package bd.com.ipay.ipayskeleton.CustomView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import bd.com.ipay.ipayskeleton.R;

public class CustomBillProviderTitleView extends LinearLayout {
    private Context context;
    private TextView mTextView;

    public CustomBillProviderTitleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public CustomBillProviderTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CustomBillProviderTitleView(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;

        View v = inflate(context, R.layout.view_bill_provider_title, null);
        mTextView = (TextView) v.findViewById(R.id.titleView);

        addView(v);
    }

    public void setTitleView(String name) {
        mTextView.setText(name);
    }
}
