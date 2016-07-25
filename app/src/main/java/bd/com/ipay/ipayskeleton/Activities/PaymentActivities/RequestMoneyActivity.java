package bd.com.ipay.ipayskeleton.Activities.PaymentActivities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.MenuItem;
import android.view.View;

import bd.com.ipay.ipayskeleton.Activities.BaseActivity;
import bd.com.ipay.ipayskeleton.PaymentFragments.RequestMoneyFragments.MoneyRequestListHolderFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.PaymentFragments.RequestMoneyFragments.RequestMoneyFragment;

public class RequestMoneyActivity extends BaseActivity {

    private FloatingActionButton mFabRequestMoney;
    private boolean switchedToPendingList = true;

    /**
     * If this value is set in the intent extras,
     * you would be taken directly to the new request page
     */
    public static final String LAUNCH_NEW_REQUEST = "LAUNCH_NEW_REQUEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_money);
        mFabRequestMoney = (FloatingActionButton) findViewById(R.id.fab_request_money);

        mFabRequestMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToRequestMoneyFragment();
            }
        });

        if (getIntent().getBooleanExtra(LAUNCH_NEW_REQUEST, false))
            switchToRequestMoneyFragment();
        else
            switchToRequestListFragment();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra(LAUNCH_NEW_REQUEST, false)) {
            finish();
        } else if (switchedToPendingList) {
            super.onBackPressed();
        } else {
            switchToRequestListFragment();
        }
    }

    private void switchToRequestListFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MoneyRequestListHolderFragment()).commit();
        mFabRequestMoney.setVisibility(View.VISIBLE);
        switchedToPendingList = true;
    }

    private void switchToRequestMoneyFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RequestMoneyFragment()).commit();
        mFabRequestMoney.setVisibility(View.GONE);
        switchedToPendingList = false;
    }

    @Override
    public Context setContext() {
        return RequestMoneyActivity.this;
    }
}


