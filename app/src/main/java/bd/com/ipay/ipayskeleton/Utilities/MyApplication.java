package bd.com.ipay.ipayskeleton.Utilities;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.LocaleList;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import bd.com.ipay.ipayskeleton.Activities.SignupOrLoginActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.BuildConfig;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRuleAndServiceCharge.BusinessRule.MandatoryBusinessRules;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LogoutRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LogoutResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RefreshToken.GetRefreshTokenRequest;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.BulkSignupUserDetailsCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Common.GenderList;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;
import okhttp3.OkHttpClient;

public class MyApplication extends MultiDexApplication implements HttpResponseListener {

	// Variables for token timer
	private static Timer mTokenTimer;
	private static TimerTask mTokenTimerTask;
	//Google Analytic
	private static GoogleAnalytics sAnalytics;
	private static Tracker sTracker;
	private static MyApplication myApplicationInstance;
	public boolean isAppInBackground = false;
	// Variables for user inactivity
	private Timer mUserInactiveTimer;
	private HttpRequestPostAsyncTask mLogoutTask = null;
	private HttpRequestPostAsyncTask mRefreshTokenAsyncTask = null;
	private OkHttpClient okHttpClient;

	public static MyApplication getMyApplicationInstance() {
		return myApplicationInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		myApplicationInstance = this;
		okHttpClient = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS)
				.connectTimeout(60, TimeUnit.SECONDS).build();
		SharedPrefManager.initialize(getApplicationContext());
		setupLanguage();

