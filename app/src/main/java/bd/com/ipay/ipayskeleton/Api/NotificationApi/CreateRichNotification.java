package bd.com.ipay.ipayskeleton.Api.NotificationApi;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.math.BigDecimal;

import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.SentReceivedRequestReviewActivity;
import bd.com.ipay.ipayskeleton.Activities.PaymentActivities.TransactionDetailsActivity;
import bd.com.ipay.ipayskeleton.Api.GenericApi.HttpRequestPostAsyncTask;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.GenericHttpResponse;
import bd.com.ipay.ipayskeleton.Api.HttpResponse.HttpResponseListener;
import bd.com.ipay.ipayskeleton.CustomView.Dialogs.AnimatedProgressDialog;
import bd.com.ipay.ipayskeleton.HttpErrorHandler;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.GenericResponseWithMessageOnly;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.RequestMoney.RequestMoneyAcceptRejectOrCancelRequest;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory.TransactionHistory;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;
import bd.com.ipay.ipayskeleton.Utilities.Constants;
import bd.com.ipay.ipayskeleton.Utilities.ContactEngine;
import bd.com.ipay.ipayskeleton.Utilities.ContactSearchHelper;
import bd.com.ipay.ipayskeleton.Utilities.Utilities;

public class CreateRichNotification {
    private static final String CHANNEL_ID_DEFAULT = "ipay_notification_channel";
    private static TransactionHistory transactionHistory;
    private static Context context;
    private String type;
    private String title;
    private AnimatedProgressDialog mProgressDialog;
    private Bitmap imageBitmap;

    private static boolean isLoggedIn;
    private static HttpRequestPostAsyncTask mRejectRequestTask = null;
    private final int REQUEST_MONEY_REVIEW_REQUEST = 101;


    public CreateRichNotification(TransactionHistory transactionHistory, Context context, String type, String title) {
        this.transactionHistory = transactionHistory;
        this.context = context;
        this.type = type;
        this.title = title;
    }

    private void setBitmap(String url, final RemoteViews remoteView, final int viewId) {
        final int size = context.getResources().getDimensionPixelSize(R.dimen.value48);
        final Bitmap profilePictureBitmap;
        Bitmap newBitmap = null;
        RoundedBitmapDrawable roundedBitmapDrawable;
        Bitmap defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_profile);

