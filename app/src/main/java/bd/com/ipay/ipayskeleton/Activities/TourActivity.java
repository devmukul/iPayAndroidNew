package bd.com.ipay.ipayskeleton.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import bd.com.ipay.android.IPayActivity;
import bd.com.ipay.ipayskeleton.BaseFragments.BaseFragment;
import bd.com.ipay.ipayskeleton.CustomView.PagerIndicator;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class TourActivity extends IPayActivity {

    private static final int TOTAL_TOUR_PAGE_COUNT = 4;
    private PagerIndicator mPagerIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        final Button buttonLogin = (Button) findViewById(R.id.button_sign_in);
        final Button buttonSignUp = (Button) findViewById(R.id.button_join_now);

        final ViewPager tourPager = (ViewPager) findViewById(R.id.tour_pager);
        final TourPagerAdapter tourPageAdapter = new TourPagerAdapter(getSupportFragmentManager());
        tourPager.setAdapter(tourPageAdapter);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TourActivity.this, SignupOrLoginActivity.class);
                intent.putExtra(Constants.TARGET_FRAGMENT, Constants.SIGN_IN);
                startActivity(intent);
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TourActivity.this, SignupOrLoginActivity.class);
                intent.putExtra(Constants.TARGET_FRAGMENT, Constants.SIGN_UP);
                startActivity(intent);
            }
        });


        mPagerIndicator = (PagerIndicator) findViewById(R.id.pager_indicator);
        mPagerIndicator.setup(R.drawable.pager_indicator_selected,
                R.drawable.pager_indicator_unselected, TOTAL_TOUR_PAGE_COUNT);

        tourPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                mPagerIndicator.setSelectedPosition(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    public void onResume() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        super.onResume();
    }

    public static class TourFragment extends BaseFragment {

        public static final String RES_ID = "RES_ID";

        public static Fragment newInstance(@LayoutRes int layoutResId) {
            TourFragment tourFragment = new TourFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(RES_ID, layoutResId);
            tourFragment.setArguments(bundle);
            return tourFragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            try {
                if (getArguments() != null) {
                    final int layoutResId = getArguments().getInt(RES_ID);
                    if (layoutResId != 0)
                        return inflater.inflate(layoutResId, container, false);
                    else return new View(getContext());
                } else return new View(getContext());
            } catch (Exception e) {
                Utilities.sendExceptionTracker(mTracker, ProfileInfoCacheManager.getAccountId(), e.getMessage());
                return new View(getContext());
            }
        }
    }

    private class TourPagerAdapter extends FragmentPagerAdapter {

        TourPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return TourFragment.newInstance(R.layout.tour_first_page);
                case 1:
                    return TourFragment.newInstance(R.layout.tour_second_page);
                case 2:
                    return TourFragment.newInstance(R.layout.tour_third_page);
                case 3:
                    return TourFragment.newInstance(R.layout.tour_fourth_page);
                default:
                    return new TourFragment();
            }
        }

        @Override
        public int getCount() {
            return TOTAL_TOUR_PAGE_COUNT;
        }
    }
}