package com.lukeshaun.mobileca1.AsyncTask;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.google.j2objc.annotations.Weak;
import com.lukeshaun.mobileca1.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.lang.ref.WeakReference;

public class AddressTask extends AsyncTask<Location, Void, String> {

    private Context mContext;
    private WeakReference<Marker> mMarker;


    public AddressTask(Context applicationContext, Marker marker) {
        mContext = applicationContext;
        mMarker = new WeakReference<>(marker);
    }

    private final String TAG = AddressTask.class.getSimpleName();

    @Override
    protected String doInBackground(Location... params) {
        // Set up the geocoder
        Geocoder geocoder = new Geocoder(mContext,
                Locale.getDefault());

        // Get the passed in location
        Location location = params[0];
        List<Address> addresses = null;

        String resultMessage = "";

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems
            resultMessage = mContext.getString(R.string.service_not_available);
            Log.e(TAG, resultMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values
            resultMessage = mContext.getString(R.string.invalid_lat_long_used);
            Log.e(TAG, resultMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // If no addresses found, print an error message.
        if (addresses == null || addresses.size() == 0) {
            if (resultMessage.isEmpty()) {
                resultMessage = mContext.getString(R.string.no_address_found);
                Log.e(TAG, resultMessage);
            }
        } else {
            // If an address is found, read it into resultMessage
            Address address = addresses.get(0);
            ArrayList<String> addressParts = new ArrayList<>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {

                // If no road at location, do not add text
                if (!address.getAddressLine(i).equals("Unnamed Road")) {
                    addressParts.add(address.getAddressLine(i));
                }
            }

            resultMessage = TextUtils.join(
                    "\n",
                    addressParts);
            // Indicates no road is present, remove it from the message
            if (resultMessage.contains("Unnamed Road")) {
                resultMessage = resultMessage.replace("Unnamed Road,", "");
            }

        }

        return resultMessage;
    }

    /**
     * Called once the background thread is finished and updates the
     * UI with the result.
     * @param address The resulting reverse geocoded address, or error
     *                message if the task failed.
     */
    @Override
    protected void onPostExecute(String address) {
        mMarker.get().setSnippet(address);
        mMarker.get().showInfoWindow();
        super.onPostExecute(address);
    }
}