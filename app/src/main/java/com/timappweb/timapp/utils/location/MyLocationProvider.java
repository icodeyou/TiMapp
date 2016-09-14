package com.timappweb.timapp.utils.location;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.exceptions.NoLastLocationException;
import com.timappweb.timapp.utils.DelayedCallHelper;
import com.timappweb.timapp.utils.Util;

/**
 * Created by stephane on 8/22/2015.
 */
public class MyLocationProvider implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    // ---------------------------------------------------------------------------------------------

    public static final String  TAG = "LocationService";
    private static final int    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;
    public static int           MIN_UPDATE_TIME = 30000; // (in milliseconds)
    public static int           MAX_UPDATE_TIME = 15000; // (in milliseconds)
    public static int           MIN_UPDATE_DISTANCE = 10;

    // ---------------------------------------------------------------------------------------------

    private Activity            activity = null;

    private GoogleApiClient     mGoogleApiClient = null;    // Might be null if Google Play services APK is not available.

    private LocationListener    mLocationListener = null;   // Location listener for Location update callbacks
    private LocationRequest     mLocationRequest;           // Location request object
    LocationManager             mLocationManager = null;    // LocationManager

    private Location            mLastLocation = null;       // The last name from any provider
    private boolean             mRequestingLocationUpdates = true;  // Enable or disable name requests

    // ---------------------------------------------------------------------------------------------
    // static

    public static LatLng convert(Location l){
        return new LatLng(l.getLatitude(), l.getLongitude());
    }

    // ---------------------------------------------------------------------------------------------
    // Initialization

    public MyLocationProvider(Activity activity, LocationListener mLocationListener){
        this.activity = activity;
        this.initGoogleApiClient();
        this.initLocationManager();
        this.setLocationListener(mLocationListener);
        Log.d(TAG, "Localisation API has been set up!");
    }

    protected synchronized void initGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this.activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        this.initLocationRequest();
    }

    private void initLocationManager() {
        mLocationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);
    }

    protected void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(MIN_UPDATE_TIME);
        mLocationRequest.setFastestInterval(MAX_UPDATE_TIME);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    // ---------------------------------------------------------------------------------------------
    // Getters

    public boolean isGPSEnabled(){
        if (!hasLocationPermission()) return false;
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public Location getLastGPSLocation() throws NoLastLocationException {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null){
            throw new NoLastLocationException(LocationManager.GPS_PROVIDER);
        }
        return location;
    }

    public Location getLastLocation() throws NoLastLocationException {
        if (!hasLocationPermission()){
            this.requestPermissions();
            throw new NoLastLocationException("App does not have user permission for location yet");
        }

        if (this.mLastLocation != null){
            return mLastLocation;
        }
        else if  (mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
            mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            return mLastLocation;
        }
        else if  (mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null){
            mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            return mLastLocation;
        }
        throw new NoLastLocationException("No name");
    }



    public boolean hasKnownPosition() {
        return mLastLocation != null;
    }


    // ---------------------------------------------------------------------------------------------
    // Setters

    public void setLocationListener(LocationListener locationListener) {
        this.mLocationListener = locationListener;
    }

    // ---------------------------------------------------------------------------------------------
    // Permission related methodes

    public void askUserToEnableGPS() {
        /*Context context = MyApplication.getApplicationBaseContext();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                activity);
        alertDialogBuilder
                .setMessage(context.getString(R.string.ask_user_to_enable_gps))
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.dialog_button_enable_gps),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                activity.startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        *//*
                        try{
                            NavUtils.navigateUpFromSameTask(activity);
                        }
                        catch (IllegalArgumentException ex){
                            // If there is no up task, do nothing
                        }*//*
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();*/

        //Source
        //https://developers.google.com/android/reference/com/google/android/gms/location/SettingsApi

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    activity, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Log.e(TAG, "Settings change for gps location are not available");
                        break;
                }
            }
        });
    }

    public static boolean hasLocationPermission() {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(MyApplication.getApplicationBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public void requestPermissions() { //@NonNull String[] permissions, int requestCode){
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this.activity,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d(TAG, "Show request name explanation to the user");
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            Toast.makeText(MyLocationProvider.this.activity, R.string.explanation_gps_usage, Toast.LENGTH_LONG).show();
            DelayedCallHelper.create(3500, new DelayedCallHelper.Callback() {
                @Override
                public void onTime() {
                    ActivityCompat.requestPermissions(MyLocationProvider.this.activity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
                }
            });
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this.activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
        }
    }

    /**
     * TODO not implemented
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(TAG, "User granted name request");
                    // this.updateLastLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // TODO
                    Log.i(TAG, "User rejected name request");
                }
                return;
            }
        }
    }
    // ---------------------------------------------------------------------------------------------
    // Location update methodes


    protected void startLocationUpdates() {
        Log.d(TAG, "Starting name updates");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
    }
    /**
     * Removes name updates from the FusedLocationApi.
     */
    public void stopLocationUpdates() {
        Log.d(TAG, "Stopping name updates");
        // It is a good practice to remove name requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent name updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
    }


    private Criteria getBestCriteria(){
        Criteria criteria = new Criteria();

        // Pour indiquer la précision voulue
        // On peut mettre ACCURACY_FINE pour une haute précision ou ACCURACY_COARSE pour une moins bonne précision
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        // Est-ce que le fournisseur doit être capable de donner une altitude ?
        criteria.setAltitudeRequired(false);
        // Est-ce que le fournisseur doit être capable de donner une direction ?
        criteria.setBearingRequired(false);
        // Est-ce que le fournisseur peut être payant ?
        criteria.setCostAllowed(false);
        // Pour indiquer la consommation d'énergie demandée
        // Criteria.POWER_HIGH pour une haute consommation, Criteria.POWER_MEDIUM pour une consommation moyenne et Criteria.POWER_LOW pour une basse consommation
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        // Est-ce que le fournisseur doit être capable de donner une vitesse ?
        criteria.setSpeedRequired(false);
        return criteria;
    }
    /**
     * TODO not implemented
     */
    public void requestMultipleUpdates(){
        Criteria c = this.getBestCriteria();
        String locationProvider = mLocationManager.getBestProvider(c, true);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d(TAG, "Best name provider is: " + locationProvider);

        //mLocationManager.requestSingleUpdate(c, mLocationListener, null);
        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
        //        MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, null);
    }


    // ---------------------------------------------------------------------------------------------
    // MyLocationProvider callback
    /**
     * MyLocationProvider callback
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Connection failed");
    }


    /**
     * MyLocationProvider callback
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.e(TAG, "Google name api onConnected()");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mRequestingLocationUpdates && hasLocationPermission()) {
            startLocationUpdates();
        }
    }

    // ---------------------------------------------------------------------------------------------

    public void connect(){
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }
    }

}

/*
    public boolean updateLastLocation() {
        if (checkSelfPermission(this.activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(this.activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
            Log.e(TAG, "Cannot get user name. No permission");
            return false;
        }

        if (!this.isGPSEnabled()){
            this.askUserToEnableGPS();
        }

        Log.d(TAG, "User has requested his name");

        Criteria c = this.getBestCriteria();
        String locationProvider = mLocationManager.getBestProvider(c, true);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


        if (mLastLocation != null){
            long locationTime = mLastLocation.getTime();
            Log.i(TAG, "Last known name is " + mLastLocation.getLongitude() + "-" + mLastLocation.getLongitude() +
                    "  " + ((System.currentTimeMillis()-locationTime)/1000) + " seconds ago" );
            if (locationTime > (System.currentTimeMillis() + MIN_UPDATE_TIME) ){
                Log.d(TAG, "This is too old, requesting update...");
            }
            else{
                Log.d(TAG, "Using last name");
                mLocationListener.onLocationChanged(mLastLocation);
                return true;
            }
        }

        Log.d(TAG, "Best name provider is: " + locationProvider);

        //mLocationManager.requestSingleUpdate(c, mLocationListener, null);
        //mLocationManager.requestLocationUpdates(locationProvider, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, mLocationListener);
        return true;
    }

*/