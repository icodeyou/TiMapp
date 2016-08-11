package com.timappweb.timapp.activities;


import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.EventsAdapter;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.Constants;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.MapAreaLoaderCallback;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.utils.DistanceHelper;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.utils.location.LocationManager;

import java.util.List;

public class LocateActivity extends BaseActivity implements LocationManager.LocationListener {

    private static final double MIN_LOCATION_CHANGED_RELOAD_PLACE   = 500.0;
    private static final long   UPDATE_SYNC_DELAY                   = 60 * 1000;
    private static final int LOADER_ID_EVENT_AROUND                 = 0;
    private static final int MARGIN_EVENT_MAX_REACHABLE             = 200;
    private String              TAG                                 = "LocateActivity";

    // ----------------------------------------------------------------------------------------------

    private RecyclerView            rvPlaces;
    private View                    progressView;
    private InputMethodManager      imm;
    private Menu                    mainMenu;
    private boolean                 eventsLoaded;
    private MapAreaLoaderCallback    mEventLoaderModel;
    private Loader<Object>          mEventLoader;

    // ----------------------------------------------------------------------------------------------
    //OVERRIDE METHODS
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "Creating LocateActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        initToolbar(true);

        progressView = findViewById(R.id.progress_view);
        rvPlaces = (RecyclerView) findViewById(R.id.list_places);
        eventsLoaded = false;

        initAdapterPlaces();
        LocationManager.addOnLocationChangedListener(this);

        mEventLoaderModel = new MapAreaLoaderCallback<Event>(this, DataSyncAdapter.SYNC_TYPE_MULTIPLE_SPOT, Event.class){
            @Override
            public void onLoadFinished(Loader<List<Event>> loader, List<Event> data) {
                super.onLoadFinished(loader, data);
                eventsLoaded = true;
                Log.d(TAG, "Loading " + data.size() + " viewPlace(s)");
                EventsAdapter placeAdapter = ((EventsAdapter) rvPlaces.getAdapter());
                placeAdapter.clear();
                if (data.size() != 0) {
                    placeAdapter.setData(data);
                    progressView.setVisibility(View.GONE);
                    rvPlaces.setVisibility(View.VISIBLE);
                    placeAdapter.notifyDataSetChanged();
                }
                else {
                    IntentsUtils.addPlace(LocateActivity.this);
                    finish();
                }
            }
        };

        mEventLoaderModel.setExpandSize(MARGIN_EVENT_MAX_REACHABLE);
        mEventLoaderModel.setSyncDelay(UPDATE_SYNC_DELAY);
        if (LocationManager.hasLastLocation()){
            mEventLoaderModel.setBounds(LocationManager.getLastLocation(), ConfigurationProvider.rules().place_max_reachable);
            initLoader();
        }

    }

    private void initLoader() {
        if (!mEventLoaderModel.hasBounds()){
            Util.appStateError(TAG, "Bounds for the loader model should be initialized before the model");
        }
        Log.d(TAG, "Initialize loader");
        mEventLoader = getSupportLoaderManager().initLoader(LOADER_ID_EVENT_AROUND, null, mEventLoaderModel);
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
        rvPlaces.setLayoutManager(new LinearLayoutManager(this));
        final EventsAdapter eventsAdapter = new EventsAdapter(this);
        rvPlaces.setAdapter(eventsAdapter);

        eventsAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Log.d(TAG, "Click on event adapter");
                Event event = eventsAdapter.getItem(position);
                IntentsUtils.viewSpecifiedEvent(LocateActivity.this, event);
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
        LocationManager.start(this);
    }

    @Override
    protected void onStop() {
        LocationManager.stop();
        super.onStop();
    }

    // ---------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------
    //PRIVATE METHODS

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        // if not loaded yet or if user location changed too much we need to reload places
        mEventLoaderModel.setBounds(newLocation, ConfigurationProvider.rules().place_max_reachable);
        if (mEventLoader == null){
            initLoader();
        }
        else{
            mEventLoader.startLoading();
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
