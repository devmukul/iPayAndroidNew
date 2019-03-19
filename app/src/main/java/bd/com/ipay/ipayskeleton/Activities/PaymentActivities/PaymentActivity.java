package bd.com.ipay.ipayskeleton.Activities.PaymentActivities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.MenuItem;
import android.view.View;

import bd.com.ipay.ipayskeleton.Activities.BaseActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.MandatoryBusinessRules;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetUserInfoResponse;
import bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments.MakePaymentByDeepLinkFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments.PaymentRequestsReceivedFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class PaymentActivity extends BaseActivity {

    private FloatingActionButton mFabMakingPayment;
    private boolean switchedToPendingList = true;
    public static MandatoryBusinessRules mMandatoryBusinessRules;
    private HttpRequestGetAsyncTask mGetProfileInfoTask = null;
    private GetUserInfoResponse mGetUserInfoResponse;

    private CustomProgressDialog mProgressDialog;

    private String address;
    private String district;
    private String country;
    private String thana;

    /**
     * If this value is set in the intent extras,
     * you would be taken directly to the new request page
     */
    public static final String LAUNCH_NEW_REQUEST = "LAUNCH_NEW_REQUEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        mFabMakingPayment = findViewById(R.id.fab_payment_making);
        mProgressDialog = new CustomProgressDialog(this);

        if (getIntent().getStringExtra("ORDER_ID") != null)
            switchToMakePaymentByDeepLinkFragment();
        else {
            switchToReceivedPaymentRequestsFragment();
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Utilities.hideKeyboard(this);
        if (item.getItemId() == android.R.id.home) {
            if (switchedToPendingList) {
                super.onBackPressed();
            }
            if (getIntent().hasExtra(Constants.MOBILE_NUMBER) || getIntent().getBooleanExtra(LAUNCH_NEW_REQUEST, false)) {
                super.onBackPressed();
            } else if (getIntent().getStringExtra("ORDER_ID") != null) {
                this.finishAffinity();
            } else {
                switchToReceivedPaymentRequestsFragment();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void onBackPressed() {
        if (getIntent().hasExtra(Constants.MOBILE_NUMBER) || getIntent().getBooleanExtra(LAUNCH_NEW_REQUEST, false)) {
            finish();
        } else if (switchedToPendingList) {
            super.onBackPressed();
        } else if (getIntent().getStringExtra("ORDER_ID") != null) {
            this.finishAffinity();
        } else {
            switchToReceivedPaymentRequestsFragment();
        }
    }

    public void switchToMakePaymentByDeepLinkFragment() {
        getSupportFragmentManager().beginTransaction().
                replace(R.id.fragment_container, new MakePaymentByDeepLinkFragment()).commit();

        mFabMakingPayment.setVisibility(View.GONE);
        switchedToPendingList = false;
    }

    public void switchToReceivedPaymentRequestsFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new PaymentRequestsReceivedFragment()).commit();
        mFabMakingPayment.setVisibility(View.VISIBLE);
        switchedToPendingList = true;
    }

    @Override
    public Context setContext() {
        return PaymentActivity.this;
    }
}
