package com.lukeshaun.mobileca1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lukeshaun.mobileca1.utilities.MapUtilities;

import java.util.Locale;

// Extends AppCompatActivity instead of FragmentActivity to show app bar, therefore shows menu.
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final String TAG = "DEBUG " + this.getClass().getSimpleName();

    // Google Maps
    private GoogleMap mMap;

    // Identify location permission request when returned from onRequestPermissionsResult() method
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Save map instance in Activity
        mMap = googleMap;

        // Creating mock sites
        createMockGeofences();

        // Creating listener to respond to clicking on geofences
        // Information about a geofence appears
        geoFenceClick(mMap);

        // Enable location tracking
        enableLocationTracking();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
        switch (item.getItemId()) {
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Respond to user clicking on a geofence.
    private void geoFenceClick(final GoogleMap map) {
        // Set up listener on map
        map.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {

                // Get center of circle
                LatLng coord = circle.getCenter();

                Log.d(TAG, "Geofence clicked at location " + coord.toString());

                // Create text to add to info window.
                String snippet = String.format(Locale.getDefault(),
                        "Lat: %1$.5f, Long: %2$.5f",
                        coord.latitude,
                        coord.longitude);

                // Create an invisible marker with 0x0, needed to display information window at circle.
                map.addMarker(new MarkerOptions()
                        .alpha(0)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.blank_image))
                        .position(coord)
                        .title(getString(R.string.dropped_pin))
                        .snippet(snippet))
                        .showInfoWindow();
            }
        });
    }


    private void createMockGeofences() {
        // Add a marker to DkIT and move the camera
        LatLng dkit = new LatLng(53.984981, -6.393973);
        LatLng crownPlaza = new LatLng(53.980856, -6.38913);
        LatLng sportsGround = new LatLng(53.989932, -6.389998);
        // Zoom levels:
        // 1: World
        // 5: Landmass / continent
        // 10: City
        // 15: Streets
        // 20: Buildings
        float zoom = 15;
        // Create camera update position
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(dkit, zoom);
        // Move camera view to CameraUpdate object location
        mMap.moveCamera(cameraUpdate);

        // Draw default geofences on map and set position
        drawGeofence(MapUtilities.defaultGeofence().center(dkit));
        drawGeofence(MapUtilities.defaultGeofence().center(crownPlaza));
        drawGeofence(MapUtilities.defaultGeofence().center(sportsGround));
    }

    /*
     * Draws a geofence location on the map with the passed in CircleOptions.
     */
    private void drawGeofence(CircleOptions geoFenceOptions) {
        mMap.addCircle(geoFenceOptions);
    }

    private void enableLocationTracking() {
        // Check if fine location permission has been allowed.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location enabled");
            // Sets the my-location button to display.
            mMap.setMyLocationEnabled(true);
        }
        // If not then prompt user for permission
        else {
            Log.d(TAG, "Location not enabled");
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    enableLocationTracking();
                    break;
                }
        }
    }
}
