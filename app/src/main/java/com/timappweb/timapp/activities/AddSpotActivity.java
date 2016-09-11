package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SpotCategoriesAdapter;
import com.timappweb.timapp.adapters.SpotsAdapter;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.Constants;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.MapAreaLoaderCallback;
import com.timappweb.timapp.data.loader.paginate.PaginateDataLoader;
import com.timappweb.timapp.data.loader.sections.SectionDataLoader;
import com.timappweb.timapp.data.loader.sections.SectionDataProviderInterface;
import com.timappweb.timapp.data.loader.sections.SectionContainer;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.SerializeHelper;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.location.ReverseGeocodingHelper;
import com.timappweb.timapp.views.CategorySelectorView;

import java.util.ArrayList;
import java.util.List;

import com.timappweb.timapp.views.RefreshableRecyclerView;
import com.timappweb.timapp.views.SwipeRefreshLayout;

public class AddSpotActivity extends BaseActivity implements LocationManager.LocationListener, OnMapReadyCallback, PaginateDataLoader.Callback {

    private static final String         TAG                             = "AddSpotActivity";
    private static final float          ZOOM_LEVEL_CENTER_MAP           = 15.0f;
    private static final int            LOADER_ID_SPOT_AROUND           = 0;
    private static final int            NUMBER_OF_MAIN_CATEGORIES       = 4;
    private static final long           MIN_DELAY_FORCE_REFRESH         = 30 * 1000;
    private static final long           MIN_DELAY_AUTO_REFRESH          = 60 * 1000;
    private static final long           REMOTE_LOAD_LIMIT = 5;

    // ---------------------------------------------------------------------------------------------

    private InputMethodManager                      imm;

    private Spot                                    currentSpot;

    //private ImageView showCategoriesButton;
    private EditText                                etNameSpot;
    private RecyclerView                            spotsRv;
    private CategorySelectorView                    categorySelector;
    private AddressResultReceiver                   mAddressResultReceiver;
    private Menu                                    menu;
    private SpotsAdapter                            spotsAdapter;
    private SwipeRefreshLayout                      mSwipeAndRefreshLayout;

    private PaginateDataLoader mDataLoader;

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);

        if (!LocationManager.hasUpToDateLastLocation()){
            Log.e(TAG, "User launch AddSpotActivity without a up to date location. Refused");
            // TODO use feedback
            finish();
            return;
        }

        //Toolbar
        this.initToolbar(true);

        //Initialize
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        spotsRv = (RecyclerView) findViewById(R.id.spots_rv);
        categorySelector = (CategorySelectorView) findViewById(R.id.category_selector);
        etNameSpot = (EditText) findViewById(R.id.name_spot);
        mSwipeAndRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        initEditText();
        initAdapters();
        setListeners();
        initDataLoader();

        if (LocationManager.hasLastLocation()){
            mDataLoader.loadNextPage();
        }
    }


    private void initDataLoader() {
        PaginateDataLoader.DataProvider<EventsInvitation> mDataProvider = new PaginateDataLoader.DataProvider<EventsInvitation>() {
            @Override
            public HttpCallManager<PaginatedResponse<EventsInvitation>> remoteLoad(PaginateDataLoader.PaginateRequestInfo info) {
                LatLngBounds bounds = LocationManager.generateBoundsAroundLocation(
                        LocationManager.getLastLocation(),
                        ConfigurationProvider.rules().place_max_reachable
                );

                RestQueryParams options = info.getQueryParams()
                        .setBounds(bounds);

                return RestClient.buildCall(RestClient.service().spots(options.toMap()));
            }

        };
        mDataLoader = new PaginateDataLoader<EventsInvitation>()
                .setCallback(this)
                .setDataProvider(mDataProvider);
    }



    private void initEditText() {
        InputFilter[] f = new InputFilter[1];
        f[0] = new InputFilter.LengthFilter(ConfigurationProvider.rules().spots_max_name_length);
        etNameSpot.setFilters(f);
        etNameSpot.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etNameSpot.clearFocus();
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

    private void setListeners() {

        etNameSpot.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String newText = s.toString();
                currentSpot.setName(newText);
                if (spotsAdapter != null && spotsAdapter.getData() != null){
                    final List<Spot> filteredSpotList = filter(spotsAdapter.getData(), newText);
                    spotsAdapter.animateTo(filteredSpotList);
                }

                spotsRv.scrollToPosition(0);
                setButtonValidation();
            }
        });
    }
    private List<Spot> filter(List<Spot> spots, String query) {
        query = query.toLowerCase();

        final List<Spot> filteredSpotList = new ArrayList<>();
        for (Spot spot : spots) {
            final String text = spot.getName().toLowerCase();
            if(text.contains(query)) {
                filteredSpotList.add(spot);
            }
        }
        return filteredSpotList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_add_spot, menu);
        getSpotAndBind();
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
            categorySelector.selectCategoryUI(currentSpot.getCategory().name, currentSpot.getCategory().getIconDrawable(this));
            etNameSpot.setText(currentSpot.name);
            etNameSpot.setSelection(currentSpot.name.length());
        }
        if (LocationManager.hasLastLocation()){
            requestReverseGeocoding(LocationManager.getLastLocation());
        }
    }

    private void setCategory(SpotCategory spotCategory) {
        Spot spot = currentSpot;
        spot.setCategory(spotCategory);
        categorySelector.selectCategoryUI(spotCategory.name,spotCategory.getIconDrawable(AddSpotActivity.this));
        etNameSpot.requestFocus();
    }

    private void finishActivityResult(Spot spot){
        Log.d(TAG, "Spot chose: " + spot);
        Intent intent = new Intent(this, AddEventActivity.class);
        intent.putExtra(IntentsUtils.KEY_SPOT, SerializeHelper.packModel(spot, Spot.class));
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
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "ExploreMapFragment.onResume()");
    }


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

        if (lastLocation == null){
            mDataLoader.loadNextPage();
        }
        else {
            mDataLoader
                    .clear()
                    .loadNextPage();
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

    @Override
    public void onLoadEnd(PaginateDataLoader.PaginateRequestInfo info, List data) {
        spotsAdapter.setData(data);
        if(currentSpot!=null) { // we can't call afterTextChanged before currentSpot is initialized
            etNameSpot.setText("");
        }
    }

    @Override
    public void onLoadError(Throwable error, PaginateDataLoader.PaginateRequestInfo info) {
        //Toast.makeText(this, R.string.cannot_load_spot_around, Toast.LENGTH_LONG).show();
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
