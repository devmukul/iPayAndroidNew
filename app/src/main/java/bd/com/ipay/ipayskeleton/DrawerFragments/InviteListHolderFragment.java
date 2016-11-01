package bd.com.ipay.ipayskeleton.DrawerFragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import bd.com.ipay.ipayskeleton.HomeFragments.ContactsFragments.ContactsFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class InviteListHolderFragment extends Fragment {

    private ContactsFragment miPayAllContactsFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_invite_holder, container, false);

        switchToAllContacts();
        return v;
    }

    private void switchToAllContacts() {
        try {
            if (getActivity() != null) {
                if (miPayAllContactsFragment == null) {
                    miPayAllContactsFragment = new ContactsFragment();
                }
                Bundle bundle = new Bundle();
                bundle.putBoolean(Constants.SHOW_ALL_MEMBERS, true);
                miPayAllContactsFragment.setArguments(bundle);
                getChildFragmentManager().beginTransaction().replace(R.id.fragment_container_contacts, miPayAllContactsFragment).commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}