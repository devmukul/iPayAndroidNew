package bd.com.ipay.ipayskeleton.Activities;

import android.content.Context;

import bd.com.ipay.android.IPayActivity;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public abstract class BaseActivity extends IPayActivity {

	private final Context context;

	public BaseActivity() {
		this.context = setContext();
	}

	protected abstract Context setContext();

	@Override
	public void onResume() {
		super.onResume();

		MyApplication myApp = (MyApplication) this.getApplication();
		myApp.isAppInBackground = false;
		Constants.HAS_COME_FROM_BACKGROUND_TO_FOREGROUND = true;

		if (SharedPrefManager.isRememberMeActive()) {
			if (Utilities.isValidTokenWindowTime())
				myApp.refreshToken();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		((MyApplication) this.getApplication()).isAppInBackground = true;
		Constants.HAS_COME_FROM_BACKGROUND_TO_FOREGROUND = false;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onUserInteraction() {
		super.onUserInteraction();

		if (!SharedPrefManager.isRememberMeActive()) {
			((MyApplication) this.getApplication()).stopUserInactivityDetectorTimer();
			((MyApplication) this.getApplication()).startUserInactivityDetectorTimer();
		}
	}
}