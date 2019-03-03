package com.lukeshaun.mobileca1.classes;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Record {

    /*
     * 'Record' Database Schema
     */
    private String checkInOutType;

    private String geoExitEnter;

    private LatLng location;

    @ServerTimestamp Date timestamp;

    private String userId;

    public Record() {}

    public Record(String checkInOutType, String geoExitEnter,
                  LatLng location, Date timestamp, String userId) {
        this.checkInOutType = checkInOutType;
        this.geoExitEnter = geoExitEnter;
        this.location = location;

        this.userId = userId;this.timestamp = timestamp;
    }


    public String getCheckInOutType() {
        return checkInOutType;
    }

    public void setCheckInOutType(String checkInOutType) {
        this.checkInOutType = checkInOutType;
    }

    public String getGeoExitEnter() {
        return geoExitEnter;
    }

    public void setGeoExitEnter(String geoExitEnter) {
        this.geoExitEnter = geoExitEnter;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
