package bd.com.ipay.ipayskeleton.Service.FCM;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.Map;
import java.util.Random;

import bd.com.ipay.ipayskeleton.Activities.UtilityBillPayActivities.IPayUtilityBillPayActionActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.Api.NotificationApi.CreateCustomNotificationAsyncTask;
import bd.com.ipay.ipayskeleton.Api.NotificationApi.CreateOtherTypeRichNotification;
import bd.com.ipay.ipayskeleton.Api.NotificationApi.CreateRichNotification;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Notification.FCMNotificationResponse;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RefreshToken.FCMRefreshTokenRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionHistory;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefConstants;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.SharedPrefManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.DeviceInfoFactory;
import bd.com.ipay.ipayskeleton.Utilities.ToasterAndLogger.Logger;

public class FCMListenerService extends FirebaseMessagingService implements HttpResponseListener {
    private FCMNotificationResponse mFcmNotificationResponse;
    private RemoteMessage.Notification notification;

    private Map data;
    private String from;
    private int serviceId;

    private HttpRequestPostAsyncTask mRefreshTokenAsyncTask;

    private SharedPreferences pref;

    private String firebaseToken;

    private String requestAction;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        firebaseToken = s;
        boolean isLoggedIn = ProfileInfoCacheManager.getLoggedInStatus(false);
        if (isLoggedIn) {
            sendFireBaseTokenToServer();
        } else {
            pref = this.getSharedPreferences(Constants.ApplicationTag, Activity.MODE_PRIVATE);
            pref.edit().putString(SharedPrefConstants.PUSH_NOTIFICATION_TOKEN, s).apply();
        }

    }

    private void sendFireBaseTokenToServer() {
        String fireBaseToken = FirebaseInstanceId.getInstance().getToken();
        Logger.logW("Firebase Token", "Refresh token called");

        if (mRefreshTokenAsyncTask == null) {
            return;
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

    @Override
    public void onMessageReceived(RemoteMessage message) {
        parseRemoteMessage(message);
    }

    private void parseRemoteMessage(RemoteMessage message) {

        from = message.getFrom();
        data = message.getData();

        Logger.logD("Message", "From: " + from);

        // Check if message contains a data payload.
        if (data.size() > 0) {
            Logger.logD("Data", "Message data payload: " + data.toString());
            setNotificationResponseFromData(data);
        }

        serviceId = FCMNotificationParser.parseServiceID(mFcmNotificationResponse);

        // Check if message contains a notification payload.

        if (data != null) {
            requestAction = (String) data.get("click_action");
            String title = (String) data.get("title");
            String body = (String) data.get("body");
            String imageUrl = (String) data.get("icon");
            CreateRichNotification createRichNotification;

            if (requestAction != null) {
                switch (requestAction) {
                    case Constants.transaction:
                        createRichNotification = new CreateRichNotification
                                (mFcmNotificationResponse.getTransactionHistory(), this,
                                        Constants.transaction, title);
                        createRichNotification.setupNotification();
                        break;
                    case Constants.request_money:
                        createRichNotification = new CreateRichNotification
                                (mFcmNotificationResponse.getTransactionHistory(), this,
                                        Constants.request_money, title);
                        createRichNotification.setupNotification();
                        break;
                    case Constants.other:
                        String meta = (String) data.get("meta");
                        NotificationMetaData nMetaData = new Gson().fromJson(meta, NotificationMetaData.class);
                        String description = nMetaData.getDescription();
                        String image = nMetaData.getImageUrl();
                        String deepLink = (String) data.get("deepLink");
                        CreateOtherTypeRichNotification createOtherTypeRichNotification =
                                new CreateOtherTypeRichNotification(this, description, title, image, deepLink);
                        createOtherTypeRichNotification.setUpRichNotification();
                        break;

                }
            } else {
                String deeplink = (String)data.get("deepLink");
                if (deeplink != null) {
                    if (deeplink.contains("schedulePayment")) {
                        createScheduledPaymentNotification(deeplink, title, body);
                    } else {
                        try {
                            createNotification(this, title, body, imageUrl);
                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }

                } else {
                    try {
                        createNotification(this, title, body, imageUrl);
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }
            }
        }
        FCMNotificationParser.parseInAppNotification(this, mFcmNotificationResponse);

    }

    private void createScheduledPaymentNotification(String deeplink, String title, String body) {
        String id = Uri.parse(deeplink).getLastPathSegment();
        Intent intent = new Intent(this, IPayUtilityBillPayActionActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra("id", id);
        intent.putExtra(Constants.ACTION_FROM_NOTIFICATION, true);
        int notificationID = new Random().nextInt();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationID, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        try {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setPriority(android.app.Notification.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(this.getResources().getColor(R.color.colorPrimary))
                    .setContentTitle(title)
                    .setGroup("Notifications")
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);
            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationID, notificationBuilder.build());
        } catch (Exception e) {

        }
    }

    private void setNotificationResponseFromData(Map data) {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(data);
        mFcmNotificationResponse = gson.fromJson(jsonElement, FCMNotificationResponse.class);
        String transactionDetailsString = mFcmNotificationResponse.getTransactionActivity();
        TransactionHistory transactionHistory = new Gson().
                fromJson(transactionDetailsString, TransactionHistory.class);
        mFcmNotificationResponse.setTransactionHistory(transactionHistory);
    }

    private void createNotification(Context context, String title, String message, String imageUrl) {
        if (serviceId == Constants.SERVICE_ID_DEEP_LINK_NOTIFICATION) {
            new CreateCustomNotificationAsyncTask(context, title,
                    message, imageUrl, mFcmNotificationResponse.getDeepLink(), mFcmNotificationResponse.getTime()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new CreateCustomNotificationAsyncTask(context, title,
                    message, imageUrl).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void httpResponseReceiver(GenericHttpResponse result) {
        if (HttpErrorHandler.isErrorFound(result, this, null)) {
            mRefreshTokenAsyncTask = null;
            return;
        } else {
            mRefreshTokenAsyncTask = null;
            if (result.getStatus() == Constants.HTTP_RESPONSE_STATUS_OK) {
                pref.edit().putString(SharedPrefConstants.PUSH_NOTIFICATION_TOKEN, null).apply();
                SharedPrefManager.setSentFireBaseToken(true);
            } else {
                pref.edit().putString(SharedPrefConstants.PUSH_NOTIFICATION_TOKEN, firebaseToken).apply();
                SharedPrefManager.setSentFireBaseToken(false);
            }
        }
    }
}