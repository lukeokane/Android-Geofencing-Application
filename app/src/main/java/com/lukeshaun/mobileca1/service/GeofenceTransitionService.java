package com.lukeshaun.mobileca1.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceTransitionService extends IntentService {

    private final String TAG = "DEBUG " + this.getClass().getSimpleName();

    private static final String classTAG = "GeofenceService";
    public GeofenceTransitionService() {
        super(classTAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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
            String geofenceId = geofence.getRequestId();

            // Handle geofence transition event
            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                broadcastGeofenceTransitionEvent(geofence, Geofence.GEOFENCE_TRANSITION_ENTER);
            }
            else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.d(TAG, "Exiting geofence - " + geofenceId);
            }
        }
    }

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
