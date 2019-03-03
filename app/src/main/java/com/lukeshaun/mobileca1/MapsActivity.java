package com.lukeshaun.mobileca1;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.lukeshaun.mobileca1.service.GeofenceTransitionService;
import com.lukeshaun.mobileca1.utility.MapUtility;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Extends AppCompatActivity instead of FragmentActivity to show app bar, therefore shows menu.
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final String TAG = "DEBUG " + this.getClass().getSimpleName();

    // Google Maps
    private GoogleMap mMap;

    // Identify location permission request when returned from onRequestPermissionsResult() method
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    /* Geofencing member variables */
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;
    private static final String GEOFENCE_TRANSITION_EVENT_BROADCAST = "GeofenceTransitionEvent";
    private Map<String, Circle> mDrawnGeofences;
    private Geofence mCurrentGeoFence;

    /* Location member variables */
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("GeofenceTransitionEvent"));

        // Initialize the FusedLocationClient.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(
                this);
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        mapFragment.getMapAsync(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // TODO Handle Result Here
            }
        };
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume called");
        super.onResume();

        // check if Google Play Services is installed...
        // it is required for geofencing.
        int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (response != ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play Services not available - prompt user to download it");
            GoogleApiAvailability.getInstance().getErrorDialog(this, response, 1).show();
        }
        else {
            Log.d(TAG, "Google Play Services is installed");
        }
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


        mDrawnGeofences = new HashMap<>();
        Geofence geofence1 = MapUtility.createGeofenceEnterExitTransitions("Dundalk Institute of Technology", dkit);
        mDrawnGeofences.put("Dundalk Institute of Technology", mMap.addCircle(MapUtility.defaultGeofence().center(dkit)));
        Geofence geofence2 = MapUtility.createGeofenceEnterExitTransitions("Crown Plaza", crownPlaza);
        mDrawnGeofences.put("Crown Plaza", mMap.addCircle(MapUtility.defaultGeofence().center(crownPlaza)));
        Geofence geofence3 = MapUtility.createGeofenceEnterExitTransitions("Muirhevna Sports Ground", sportsGround);
        mDrawnGeofences.put("Muirhevna Sports Ground", mMap.addCircle(MapUtility.defaultGeofence().center(sportsGround)));

        // Group a list of geofences to be monitored and customize how each geofence notifications should be reported
        // In our case, they will all have the same initial trigger which occurs on entering the geofence.
        List<Geofence> geofenceList = new LinkedList<>();
        geofenceList.add(geofence1);
        geofenceList.add(geofence2);
        geofenceList.add(geofence3);

        // Building a geofence request to be sent to the geofence client to begin monitoring.
        // Monitoring will begin when the device enters the geofence
        GeofencingRequest geofenceRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Sends a pending intent with a list of Geofence transitions to the GeofenceTransitionService when any occur.
            mGeofencingClient.addGeofences(geofenceRequest, getGeofencePendingIntent())
                    // If error
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Geofences Added");
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // This error usually occurs when location access is turned off.
                            // This error can also occur on an emulator when all location services (GPS / WiFi / Network) are turned on (my experience).
                            // If running on an emulator, turn off all location services and turn back on again to prevent this emulator issue.
                            if (e.getMessage().contains("1000")) {
                                Log.w(TAG, "GEOFENCE_NOT_AVAILABLE error on adding geofences");
                            }
                            else {
                                Log.d(TAG, "Geofence adding failed: " + e.getStackTrace().toString());
                            }
                        }
                    });
        }
        else {
            Log.d(TAG, "Geofences not added, no permission");
        }
    }

    private void enableLocationTracking() {
        // Check if fine location permission has been allowed.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location enabled");
            mMap.setMyLocationEnabled(true);

            // Start requestLocationUpdates, this is a best-attempt way to get location...
            // every 10 seconds.
            // NOTE: When activity is opened and there is no location permissions...
            // the location device location does not show up on the map even after accepting location permissions.
            // requestLocationUpdates fixes this by instantiating the map with location information
            mFusedLocationClient.requestLocationUpdates
                    (MapUtility.createLocationRequest(), mLocationCallback,
                            null /* Looper */);

            // Creating mock sites
            createMockGeofences();
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

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent
        if (mGeofencePendingIntent != null)
        {
            return mGeofencePendingIntent;
        }

        // Create new intent
        Intent intent = new Intent(this, GeofenceTransitionService.class);

        // Indicating what service to send Geofence transition events to when they occur.
        // Using flag FLAG_UPDATE_CURRENT to get same pending intent back.
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return mGeofencePendingIntent;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check broadcast is from GeofenceTransitionService
            if (intent.getAction() == GEOFENCE_TRANSITION_EVENT_BROADCAST) {
                Log.d(TAG, "Received broadcast from " + GEOFENCE_TRANSITION_EVENT_BROADCAST);

                // Get Geofence and Geofence event information
                Bundle bundle = intent.getBundleExtra("Geofence");
                Geofence geofence = bundle.getParcelable("Geofence");
                int geofenceEvent = intent.getIntExtra("GeofenceTransition", -1);

                Circle drawnGeofence = mDrawnGeofences.get(geofence.getRequestId());

                // Device entered a geofence
                if (geofenceEvent == Geofence.GEOFENCE_TRANSITION_ENTER) {
                        mCurrentGeoFence = geofence;
                        MapUtility.setGeofenceGreen(drawnGeofence);
                }
                else if (geofenceEvent == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    MapUtility.setGeofenceDefault(drawnGeofence);
                }
                // Error occurred when parsing the integer in the Intent
                if (geofenceEvent == -1)
                {
                    throw new InvalidParameterException("GeofenceTransition in Intent use default value");
                }

                Log.d(TAG, "REQUEST ID: " + geofence.getRequestId());
                Log.d(TAG, "REQUEST ID: " + geofenceEvent);
            }
        }
    };
}
