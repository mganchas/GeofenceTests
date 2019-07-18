package com.example.x190629.testes_geofence.services.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.x190629.testes_geofence.R;

/**
 * Created by X190629 on 16/07/2019.
 */

public class NotificationService {
    private static final String TAG = NotificationService.class.getSimpleName();

    public static void cancelAll(@NonNull Context context) {
        Log.d(TAG, "cancelAll()");

        NotificationManager notif = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notif != null) {
            notif.cancelAll();
        }
    }

    public static void sendNotification(@NonNull Context context, @NonNull String subText, @NonNull String subject, @NonNull String body, int icon)
    {
        Log.d(TAG, "sendNotification(): \n" +
                "body = " + body + "\n" +
                "subject = " + subject + "\n" +
                "subText = " + subText + "\n");

        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentTitle(subject)
                .setSubText(subText)
                .setContentText(body)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(Notification.CATEGORY_MESSAGE);
        }

        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }
}
