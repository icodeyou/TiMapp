package com.timappweb.timapp.utils;

import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.exceptions.NoLastLocationException;

/**
 * Created by stephane on 8/22/2015.
 */
public class MyLocationProvider implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LocationService";
    private Location mLastLocation = null;
    private GoogleApiClient mGoogleApiClient = null;// Might be null if Google Play services APK is not available.
    private Activity activity = null;

    LocationManager mLocationManager = null;

    public static int MIN_UPDATE_TIME = 10000;
    public static int MIN_UPDATE_DISTANCE = 10;


    public void getUserLocation(LocationListener mLocationListener){
        Log.d(TAG, "User has requested his location");

        Criteria c = this.getCriteria();
        String locationProvider = mLocationManager.getBestProvider(c, true);

        mLastLocation = mLocationManager.getLastKnownLocation(locationProvider);

        if (mLastLocation != null){
            long locationTime = mLastLocation.getTime();
            Log.i(TAG, "Last known location is " + mLastLocation.getLongitude() + "-" + mLastLocation.getLongitude());
            if (locationTime > (System.currentTimeMillis() + MIN_UPDATE_TIME) ){
                Log.d(TAG, "This is too old, requesting update...");
            }
            else{
                mLocationListener.onLocationChanged(mLastLocation);
            }
        }
        mLocationManager.requestSingleUpdate(c, mLocationListener, null);
        //mLocationManager.requestLocationUpdates(locationProvider, MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, mLocationListener);
    }


    public MyLocationProvider(Activity activity){
        this.activity = activity;
        this.buildGoogleApiClient();
        mLocationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);
        Log.d(TAG, "Localisation API has been setBounds up!");
    }
/*

    public Location getLastLocation(){
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = this.getCriteria();
        String providerName = locationManager.getBestProvider(criteria, false);
        Location l = locationManager.getLastKnownLocation(providerName);
        if (l != null){
            Log.i(TAG, "Get last location: " + l.getLongitude() + " - " + l.getLatitude());
            return l;
        }
        else{
            return null; // TODO throw exception
        }
    }
    */

    public LatLng getLastPosition() throws NoLastLocationException {
        Location l = this.getLastLocation();
        return new LatLng(l.getLatitude(), l.getLongitude());
    }

    private Criteria getCriteria(){
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

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this.activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * MyLocationProvider callback
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.e(TAG, "User connected!");
        mLastLocation =  LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (this.hasKnownPosition()) {
            Log.i(TAG, "User position is: " + mLastLocation.getLatitude() + " - " + mLastLocation.getLongitude());
        }
        else{
            Log.e(TAG, "Cannot get user position");
        }
    }

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

    public boolean hasKnownPosition() {
        return mLastLocation != null;
    }
    
    public Location getLastLocation() throws NoLastLocationException {
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
        throw new NoLastLocationException("No location");
    }
}
