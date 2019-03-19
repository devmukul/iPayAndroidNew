package bd.com.ipay.ipayskeleton.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.android.IPayActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPutAsyncTask;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UpdateNotificationStateRequest;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DeepLinkAction;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;
import bd.com.ipay.ipayskeleton.Utilities.TokenManager;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class LauncherActivity extends IPayActivity {

    private boolean firstLaunch = false;
    private static final String TAG = LauncherActivity.class.getSimpleName();
    boolean loggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loggedIn = ProfileInfoCacheManager.getLoggedInStatus(true);

        String activityAction = getIntent().getAction();
        System.out.println("NOTIFICATION TEST - " + getIntent().getExtras().toString());
        if (activityAction == null) {
            activityAction = Intent.ACTION_MAIN;
        }
        switch (activityAction) {
            case Intent.ACTION_VIEW:

                Uri uri = getIntent().getData();
                if (uri != null)
                    Logger.logD(TAG, uri.toString());
                DeepLinkAction deepLinkAction = Utilities.parseUriForDeepLinkingAction(uri);
                if (deepLinkAction != null) {
                    if (SharedPrefManager.isRememberMeActive() && loggedIn) {
                        if (!TextUtils.isEmpty(deepLinkAction.getAction()) && !deepLinkAction.getAction().equals("signup")) {
                            if (deepLinkAction.getAction().contains("promotions")) {
                                Utilities.performDeepLinkAction(this, deepLinkAction);
                                if (getIntent().hasExtra("time")) {
                                    List<Long> timeList = new ArrayList<>();
                                    timeList.add(getIntent().getLongExtra("time", 0));
                                    UpdateNotificationStateRequest updateNotificationStateRequest = new UpdateNotificationStateRequest(timeList, "VISITED");
                                    new HttpRequestPutAsyncTask(Constants.COMMAND_UPDATE_NOTIFICATION_STATE,
                                            Constants.BASE_URL_PUSH_NOTIFICATION + "v2/update",
                                            new Gson().toJson(updateNotificationStateRequest), this, true).
                                            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            } else {
                                try {
                                    Utilities.performDeepLinkAction(this, deepLinkAction);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            launchHomeActivity();
                        }
                    } else {
                        launchSigninOrLoginActivity(deepLinkAction);
                    }
                } else {
                    if (SharedPrefManager.isRememberMeActive() && loggedIn) {
                        launchHomeActivity();
                    } else {
                        launchSigninOrLoginActivity(null);
                    }
                }
                break;
            case Intent.ACTION_MAIN:
            default:
                firstLaunch = SharedPrefManager.getFirstLaunch();
                startApplication();
                break;
        }
    }

    private void startApplication() {
        if (firstLaunch) {
            launchTourActivity();
        } else {
            if (SharedPrefManager.isRememberMeActive() && loggedIn) {
                launchHomeActivity();
            } else {
                launchSigninOrLoginActivity(null);
            }
        }
    }

    private void launchTourActivity() {
        Intent intent;
        intent = new Intent(LauncherActivity.this, TourActivity.class);
        startActivity(intent);
        finish();
    }

    private void launchHomeActivity() {
        if (ProfileInfoCacheManager.isAccountSwitched()) {
            TokenManager.setOnAccountId(ProfileInfoCacheManager.getOnAccountId());
        }
        Intent intent;
        intent = new Intent(LauncherActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void launchSigninOrLoginActivity(DeepLinkAction parcelable) {
        ProfileInfoCacheManager.updateProfileInfoCache(Utilities.getMainUserInfoFromJsonString(ProfileInfoCacheManager.getMainUserProfileInfo()));
        Intent intent;
        intent = new Intent(LauncherActivity.this, SignupOrLoginActivity.class);
        intent.putExtra(Constants.TARGET_FRAGMENT, Constants.SIGN_IN);
        if (parcelable != null)
            intent.putExtra(Constants.DEEP_LINK_ACTION, parcelable);
        startActivity(intent);
        finish();
    }

}
