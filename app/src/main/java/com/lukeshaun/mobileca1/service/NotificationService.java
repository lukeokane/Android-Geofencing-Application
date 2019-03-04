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

        // User will be navigated to MapActivity when they tap on the notification
        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        // Get system's notification manager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //
        String NOTIFICATION_CHANNEL_ID = "com.mobiledev.oc.whatshappening.notification";

        // If SDK version is Oreo or greater, use NotificationChannel to style notifications.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification",
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
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        // Modify text and view of notification
        notificationBuilder
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
