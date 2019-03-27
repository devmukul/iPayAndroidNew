package bd.com.ipay.ipayskeleton.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import bd.com.ipay.ipayskeleton.HomeFragments.NotificationFragment;
import bd.com.ipay.ipayskeleton.HomeFragments.NotificationHolderFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.MakePaymentFragments.PaymentRequestReceivedDetailsFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.RequestMoneyFragments.SentReceivedRequestReviewFragment;
import bd.com.ipay.ipayskeleton.ProfileFragments.RecommendationReviewFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.EditPermissionSourceOfFundBottomSheetFragment;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class NotificationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        String tag = getIntent().getStringExtra(Constants.TAG);

        if (tag != null && tag.equals(Constants.REQUEST_PAYMENT))
            switchToReceivedPaymentRequestDetailsFragment(getIntent().getExtras());
        else if (tag != null && tag.equals(Constants.REQUEST))
            switchToReceivedRequestReviewFragment();
        else if (tag != null && tag.equals(Constants.RECOMMENDATION))
            switchToRecommendationReviewFragment(getIntent().getExtras());
        else
            switchToNotificationFragment();

        setTitle(getString(R.string.notification));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void switchToNotificationFragment() {
        switchToNotificationFragment("");
    }

    public void switchToEditPermissionFragment(
            EditPermissionSourceOfFundBottomSheetFragment
                    editPermissionSourceOfFundBottomSheetFragment, Bundle bundle) {
        while (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        }
        editPermissionSourceOfFundBottomSheetFragment =
                new EditPermissionSourceOfFundBottomSheetFragment();
        editPermissionSourceOfFundBottomSheetFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                editPermissionSourceOfFundBottomSheetFragment).setCustomAnimations(R.anim.slide_up,R.anim.slide_down)
                .addToBackStack(null).commit();


    }

    public void switchToNotificationFragment(String tag) {
        if (tag.equals("")) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NotificationHolderFragment()).commit();

        } else if (tag.equals(Constants.RELOAD)) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.TAG, Constants.RELOAD);
            HomeActivity.mNotificationFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new NotificationHolderFragment()).commit();
        }
    }

    public void switchToReceivedPaymentRequestDetailsFragment(Bundle bundle) {

        PaymentRequestReceivedDetailsFragment paymentRequestReceivedDetailsFragment = new PaymentRequestReceivedDetailsFragment();
        paymentRequestReceivedDetailsFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, paymentRequestReceivedDetailsFragment).commit();
    }

    private void switchToReceivedRequestReviewFragment() {

        SentReceivedRequestReviewFragment receivedRequestReviewFragment = new SentReceivedRequestReviewFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, receivedRequestReviewFragment).commit();

    }

    public void switchToRecommendationReviewFragment(Bundle bundle) {
        RecommendationReviewFragment recommendationReviewFragment = new RecommendationReviewFragment();
        recommendationReviewFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, recommendationReviewFragment).commit();
    }

    @Override
    public Context setContext() {
        return NotificationActivity.this;
    }

    @Override
    public void onBackPressed() {
        Utilities.hideKeyboard(this);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null) {
                if (fragment instanceof NotificationHolderFragment) {
                    NotificationFragment notificationFragment = (NotificationFragment)
                            ((NotificationHolderFragment) fragment).getNotificationFragment();
                    if (notificationFragment != null) {
                        if (notificationFragment.onBackPressed()) {
                            return;
                        }
                    }
                }
            }
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
        else {
            super.onBackPressed();
        }

    }
}




