package bd.com.ipay.ipayskeleton.Activities.PaymentActivities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import bd.com.ipay.ipayskeleton.Activities.BaseActivity;
import bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments.SingleInvoiceFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;


public class SingleInvoiceActivity extends BaseActivity {

    private SingleInvoiceFragment singleInvoiceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_invoice);

        String result = getIntent().getStringExtra(Constants.RESULT);

        singleInvoiceFragment = new SingleInvoiceFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT, result);
        singleInvoiceFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, singleInvoiceFragment).commit();

    }

    @Override
    public Context setContext() {
        return SingleInvoiceActivity.this;
    }
}
