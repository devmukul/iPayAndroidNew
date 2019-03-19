package bd.com.ipay.ipayskeleton.Activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mikepenz.actionitembadge.library.ActionItemBadge;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.AboutActivity;
import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ActivityLogActivity;
import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ContactsActivity;
import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.HelpAndSupportActivity;
import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ManageBanksActivity;
import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.ProfileActivity;
import bd.com.ipay.ipayskeleton.Activities.DrawerActivities.SecuritySettingsActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.SentReceivedRequestReviewActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.TransactionDetailsActivity;
import bd.com.ipay.ipayskeleton.Api.ContactApi.GetContactsAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestGetAsyncTask;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.ResourceApi.GetAllBusinessListAsyncTask;
import bd.com.ipay.ipayskeleton.Api.ResourceApi.GetAvailableBankAsyncTask;
import bd.com.ipay.ipayskeleton.Api.ResourceApi.GetBusinessTypesAsyncTask;
import bd.com.ipay.ipayskeleton.Api.ResourceApi.GetRelationshipListAsyncTask;
import bd.com.ipay.ipayskeleton.Aspect.ValidateAccess;
import bd.com.ipay.ipayskeleton.CustomView.AutoResizeTextView;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AddPromoDialogBuilder;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.CustomProgressDialog;
import bd.com.ipay.ipayskeleton.CustomView.ProfileImageView;
import bd.com.ipay.ipayskeleton.DataCollectors.Model.LocationCollector;
import bd.com.ipay.ipayskeleton.DataCollectors.Model.UserLocation;
import bd.com.ipay.ipayskeleton.HomeFragments.DashBoardFragment;
import bd.com.ipay.ipayskeleton.HomeFragments.HomeFragment;
import bd.com.ipay.ipayskeleton.HomeFragments.NotificationFragment;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.BusinessContact.GetAllBusinessContactRequestBuilder;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AccessControl.GetAccessControlResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Balance.RefreshBalanceResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Business.Employee.GetBusinessInformationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GetDeepLinkedNotificationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LogoutRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.LoginAndSignUp.LogoutResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Notification.Notification;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.GetProfileInfoResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion.ProfileCompletionPropertyConstants;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RefreshToken.FCMRefreshTokenRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RefreshToken.FcmLogoutRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.BusinessType;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Resource.Relationship;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionHistory;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.SourceOfFund.SourceOfFundActivity;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.GetSponsorListResponse;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.Sponsor;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ACLManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.ContactSearchHelper;
import bd.com.ipay.ipayskeleton.Utilities.DeviceInfoFactory;
import bd.com.ipay.ipayskeleton.Utilities.DialogUtils;
import bd.com.ipay.ipayskeleton.Utilities.MyApplication;
import bd.com.ipay.ipayskeleton.Utilities.ServiceIdConstants;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Toaster;
import bd.com.ipay.ipayskeleton.Utilities.TokenManager;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class HomeActivity extends BaseActivity
		implements NavigationView.OnNavigationItemSelectedListener, LocationListener, HttpResponseListener {

	private static final int REQUEST_CODE_PERMISSION = 1001;

	private HttpRequestPostAsyncTask mLocationUpdateRequestAsyncTask;

	private HttpRequestPostAsyncTask mLogoutTask = null;
	private HttpRequestGetAsyncTask mGetProfileInfoTask = null;

	private HttpRequestGetAsyncTask mGetAccessControlTask = null;

	private HttpRequestGetAsyncTask mGetBusinessInformationAsyncTask;

	private HttpRequestPostAsyncTask firebaseLogoutTask;

    private HttpRequestGetAsyncTask mGetNotificationAsyncTask;

	private HttpRequestPostAsyncTask mRefreshTokenAsyncTask;

	private AutoResizeTextView mMobileNumberView;
	private TextView mNameView;
	private ProfileImageView mProfileImageView;
	private ProfileImageView mOptionMenuProfileImageView;
	private NavigationView mNavigationView;

	public CustomProgressDialog mProgressDialog;
	public static NotificationFragment mNotificationFragment;
	private Menu mOptionsMenu;
	private int mBadgeCount = 0;

	private static boolean switchedToHomeFragment = true;
	private boolean exitFromApplication = false;

	private LocationManager mLocationManager;
	private DrawerLayout drawer;

	private HttpRequestPostAsyncTask mRefreshBalanceTask;
    private HttpRequestGetAsyncTask getSponsorListAsyncTask;
    public static ArrayList<Sponsor> mSponsorList;

	public HomeActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_home);
		mSponsorList = new ArrayList<>();if (getIntent() != null) {
			if (getIntent().getData() != null && getIntent().getData().toString().contains("www.ipay.com.bd")) {
				try {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.addCategory(Intent.CATEGORY_BROWSABLE);
					intent.setData(getIntent().getData());
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(this, R.string.no_browser_found_error_message, Toast.LENGTH_SHORT).show();
				}
				return;
			}

		}

        if (getIntent().hasExtra(Constants.TRANSACTION_DETAILS)) {
            String desiredActivity = getIntent().getStringExtra(Constants.DESIRED_ACTIVITY);
            if (desiredActivity.equals(Constants.TRANSACTION)) {
                Intent intent = new Intent(this, TransactionDetailsActivity.class);
                intent.putExtra(Constants.TRANSACTION_DETAILS, getIntent().
                        getParcelableExtra(Constants.TRANSACTION_DETAILS));
                startActivity(intent);
            } else {
                TransactionHistory transactionHistory = getIntent().getParcelableExtra(Constants.TRANSACTION_DETAILS);
                Intent intent = launchRequestMoneyReviewPageIntent(transactionHistory,
                        getIntent().getBooleanExtra(Constants.ACTION_FROM_NOTIFICATION, false));
                startActivity(intent);
            }
        }
		refreshBalance();
		mProgressDialog = new CustomProgressDialog(HomeActivity.this);
		if (!SharedPrefManager.isFireBaseTokenSent()) {
			sendFireBaseTokenToServer();
		}
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayShowTitleEnabled(false);
			getSupportActionBar().setLogo(R.drawable.logo_ipay);
		}
		DialogUtils.showAppUpdateDialog = null;

		mNavigationView = findViewById(R.id.nav_view);

		drawer = findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

		drawer.addDrawerListener(toggle);
		toggle.syncState();

		String mUserID = ProfileInfoCacheManager.getMobileNumber();

		SharedPrefManager.setFirstLaunch(false);

		mMobileNumberView = mNavigationView.getHeaderView(0).findViewById(R.id.textview_mobile_number);
		mNameView = mNavigationView.getHeaderView(0).findViewById(R.id.textview_name);
		mProfileImageView = mNavigationView.getHeaderView(0).findViewById(R.id.profile_picture);
		mMobileNumberView.setText(mUserID);
		mNavigationView.setNavigationItemSelectedListener(this);

		switchToDashBoard();

		updateProfileInfo();

		// Sync contacts
		if (ACLManager.hasServicesAccessibility(ServiceIdConstants.GET_CONTACTS))
			new GetContactsAsyncTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		// DBContactNode sync is done as follows: first all the contacts are downloaded from the server
		// (#GetContactsAsyncTask) and stored in the database (#SyncContactsAsyncTask).
		// Then difference with phone contacts is calculated, and this difference is sent to the
		// server. If there is any new contact on the phone, we download all contacts from the
		// server again to keep phone and server contacts in sync.

		Logger.logW("Token", TokenManager.getToken());

		// The same notification fragment is used when NotificationActivity is launched.
		// We are initializing it here to load notification badge count.
		mNotificationFragment = new NotificationFragment();
		mNotificationFragment.setOnNotificationUpdateListener(new NotificationFragment.OnNotificationUpdateListener() {
			@Override
			public void onNotificationUpdate(List<Notification> notifications) {
				updateNotificationBadgeCount(notifications.size());
			}
		});
		// We need to show the notification badge count. So loading the notification lists to count
		// the number of pending notifications. Once the notifications are loaded, updateNotificationBadgeCount()
		// is called from NotificationFragment.
		mNotificationFragment.getNotificationLists(this);
		// Registering the notification broadcast receiver
		mNotificationFragment.registerNotificationBroadcastReceiver(this);

		// Load the list of available banks, which will be accessed from multiple activities
		getAvailableBankList();

		//get sponsor list

        if (ACLManager.hasServicesAccessibility(ServiceIdConstants.GET_SOURCE_OF_FUND)) {
            attemptGetSponsorList();
        }
        // Load the list of available business types, which will be accessed from multiple activities
		getAvailableBusinessTypes();

		// Fetch available relationship list
		getRelationshipList();

		// Fetch ACL List
		if (SharedPrefManager.isRememberMeActive()) {
			getAccessControlList();
		}

		// Check if important permissions (e.g. Contacts permission) is given. If not,
		// request user for permission.
		attemptRequestForPermission();
		if (Utilities.isNecessaryPermissionExists(this, Constants.LOCATION_PERMISSIONS)) {
			startLocationCollection();
		}

		getAllBusinessAccountsList();

		// If profile picture gets updated, we need to refresh the profile picture in the drawer.
		LocalBroadcastManager.getInstance(this).registerReceiver(mProfilePictureUpdateBroadcastReceiver,
				new IntentFilter(Constants.PROFILE_PICTURE_UPDATE_BROADCAST));

		LocalBroadcastManager.getInstance(this).registerReceiver(mProfileInfoUpdateBroadcastReceiver,
				new IntentFilter(Constants.PROFILE_INFO_UPDATE_BROADCAST));
	}

    private Intent launchRequestMoneyReviewPageIntent(TransactionHistory transactionHistory, boolean isAccepted) {
        Intent intent = new Intent(this, SentReceivedRequestReviewActivity.class);
        intent.putExtra(Constants.AMOUNT, new BigDecimal(transactionHistory.getAmount()));
        intent.putExtra(Constants.RECEIVER_MOBILE_NUMBER,
                ContactEngine.formatMobileNumberBD(transactionHistory.getAdditionalInfo().getNumber()));

        intent.putExtra(Constants.DESCRIPTION_TAG, transactionHistory.getPurpose());
        intent.putExtra(Constants.ACTION_FROM_NOTIFICATION, isAccepted);
        intent.putExtra(Constants.TRANSACTION_ID, transactionHistory.getTransactionID());
        intent.putExtra(Constants.NAME, transactionHistory.getReceiver());
        intent.putExtra(Constants.PHOTO_URI, Constants.BASE_URL_FTP_SERVER + transactionHistory.getAdditionalInfo().getUserProfilePic());
        intent.putExtra(Constants.SWITCHED_FROM_TRANSACTION_HISTORY, true);
        intent.putExtra(Constants.IS_IN_CONTACTS,
                new ContactSearchHelper(this).searchMobileNumber(transactionHistory.getAdditionalInfo().getNumber()));

        if (transactionHistory.getType().equalsIgnoreCase(Constants.TRANSACTION_TYPE_CREDIT)) {
            intent.putExtra(Constants.REQUEST_TYPE, Constants.REQUEST_TYPE_SENT_REQUEST);
        }
        return intent;
    }


	private void attemptGetSponsorList() {
        if (getSponsorListAsyncTask != null) {
            return;
        } else {
            getSponsorListAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_SPONSOR_LIST, Constants.BASE_URL_MM + Constants.URL_GET_SPONSOR,
                    this, this, true);
            getSponsorListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }@SuppressWarnings("MissingPermission")
	private void startLocationCollection() {
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (Utilities.isNecessaryPermissionExists(this, Constants.LOCATION_PERMISSIONS) && mLocationManager != null) {
			final String locationProvider;
			if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				locationProvider = LocationManager.NETWORK_PROVIDER;
			} else {
				locationProvider = LocationManager.GPS_PROVIDER;
			}
			mLocationManager.requestSingleUpdate(locationProvider, this, Looper.myLooper());
		}
	}

	private void sendFireBaseTokenToServer() {
		String fireBaseToken = ProfileInfoCacheManager.getPushNotificationToken(null);
		Logger.logW("Firebase Token", "Refresh token called");

		if (mRefreshTokenAsyncTask != null) {
			mRefreshTokenAsyncTask = null;
		}

		String myDeviceID = DeviceInfoFactory.getDeviceId(this);
		FCMRefreshTokenRequest mFcmRefreshTokenRequest = new FCMRefreshTokenRequest(fireBaseToken, myDeviceID, Constants.MOBILE_ANDROID);
		Gson gson = new Gson();
		String json = gson.toJson(mFcmRefreshTokenRequest);
		mRefreshTokenAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_REFRESH_FIREBASE_TOKEN,
				Constants.BASE_URL_PUSH_NOTIFICATION + Constants.URL_REFRESH_FIREBASE_TOKEN, json, this, true);
		mRefreshTokenAsyncTask.mHttpResponseListener = this;

		mRefreshTokenAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * update Profile info fetches from the Profile Information API.
	 * If the account type is business then, an additional task is done by calling the
	 * Business Information API as the Profile API doesn't provide us the Business Name
	 */
	private void updateProfileInfo() {
		getProfileInfo();
		if (ProfileInfoCacheManager.isBusinessAccount()) {
			getBusinessInformation();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_activity, menu);
		mOptionsMenu = menu;

		MenuItem mProfilePictureMenu = mOptionsMenu.findItem(R.id.action_profile_image);
		FrameLayout rootView = (FrameLayout) mProfilePictureMenu.getActionView();
		mOptionMenuProfileImageView = rootView.findViewById(R.id.profile_picture);
		ImageView mVerificationStatusView = rootView.findViewById(R.id.verification_status);
		mOptionMenuProfileImageView.setAccountPhoto(Constants.BASE_URL_FTP_SERVER + ProfileInfoCacheManager.getProfileImageUrl(), false);
		if (!ProfileInfoCacheManager.isAccountVerified()) {
			mVerificationStatusView.setImageResource(R.drawable.ic_unvarified);
		} else {
			mVerificationStatusView.setImageResource(R.drawable.ic_varified_actionbar);
		}

		mOptionMenuProfileImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(HomeActivity.this, ProfileActivity.class);
				startActivity(i);
			}
		});

		// If the menu is recreated, then restore the previous badge count
		updateNotificationBadgeCount(mBadgeCount);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_notification:
				Intent intent = new Intent(this, NotificationActivity.class);
				startActivity(intent);
				return true;
			case R.id.action_profile_image:
				Intent i = new Intent(this, ProfileActivity.class);
				startActivity(i);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Utilities.hideKeyboard(this);
		getNotifications();
	}

	private void getNotifications() {
		if (mGetNotificationAsyncTask == null) {
			String url = Constants.BASE_URL_PUSH_NOTIFICATION + Constants.URL_PULL_NOTIFICATION;

			mGetNotificationAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_NOTIFICATION,
					url, this, this, false);
			mGetNotificationAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	@Override
	public void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mProfilePictureUpdateBroadcastReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mProfileInfoUpdateBroadcastReceiver);
		super.onDestroy();
	}

	private void updateProfileData() {
		if (ProfileInfoCacheManager.getUserName() != null) {
            if (!ProfileInfoCacheManager.getUserName().equals("")) {
                mNameView.setText(ProfileInfoCacheManager.getUserName());
            }
        }
		mMobileNumberView.setText(ProfileInfoCacheManager.getMobileNumber());
		mProfileImageView.setAccountPhoto(Constants.BASE_URL_FTP_SERVER +
				ProfileInfoCacheManager.getProfileImageUrl(), false);
		if (mOptionMenuProfileImageView != null) {
			mOptionMenuProfileImageView.setAccountPhoto(Constants.BASE_URL_FTP_SERVER +
					ProfileInfoCacheManager.getProfileImageUrl(), false);
		} else {
			invalidateOptionsMenu();
		}
	}

	private void attemptRequestForPermission() {
		String[] requiredPermissions = {Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION};

		List<String> permissionsToRequest = new ArrayList<>();
		for (String permission : requiredPermissions) {
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
				permissionsToRequest.add(permission);
			}
		}

		if (!permissionsToRequest.isEmpty()) {
			ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
					REQUEST_CODE_PERMISSION);
		}
	}

	private void getAllBusinessAccountsList() {
		GetAllBusinessContactRequestBuilder mGetAllBusinessContactRequestBuilder = new GetAllBusinessContactRequestBuilder(0);
		new GetAllBusinessListAsyncTask(this, mGetAllBusinessContactRequestBuilder.getGeneratedUri()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		switch (requestCode) {
			case REQUEST_CODE_PERMISSION:
				for (int i = 0; i < permissions.length; i++) {
					Logger.logW(permissions[i], grantResults[i] + "");

					if (permissions[i].equals(Manifest.permission.READ_CONTACTS)) {
						if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
							if (ACLManager.hasServicesAccessibility(ServiceIdConstants.GET_CONTACTS))
								new GetContactsAsyncTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						}
					} else if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
						startLocationCollection();
					}
				}

				break;
		}
	}

	@ValidateAccess
	public void switchToDashBoard() {
		mNavigationView.getMenu().getItem(0).setChecked(true);
		getSupportFragmentManager().beginTransaction().replace(R.id.container, new DashBoardFragment()).commit();
		switchedToHomeFragment = true;
	}

	private void updateNotificationBadgeCount(int badgeCount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
		mBadgeCount = badgeCount;
		if (mOptionsMenu != null) {
			int totalBadge = badgeCount + SharedPrefManager.getNotificationCount();
			if (totalBadge > 0) {
				if(totalBadge>9) {
                    ActionItemBadge.update(this, mOptionsMenu.findItem(R.id.action_notification), getResources().getDrawable(R.drawable.ic_bell), ActionItemBadge.BadgeStyles.DARK_GREY,
                            String.valueOf(numberFormat.format(9)) + "+");
                }
				else
					ActionItemBadge.update(this, mOptionsMenu.findItem(R.id.action_notification), getResources().getDrawable(R.drawable.ic_bell), ActionItemBadge.BadgeStyles.DARK_GREY,
                            String.valueOf(numberFormat.format(totalBadge)));
			} else {
				ActionItemBadge.update(this, mOptionsMenu.findItem(R.id.action_notification), getResources().getDrawable(R.drawable.ic_bell), ActionItemBadge.BadgeStyles.DARK_GREY, null);
			}
		}
	}

	@ValidateAccess
	public void switchToManageBanksActivity() {
		Intent intent = new Intent(HomeActivity.this, ManageBanksActivity.class);
		startActivity(intent);
		switchedToHomeFragment = false;
	}

	@ValidateAccess
    public void switchToContactsActivity() {
        Intent intent = new Intent(HomeActivity.this, ContactsActivity.class);
        startActivity(intent);
        switchedToHomeFragment = false;
    }

    @ValidateAccess
	public void switchToActivityLogActivity() {
		Intent intent = new Intent(HomeActivity.this, ActivityLogActivity.class);
		startActivity(intent);
		switchedToHomeFragment = false;
	}

	@ValidateAccess
	public void switchToSecuritySettingsActivity() {
		Intent intent = new Intent(HomeActivity.this, SecuritySettingsActivity.class);
		startActivity(intent);
		switchedToHomeFragment = false;
	}

	@ValidateAccess
	public void switchToAboutActivity() {
		Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
		startActivity(intent);
		switchedToHomeFragment = false;
	}

	@ValidateAccess
	public void switchToHelpActivity() {
		Intent intent = new Intent(HomeActivity.this, HelpAndSupportActivity.class);
		startActivity(intent);
		switchedToHomeFragment = false;
	}

	@ValidateAccess
	public void switchToInviteActivity() {
		Intent intent = new Intent(HomeActivity.this, InviteFriendActivity.class);
		startActivity(intent);
		switchedToHomeFragment = false;
	}

	public void switchToIpaySourceOfFundActivity() {
        Intent intent = new Intent(HomeActivity.this, SourceOfFundActivity.class);
        startActivity(intent);
        switchedToHomeFragment = false;
    }

    public void showPromoCodeDialog() {

		AddPromoDialogBuilder addPromoDialogBuilder = new AddPromoDialogBuilder(HomeActivity.this, new AddPromoDialogBuilder.AddPromoListener() {
			@Override
			public void onPromoAddSuccess() {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						refreshBalance();
					}
				}, 1000);

			}
		});
		addPromoDialogBuilder.show();
	}

    @Override
    @ValidateAccess
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        int id = item.getItemId();
//         Handle navigation view item clicks here.

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				gotoDrawerItem(item);
			}
		}, 250);
		return true;
	}

	private void gotoDrawerItem(MenuItem item) {
		int id = item.getItemId();
		try {
			drawer.closeDrawer(GravityCompat.START);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (id == R.id.nav_account) {

			launchEditProfileActivity(ProfileCompletionPropertyConstants.PROFILE_INFO, new Bundle());
		} else if (id == R.id.nav_contact) {

			switchToContactsActivity();

		} else if (id == R.id.nav_user_activity) {
			switchToActivityLogActivity();

		} else if (id == R.id.nav_security_settings) {

			switchToSecuritySettingsActivity();

		} else if (id == R.id.nav_language) {

			final List<String> languageString = new ArrayList<>();
			languageString.add(getString(R.string.language_english));
			languageString.add(getString(R.string.language_bengali));

			final ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item);
			stringArrayAdapter.addAll(languageString);
			new AlertDialog.Builder(this)
					.setAdapter(stringArrayAdapter, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int position) {
							if (position < languageString.size()) {
								final String languageName = languageString.get(position);
								if (languageName.equals(getString(R.string.language_english))) {
									if (!Constants.APP_LANGUAGE_ENGLISH.equals(SharedPrefManager.getAppLanguage())) {
										SharedPrefManager.setAppLanguage(Constants.APP_LANGUAGE_ENGLISH);
									} else {
										return;
									}
								} else {
									if (!Constants.APP_LANGUAGE_BENGALI.equals(SharedPrefManager.getAppLanguage())) {
										SharedPrefManager.setAppLanguage(Constants.APP_LANGUAGE_BENGALI);
									} else {
										return;
									}
								}

								recreate();
							}
						}
					})
					.create()
					.show();

		} else if (id == R.id.nav_ipay_source_of_fund) {
            switchToIpaySourceOfFundActivity();

        } else if (id == R.id.nav_promo) {

			if (drawer.isDrawerOpen(GravityCompat.START)) {
				drawer.closeDrawer(GravityCompat.START);
			}

			showPromoCodeDialog();

        } else if (id == R.id.nav_help) {

			switchToHelpActivity();

		} else if (id == R.id.nav_about) {

			switchToAboutActivity();
			switchedToHomeFragment = false;

		} else if (id == R.id.nav_logout) {
			if (Utilities.isConnectionAvailable(HomeActivity.this)) {
				new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            FirebaseInstanceId.getInstance().deleteInstanceId();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                attemptFirebaseLogout();
            }
			} else {
				Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
			}
		}


	private boolean isProfileInfoAvailable() {
		return (ProfileInfoCacheManager.getAccountId() != Constants.INVALID_ACCOUNT_ID) &&
				(!ProfileInfoCacheManager.isBusinessAccount() || !TextUtils.isEmpty(ProfileInfoCacheManager.getUserName()));
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		} else if (!switchedToHomeFragment)
			switchToDashBoard();
		else {
			new AlertDialog.Builder(HomeActivity.this)
					.setMessage(R.string.are_you_sure_to_exit)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (SharedPrefManager.isRememberMeActive()) {
								finish();
							} else {
								if (Utilities.isConnectionAvailable(HomeActivity.this)) {
									exitFromApplication = true;
									attemptFirebaseLogout();
								} else {
									ProfileInfoCacheManager.setLoggedInStatus(false);
									((MyApplication) HomeActivity.this.getApplication()).clearTokenAndTimer();
									finish();
								}
							}
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// do nothing
						}
					})
					.show();
		}
	}

	@ValidateAccess
	private void launchEditProfileActivity(String type, Bundle bundle) {
		Intent intent = new Intent(this, ProfileActivity.class);
		intent.putExtra(Constants.TARGET_FRAGMENT, type);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	private void attemptLogout() {
		if (mLogoutTask != null) {
			return;
		}
		TokenManager.setOnAccountId(Constants.ON_ACCOUNT_ID_DEFAULT);
		try {
			LogoutRequest mLogoutModel = new LogoutRequest(Utilities.getMainUserInfoFromJsonString(ProfileInfoCacheManager.getMainUserProfileInfo()).getMobileNumber());
			Gson gson = new Gson();
			String json = gson.toJson(mLogoutModel);

			mLogoutTask = new HttpRequestPostAsyncTask(Constants.COMMAND_LOG_OUT,
					Constants.BASE_URL_MM + Constants.URL_LOG_OUT, json, HomeActivity.this, false);
			mLogoutTask.mHttpResponseListener = this;
			mLogoutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			mProgressDialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void removeRejectedEntriesForSponsors(ArrayList<Sponsor> sponsors) {
        mSponsorList = new ArrayList<>();
        mSponsorList.clear();
        for (int i = 0; i < sponsors.size(); i++) {
            if (!sponsors.get(i).getStatus().equals("REJECTED") && !sponsors.get(i).getStatus().equals("PENDING")) {
                mSponsorList.add(sponsors.get(i));
            }
        }


    }private void refreshBalance() {
		if (!ACLManager.hasServicesAccessibility(ServiceIdConstants.BALANCE)) {
			return;
		}
		if (mRefreshBalanceTask != null)
			return;

		mRefreshBalanceTask = new HttpRequestPostAsyncTask(Constants.COMMAND_REFRESH_BALANCE,
				Constants.BASE_URL_SM + Constants.URL_REFRESH_BALANCE, null, this, true);
		mRefreshBalanceTask.mHttpResponseListener = this;
		mRefreshBalanceTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void getProfileInfo() {
		if (mGetProfileInfoTask != null) {
			return;
		}

		mGetProfileInfoTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_PROFILE_INFO_REQUEST,
				Constants.BASE_URL_MM + Constants.URL_GET_PROFILE_INFO_REQUEST, HomeActivity.this, true);
		mGetProfileInfoTask.mHttpResponseListener = this;
		mGetProfileInfoTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void getBusinessInformation() {
		if (mGetBusinessInformationAsyncTask != null)
			return;

		mGetBusinessInformationAsyncTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_BUSINESS_INFORMATION,
				Constants.BASE_URL_MM + Constants.URL_GET_BUSINESS_INFORMATION, HomeActivity.this, this, true);
		mGetBusinessInformationAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void getAvailableBankList() {
		GetAvailableBankAsyncTask getAvailableBanksTask = new GetAvailableBankAsyncTask(this);
		getAvailableBanksTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void getAvailableBusinessTypes() {
		// Load business types, then extract the name of the business type from businessTypeId
		GetBusinessTypesAsyncTask mGetBusinessTypesAsyncTask = new GetBusinessTypesAsyncTask(this, new GetBusinessTypesAsyncTask.BusinessTypeLoadListener() {
			@Override
			public void onLoadSuccess(List<BusinessType> businessTypes) {

			}

			@Override
			public void onLoadFailed() {
			}
		});
		mGetBusinessTypesAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void getRelationshipList() {
		GetRelationshipListAsyncTask mGetRelationshipListAsyncTask = new GetRelationshipListAsyncTask(this, new GetRelationshipListAsyncTask.RelationshipLoadListener() {
			@Override
			public void onLoadSuccess(List<Relationship> relationshipList) {
			}

			@Override
			public void onLoadFailed() {

			}
		});
		mGetRelationshipListAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void getAccessControlList() {
		if (mGetAccessControlTask != null)
			return;

		mGetAccessControlTask = new HttpRequestGetAsyncTask(Constants.COMMAND_GET_ACCESS_CONTROL_LIST,
				Constants.BASE_URL_MM + Constants.URL_GET_ACCESS_CONTROL_LIST, HomeActivity.this, true);
		mGetAccessControlTask.mHttpResponseListener = this;
		mGetAccessControlTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	public void httpResponseReceiver(GenericHttpResponse result) {
		if (HttpErrorHandler.isErrorFound(result, this, mProgressDialog)) {
			mLogoutTask = null;
			mGetProfileInfoTask = null;
			mGetBusinessInformationAsyncTask = null;
			mLocationUpdateRequestAsyncTask = null;
			mGetNotificationAsyncTask = null;
			mRefreshTokenAsyncTask = null;
			firebaseLogoutTask = null;return;
		}
		mProgressDialog.dismiss();
		Gson gson = new Gson();

		switch (result.getApiCommand()) {
			case Constants.COMMAND_LOG_OUT:

				try {
					LogoutResponse mLogOutResponse = gson.fromJson(result.getJsonString(), LogoutResponse.class);

                    if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                        if (ProfileInfoCacheManager.isAccountSwitched()) {
                            ProfileInfoCacheManager.setAccountType(Utilities.getMainUserInfoFromJsonString(ProfileInfoCacheManager.getMainUserProfileInfo()).getAccountType());
                            ProfileInfoCacheManager.updateBusinessInfoCache(Constants.ACCOUNT_INFO_DEFAULT);
                            ProfileInfoCacheManager.saveMainUserBusinessInfo(Utilities.getMainBusinessProfileInfoString(Constants.ACCOUNT_INFO_DEFAULT));
                            ProfileInfoCacheManager.updateProfileInfoCache(Utilities.
                                    getMainUserInfoFromJsonString(ProfileInfoCacheManager.getMainUserProfileInfo()));
                            ProfileInfoCacheManager.setSwitchAccount(Constants.ACCOUNT_DEFAULT);
                            TokenManager.setOnAccountId(Constants.ON_ACCOUNT_ID_DEFAULT);
                            ProfileInfoCacheManager.setOnAccountId(Constants.ON_ACCOUNT_ID_DEFAULT);
                            ProfileInfoCacheManager.setId(Constants.ACCOUNT_ID_DEFAULT);
                        }
                        if (!exitFromApplication) {
                            ((MyApplication) this.getApplication()).launchLoginPage(null);
                        } else {
                            // Exit the application
                            ((MyApplication) this.getApplication()).clearTokenAndTimer();
                            finish();
                        }
                    } else {
                        if (ProfileInfoCacheManager.isAccountSwitched()) {
                            ((MyApplication) this.getApplication()).launchLoginPage(null);
                        }
                        Toast.makeText(HomeActivity.this, mLogOutResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }

				} catch (Exception e) {
					if (ProfileInfoCacheManager.isAccountSwitched()) {
						((MyApplication) this.getApplication()).launchLoginPage(null);
					}
					e.printStackTrace();
					Toast.makeText(HomeActivity.this, R.string.could_not_sign_out, Toast.LENGTH_LONG).show();
				}

				mProgressDialog.dismiss();
				mLogoutTask = null;

				break;
			case Constants.COMMAND_FIREBASE_LOGOUT:
                attemptLogout();
                firebaseLogoutTask = null;
                break;

            case Constants.COMMAND_GET_PROFILE_INFO_REQUEST:

				try {

					GetProfileInfoResponse mGetProfileInfoResponse = gson.fromJson(result.getJsonString(), GetProfileInfoResponse.class);
					if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

						if (!ProfileInfoCacheManager.isBusinessAccount()){
							if (!mGetProfileInfoResponse.getName().equals("")) {
                                mNameView.setText(mGetProfileInfoResponse.getName());
                            }
                        }

						String imageUrl = Utilities.getImage(mGetProfileInfoResponse.getProfilePictures(), Constants.IMAGE_QUALITY_HIGH);

						//saving user info in shared preference
						ProfileInfoCacheManager.updateProfileInfoCache(mGetProfileInfoResponse);
						if (!ProfileInfoCacheManager.isAccountSwitched()) {
							ProfileInfoCacheManager.saveMainUserProfileInfo(Utilities.getMainUserProfileInfoString(mGetProfileInfoResponse));
						}
						mProfileImageView.setAccountPhoto(Constants.BASE_URL_FTP_SERVER + imageUrl, false);
						mOptionMenuProfileImageView.setAccountPhoto(Constants.BASE_URL_FTP_SERVER + imageUrl, false);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Toaster.makeText(HomeActivity.this, R.string.profile_info_get_failed, Toast.LENGTH_SHORT);
				}

				mGetProfileInfoTask = null;

				break;

			case Constants.COMMAND_REFRESH_BALANCE:
				try {
					if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
						RefreshBalanceResponse mRefreshBalanceResponse = gson.fromJson(result.getJsonString(), RefreshBalanceResponse.class);
						String balance = mRefreshBalanceResponse.getBalance() + "";
						SharedPrefManager.setUserBalance(balance);
						Intent intent = new Intent();
						intent.setAction(Constants.BALANCE_UPDATE_BROADCAST);
						intent.putExtra(HomeFragment.BALANCE_KEY, mRefreshBalanceResponse.getBalance());
						sendBroadcast(intent);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				mRefreshBalanceTask = null;
				break;

			case Constants.COMMAND_GET_ACCESS_CONTROL_LIST:

				try {
					GetAccessControlResponse mGetAccessControlResponse = gson.fromJson(result.getJsonString(), GetAccessControlResponse.class);
					if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {

						// Saving the allowed services id for the user
						if (mGetAccessControlResponse.getAccessControlList() != null) {
							ACLManager.updateAllowedServiceArray(mGetAccessControlResponse.getAccessControlList());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				mGetAccessControlTask = null;

				break;
			case Constants.COMMAND_GET_NOTIFICATION:
				if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
					GetDeepLinkedNotificationResponse getDeepLinkedNotificationResponse = new Gson().
							fromJson(result.getJsonString(), GetDeepLinkedNotificationResponse.class);
					SharedPrefManager.setNotificationCount(getDeepLinkedNotificationResponse.getNotSeenCount());
					updateNotificationBadgeCount(mBadgeCount);
				}
				mGetNotificationAsyncTask = null;
				break;
			case Constants.COMMAND_REFRESH_FIREBASE_TOKEN:
				if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
					SharedPrefManager.setSentFireBaseToken(true);
				}
				mRefreshTokenAsyncTask = null;
				break;
			case Constants.COMMAND_GET_SPONSOR_LIST:
                if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                    GetSponsorListResponse getSponsorListResponse = new Gson().
                            fromJson(result.getJsonString(), GetSponsorListResponse.class);
                    removeRejectedEntriesForSponsors(getSponsorListResponse.getSponsor());
                }
                getSponsorListAsyncTask = null;
                break;
            case Constants.COMMAND_GET_BUSINESS_INFORMATION:
				try {
					GetBusinessInformationResponse mGetBusinessInformationResponse = gson.fromJson(result.getJsonString(), GetBusinessInformationResponse.class);

					if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
						mNameView.setText(mGetBusinessInformationResponse.getBusinessName());

						String imageUrl = Utilities.getImage(mGetBusinessInformationResponse.getProfilePictures(), Constants.IMAGE_QUALITY_HIGH);

						//saving user info in shared preference
						ProfileInfoCacheManager.updateBusinessInfoCache(mGetBusinessInformationResponse);
						ProfileInfoCacheManager.saveMainUserBusinessInfo(Utilities.getMainBusinessProfileInfoString(mGetBusinessInformationResponse));
						mProfileImageView.setAccountPhoto(Constants.BASE_URL_FTP_SERVER + imageUrl, false);
						mOptionMenuProfileImageView.setAccountPhoto(Constants.BASE_URL_FTP_SERVER + imageUrl, false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				mGetBusinessInformationAsyncTask = null;
				break;
			case Constants.COMMAND_POST_USER_LOCATION:
				mLocationUpdateRequestAsyncTask = null;
				break;
		}
	}

	private void attemptFirebaseLogout() {
        if (firebaseLogoutTask != null) {
            return;
        }
        String url = Constants.BASE_URL_PUSH_NOTIFICATION + Constants.URL_FIREBASE_LOGOUT;
        FcmLogoutRequest fcmLogoutRequest = new FcmLogoutRequest(FirebaseInstanceId.getInstance().getToken());
        firebaseLogoutTask = new HttpRequestPostAsyncTask(Constants.
                COMMAND_FIREBASE_LOGOUT, url,
                new Gson().toJson(fcmLogoutRequest), this, this, true);
        firebaseLogoutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			UserLocation userLocation = new UserLocation(location.getLatitude(), location.getLongitude());
			if (mLocationUpdateRequestAsyncTask == null) {
				try {
					sendUserLocation(Collections.singletonList(userLocation));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	private void sendUserLocation(List<UserLocation> userLocationList) {
		LocationCollector locationCollector = new LocationCollector();
		locationCollector.setDeviceId(DeviceInfoFactory.getDeviceId(this));
		locationCollector.setUuid(ProfileInfoCacheManager.getUUID());
		locationCollector.setMobileNumber(ProfileInfoCacheManager.getMobileNumber());
		locationCollector.setLocationList(userLocationList);
		String body = new GsonBuilder().create().toJson(locationCollector);
		mLocationUpdateRequestAsyncTask = new HttpRequestPostAsyncTask(Constants.COMMAND_POST_USER_LOCATION, Constants.BASE_URL_DATA_COLLECTOR + Constants.URL_ENDPOINT_LOCATION_COLLECTOR,
				body, this, this, true);
		mLocationUpdateRequestAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		mLocationManager.removeUpdates(this);
	}

	@Override
	public Context setContext() {
		return HomeActivity.this;
	}

	private final BroadcastReceiver mProfilePictureUpdateBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newProfilePicture = intent.getStringExtra(Constants.PROFILE_PICTURE);
			Logger.logD("Broadcast home activity", newProfilePicture);

			mProfileImageView.setAccountPhoto(newProfilePicture, true);
			mOptionMenuProfileImageView.setAccountPhoto(newProfilePicture, true);

			// We need to update the profile picture url in ProfileInfoCacheManager. Ideally,
			// we should have received a push from the server and FcmListenerService should have
			// done this task. But as long as push is unreliable, this call is here to stay.
			updateProfileInfo();
		}
	};

	private final BroadcastReceiver mProfileInfoUpdateBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateProfileData();
		}
	};

}