		ProfileInfoCacheManager.initialize(getApplicationContext());
        BulkSignupUserDetailsCacheManager.initialize(getApplicationContext());
		ACLManager.initialize(this);
		TokenManager.initialize(this);
		BusinessRuleCacheManager.initialize(this);
		setDefaultBusinessRules();
		sAnalytics = GoogleAnalytics.getInstance(this);
	}

	private void setupLanguage() {
		final String appLanguage = SharedPrefManager.getAppLanguage();
		final Locale locale;
		switch (appLanguage) {
			case Constants.APP_LANGUAGE_ENGLISH:
				locale = Locale.US;
				break;
			default:
			case Constants.APP_LANGUAGE_BENGALI:
				locale = new Locale("bn");
				break;
		}
		Locale.setDefault(locale);
		final Configuration appConfig = getResources().getConfiguration();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			appConfig.setLocales(new LocaleList(locale));
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			appConfig.setLocale(locale);
		}

		getBaseContext().getResources().updateConfiguration(appConfig, getResources().getDisplayMetrics());
		getResources().updateConfiguration(appConfig, getResources().getDisplayMetrics());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			getBaseContext().createConfigurationContext(appConfig);
			createConfigurationContext(appConfig);
		}
		Utilities.updateLocale();
		GenderList.initialize(this);
	}

	public OkHttpClient getOkHttpClient() {
		return okHttpClient;
	}

	public void startUserInactivityDetectorTimer() {
		if (mUserInactiveTimer != null)
			mUserInactiveTimer.cancel();

		this.mUserInactiveTimer = new Timer();
		TimerTask mUserInactiveTimerTask = new TimerTask() {
			public void run() {
				forceLogoutForInactivity();
			}
		};

		// 5 Minutes inactive time
		long AUTO_LOGOUT_TIMEOUT = 5 * 60 * 1000;
		this.mUserInactiveTimer.schedule(mUserInactiveTimerTask,
				AUTO_LOGOUT_TIMEOUT);
	}

	public void stopUserInactivityDetectorTimer() {
		if (this.mUserInactiveTimer != null) {
			this.mUserInactiveTimer.cancel();
			this.mUserInactiveTimer = null;
		}
	}

	/**
	 * set Default Business Rules is to cache the offline business rules
	 */
	private void setDefaultBusinessRules() {
		for (String serviceTag : BusinessRuleConstants.SERVICE_BUSINESS_RULE_TAGS) {
			if (!BusinessRuleCacheManager.ifContainsBusinessRule(serviceTag)) {
				MandatoryBusinessRules mandatoryBusinessRules = new MandatoryBusinessRules(serviceTag);
				mandatoryBusinessRules.setDefaultRules();
				BusinessRuleCacheManager.setBusinessRules(serviceTag, mandatoryBusinessRules);
			}
		}
	}

	public void attemptLogout() {
		if (mLogoutTask != null) {
			return;
		}

		String mUserID = ProfileInfoCacheManager.getMobileNumber();

		LogoutRequest mLogoutModel = new LogoutRequest(mUserID);
		Gson gson = new Gson();
		String json = gson.toJson(mLogoutModel);

		mLogoutTask = new HttpRequestPostAsyncTask(Constants.COMMAND_LOG_OUT,
				Constants.BASE_URL_MM + Constants.URL_LOG_OUT, json, getApplicationContext(), false);
		mLogoutTask.mHttpResponseListener = this;

		mLogoutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void startTokenTimer() {
		stopTokenTimer();

		mTokenTimer = new Timer();
		mTokenTimerTask = new TimerTask() {
			public void run() {
				refreshToken();
			}
		};

		mTokenTimer.schedule(mTokenTimerTask,
				TokenManager.getiPayTokenTimeInMs());
	}

	private void stopTokenTimer() {
		if (mTokenTimerTask != null) {
			mTokenTimerTask.cancel();
		}

		if (mTokenTimer != null) {
			mTokenTimer.cancel();
		}
	}

	public void refreshToken() {
		Logger.logW("Token_Timer", "Refresh token called");

		if (mRefreshTokenAsyncTask != null) {
			mRefreshTokenAsyncTask.cancel(true);
			mRefreshTokenAsyncTask = null;
		}

		GetRefreshTokenRequest mGetRefreshTokenRequest = new GetRefreshTokenRequest(TokenManager.getRefreshToken());
		Gson gson = new Gson();
		String json = gson.toJson(mGetRefreshTokenRequest);
		mRefreshTokenAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_REFRESH_TOKEN,
				Constants.BASE_URL_MM + Constants.URL_GET_REFRESH_TOKEN, json, getApplicationContext(), true);
		mRefreshTokenAsyncTask.mHttpResponseListener = this;

		mRefreshTokenAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void forceLogoutForInactivity() {
		if (Utilities.isConnectionAvailable(getApplicationContext())) attemptLogout();
		else launchLoginPage(getString(R.string.please_log_in_again));
	}

	// Launch login page for token timeout/un-authorized/logout called for user inactivity
	public void launchLoginPage(String message) {
		boolean loggedIn = ProfileInfoCacheManager.getLoggedInStatus(true);

		// If the user is not logged in already, no need to launch the Login page.
		// Return from here
		if (!loggedIn) return;
		if (ProfileInfoCacheManager.isAccountSwitched()) {
			TokenManager.setOnAccountId(Constants.ON_ACCOUNT_ID_DEFAULT);
			ProfileInfoCacheManager.updateBusinessInfoCache(null);
			ProfileInfoCacheManager.saveMainUserBusinessInfo(null);
			ProfileInfoCacheManager.updateProfileInfoCache(Utilities.getMainUserInfoFromJsonString(ProfileInfoCacheManager.getMainUserProfileInfo()));
		}
		if (!isAppInBackground) {
			ProfileInfoCacheManager.setLoggedInStatus(false);

			Intent intent = new Intent(getApplicationContext(), SignupOrLoginActivity.class);
			if (message != null)
				intent.putExtra(Constants.MESSAGE, message);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		}

		SharedPrefManager.setRememberMeActive(false);
		clearTokenAndTimer();
	}

	// Clear token and timer after logout
	public void clearTokenAndTimer() {
		stopUserInactivityDetectorTimer();
		stopTokenTimer();
		TokenManager.invalidateToken();
	}

	/**
	 * Gets the default {@link Tracker} for this {@link Application}.
	 *
	 * @return tracker
	 */
	synchronized public Tracker getDefaultTracker() {
		if (sTracker == null) {
			if (!BuildConfig.DEBUG) {
				sTracker = sAnalytics.newTracker(R.xml.global_tracker_for_live);
			} else {
				sTracker = sAnalytics.newTracker(R.xml.global_tracker);
			}
		}
		return sTracker;
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {

		if (HttpErrorHandler.isErrorFound(result, getApplicationContext(), null)) {
			mLogoutTask = null;
			mRefreshTokenAsyncTask = null;
			return;
		}

		Gson gson = new Gson();

		if (result.getApiCommand().equals(Constants.COMMAND_LOG_OUT)) {
			try {
				LogoutResponse mLogOutResponse = gson.fromJson(result.getJsonString(), LogoutResponse.class);
				launchLoginPage(getApplicationContext().getString(R.string.please_log_in_again));

				// Launched login page for both success and failure case. Handled the failure here.
				if (result.getStatus() != Constants.HTTP_RESPONSE_STATUS_OK)
					Toast.makeText(getApplicationContext(), mLogOutResponse.getMessage(), Toast.LENGTH_LONG).show();

			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), R.string.could_not_sign_out, Toast.LENGTH_LONG).show();
			}

			mLogoutTask = null;

		} else if (result.getApiCommand().equals(Constants.COMMAND_REFRESH_TOKEN)) {
			// No action need to be done.
			mRefreshTokenAsyncTask = null;
		}
	}
}
