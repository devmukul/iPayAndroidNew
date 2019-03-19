package bd.com.ipay.ipayskeleton.Activities.DialogActivities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import bd.com.ipay.ipayskeleton.HomeFragments.ContactsFragments.ContactPickerFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

/**
 * Use this activity to pick an iPay contact. After launching the activity, get the value of
 * Constants.MOBILE_NUMBER from the intent in onActivityResult.
 * <p>
 * If want to show only verified users, pass (Constants.VERIFIED_USERS_ONLY, false) in the intent
 * while starting the activity.
 */
public class ContactPickerDialogActivity extends FragmentActivity {

    private boolean mShowVerifiedUsersOnly;
    private boolean miPayMembersOnly;
    private boolean mBusinessMembersOnly;
    private boolean mPersonalMembersOnly;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact_picker);

        mShowVerifiedUsersOnly = getIntent().getBooleanExtra(Constants.VERIFIED_USERS_ONLY, false);
        miPayMembersOnly = getIntent().getBooleanExtra(Constants.IPAY_MEMBERS_ONLY, false);
        mBusinessMembersOnly = getIntent().getBooleanExtra(Constants.BUSINESS_ACCOUNTS_ONLY, false);
        mPersonalMembersOnly = getIntent().getBooleanExtra(Constants.PERSONAL_ACCOUNT, false);

        ContactPickerFragment fragment = new ContactPickerFragment();

        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.VERIFIED_USERS_ONLY, mShowVerifiedUsersOnly);
        bundle.putBoolean(Constants.IPAY_MEMBERS_ONLY, miPayMembersOnly);
        bundle.putBoolean(Constants.BUSINESS_ACCOUNTS_ONLY, mBusinessMembersOnly);
        bundle.putBoolean(Constants.PERSONAL_ACCOUNT,mPersonalMembersOnly);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment).commit();
    }

}
