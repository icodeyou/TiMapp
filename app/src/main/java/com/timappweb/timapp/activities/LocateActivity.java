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

import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.EventsAdapter;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.Constants;
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
import com.twotoasters.jazzylistview.effects.TiltEffect;
import com.twotoasters.jazzylistview.recyclerview.JazzyRecyclerViewScrollListener;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class LocateActivity extends BaseActivity implements LocationManager.LocationListener, PaginateDataLoader.Callback {

    private static final double MIN_LOCATION_CHANGED_RELOAD_PLACE   = 500.0;
    private static final long   UPDATE_SYNC_DELAY                   = 60 * 1000;
    private static final int LOADER_ID_EVENT_AROUND                 = 0;
    private static final int MARGIN_EVENT_MAX_REACHABLE             = 200;
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
        LocationManager.addOnLocationChangedListener(this);

        mEventLoaderModel = new MapAreaLoaderCallback<Event>();
        mEventLoaderModel
                .setCallback(this)
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

        if (LocationManager.hasLastLocation()){
            mEventLoaderModel.loadNextPage();
        }

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

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        // if not loaded yet or if user location changed too much we need to reload places
        if (lastLocation == null){
            mEventLoaderModel.loadNextPage();
        }
        else{
            mEventLoaderModel
                    .clear()
                    .loadNextPage();
        }
    }

    @Override
    public void onLoadEnd(PaginateDataLoader.PaginateRequestInfo info, List data) {
        Log.d(TAG, "Loading " + data.size() + " viewPlace(s)");
        EventsAdapter placeAdapter = ((EventsAdapter) rvEvents.getAdapter());
        placeAdapter.clear();
        if (data.size() != 0) {
            placeAdapter.setData(data);
            progressView.setVisibility(View.GONE);
            rvEvents.setVisibility(View.VISIBLE);
            placeAdapter.notifyDataSetChanged();
        }
        else {
            IntentsUtils.addPlace(LocateActivity.this);
            finish();
        }
    }

    @Override
    public void onLoadError(Throwable error, PaginateDataLoader.PaginateRequestInfo info) {
        progressView.setVisibility(View.GONE);
        // TODO
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
