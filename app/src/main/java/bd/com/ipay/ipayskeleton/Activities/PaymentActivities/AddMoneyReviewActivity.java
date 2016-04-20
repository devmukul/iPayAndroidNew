package bd.com.ipay.ipayskeleton.Activities.PaymentActivities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import bd.com.ipay.ipayskeleton.Activities.BaseActivity;
import bd.com.ipay.ipayskeleton.PaymentFragments.AddMoneyFragments.AddMoneyReviewFragment;
import bd.com.ipay.ipayskeleton.PaymentFragments.SendMoneyFragments.SendMoneyFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class AddMoneyReviewActivity extends BaseActivity {

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_money_review);
        pref = getSharedPreferences(Constants.ApplicationTag, Activity.MODE_PRIVATE);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new AddMoneyReviewFragment()).commit();

    }

    @Override
    public Context setContext() {
        return AddMoneyReviewActivity.this;
    }
}
