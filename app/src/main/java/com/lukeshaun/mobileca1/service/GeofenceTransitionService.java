package com.lukeshaun.mobileca1.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.lukeshaun.mobileca1.MapsActivity;
import com.lukeshaun.mobileca1.R;

import java.util.List;

public class GeofenceTransitionService extends Service {

    private final String TAG = "DEBUG " + this.getClass().getSimpleName();



    public GeofenceTransitionService() {}

    /**
     * Make the Service a Foreground Service to keep tracking the device...
     * and recording activity while the app is not in the foreground
     */
    @Override
    public void onCreate() {
        super.onCreate();

        Intent notificationIntent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "com.lukeshaun.mobileca1";
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(2, notification);
        }
        else {
            startForeground(1, new Notification());
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // An intent to commence the service is passed in.
        // Intents from GeofencingClient will have an extra to identify itself.
        if(intent.hasExtra("GEOFENCE_REQUEST")) {
            onGeofencingIntent(intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Not needed since we have no BoundService objects binding to this service.
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Receives intents called by the GeofencingClient.
     * This method occurs when a geofence transition has happened.
     * The method handles these transition events.
     * @param intent
     */
    protected void onGeofencingIntent(Intent intent) {
        // Retrieve the Geofence event passed in
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) {
            // Log the error
            Log.e("ERROR", "Geofence event returned with error code " + event.getErrorCode());
        }
        else {
            // Get the transition type
            int transition = event.getGeofenceTransition();

            // Retrieve the triggering geo fences, get the first element.
            List<Geofence> geofences = event.getTriggeringGeofences();
            Geofence geofence = geofences.get(0);

            // Broadcast transition
            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                broadcastGeofenceTransitionEvent(geofence, Geofence.GEOFENCE_TRANSITION_ENTER);
            }
            else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                broadcastGeofenceTransitionEvent(geofence, Geofence.GEOFENCE_TRANSITION_EXIT);
            }
        }
    }

    /**
     * Broadcast the change for acitivities to update their UI
     * @param geofence the geofence that the transition occurred in
     * @param transition the transition type
     */
    private void broadcastGeofenceTransitionEvent(Geofence geofence, int transition) {
        Log.d(TAG, "Broadcasting geofence transition event, transition ID: " + transition + ", geofence: " + geofence.getRequestId());
        // Create intent to pass into broadcast manager
        Intent intent = new Intent("GeofenceTransitionEvent");

        // Pass Geofence and it's transition in the broadcast
        Bundle bundle = new Bundle();
        bundle.putParcelable("Geofence", (Parcelable) geofence);
        intent.putExtra("Geofence", bundle);
        intent.putExtra("GeofenceTransition", transition);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
