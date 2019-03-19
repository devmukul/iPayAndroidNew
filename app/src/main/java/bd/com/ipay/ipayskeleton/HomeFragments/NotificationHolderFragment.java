package bd.com.ipay.ipayskeleton.HomeFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import bd.com.ipay.ipayskeleton.R;

public class NotificationHolderFragment extends Fragment {
	public static TabLayout mNotificationTabLayout;
	private NotificationFragmentPagerAdapter mNotificationFragmentPagerAdapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_notification_holder, container, false);
		ViewPager mNotificationViewPager = view.findViewById(R.id.view_pager_notification);
		mNotificationFragmentPagerAdapter = new NotificationFragmentPagerAdapter(getContext(), getChildFragmentManager());
		mNotificationViewPager.setAdapter(mNotificationFragmentPagerAdapter);
		mNotificationTabLayout = view.findViewById(R.id.sliding_tabs);
		mNotificationTabLayout.setupWithViewPager(mNotificationViewPager);
		return view;
	}

	public Fragment getNotificationFragment() {
		return mNotificationFragmentPagerAdapter.getItem(1);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
}