package bd.com.ipay.ipayskeleton.HomeFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.BroadcastReceiverClass.BroadcastServiceIntent;
import bd.com.ipay.ipayskeleton.HomeFragments.TransactionHistoryFragments.TransactionHistoryHolderFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class DashBoardFragment extends Fragment
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private HomeFragment mHomeFragment;
    private IpayHereFragment mIPayHereFragment;
    private TransactionHistoryHolderFragment mTransactionHistoryFragment;
    private NewsRoomFragment mNewsRoomFragment;

    private MenuItem mPrevMenuItem;
    private BottomNavigationView bottomNavigationView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();
        mHomeFragment = new HomeFragment();
        mTransactionHistoryFragment = new TransactionHistoryHolderFragment();
        mNewsRoomFragment = new NewsRoomFragment();
        mIPayHereFragment = new IpayHereFragment();

        bottomNavigationView = view.findViewById(R.id.bottom_navigation_view);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.navigation_wallet);
        if (getFragmentManager() != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mHomeFragment)
                    .commit();
        }
    }

    private void setTitle() {
        if (getActivity() instanceof HomeActivity &&
                ((HomeActivity) getActivity()).getSupportActionBar() != null) {
            ActionBar actionBar = ((HomeActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayUseLogoEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (getFragmentManager() == null)
            return false;
        if (item.getItemId() == bottomNavigationView.getSelectedItemId())
            return false;
        if (mPrevMenuItem != null) {
            mPrevMenuItem.setChecked(false);
        } else {
            bottomNavigationView.getMenu().getItem(0).setChecked(false);
        }

        item.setChecked(true);
        mPrevMenuItem = item;
        switch (item.getItemId()) {
            case R.id.navigation_wallet:
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mHomeFragment)
                        .commit();
                break;
            case R.id.navigation_ipay_here:
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mIPayHereFragment)
                        .commit();
                break;
            case R.id.navigation_transaction:
                BroadcastServiceIntent.sendBroadcast(getActivity(),
                        Constants.PENDING_TRANSACTION_HISTORY_UPDATE_BROADCAST);
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mTransactionHistoryFragment)
                        .commit();
                break;
            case R.id.navigation_news_room:
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, mNewsRoomFragment)
                        .commit();
                break;
        }
        return true;
    }
}