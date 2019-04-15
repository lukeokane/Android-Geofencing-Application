package com.lukeshaun.mobileca1;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lukeshaun.mobileca1.AsyncTask.AddressTask;
import com.lukeshaun.mobileca1.classes.Record;
import com.lukeshaun.mobileca1.service.GeofenceTransitionService;
import com.lukeshaun.mobileca1.service.NotificationService;
import com.lukeshaun.mobileca1.service.NotificationService.NotificationBinder;
import com.lukeshaun.mobileca1.utility.MapUtility;
import com.lukeshaun.mobileca1.Adapter.InfoWindowAdapter;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

// Extends AppCompatActivity instead of FragmentActivity to show app bar, therefore shows menu.
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final String TAG = "DEBUG " + this.getClass().getSimpleName();

    /* Google Maps member variables */
    private GoogleMap mMap;
    private Map<String, Circle> mDrawnGeofences;
    private Boolean mIsClockedIn = false;
    private Marker mMarker;

    // Identify location permission request when returned from onRequestPermissionsResult() method
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    /* Geofencing member variables */
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;
    private static final String GEOFENCE_TRANSITION_BROADCAST =
            BuildConfig.APPLICATION_ID + ".GEOFENCE_TRANSITION_BROADCAST";

    private Geofence mCurrentGeofence;
    private boolean mInGeofence;

    /* Notification member variables */
    private LocationBroadcastReceiver mLocationBroadcastReceiver = new LocationBroadcastReceiver();
    private NotificationService mNotificationService;
    private boolean isNSBound;

    /* Location member variables */
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LatLng mLastLocationUpdate;

    /* Firebase member variables */
    private FirebaseFirestore mFirestore;

    /* UI member variables */
    private Button mClockInOutButton;

    /* User Information variables */
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Local Bound Service
        bindService(new Intent(this, NotificationService.class), notificationConnection, Context.BIND_AUTO_CREATE);

        // Register receiver with LocalBroadcastManager.
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationBroadcastReceiver, new IntentFilter(GEOFENCE_TRANSITION_BROADCAST));

        // Click in and out button
        mClockInOutButton = findViewById(R.id.clockInOutButton);
        mClockInOutButton.setVisibility(View.INVISIBLE);
        mClockInOutButton.setOnClickListener(clockInOutListener);

        Intent intent = new Intent(this, GeofenceTransitionService.class);
        startService(intent);

        initFirebase();

        // Initialize the FusedLocationClient.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(
                this);
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        mapFragment.getMapAsync(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mLastLocationUpdate = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
            }
        };

        mAuth = FirebaseAuth.getInstance();
        // [START config_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END config_signin]
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        ImageView mNearbyPlacesButton = findViewById(R.id.nearbyPlacesButton);
        mNearbyPlacesButton.setOnClickListener(nearbyPlacesListener);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Save map instance in Activity
        mMap = googleMap;

        InfoWindowAdapter infoWindow = new InfoWindowAdapter(this);
        mMap.setInfoWindowAdapter(infoWindow);

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
            case R.id.sign_out_button:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signOut()
    {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        onSignOutComplete();
                        Toast.makeText(getApplicationContext(), "You have been signed out.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onSignOutComplete()
    {
        Intent signedOutIntent = new Intent(this, MainActivity.class);
        startActivity(signedOutIntent);
        finish();
    }

    // Respond to user clicking on a geofence.
    private void geoFenceClick(final GoogleMap map) {
        // Set up listener on map
        map.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {

                for (String key : mDrawnGeofences.keySet()) {
                    if (mDrawnGeofences.get(key).getCenter().equals(circle.getCenter())) {

                        Log.d(TAG, "Geofence clicked at geofence " + key);

                        // Start the reverse geocode AsyncTask
                        Location location = new Location("");
                        location.setLatitude(circle.getCenter().latitude);
                        location.setLongitude(circle.getCenter().longitude);

                        // Create an invisible mMarker with 0x0, needed to display information window at circle.
                        mMarker = map.addMarker(new MarkerOptions()
                                .alpha(0)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.blank_image))
                                .position(circle.getCenter())
                                .title(key));

                        new AddressTask(MapsActivity.this,
                                mMarker).execute(location);

                    }
                }
            }
        });
    }

    private void createMockGeofences() {
        // Add a mMarker to DkIT and move the camera
        LatLng dkit = new LatLng(53.984981, -6.393973);
        LatLng crownPlaza = new LatLng(53.980856, -6.38913);
        LatLng sportsGround = new LatLng(53.989932, -6.389998);

        mDrawnGeofences = new HashMap<>();
        Geofence geofence1 = MapUtility.createGeofenceEnterExitTransitions("Dundalk Institute of Technology", dkit);
        mDrawnGeofences.put("Dundalk Institute of Technology", mMap.addCircle(MapUtility.defaultGeofence().center(dkit)));
        Geofence geofence2 = MapUtility.createGeofenceEnterExitTransitions("Crown Plaza", crownPlaza);
        mDrawnGeofences.put("Crown Plaza", mMap.addCircle(MapUtility.defaultGeofence().center(crownPlaza)));
        Geofence geofence3 = MapUtility.createGeofenceEnterExitTransitions("Muirhevna Sports Ground", sportsGround);
        mDrawnGeofences.put("Muirhevna Sports Ground", mMap.addCircle(MapUtility.defaultGeofence().center(sportsGround)));

        // 15: Street Level Zoom
        float zoom = 15;
        // Create camera update position
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(dkit, zoom);
        // Move camera view to CameraUpdate object location
        mMap.moveCamera(cameraUpdate);

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
                ACCESS_FINE_LOCATION)
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
                                Log.d(TAG, "Geofence adding failed: " + Arrays.toString(e.getStackTrace()));
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
                ACCESS_FINE_LOCATION)
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
                            {ACCESS_FINE_LOCATION},
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

        // Create intent with identifier
        Intent intent = new Intent(this, GeofenceTransitionService.class);
        intent.putExtra("GEOFENCE_REQUEST", "");

        // Indicating what service to send GeofencingClient transition events to when they occur.
        // Using flag FLAG_UPDATE_CURRENT to get same pending intent back.
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return mGeofencePendingIntent;
    }

    private void initFirebase() {
        mFirestore = FirebaseFirestore.getInstance();
    }

    private ImageView.OnClickListener nearbyPlacesListener = new ImageView.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent nearbyPlacesIntent = new Intent(getApplicationContext(), NearbyPlacesActivity.class);
            startActivity(nearbyPlacesIntent);
        }
    };

    private ServiceConnection notificationConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NotificationBinder binder = (NotificationBinder) service;
            mNotificationService = binder.getService();
            isNSBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isNSBound = false;
        }
    };

    private View.OnClickListener clockInOutListener = new View.OnClickListener() {

        public void onClick(View view) {

            // Get records collection
            CollectionReference records = mFirestore.collection("records");

            // If clocked out, then clock in
            if (!mIsClockedIn) {
                // Add record of clock in
                records.add(new Record(Record.CLOCK_IN, null, mCurrentGeofence.getRequestId(), mLastLocationUpdate, mAuth.getCurrentUser().getEmail()));
                MapUtility.setGeofenceGreen(mDrawnGeofences.get(mCurrentGeofence.getRequestId()));

                // Set clock out style
                mClockInOutButton.setBackgroundColor(getResources().getColor(R.color.clockOutColor));
                mClockInOutButton.setText(R.string.clock_out);
            }
            // If clocked in, then clock out.
            else {
                // Add record of clock out
                records.add(new Record(Record.CLOCK_OUT, null, mCurrentGeofence.getRequestId(), mLastLocationUpdate, mAuth.getCurrentUser().getEmail()));
                MapUtility.setGeofenceDefault(mDrawnGeofences.get(mCurrentGeofence.getRequestId()));

                // If user clocked out while not in the geofence, hide button.
                if (!mInGeofence) {
                    mClockInOutButton.setVisibility(View.INVISIBLE);
                }

                // Set clock in style
                mClockInOutButton.setBackgroundColor(getResources().getColor(R.color.clockInColor));
                mClockInOutButton.setText(R.string.clock_in);

            }

            // Change boolean value
            mIsClockedIn = !mIsClockedIn;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isNSBound) {
            unbindService(notificationConnection);
            isNSBound = false;
        }
        if (mIsClockedIn) {
            // Save record
            CollectionReference records = mFirestore.collection("records");
            records.add(new Record(Record.CLOCK_OUT, null, mCurrentGeofence.getRequestId(), mLastLocationUpdate, mAuth.getCurrentUser().getEmail()));
        }
        Intent intent = new Intent(this, GeofenceTransitionService.class);
        stopService(intent);
    }

    class LocationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Check broadcast is from GeofenceTransitionService
            if (intent.getAction() == GEOFENCE_TRANSITION_BROADCAST) {
                Log.d(TAG, "Received broadcast from " + GEOFENCE_TRANSITION_BROADCAST);

                // Get Geofence and Geofence event information
                Bundle bundle = intent.getBundleExtra("Geofence");
                Geofence geofence = bundle.getParcelable("Geofence");
                int geofenceEvent = intent.getIntExtra("GeofenceTransition", -1);

                // Error occurred when parsing the integer in the Intent
                if (geofenceEvent == -1)
                {
                    throw new InvalidParameterException("GeofenceTransition in Intent used default value");
                }

                Circle drawnGeofence = mDrawnGeofences.get(geofence.getRequestId());

                // Device entered a geofence
                if (geofenceEvent == Geofence.GEOFENCE_TRANSITION_ENTER) {

                    // If clocked in and re-entering the geofence, save "ENTERED" geofence record.
                    // This will indicate a user is clocked in and has re-entered the site after leaving it.
                    if (mIsClockedIn && geofence.getRequestId().equals(mCurrentGeofence.getRequestId())) {
                        // Save record
                        CollectionReference records = mFirestore.collection("records");
                        records.add(new Record(null, Record.GEOFENCE_ENTER, geofence.getRequestId(), drawnGeofence.getCenter(), mAuth.getCurrentUser().getEmail()));

                        // Update geofence to green
                        MapUtility.setGeofenceGreen(drawnGeofence);

                        mNotificationService.sendNotification("Re-entered the site", "You left the site and are now back");
                    }
                    else {

                        // Hide clock out button
                        mClockInOutButton.setVisibility(View.VISIBLE);
                        mClockInOutButton.setBackgroundColor(getResources().getColor(R.color.clockInColor));
                        mClockInOutButton.setText(R.string.clock_in);

                        // Set the geofence the device is in
                        mCurrentGeofence = geofence;
                    }

                    mInGeofence = true;
                }
                else if (geofenceEvent == Geofence.GEOFENCE_TRANSITION_EXIT) {

                    // If clocked in and leaving the geofence, save "EXITED" geofence record.
                    // This will indicate a user is clocked in and has re-entered the site after leaving it.
                    if (mIsClockedIn && geofence.getRequestId().equals(mCurrentGeofence.getRequestId())) {
                        // Save record
                        CollectionReference records = mFirestore.collection("records");
                        records.add(new Record(null, Record.GEOFENCE_EXIT, geofence.getRequestId(), drawnGeofence.getCenter(), mAuth.getCurrentUser().getEmail()));

                        // Update geofence to green
                        MapUtility.setGeofenceRed(drawnGeofence);

                        mNotificationService.sendNotification("Leaving site while clocked in", "WARNING: you are now outside of the site while clocked in.");
                    }
                    else {
                        // No longer required to be in a geofence, remove geofence
                        mCurrentGeofence = null;
                        mClockInOutButton.setVisibility(View.INVISIBLE);
                    }

                    mInGeofence = false;
                }
            }
        }
    }
}
