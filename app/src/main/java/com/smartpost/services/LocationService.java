package com.smartpost.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.smartpost.utils.Constants;


import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


import static android.content.ContentValues.TAG;


public class LocationService extends Service implements LocationListener {

    private Context context;


    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 3000; // 1 minute

    // Declaring a Location Manager
    protected LocationManager locationManager;


    public LocationService() {

    }


    public LocationService(Context context) {
        this.context = context;


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: ");


        //   myApplication = (MyApplication) getApplication();
        //  myApplication.getComponent().inject(this);


        fetchLocation();
        return START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onLocationChanged(Location location) {

        this.location = location;

        Log.d(TAG, "onLocationChanged: Latitude " + location.getLatitude());
        Toast.makeText(getApplicationContext(), "Location Changed in my own location", Toast.LENGTH_SHORT).show();


        //upload location
        uploadLocation();


    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

        Log.d(TAG, "onProviderEnabled: Latitude ");

        //Toast.makeText(getApplicationContext(), "location On", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onProviderDisabled(String s) {

        Log.d(TAG, "onProviderDisabled: Latitude ");
       Toast.makeText(getApplicationContext(), "location Off", Toast.LENGTH_LONG).show();
    }


    /**/

    public Location fetchLocation() {


        try {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            // first request location updates from both the providers
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {



                        // permision not granted
                       // return 0;
                    }

                   // resetting updates
                    locationManager.removeUpdates(this);
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }


            }

        } catch (Exception e) {
            e.printStackTrace();

           // stopSelf();
        }
        return location;
    }


    void uploadLocation(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference fbDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN_KEY).child(user.getUid());

        fbDatabase.child(Constants.FIREBASE_LAT).setValue(location.getLatitude());

        fbDatabase.child(Constants.FIREBASE_LONG).setValue(location.getLongitude());

       /* AmbulanceActivity.ambulance.setLongitude(location.getLongitude());
        AmbulanceActivity.ambulance.setLatitude(location.getLatitude());*/

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Log.d(TAG, "onTaskRemoved: ");
        //Toast.makeText(getApplicationContext(),"location app  Destroyed",Toast.LENGTH_LONG).show();

       // fetchLocation();
//        Intent intent = new Intent(getApplicationContext(),LocationService.class);
//        startService(intent);

    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(this);
        super.onDestroy();
       // Log.d(TAG, "onDestroy: ");
        //Toast.makeText(getApplicationContext(),"Service Destroyed",Toast.LENGTH_LONG).show();
    }
}
