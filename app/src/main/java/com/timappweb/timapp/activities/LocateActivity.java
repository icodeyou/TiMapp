package com.timappweb.timapp.activities;


import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.model.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.services.FetchAddressIntentService;
import com.timappweb.timapp.config.Constants;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.utils.Util;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class LocateActivity extends BaseActivity{

    private String TAG = "LocateActivity";

    //Views
    private RecyclerView    rvPlaces;
    private View            noPlaceView;
    private View            buttonAddPlace;
    private TextView        textButtonAddPlace;
    private View            noConnectionView;

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
        progressView = findViewById(R.id.progress_view);
        noPlaceView = findViewById(R.id.layout_if_no_place);
        rvPlaces = (RecyclerView) findViewById(R.id.list_places);
        buttonAddPlace = findViewById(R.id.button_add_spot);
        noConnectionView = findViewById(R.id.no_connection_view);

        // Init variables
        mResultReceiver = new AddressResultReceiver(new Handler());

        setListeners();
        initAdapterPlaces();
        initLocationListener();

    }

    private void initAdapterPlaces() {
        final LocateActivity that = this;

        //RV
        rvPlaces.setLayoutManager(new LinearLayoutManager(this));

        //Adapter
        final PlacesAdapter placesAdapter = new PlacesAdapter(this, true);
        rvPlaces.setAdapter(placesAdapter);

        placesAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Log.d(TAG, "Click on place adapter");
                if (!MyApplication.hasFineLocation()) {
                    Toast.makeText(getApplicationContext(), R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
                    return;
                }
                // We know that lastLocation is define because places are loaded only when location is defined
                Place place = placesAdapter.getItem(position);
                Post post = new Post();
                post.longitude = MyApplication.getLastLocation().getLongitude();
                post.latitude = MyApplication.getLastLocation().getLatitude();
                IntentsUtils.addPostStepTags(that, place, post);
            }

        });
    }

    private void setListeners() {
        final LocateActivity that = this;

        buttonAddPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.addPlace(that);
                //buttonAddPlace.setEnabled(false);
            }
        });
        //buttonAddPlace.setOnTouchListener(new ColorTopRadiusOnTouchListener(this, textButtonAddPlace));
    }

    @Override
    protected void onPause()
    {
        super.onPause();
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
    // ----------------------------------------------------------------------------------------------
    //PRIVATE METHODS

    /**
     * Load places once user name is known
     */
    private void initLocationListener() {
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "New location found for locate activity");
                MyApplication.setLastLocation(location);
                if (MyApplication.hasFineLocation()){
                    loadPlaces(location);
                }
                //startIntentServiceReverseGeocoding(location);
            }
        };

        initLocationProvider(mLocationListener);
    }


    private void loadPlaces(Location location){
        final Activity activity = this;
        Log.d(TAG, "Loading places with location: " + Util.print(location));
        QueryCondition conditions = new QueryCondition();
        conditions.setUserLocation(location.getLatitude(), location.getLongitude());

        Call<List<Place>> call = RestClient.service().placeReachable(conditions.toMap());
        call.enqueue(new RestCallback<List<Place>>(this) {

            @Override
            public void onResponse(Response<List<Place>> response) {
                super.onResponse(response);

                if (response.isSuccess()){
                    List<Place> places = response.body();
                    Log.d(TAG, "Loading " + places.size() + " viewPlaceFromPublish(s)");
                    PlacesAdapter placeAdapter = ((PlacesAdapter) rvPlaces.getAdapter());
                    placeAdapter.clear();
                    progressView.setVisibility(View.GONE);
                    buttonAddPlace.setVisibility(View.VISIBLE);
                    if (places.size() != 0) {
                        placeAdapter.setData(places);
                        noPlaceView.setVisibility(View.GONE);
                        rvPlaces.setVisibility(View.VISIBLE);
                    } else {
                        IntentsUtils.addPlace(activity);
                    }
                    placeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                progressView.setVisibility(View.GONE);
                noConnectionView.setVisibility(View.VISIBLE);
            }
        });
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
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
    // GETTERS AND SETTERS

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // MISCELLANEOUS


}
