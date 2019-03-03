package com.lukeshaun.mobileca1.utility;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public final class MapUtility {

    private MapUtility() {
    }

    private static CircleOptions newCircleOptions() {
        return new CircleOptions()
                .radius(100)
                .clickable(true)
                .strokeWidth(5.0f);
    }
    public static CircleOptions defaultGeofence() {
        return newCircleOptions()
                .fillColor(0x440000FF)
                .strokeColor(0x990000FF);
    }

    public static Circle setGeofenceDefault(Circle geofence) {
        geofence.setFillColor(0x440000FF);
        geofence.setStrokeColor(0x990000FF);

        return geofence;
    }

    public static CircleOptions greenGeofence() {
        return newCircleOptions()
                .fillColor(0x4400FF00)
                .strokeColor(0x9900FF00);
    }

    public static Circle setGeofenceGreen(Circle geofence) {
        geofence.setFillColor(0x4400FF00);
        geofence.setStrokeColor(0x9900FF00);

        return geofence;
    }

    public static CircleOptions redGeofence() {
        return newCircleOptions()
                .fillColor(0x44FF0000)
                .strokeColor(0x99FF0000);
    }

    public static Circle setGeofenceRed(Circle geofence) {
        geofence.setFillColor(0x44FF0000);
        geofence.setStrokeColor(0x9900FF00);
        return geofence;
    }

    public static Geofence createGeofenceEnterExitTransitions(String id, LatLng location) {
        return new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(location.latitude, location.longitude, 100)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setNotificationResponsiveness(1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

    }

    public static LocationRequest createLocationRequest() {
        return new LocationRequest()
                .setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}
