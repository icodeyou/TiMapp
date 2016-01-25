package com.timappweb.timapp.activities;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.services.FetchAddressIntentService;
import com.timappweb.timapp.config.Constants;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.utils.Util;

import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class LocateActivity extends BaseActivity{

    private String TAG = "LocateActivity";

    //Views
    private ListView    listPlaces;
    private View        placesAndBottomLine;
    private View        noPlaceView;

    // ProgressBar and ProgressDialog
    private View progressView;

    // Location
    private AddressResultReceiver       mResultReceiver;        // For reverse geocoding

    //others
    private InputMethodManager imm;
    private Menu mainMenu;

    private LocationListener mLocationListener;

    // ----------------------------------------------------------------------------------------------
    //OVERRIDE METHODS
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "Creating LocateActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        this.initToolbar(false);

        //Initialize variables
        this.progressView = findViewById(R.id.progress_view);
        this.placesAndBottomLine = findViewById(R.id.places_and_bottom_line);
        this.noPlaceView = findViewById(R.id.layout_if_no_place);
        listPlaces = (ListView) findViewById(R.id.list_places);
        Button buttonAddSpot = (Button) findViewById(R.id.button_add_spot);

        // -----------------------------------------------------------------------------------------
        // Init variables
        final LocateActivity that = this;
        mResultReceiver = new AddressResultReceiver(new Handler());
        final PlacesAdapter placesAdapter = new PlacesAdapter(this);

        placesAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Log.d(TAG, "Click on place adapter");
                // We know that lastLocation is define because places are loaded only when location is defined
                Place place = placesAdapter.getItem(position);
                Post post = new Post();
                post.longitude = MyApplication.lastLocation.getLongitude();
                post.latitude = MyApplication.lastLocation.getLatitude();
                IntentsUtils.addPostStepTags(that, place, post);
            }

        });

        listPlaces.setAdapter(placesAdapter);


        //Listeners
        buttonAddSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.addPlace(that);
            }
        });

        //if (BuildConfig.DEBUG){
        //   placesAdapter.generateDummyData();
        //}

        initLocationListener();


    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------
    //PRIVATE METHODS

    /**
     * Load places once user name is known
     */
    private void initLocationListener() {
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "Location has changed: " + Util.print(location));
                loadPlaces(location);
                MyApplication.lastLocation = location;
                //startIntentServiceReverseGeocoding(location);
            }
        };

        initLocationProvider(mLocationListener);
    }


    private void loadPlaces(Location location){
        Log.d(TAG, "Loading places with location: " + Util.print(location));
        QueryCondition conditions = new QueryCondition();
        conditions.setUserLocation(location.getLatitude(), location.getLongitude());
        RestClient.service().placeReachable(conditions.toMap(), new RestCallback<List<Place>>(this) {

            @Override
            public void success(List<Place> place, Response response) {
                Log.d(TAG, "Loading " + place.size() + " viewPlaceFromPublish(s)");
                PlacesAdapter placeAdapter = ((PlacesAdapter) listPlaces.getAdapter());
                placeAdapter.clear();
                progressView.setVisibility(View.GONE);
                if (place.size() != 0) {
                    placeAdapter.addAll(place);
                    noPlaceView.setVisibility(View.GONE);
                    placesAndBottomLine.setVisibility(View.VISIBLE);
                } else {
                    noPlaceView.setVisibility(View.VISIBLE);
                }
                placeAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                super.failure(error);
                Toast.makeText(this.context, R.string.error_server_unavailable, Toast.LENGTH_LONG).show();
            }

        });
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------
    //PROTECTED METHODS
    protected void startIntentServiceReverseGeocoding(Location location) {
        Log.d(TAG, "Starting IntentService to get use address from name");
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
                Toast.makeText(getApplicationContext(), R.string.address_found, Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), R.string.address_found, Toast.LENGTH_LONG).show();
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


}
