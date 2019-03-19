package bd.com.ipay.ipayskeleton.Api.NotificationApi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.util.Random;

import bd.com.ipay.ipayskeleton.Activities.RichNotificationDetailsActivity;
import bd.com.ipay.ipayskeleton.R;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class CreateOtherTypeRichNotification {
    private String description;
    private String title;
    private String imageUrl;
    private Context context;
    private String deepLink;
    private Bitmap bigPictureBitmap;
    private static final String CHANNEL_ID_DEFAULT = "ipay_notification_channel";

    public CreateOtherTypeRichNotification(Context context, String description, String title, String imageUrl, String deepLink) {
        this.context = context;
        this.description = description;
        this.title = title;
        this.imageUrl = imageUrl;
        this.deepLink = deepLink;
    }

    public void setUpRichNotification() {
        Intent intent = new Intent(context, RichNotificationDetailsActivity.class);
        intent.putExtra(Constants.IMAGE_URL, imageUrl);
        intent.putExtra(Constants.DEEP_LINK, deepLink);
        intent.putExtra(Constants.TITLE, title);
        intent.putExtra(Constants.DESCRIPTION, description);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, intent, PendingIntent.FLAG_ONE_SHOT);
        initiateNotificationAction(pendingIntent);
    }

    private void initiateNotificationAction(final PendingIntent pendingIntent) {
        try {
            bigPictureBitmap = BitmapFactory.decodeFile(Glide.with(context)
                    .load(Constants.BASE_URL_FTP_SERVER+imageUrl)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get().getPath());
        } catch (Exception e) {
            e.printStackTrace();

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String CHANNEL_ID = context.getPackageName();
            CharSequence name = "ipay";
            String description = context.getPackageCodePath();
            int importance = NotificationManager.IMPORTANCE_HIGH;
            final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_ipay_verifiedmember)
                    .setContentIntent(pendingIntent)
                    .setColor(context.getResources().getColor(R.color.colorPrimary))
                    .setAutoCancel(true)
                    .setContentTitle("iPay")
                    .setContentText(title)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .setSummaryText(title)
                            .bigPicture(bigPictureBitmap));
            notificationManager.notify(new Random().nextInt(), mBuilder.build());

        } else {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID_DEFAULT)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.ic_ipay_verifiedmember)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setContentTitle("iPay")
                    .setContentText(title)
                    .setColor(context.getResources().getColor(R.color.colorPrimary))
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .setSummaryText(title)
                            .bigPicture(bigPictureBitmap));
            notificationManager.notify(new Random().nextInt(), mBuilder.build());
        }
    }
}