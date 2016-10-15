package com.timappweb.timapp.activities;


import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.EventsAdapter;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.MapAreaLoaderCallback;
import com.timappweb.timapp.data.loader.paginate.PaginateDataLoader;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.RetryDialog;
import com.twotoasters.jazzylistview.effects.TiltEffect;
import com.twotoasters.jazzylistview.recyclerview.JazzyRecyclerViewScrollListener;

import java.util.List;

public class LocateActivity extends BaseActivity implements LocationManager.LocationListener, PaginateDataLoader.Callback {

    private String              TAG                                 = "LocateActivity";

    // ----------------------------------------------------------------------------------------------

    private RecyclerView rvEvents;
    private View                    progressView;
    private InputMethodManager      imm;
    private Menu                    mainMenu;
    private MapAreaLoaderCallback<Event>    mEventLoaderModel;

    // ----------------------------------------------------------------------------------------------
    //OVERRIDE METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate);
        initToolbar(true);

        progressView = findViewById(R.id.progress_view);
        progressView.setVisibility(View.VISIBLE);
        rvEvents = (RecyclerView) findViewById(R.id.list_events);

        initRvAndAdapter();

        mEventLoaderModel = new MapAreaLoaderCallback<Event>();
        mEventLoaderModel
                .setDataProvider(new PaginateDataLoader.DataProvider() {
                    @Override
                    public HttpCallManager<PaginatedResponse> remoteLoad(PaginateDataLoader.PaginateRequestInfo info) {
                        LatLngBounds bounds = LocationManager.generateBoundsAroundLocation(
                                LocationManager.getLastLocation(),
                                ConfigurationProvider.rules().place_max_reachable
                        );
                        RestQueryParams options = info.getQueryParams()
                                .setBounds(bounds);
                        return RestClient.buildCall(RestClient.service().places(options.toMap()));
                    }
                });

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
                IntentsUtils.addEvent(LocateActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        LocationManager.start(this);
        LocationManager.addOnLocationChangedListener(this);
        mEventLoaderModel.setCallback(this);
        if (LocationManager.hasLastLocation()){
            mEventLoaderModel.loadNextPage();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopping LocateActivity");
        LocationManager.removeLocationListener(this);
        LocationManager.stop(this);
        mEventLoaderModel.setCallback(null);
        mEventLoaderModel.stop();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        // if not loaded yet or if user location changed too much we need to reload places
        if (!mEventLoaderModel.isLoading()){
            if (lastLocation == null){
                mEventLoaderModel.loadNextPage();
            }
            else{
                mEventLoaderModel
                        .clear()
                        .loadNextPage();
            }
        }
    }

    @Override
    public void onLoadEnd(PaginateDataLoader.PaginateRequestInfo info, List data) {
        Log.d(TAG, "Loading " + data.size() + " event(s) around user.");
        EventsAdapter placeAdapter = ((EventsAdapter) rvEvents.getAdapter());
        placeAdapter.clear();
        if (data.size() != 0) {
            placeAdapter.setData(data);
            progressView.setVisibility(View.GONE);
            rvEvents.setVisibility(View.VISIBLE);
            placeAdapter.notifyDataSetChanged();
        }
        else {
            IntentsUtils.addEvent(LocateActivity.this);
        }
    }

    @Override
    public void onLoadError(Throwable error, PaginateDataLoader.PaginateRequestInfo info) {
        progressView.setVisibility(View.GONE);
        RetryDialog.builder(this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mEventLoaderModel
                                .clear()
                                .loadNextPage();
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }
    // ---------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------
    //PRIVATE METHODS

    private void initRvAndAdapter() {
        JazzyRecyclerViewScrollListener jazzyRecyclerViewScrollListener = new JazzyRecyclerViewScrollListener();
        jazzyRecyclerViewScrollListener.setTransitionEffect(new TiltEffect());
        rvEvents.addOnScrollListener(jazzyRecyclerViewScrollListener);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        final EventsAdapter eventsAdapter = new EventsAdapter(this);
        rvEvents.setAdapter(eventsAdapter);

        eventsAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Log.d(TAG, "Click on event adapter");
                Event event = eventsAdapter.getItem(position);
                IntentsUtils.viewSpecifiedEvent(LocateActivity.this, event);
            }

        });
    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // INNER CLASSES
/*
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(TAG, "Receive result from service: " + resultCode);

            // Show a toast comment if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Toast.makeText(getApplicationContext(), R.string.address_found, Toast.LENGTH_LONG).builder();
            }
        }
    }*/


}
