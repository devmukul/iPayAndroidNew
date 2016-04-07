package bd.com.ipay.ipayskeleton.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import bd.com.ipay.ipayskeleton.AddMoneyFragments.AddMoneyFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class AddMoneyActivity extends BaseActivity {

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_in);
        pref = getSharedPreferences(Constants.ApplicationTag, Activity.MODE_PRIVATE);

        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new AddMoneyFragment()).commit();

    }

    @Override
    public Context setContext() {
        return AddMoneyActivity.this;
    }
}
