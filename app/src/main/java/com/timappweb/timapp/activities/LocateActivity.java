package com.timappweb.timapp.activities;


import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.EventsAdapter;
import com.timappweb.timapp.config.Constants;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.QueryCondition;
import com.timappweb.timapp.utils.DistanceHelper;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.utils.location.LocationManager;

import java.util.List;

import retrofit2.Call;

public class LocateActivity extends BaseActivity implements LocationManager.LocationListener {

    private static final double MIN_LOCATION_CHANGED_RELOAD_PLACE = 500.0;
    private String TAG = "LocateActivity";

    //Views
    private RecyclerView    rvPlaces;

    // ProgressBar and ProgressDialog
    private View progressView;

    //others
    private InputMethodManager imm;
    private Menu mainMenu;

    private LocationListener mLocationListener;
    private boolean eventsLoaded;

    // ----------------------------------------------------------------------------------------------
    //OVERRIDE METHODS
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "Creating LocateActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);

        initToolbar(true);

        //Initialize variables
        progressView = findViewById(R.id.progress_view);
        //noPlaceView = findViewById(R.id.layout_if_no_place);
        rvPlaces = (RecyclerView) findViewById(R.id.list_places);

        // Init variables
        eventsLoaded = false;

        initAdapterPlaces();

        //int colorRes = ContextCompat.getColor(this, R.color.colorPrimary);
        //initToolbar(false, colorRes);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_locate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_skip:
                IntentsUtils.addPlace(LocateActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initAdapterPlaces() {
        final LocateActivity that = this;

        //RV
        rvPlaces.setLayoutManager(new LinearLayoutManager(this));

        //Adapter
        final EventsAdapter eventsAdapter = new EventsAdapter(this);
        rvPlaces.setAdapter(eventsAdapter);

        eventsAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Log.d(TAG, "Click on event adapter");
                /*if (!MyApplication.hasFineLocation()) {
                    Toast.makeText(getApplicationContext(), R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
                    return;
                }
                // We know that lastLocation is define because places are loaded only when location is defined
                Event event = eventsAdapter.getItem(position);
                EventPost eventPost = new EventPost();
                eventPost.longitude = MyApplication.getLastLocation().getLongitude();
                eventPost.latitude = MyApplication.getLastLocation().getLatitude();*/
                Event event = eventsAdapter.getItem(position);
                IntentsUtils.viewSpecifiedEvent(that, event);
            }

        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocationManager.addOnLocationChangedListener(this);
        LocationManager.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    // ---------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------
    //PRIVATE METHODS



    // TODO migrate to service
    private void loadPlaces(Location location){
        Log.d(TAG, "Loading places with location: " + Util.print(location));
        QueryCondition conditions = new QueryCondition();
        conditions.setUserLocation(location.getLatitude(), location.getLongitude());

        Call<List<Event>> call = RestClient.service().placeReachable(conditions.toMap());

        RestClient.buildCall(call)
                .onResponse(new HttpCallback<List<Event>>() {
                    @Override
                    public void successful(List<Event> events) {
                        eventsLoaded = true;
                        Log.d(TAG, "Loading " + events.size() + " viewPlace(s)");
                        EventsAdapter placeAdapter = ((EventsAdapter) rvPlaces.getAdapter());
                        placeAdapter.clear();
                        if (events.size() != 0) {
                            placeAdapter.setData(events);
                            progressView.setVisibility(View.GONE);
                            rvPlaces.setVisibility(View.VISIBLE);
                            placeAdapter.notifyDataSetChanged();
                        } else {
                            IntentsUtils.addPlace(LocateActivity.this);
                            finish();
                        }
                    }

                    @Override
                    public void notSuccessful() {
                        super.notSuccessful();
                    }
                })
                .perform();
    }

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        // if not loaded yet or if user location changed too much we need to reload places
        if (eventsLoaded == false || (lastLocation != null && DistanceHelper.distFrom(newLocation, lastLocation) > MIN_LOCATION_CHANGED_RELOAD_PLACE)) {
            loadPlaces(newLocation);
        }
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
            }
        }
    }


}
