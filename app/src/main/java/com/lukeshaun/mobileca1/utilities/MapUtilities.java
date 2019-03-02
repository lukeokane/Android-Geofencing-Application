package com.lukeshaun.mobileca1.utilities;

import com.google.android.gms.maps.model.CircleOptions;

public final class MapUtilities {

    private MapUtilities() {
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

    public static CircleOptions greenGeofence() {
        return newCircleOptions()
                .fillColor(0x4400FF00)
                .strokeColor(0x9900FF00);
    }

    public static CircleOptions redGeofence() {
        return newCircleOptions()
                .fillColor(0x44FF0000)
                .strokeColor(0x99FF0000);
    }
}
