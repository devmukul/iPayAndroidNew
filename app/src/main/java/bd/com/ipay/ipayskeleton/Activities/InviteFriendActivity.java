package bd.com.ipay.ipayskeleton.Activities;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;

import bd.com.ipay.ipayskeleton.HomeFragments.InviteFriendFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class InviteFriendActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friend);
        switchToRequestMoneyFragment();
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Utilities.hideKeyboard(this);
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }



    public void switchToRequestMoneyFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new InviteFriendFragment()).commit();
    }

    @Override
    public Context setContext() {
        return InviteFriendActivity.this;
    }
}

