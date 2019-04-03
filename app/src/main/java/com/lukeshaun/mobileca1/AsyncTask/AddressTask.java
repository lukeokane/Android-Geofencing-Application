package com.lukeshaun.mobileca1.AsyncTask;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.lukeshaun.mobileca1.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import java.lang.ref.WeakReference;

public class AddressTask extends AsyncTask<Location, Void, Address> {

    private Context mContext;
    private WeakReference<Marker> mMarker;


    public AddressTask(Context applicationContext, Marker marker) {
        mContext = applicationContext;
        mMarker = new WeakReference<>(marker);
    }

    private final String TAG = AddressTask.class.getSimpleName();

    @Override
    protected Address doInBackground(Location... params) {
        // Set up the geocoder
        Geocoder geocoder = new Geocoder(mContext,
                Locale.getDefault());

        // Get the passed in location
        Location location = params[0];
        List<Address> addresses = null;

        Address address = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems
            Log.e(TAG, mContext.getString(R.string.service_not_available), ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values
            Log.e(TAG, mContext.getString(R.string.invalid_lat_long_used) + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // If no addresses found, print an error message.
        if (addresses == null || addresses.size() == 0) {
                Log.e(TAG, mContext.getString(R.string.no_address_found));
        } else {
            // If an address is found, read it into resultMessage
            address = addresses.get(0);
        }

        return address;
    }

    /**
     * Called once the background thread is finished and updates the
     * UI with the result.
     * @param address The resulting reverse geocoded address, or error
     *                message if the task failed.
     */
    @Override
    protected void onPostExecute(Address address) {
        mMarker.get().setTag(address);
        mMarker.get().showInfoWindow();
        super.onPostExecute(address);
    }
}