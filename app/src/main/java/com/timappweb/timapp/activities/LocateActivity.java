package com.timappweb.timapp.activities;


import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.services.FetchAddressIntentService;
import com.timappweb.timapp.utils.Constants;
import com.timappweb.timapp.utils.MyLocationProvider;
import com.timappweb.timapp.utils.Util;

import java.util.List;

import retrofit.client.Response;

public class LocateActivity extends BaseActivity{

    private String TAG = "LocateActivity";

    //Views
    ListView listPlaces;

    // ProgressBar and ProgressDialog
    private ProgressBar progressBarLocation;
    private static ProgressDialog progressDialog = null;

    // Location
    private MyLocationProvider          locationProvider;
    private LocationListener            mLocationListener;
    private AddressResultReceiver       mResultReceiver;        // For reverse geocoding

    //others
    private InputMethodManager imm;
    private Menu mainMenu;


    // ----------------------------------------------------------------------------------------------
    //OVERRIDE METHODS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Creating LocateActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        this.initToolbar(true);

        //Initialize variables
        this.progressDialog = new ProgressDialog(this);
        this.progressDialog.setMessage("Please wait...");

        initLocationListener();
        initLocationProvider();

        // -----------------------------------------------------------------------------------------
        // Init variables
        mResultReceiver = new AddressResultReceiver(new Handler());
        Button buttonAddSpot = (Button) findViewById(R.id.button_add_spot);
        listPlaces = (ListView) findViewById(R.id.list_places);

        PlacesAdapter placesAdapter = new PlacesAdapter(this);
        listPlaces.setAdapter(placesAdapter);

        showLoader();

        //Listeners
        buttonAddSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddPlaceActivity.class);
                startActivity(intent);
            }
        });

        if (BuildConfig.DEBUG){
            placesAdapter.generateDummyData();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        locationProvider.connect();
    }

    @Override
    protected void onStop() {
        locationProvider.disconnect();
        super.onStop();
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------
    //PRIVATE METHODS

    /**
     * Load places once user location is known
     */
    private void initLocationListener() {
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "User location has changed: " + Util.print(location));
                progressDialog.hide();

                loadPlaces(location);

                startIntentServiceReverseGeocoding(location);
            }
        };
    }



    private void initLocationProvider() {
        locationProvider = new MyLocationProvider(this, mLocationListener);

        if (!locationProvider.isGPSEnabled()){
            locationProvider.askUserToEnableGPS();
        }
    }

    private void loadPlaces(Location location){
        QueryCondition conditions = new QueryCondition();
        conditions.setUserLocation(location.getLatitude(), location.getLongitude());
        RestClient.service().placeReachable(conditions, new RestCallback<List<Place>>(this) {

            @Override
            public void success(List<Place> place, Response response) {
                if (place.size() != 0){
                    showPlaces();
                    ((PlacesAdapter)listPlaces.getAdapter()).addAll(place);
                }
            }
        });
    }

    private void showPlaces() {
        // TODO jean: affiche le layout qui contient les places et cache celui qui affiche le chargement de la position de l'utilisateur
    }

    private void showLoader(){
        // TODO jean: affiche un loader tant que l'on a pas la position GPS de l'utilisateur
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------
    //PROTECTED METHODS
    protected void startIntentServiceReverseGeocoding(Location location) {
        Log.d(TAG, "Starting IntentService to get use address from location");
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // INNER CLASSES

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(TAG, "Receive result from service: " + resultCode);

            // Show a toast comment if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(getApplicationContext(), R.string.address_found, Toast.LENGTH_LONG);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // GETTERS AND SETTERS

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // MISCELLANEOUS
    public void testClick(View view) {
        Intent intent = new Intent(this,TagActivity.class);
        startActivity(intent);
    }


}
