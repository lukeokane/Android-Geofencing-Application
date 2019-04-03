package com.lukeshaun.mobileca1.classes;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Record {

    /*
     * 'Record' Database Schema
     */
    private String clockInOutType;

    private String geoEnterExit;

    private String jobID;

    private LatLng location;

    @ServerTimestamp Date timestamp;

    private String userId;

    public static final String CLOCK_IN = "IN";
    public static final String CLOCK_OUT = "OUT";
    public static final String GEOFENCE_ENTER = "ENTERED";
    public static final String GEOFENCE_EXIT = "EXITED";

    public Record() {}

    public Record(String clockInOutType, String geoEnterExit, String jobID,
                  LatLng location, String userId) {

        this.clockInOutType = clockInOutType;
        this.geoEnterExit = geoEnterExit;
        this.jobID = jobID;
        this.location = location;
        this.userId = userId;
    }


    public String getClockInOutType() {
        return clockInOutType;
    }

    public void setClockInOutType(String clockInOutType) {
        this.clockInOutType = clockInOutType;
    }

    public String getGeoEnterExit() {
        return geoEnterExit;
    }

    public void setGeoEnterExit(String geoEnterExit) {
        this.geoEnterExit = geoEnterExit;
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

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }
}