        try {
            if (url != null && !url.contains("ipay.com")) {
                url = Constants.BASE_URL_FTP_SERVER + url;
            }
            profilePictureBitmap = Glide.with(context)
                    .load(url)
                    .asBitmap()
                    .into(size, size).get();
            if (imageBitmap == null) {
                imageBitmap = getCircleBitmap(profilePictureBitmap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (imageBitmap != null) {
            remoteView.setImageViewBitmap(viewId, imageBitmap);
        } else {
            remoteView.setImageViewBitmap(viewId, defaultBitmap);
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    public void setupNotification() {

        isLoggedIn = ProfileInfoCacheManager.getLoggedInStatus(false);

        PendingIntent pendingIntent = getNotificationPendingIntent();
        RemoteViews transactionNotificationView = new RemoteViews
                (context.getPackageName(), R.layout.list_item_transaction_history_notification);
        RemoteViews transactionNotificationViewCollapsed = new RemoteViews
                (context.getPackageName(), R.layout.list_item_ta_notification_collapsed_view);
        transactionNotificationViewCollapsed.setTextViewText(R.id.title, title);

        if (type.equals(Constants.transaction)) {
            transactionNotificationView.setViewVisibility(R.id.button_layout, View.GONE);
        } else {
            transactionNotificationView.setViewVisibility(R.id.button_layout, View.VISIBLE);
        }

        Intent acceptIntent = new Intent(context, NotificationButtonClickHandler.class);
        acceptIntent.putExtra(Constants.ACTION, Constants.ACCEPTED);

        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(context,
                1, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent rejectIntent = new Intent(context, NotificationButtonClickHandler.class);
        rejectIntent.putExtra(Constants.ACTION, Constants.REJECTED);

        PendingIntent rejectPendingIntent = PendingIntent.getBroadcast(context, 2,
                rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        transactionNotificationView.setOnClickPendingIntent(R.id.accept, acceptPendingIntent);
        transactionNotificationView.setOnClickPendingIntent(R.id.reject, rejectPendingIntent);

        fillUpViewsWithNecessaryData(transactionNotificationView);
        initiateNotificationAction(pendingIntent, transactionNotificationView, transactionNotificationViewCollapsed);
    }

    public void initiateNotificationAction(PendingIntent pendingIntent, RemoteViews transactionNotificationView, RemoteViews collapsedView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = context.getPackageName();
            CharSequence name = "ipay";
            String description = context.getPackageCodePath();
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_ipay_verifiedmember)
                    .setContentText(title)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setContentTitle(title)
                    .setCustomContentView(collapsedView)
                    .setCustomBigContentView(transactionNotificationView);
            notificationManager.notify(transactionHistory.getTransactionID().hashCode(), mBuilder.build());
        } else {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID_DEFAULT)
                    .setSmallIcon(R.drawable.ic_ipay_verifiedmember)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setContentText(title)
                    .setCustomContentView(collapsedView)
                    .setCustomBigContentView(transactionNotificationView);
            notificationManager.notify(transactionHistory.getTransactionID().hashCode(), mBuilder.build());
        }
    }

    public void fillUpViewsWithNecessaryData(RemoteViews transactionNotificationView) {
        final String description = transactionHistory.getShortDescription();
        final String receiver = transactionHistory.getReceiver();
        String responseTime = Utilities.formatDayMonthYear(transactionHistory.getTime());
        final Integer statusCode = transactionHistory.getStatusCode();
        final Double balance = transactionHistory.getAccountBalance();
        final String outletName = transactionHistory.getOutletName();

        transactionNotificationView.setTextViewText(R.id.title, title);

        if (this.type.equals("transaction")) {
            if (balance != null) {
                transactionNotificationView.setTextViewText(R.id.amount, Utilities.formatTakaWithComma(balance));
            }

            transactionNotificationView.setTextViewText(R.id.net_amount, String.valueOf(
                    Utilities.formatTakaFromString(transactionHistory.getNetAmountFormatted())));
        } else {
            transactionNotificationView.setTextViewText(R.id.net_amount, String.valueOf(
                    Utilities.formatTakaFromString(Double.toString(transactionHistory.getNetAmount()))));
        }

        switch (statusCode) {
            case Constants.TRANSACTION_STATUS_ACCEPTED: {
                transactionNotificationView.setImageViewResource
                        (R.id.status_description_icon, R.drawable.transaction_tick_sign);
                break;
            }
            case Constants.TRANSACTION_STATUS_CANCELLED: {
                transactionNotificationView.setImageViewResource
                        (R.id.status_description_icon, R.drawable.transaction_cross_sign);
                break;
            }
            case Constants.TRANSACTION_STATUS_REJECTED: {

                transactionNotificationView.setImageViewResource
                        (R.id.status_description_icon, R.drawable.transaction_cross_sign);
                break;
            }
            case Constants.TRANSACTION_STATUS_FAILED: {
                transactionNotificationView.setImageViewResource
                        (R.id.status_description_icon, R.drawable.transaction_cross_sign);
                break;
            }
        }

        transactionNotificationView.setTextViewText(R.id.activity_description, description);

        if (receiver != null && !receiver.equals("")) {
            transactionNotificationView.setViewVisibility(R.id.receiver, View.VISIBLE);
            if (!TextUtils.isEmpty(outletName)) {
                transactionNotificationView.setTextViewText(R.id.receiver, receiver + " (" + outletName + ")");
            } else {
                transactionNotificationView.setTextViewText(R.id.receiver, receiver);
            }
        } else transactionNotificationView.setViewVisibility(R.id.receiver, View.GONE);

        if (DateUtils.isToday(transactionHistory.getTime())) {
            responseTime = "Today, " + Utilities.formatTimeOnly(transactionHistory.getTime());
        }
        transactionNotificationView.setTextViewText(R.id.time, responseTime);

        if (transactionHistory.getAdditionalInfo().getType().equalsIgnoreCase(Constants.TRANSACTION_TYPE_USER)) {
            String imageUrl = transactionHistory.getAdditionalInfo().getUserProfilePic();

            transactionNotificationView.setViewVisibility(R.id.other_image, View.INVISIBLE);
            transactionNotificationView.setViewVisibility(R.id.profile_picture, View.VISIBLE);

            setBitmap(Constants.BASE_URL_FTP_SERVER + imageUrl,
                    transactionNotificationView, R.id.profile_picture);
        } else {
            int iconId = transactionHistory.getAdditionalInfo().getImageWithType(context);
            transactionNotificationView.setViewVisibility(R.id.other_image, View.VISIBLE);
            transactionNotificationView.setViewVisibility(R.id.profile_picture, View.INVISIBLE);
            transactionNotificationView.setImageViewResource(R.id.other_image, iconId);
        }
    }

    private PendingIntent getNotificationPendingIntent() {
        PendingIntent pendingIntent;
        if (type.equals("transaction")) {
            Intent intent = new Intent(context, TransactionDetailsActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.putExtra(Constants.TRANSACTION_DETAILS, transactionHistory);
            pendingIntent = PendingIntent.getActivity(context, 0,
                    intent, PendingIntent.FLAG_ONE_SHOT);
        } else {
            Intent intent = launchRequestMoneyReviewPageIntent(transactionHistory, false, isLoggedIn, context);
            pendingIntent = PendingIntent.getActivity(context, 0,
                    intent, PendingIntent.FLAG_ONE_SHOT);
        }
        return pendingIntent;
    }

    public static class NotificationButtonClickHandler extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(Constants.ACTION).equals(Constants.REJECTED)) {
                rejectMoneyRequest();
            } else {
                Intent reviewPageIntent = launchRequestMoneyReviewPageIntent
                        (transactionHistory, true, isLoggedIn, context);
                reviewPageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(reviewPageIntent);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancel(transactionHistory.getTransactionID().hashCode());
            }
        }
    }

    public static void rejectMoneyRequest() {
        if (mRejectRequestTask != null) {
            return;
        }

        RequestMoneyAcceptRejectOrCancelRequest
                mRequestMoneyAcceptRejectOrCancelRequest = new RequestMoneyAcceptRejectOrCancelRequest(transactionHistory.getTransactionID());

        Gson gson = new Gson();
        String json = gson.toJson(mRequestMoneyAcceptRejectOrCancelRequest);
        mRejectRequestTask = new HttpRequestPostAsyncTask(Constants.COMMAND_REJECT_REQUESTS_MONEY,
                Constants.BASE_URL_SM + Constants.URL_REJECT_NOTIFICATION_REQUEST, json, context, false);
        mRejectRequestTask.mHttpResponseListener = new HttpResponseListener() {
            @Override
            public void httpResponseReceiver(GenericHttpResponse result) {
                if (HttpErrorHandler.isErrorFound(result, context, null)) {
                    mRejectRequestTask = null;
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.cancel(transactionHistory.getTransactionID().hashCode());
                    return;
                } else {
                    GenericResponseWithMessageOnly genericResponseWithMessageOnly = new Gson().
                            fromJson(result.getJsonString(), GenericResponseWithMessageOnly.class);
                    Toast.makeText(context, genericResponseWithMessageOnly.getMessage(), Toast.LENGTH_LONG).show();
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.cancel(transactionHistory.getTransactionID().hashCode());
                    mRejectRequestTask = null;
                }
            }
        };
        mRejectRequestTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static Intent launchRequestMoneyReviewPageIntent(TransactionHistory transactionHistory, boolean isAccepted, boolean isLoggedIn, Context context) {
        Intent intent = new Intent(context, SentReceivedRequestReviewActivity.class);
        intent.putExtra(Constants.AMOUNT, new BigDecimal(transactionHistory.getAmount()));
        intent.putExtra(Constants.RECEIVER_MOBILE_NUMBER,
                ContactEngine.formatMobileNumberBD(transactionHistory.getAdditionalInfo().getNumber()));

        intent.putExtra(Constants.DESCRIPTION_TAG, transactionHistory.getPurpose());
        intent.putExtra(Constants.ACTION_FROM_NOTIFICATION, false);
        intent.putExtra(Constants.TRANSACTION_ID, transactionHistory.getTransactionID());
        intent.putExtra(Constants.NAME, transactionHistory.getReceiver());
        intent.putExtra(Constants.PHOTO_URI, Constants.BASE_URL_FTP_SERVER + transactionHistory.getAdditionalInfo().getUserProfilePic());
        intent.putExtra(Constants.SWITCHED_FROM_TRANSACTION_HISTORY, true);
        intent.putExtra(Constants.IS_IN_CONTACTS,
                new ContactSearchHelper(context).searchMobileNumber(transactionHistory.getAdditionalInfo().getNumber()));

        if (transactionHistory.getType().equalsIgnoreCase(Constants.TRANSACTION_TYPE_CREDIT)) {
            intent.putExtra(Constants.REQUEST_TYPE, Constants.REQUEST_TYPE_SENT_REQUEST);
        }
        return intent;
    }
}
