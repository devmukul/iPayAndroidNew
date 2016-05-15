package bd.com.ipay.ipayskeleton.Activities.PaymentActivities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import bd.com.ipay.ipayskeleton.Activities.BaseActivity;
import bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments.CreateInvoiceFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments.InvoicesSentFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class MakePaymentActivity extends BaseActivity {

    private FloatingActionButton mFabCreateInvoice;
    private boolean switchedToInvoicesList = true;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment);
        pref = getSharedPreferences(Constants.ApplicationTag, Activity.MODE_PRIVATE);

        mFabCreateInvoice = (FloatingActionButton) findViewById(R.id.fab_create_invoice);

        mFabCreateInvoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToCreateInvoiceFragment();
            }
        });

        switchToInvoicesSentFragment();
    }

    @Override
    public void onBackPressed() {
        if (switchedToInvoicesList) super.onBackPressed();
        else {
            switchToInvoicesSentFragment();
        }
    }

    public void switchToInvoicesSentFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new InvoicesSentFragment()).commit();
        mFabCreateInvoice.setVisibility(View.VISIBLE);
        switchedToInvoicesList = true;
    }
    
    public void switchToCreateInvoiceFragment() {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CreateInvoiceFragment()).commit();
        mFabCreateInvoice.setVisibility(View.GONE);
        switchedToInvoicesList = false;
    }

    @Override
    public Context setContext() {
        return MakePaymentActivity.this;
    }
}





