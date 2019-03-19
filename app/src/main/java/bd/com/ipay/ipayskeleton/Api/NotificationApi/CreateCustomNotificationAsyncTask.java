package bd.com.ipay.ipayskeleton.Api.NotificationApi;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import bd.com.ipay.ipayskeleton.Activities.HomeActivity;
import bd.com.ipay.ipayskeleton.Activities.LauncherActivity;
import bd.com.ipay.ipayskeleton.Activities.SignupOrLoginActivity;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.CacheManager.ProfileInfoCacheManager;

public class CreateCustomNotificationAsyncTask extends AsyncTask<String, Void, Bitmap> {

    private Context mContext;
    private String title, message, imageUrl, deepLink;
    private long time;

    public CreateCustomNotificationAsyncTask(Context context, String title, String message, String imageUrl) {
        this.mContext = context;
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
    }

    public CreateCustomNotificationAsyncTask(Context mContext, String title, String message, String imageUrl, String deepLink) {
        this.mContext = mContext;
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
        this.deepLink = deepLink;
    }

    public CreateCustomNotificationAsyncTask(Context mContext, String title, String message, String imageUrl, String deepLink, long time) {
        this.mContext = mContext;
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
        this.deepLink = deepLink;
        this.time = time;
    }

    @Override
    protected Bitmap doInBackground(String... params) {

        InputStream in;
        try {
            URL url = new URL(this.imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            in = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(in);
            return myBitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onPostExecute(Bitmap result) {

        int notificationID = new Random().nextInt();
        Intent intent = null;
        if (deepLink != null) {
            if (!deepLink.isEmpty()) {
                intent = new Intent(mContext, LauncherActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(deepLink));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("time", this.time);
            }
        } else {
            if (ProfileInfoCacheManager.getLoggedInStatus(false)) {
                intent = new Intent(mContext, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            } else {
                intent = new Intent(mContext, SignupOrLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
        }
        if (intent != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notificationID, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            try {
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                        .setPriority(android.app.Notification.PRIORITY_HIGH)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(mContext.getResources().getColor(R.color.colorPrimary))
                        .setContentTitle(title)
                        .setGroup("Notifications")
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
                if (Build.VERSION.SDK_INT >= 21) {
                    notificationBuilder.setVibrate(new long[0]);
                }

                if (result != null) {
                    notificationBuilder.setLargeIcon(result);
                }

                NotificationManager notificationManager =
                        (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(notificationID, notificationBuilder.build());
            } catch (Exception e)

            {
                e.printStackTrace();
            }
        }

    }
}
