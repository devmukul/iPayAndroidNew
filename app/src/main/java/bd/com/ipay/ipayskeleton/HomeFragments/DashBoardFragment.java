package bd.com.ipay.ipayskeleton.HomeFragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.BroadcastReceiverClass.BroadcastServiceIntent;
import bd.com.ipay.ipayskeleton.HomeFragments.TransactionHistoryFragments.TransactionHistoryHolderFragment;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DeepLinkAction;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class DashBoardFragment extends Fragment {

    private final int HOME_TAB = 0;
    private final int PAY_TAB = 1;
    private final int TRANSACTION_HISTORY_TAB = 2;
    private final int OFFER_TAB = 3;

    private final int TOTAL_PAGE_COUNT = 4;

    private HomeFragment mHomeFragment;
    private PayDashBoardFragment mPayFragment;
    private OfferFragment mOfferHolderFragment;
    private TransactionHistoryHolderFragment mTransactionHistoryFragment;

    private TabLayout.Tab homeTab;
    private TabLayout.Tab payTab;
    private TabLayout.Tab offerTab;
    private TabLayout.Tab transactionHistoryTab;

    private View homeTabView;
    private View payTabView;
    private View offerTabView;
    private View transactionHistoryTabView;

    private ViewPager viewPager;
    private String mDeepLinkActionPath;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);
        setTitle();
        mHomeFragment = new HomeFragment();
        mTransactionHistoryFragment = new TransactionHistoryHolderFragment();
        mOfferHolderFragment = new OfferFragment();
        mPayFragment = new PayDashBoardFragment();

        viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        viewPager.setAdapter(new DashBoardTabAdapter(getChildFragmentManager()));
        viewPager.setOffscreenPageLimit(3);    // TODO: Change upon number of tabs

        final TabLayout tabLayout = (TabLayout) v.findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);


        homeTab = tabLayout.getTabAt(HOME_TAB);
        transactionHistoryTab = tabLayout.getTabAt(TRANSACTION_HISTORY_TAB);
        payTab = tabLayout.getTabAt(PAY_TAB);
        offerTab = tabLayout.getTabAt(OFFER_TAB);

        mDeepLinkActionPath = getActivity().getIntent().getStringExtra(Constants.PATH);

        setupCustomViewsForTabLayout();

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                if (tab.getPosition() == TRANSACTION_HISTORY_TAB) {
                    BroadcastServiceIntent.sendBroadcast(getActivity(), Constants.PENDING_TRANSACTION_HISTORY_UPDATE_BROADCAST);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Utilities.hideKeyboard(getActivity());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if (mDeepLinkActionPath != null && mDeepLinkActionPath.contains("promotions")) {
            viewPager.setCurrentItem(OFFER_TAB, true);
        }

        return v;
    }

    private void setupCustomViewsForTabLayout() {
        homeTabView = getActivity().getLayoutInflater().inflate(R.layout.view_single_tab_background, null);
        offerTabView = getActivity().getLayoutInflater().inflate(R.layout.view_single_tab_background, null);
        transactionHistoryTabView = getActivity().getLayoutInflater().inflate(R.layout.view_single_tab_background, null);
        payTabView = getActivity().getLayoutInflater().inflate(R.layout.view_single_tab_background, null);
        setTabViews();
    }

    private void setTabViews() {
        setTabIconsWithTexts();

        homeTab.setCustomView(homeTabView);
        offerTab.setCustomView(offerTabView);
        transactionHistoryTab.setCustomView(transactionHistoryTabView);
        payTab.setCustomView(payTabView);
    }

    private void setTitle() {
        ((HomeActivity) getActivity()).getSupportActionBar().setDisplayUseLogoEnabled(true);
        ((HomeActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setTabIconsWithTexts() {
        ((ImageView) homeTabView.findViewById(R.id.tab_icon)).setImageResource(R.drawable.ic_wallet);
        ((ImageView) offerTabView.findViewById(R.id.tab_icon)).setImageResource(R.drawable.ic_offer);
        ((ImageView) transactionHistoryTabView.findViewById(R.id.tab_icon)).setImageResource(R.drawable.ic_transaction);
        ((ImageView) payTabView.findViewById(R.id.tab_icon)).setImageResource(R.drawable.ic_pay);

        ((TextView) homeTabView.findViewById(R.id.tab_text)).setText(getActivity().getResources().getString(R.string.wallet));
        ((TextView) offerTabView.findViewById(R.id.tab_text)).setText(getActivity().getResources().getString(R.string.offer));
        ((TextView) payTabView.findViewById(R.id.tab_text)).setText(getActivity().getResources().getString(R.string.pay));
        ((TextView) transactionHistoryTabView.findViewById(R.id.tab_text)).setText(getActivity().getResources().getString(R.string.transaction));
    }

    private class DashBoardTabAdapter extends FragmentPagerAdapter {

        public DashBoardTabAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch (pos) {
                case HOME_TAB:
                    return mHomeFragment;
                case PAY_TAB:
                    return mPayFragment;
                case TRANSACTION_HISTORY_TAB:
                    return mTransactionHistoryFragment;
                case OFFER_TAB:
                    return mOfferHolderFragment;
                default:
                    return new Fragment();
            }
        }

        @Override
        public int getCount() {
            return TOTAL_PAGE_COUNT;
        }
    }

}