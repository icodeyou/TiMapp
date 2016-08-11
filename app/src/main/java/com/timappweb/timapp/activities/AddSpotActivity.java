package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SpotCategoriesAdapter;
import com.timappweb.timapp.adapters.SpotsAdapter;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.Constants;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.MapAreaLoaderCallback;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.location.ReverseGeocodingHelper;
import com.timappweb.timapp.views.CategorySelectorView;

import java.util.List;

public class AddSpotActivity extends BaseActivity implements LocationManager.LocationListener, OnMapReadyCallback {

    private static final String         TAG                             = "AddSpotActivity";
    private static final float          ZOOM_LEVEL_CENTER_MAP           = 15.0f;
    private static final int            LOADER_ID_SPOT_AROUND           = 0;
    private static final int            NUMBER_OF_MAIN_CATEGORIES       = 4;
    private static final long           UPDATE_SYNC_DELAY               = 60 * 1000;
    private static final int            MARGIN_PLACE_MAX_REACHABLE      = 100;

    // ---------------------------------------------------------------------------------------------

    private InputMethodManager                      imm;

    private Spot                                    currentSpot;

    //private ImageView showCategoriesButton;
    private EditText                                etCustomPlace;
    private RecyclerView                            spotsRv;
    private CategorySelectorView                    categorySelector;
    private GoogleMap                               gMap;
    private AddressResultReceiver                   mAddressResultReceiver;
    private Menu                                    menu;
    private Loader<List<Spot>>                      mSpotLoader;
    private SpotsAdapter                            spotsAdapter;
    private SpotCategory                            categorySelected;
    private MapAreaLoaderCallback mSpotLoaderModel;
    private SwipeRefreshLayout                      mSwipeAndRefreshLayout;

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);

        if (!LocationManager.hasUpToDateLastLocation()){
            Log.e(TAG, "User launch AddSpotActivity without a up to date location. Refused");
            finish();
            return;
        }

        //Toolbar
        this.initToolbar(true);

        //Initialize
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        spotsRv = (RecyclerView) findViewById(R.id.spots_rv);
        categorySelector = (CategorySelectorView) findViewById(R.id.category_selector);
        etCustomPlace = (EditText) findViewById(R.id.name_spot);
        mSwipeAndRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        initEditText();
        initAdapters();
        setListeners();

        mSpotLoaderModel = new MapAreaLoaderCallback<Spot>(this, DataSyncAdapter.SYNC_TYPE_MULTIPLE_SPOT, Spot.class){
            @Override
            public void onLoadFinished(Loader<List<Spot>> loader, List<Spot> data) {
                super.onLoadFinished(loader, data);
                spotsAdapter.setData(data);

            }
        };
        mSpotLoaderModel.setExpandSize(MARGIN_PLACE_MAX_REACHABLE);
        mSpotLoaderModel.setSwipeAndRefreshLayout(mSwipeAndRefreshLayout);
        mSpotLoaderModel.setSyncDelay(UPDATE_SYNC_DELAY);
        if (LocationManager.hasLastLocation()){
            mSpotLoaderModel.setBounds(LocationManager.getLastLocation(), ConfigurationProvider.rules().place_max_reachable);
            initLoader();
        }

    }

    /**
     * @warning: it must be initialized only when the model has a LatLngBounds, otherwise null pointer exception
     */
    private void initLoader() {
        if (!mSpotLoaderModel.hasBounds()){
            Util.appStateError(TAG, "Bounds for the loader model should be initialized before the model");
        }
        Log.d(TAG, "Initialize loader");
        mSpotLoader = getSupportLoaderManager().initLoader(LOADER_ID_SPOT_AROUND, null, mSpotLoaderModel);
    }
    private void initEditText() {
        InputFilter[] f = new InputFilter[1];
        f[0] = new InputFilter.LengthFilter(ConfigurationProvider.rules().spot_min_name_length);
        etCustomPlace.setFilters(f);
        //TODO Steph : Mettre une config pour le max !
        etCustomPlace.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etCustomPlace.clearFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_add_spot, menu);
        getSpotAndBind(); //Method called here because it causes menu button validation in EditText Listener
        setButtonValidation();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_create:
                finishActivityResult(currentSpot);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getSpotAndBind() {
        currentSpot = IntentsUtils.extractSpot(getIntent());
        if (currentSpot == null){
            currentSpot = new Spot();
        } else {
            categorySelector.selectCategoryUI(currentSpot.name, currentSpot.getCategory().getSmallIcon());
            etCustomPlace.setText(currentSpot.name);
            etCustomPlace.setSelection(currentSpot.name.length());
        }
        if (LocationManager.hasLastLocation()){
            requestReverseGeocoding(LocationManager.getLastLocation());
        }
    }

    private void initAdapters() {
        final SpotCategoriesAdapter spotCategoriesAdapterMain = new SpotCategoriesAdapter(this,false);
        final SpotCategoriesAdapter spotCategoriesAdapterAll = new SpotCategoriesAdapter(this,true);

        spotCategoriesAdapterMain.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                setCategory(spotCategoriesAdapterMain.getCategory(position));
                setButtonValidation();
            }
        });

        spotCategoriesAdapterAll.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                setCategory(spotCategoriesAdapterAll.getCategory(position));
                setButtonValidation();
            }
        });

        categorySelector.setAdapters(spotCategoriesAdapterMain, spotCategoriesAdapterAll);

        spotsRv.setLayoutManager(new LinearLayoutManager(this));
        spotsAdapter = new SpotsAdapter(this);
        spotsAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                currentSpot = spotsAdapter.getItem(position);
                finishActivityResult(currentSpot);
            }
        });
        spotsRv.setAdapter(spotsAdapter);
    }

    private void setCategory(SpotCategory spotCategory) {
        Spot spot = currentSpot;
        spot.setCategory(spotCategory);
        categorySelector.selectCategoryUI(spotCategory.name,spotCategory.getSmallIcon());
        etCustomPlace.requestFocus();
    }

    private void setListeners() {

        etCustomPlace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentSpot.setName(s.toString());
                setButtonValidation();
            }
        });
    }

    private void finishActivityResult(Spot spot){
        Log.d(TAG, "Spot chose: " + spot);
        Intent intent = new Intent(this, AddEventActivity.class);
        intent.putExtra(IntentsUtils.KEY_SPOT, spot);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void setButtonValidation() {
        menu.findItem(R.id.action_create).setEnabled(currentSpot.isValid());
    }

    public int getNumberOfMainCategories() {
        return NUMBER_OF_MAIN_CATEGORIES;
    }

    @Override
    public void onPause() {
        //mapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        //mapView.onResume();
        super.onResume();
        Log.d(TAG, "ExploreMapFragment.onResume()");
        //this.loadMapIfNeeded();
    }

/*    private void loadMapIfNeeded() {
        try {
            if (gMap == null){
                gMap = mapView.getMap();
            }
            gMap.setIndoorEnabled(true);
            Location location = LocationManager.getLastLocation();
            if (location != null){
                updateMapCenter(location);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMap(){
        mapView.onCreate(null);
        mapView.getMapAsync(this);
        gMap = mapView.getMap();
        gMap.setIndoorEnabled(true);
        gMap.setMyLocationEnabled(true);
    }

    private void updateMapCenter(Location location){
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_LEVEL_CENTER_MAP));
    }*/


    private void requestReverseGeocoding(Location location){
        if (mAddressResultReceiver == null){
            mAddressResultReceiver = new AddressResultReceiver();
        }
        currentSpot.setLocation(location);
        ReverseGeocodingHelper.request(AddSpotActivity.this, location, mAddressResultReceiver);
    }
    // =============================================================================================

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        //requestReverseGeocoding(newLocation);
        mSpotLoaderModel.setBounds(newLocation, ConfigurationProvider.rules().place_max_reachable);
        if (mSpotLoader == null){
            initLoader();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        LocationManager.addOnLocationChangedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocationManager.removeLocationListener(this);
    }

    // =============================================================================================

    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver() {
            super(new Handler());
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(TAG, "Receive result from service: " + resultCode);
            if (resultCode == Constants.SUCCESS_RESULT) {
                String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
                currentSpot.setAddress(addressOutput);
            }
        }
    }

}
