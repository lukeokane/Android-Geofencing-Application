package com.lukeshaun.mobileca1.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.lukeshaun.mobileca1.MapsActivity;
import com.lukeshaun.mobileca1.R;

import java.util.Random;


public class NotificationService extends Service {

    private final String TAG = "DEBUG " + this.getClass().getSimpleName();

    private static final String NOTIFICATION_ID = "com.lukeshaun.mobileca1.service.notificationservice";

    private final IBinder mBinder = new NotificationBinder();

    public NotificationService() {}

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Send a system wide notification to appear
     * @param title title to appear on notification
     * @param message message to appear on notification
     */
    public void sendNotification(String title, String message) {

        Log.d(TAG, "Sending notification with title '" + title + "'");

        // User will be navigated to MapActivity when they tap on the notification
        Intent intent = new Intent(this, MapsActivity.class);

        // Set this flag so the notification will re-open the MapsActivity's current instance.
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // Set PendingIntent flag as FLAG_ONE_SHOT to prevent the notification creating a new MapActivity instance.
        // So the user will be directed to the current instance of MapActivity only.
        // Once this notification is clicked, the PendingIntent will fail since it is set to only be called once.
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        // Get system's notification manager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // If SDK version is Oreo or greater, use NotificationChannel to style notifications.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_ID, "Notification",
                    NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setDescription("");
            notificationChannel.enableLights(true);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableLights(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        // Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_ID);

        // Modify text and view of notification
        notificationBuilder
                .setChannelId(NOTIFICATION_ID)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setContentText(message)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setColor(Color.BLUE)
                .setSmallIcon(R.drawable.notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentInfo("Info");

        notificationManager.notify(0, notificationBuilder.build());
    }

    public class NotificationBinder extends Binder {
        public NotificationService getService() { return NotificationService.this; }
    }
}
