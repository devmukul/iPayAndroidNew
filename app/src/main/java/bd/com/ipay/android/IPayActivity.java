package bd.com.ipay.android;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;

import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Common.GenderList;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class IPayActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupLanguage();
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
		getApplication().getBaseContext().getResources().updateConfiguration(appConfig, getResources().getDisplayMetrics());
		getApplication().getResources().updateConfiguration(appConfig, getResources().getDisplayMetrics());
		getResources().updateConfiguration(appConfig, getResources().getDisplayMetrics());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			getApplication().getBaseContext().createConfigurationContext(appConfig);
			getBaseContext().createConfigurationContext(appConfig);
			createConfigurationContext(appConfig);
		}
		Utilities.updateLocale();
		GenderList.initialize(this);
	}
}
