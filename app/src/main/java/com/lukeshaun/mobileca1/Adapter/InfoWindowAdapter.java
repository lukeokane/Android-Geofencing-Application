package com.lukeshaun.mobileca1.Adapter;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.lukeshaun.mobileca1.R;

import java.util.ArrayList;

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public InfoWindowAdapter(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        // Get info window view
        View view = ((Activity)context).getLayoutInflater()
                .inflate(R.layout.info_window, null);

        // Get info window UI objects
        TextView titleTextView = view.findViewById(R.id.info_window_title);
        TextView addressTextView = view.findViewById(R.id.info_window_address);
        TextView postalCodeTextView = view.findViewById(R.id.info_window_postcode);

        // Check if an address has been passed into the marker
        Address address = (Address) marker.getTag();

        // Set title view to title
        titleTextView.setText(marker.getTitle());
        // If address present, parse and create readable address.
        // Set the address in the UI objects

        String addressString = "";
        if (address != null) {

            // e.g 35 / My House Name
            String featureName = address.getFeatureName();
            // e.g. Thornberry Estate, Riverdale Bridge
            String thoroughfare = address.getThoroughfare();
            // e.g. Dundalk, Ardee, Drogheda
            String locality = address.getLocality();
            // e.g. County Louth, Fingal
            String adminArea = address.getAdminArea();
            // e.g Ireland, Germany, South Africa
            String country = address.getCountryName();
            // e.g A492 BSO, 73108
            String postalCode = address.getPostalCode();

            // Attempt to make an address in the form **Area/Feature/Premises Name, Thoroughfare, Locality, Admin Area, Country**
            // e.g. 35, Stoney Lane, Dundalk, County Louth, Ireland
            ArrayList<String> validAddressValues = new ArrayList<>();

            if (!featureName.equals("Unnamed Road") && featureName != null) {
                validAddressValues.add(featureName);
            }
            if (!thoroughfare.equals("Unnamed Road") && thoroughfare != null) {
                validAddressValues.add(thoroughfare);
            }
            if (locality != null) {
                validAddressValues.add(locality);
            }
            if (adminArea != null) {
                validAddressValues.add(adminArea);
            }
            if (country != null) {
                validAddressValues.add(country);
            }

            // Set address text
            addressTextView.setText(TextUtils.join(", ", validAddressValues));

            // Set postal code text if it is present, if not then hide the TextView;
            if (postalCode != null) {
                postalCodeTextView.setText(postalCode);
                postalCodeTextView.setVisibility(View.VISIBLE);
            }
            else {
                postalCodeTextView.setVisibility(View.GONE);
            }
        }

        return view;
    }
}
