package com.lukeshaun.mobileca1.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.lukeshaun.mobileca1.MapsActivity;
import com.lukeshaun.mobileca1.R;

import java.util.Random;


public class NotificationService extends IntentService {

    private final String TAG = "DEBUG " + this.getClass().getSimpleName();

    private static final String NOTIFICATION_ID = "com.lukeshaun.mobileca1.service.notificationservice";

    public NotificationService() {super(NOTIFICATION_ID);}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        sendNotification(title, message);
    }

    /**
     * Send a system wide notification to appear
     * @param title title to appear on notification
     * @param message message to appear on notification
     */
    private void sendNotification(String title, String message) {

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
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentInfo("Info");

        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }
}
