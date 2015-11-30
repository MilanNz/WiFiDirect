package com.wifidirect.milan.wifidirect.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.wifidirect.milan.wifidirect.R;
import com.wifidirect.milan.wifidirect.activities.MainActivity;

/**
 * Created by milan on 30.11.15..
 */
public class WifiNotification {
    /** Notification id. */
    private static final int NOTIFICATION_ID = 112;
    private static NotificationCompat.Builder mBuilder;
    private static NotificationManager mNotificationManager;


    /** Create new message notification.
     * @param context Context
     * @param message String message */
    public static void createNotification(Context context, String message) {
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.ic_message_white_24dp);
        mBuilder.setContentTitle("New Message");
        mBuilder.setContentText(message);

        Intent resultIntent = new Intent(context, MainActivity.class);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0
                , PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        mNotificationManager = (NotificationManager)context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /** Remove message notification. */
    public static void removeNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }


}
