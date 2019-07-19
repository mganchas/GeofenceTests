package com.example.x190629.testes_geofence.services.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.x190629.testes_geofence.MainActivity;
import com.example.x190629.testes_geofence.R;

import static android.provider.Settings.Global.getString;

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

    public static void sendNotification(@NonNull Context context, @NonNull String subText, @NonNull String subject, @NonNull String body, int icon) {
        Log.d(TAG, "sendNotification(): \n" +
                "body = " + body + "\n" +
                "subject = " + subject + "\n" +
                "subText = " + subText + "\n");

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String channel = "location_worker_channel_01"; // The id of the channel.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channel);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            /* Create or update. */
            NotificationChannel notificationChannel = new NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            notificationBuilder
                    .setContentTitle(subject)  // We still need this because on old android versions bigtextstyle displays nothing
                    .setContentText(body)      // We still need this because on old android versions bigtextstyle displays nothing
                    .setSmallIcon(icon)
                    .setChannelId(channel)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSound(soundUri)
                    .setAutoCancel(true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            {
                notificationBuilder
                        .setContentTitle(subject)  // We still need this because on old android versions bigtextstyle displays nothing
                        .setContentText(body)      // We still need this because on old android versions bigtextstyle displays nothing
                        .setSmallIcon(icon)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_all_out_black_24dp))
                        .setChannelId(channel)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSound(soundUri)
                        .setAutoCancel(true);
            } else {
                notificationBuilder
                        .setContentTitle(subject)  // We still need this because on old android versions bigtextstyle displays nothing
                        .setContentText(body)      // We still need this because on old android versions bigtextstyle displays nothing
                        .setSmallIcon(icon)
                        .setChannelId(channel)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSound(soundUri)
                        .setAutoCancel(true);
            }
        }

        if (notificationManager != null) {
            Log.d(TAG, "A enviar");
            notificationManager.notify(String.valueOf(System.currentTimeMillis()), 1, notificationBuilder.build());
        }
    }
}
