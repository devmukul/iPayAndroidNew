package bd.com.ipay.ipayskeleton.HomeFragments;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.R;

public class NotificationFragmentPagerAdapter extends FragmentPagerAdapter {

	private Context context;

	NotificationFragmentPagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		this.context = context;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				return new NotificationDeeplinkedFragment();
			case 1:
				return HomeActivity.mNotificationFragment;
		}
		return null;
	}


	@Override
	public int getCount() {
		return 2;
	}

	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		switch (position) {
			case 0:
				return context.getString(R.string.notifications_notifications);
			case 1:
				return context.getString(R.string.pending_notifications);
		}
		return super.getPageTitle(position);
	}
}
