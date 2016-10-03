package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SpotCategoriesAdapter;
import com.timappweb.timapp.adapters.SpotsAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.SpotItem;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.Constants;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.RecyclerViewManager;
import com.timappweb.timapp.data.loader.paginate.PaginateDataLoader;
import com.timappweb.timapp.data.loader.paginate.PaginateRecyclerViewManager;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.SerializeHelper;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.location.ReverseGeocodingHelper;
import com.timappweb.timapp.views.CategorySelectorView;

import java.util.List;

import com.timappweb.timapp.views.SwipeRefreshLayout;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import io.fabric.sdk.android.Fabric;

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
    //private AddressResultReceiver                   mAddressResultReceiver;
    private Menu                                    menu;
    private SpotsAdapter mAdapter;
    private SwipeRefreshLayout                      mSwipeAndRefreshLayout;
    private View                                    progressView;
    private View                                    noDataView;
    private PaginateDataLoader mDataLoader;

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);

        //Toolbar
        this.initToolbar(true);

        //Initialize
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        spotsRv = (RecyclerView) findViewById(R.id.spots_rv);
        categorySelector = (CategorySelectorView) findViewById(R.id.category_selector);
        etNameSpot = (EditText) findViewById(R.id.name_spot);
        mSwipeAndRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        progressView = findViewById(R.id.progress_view);
        noDataView = findViewById(R.id.no_data_view);

        // TODO : TUTO (ShowCaseView): If the place already exists in the list below, you can just select it.

        initEditText();
        initAdapters();
        setListeners();
        initDataLoader();

        new PaginateRecyclerViewManager(this, mAdapter, mDataLoader)
                .setItemTransformer(new RecyclerViewManager.ItemTransformer<Spot>() {
                    @Override
                    public AbstractFlexibleItem createItem(Spot data) {
                        return new SpotItem(data);
                    }
                })
                .setSwipeRefreshLayout(mSwipeAndRefreshLayout)
                .setCallback(this)
                .setNoDataView(noDataView)
                ;

        if (LocationManager.hasLastLocation()){
            mDataLoader.loadNextPage();
        }
    }


    private void initDataLoader() {
        //TODO Steph : progressView.setVisibility(View.VISIBLE);
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
                .setMinDelayForceRefresh(MIN_DELAY_FORCE_REFRESH)
                .setMinDelayAutoRefresh(MIN_DELAY_AUTO_REFRESH)
                .setCallback(this)
                .setDataProvider(mDataProvider);
    }



    private void initEditText() {
        InputFilter[] f = new InputFilter[1];
        f[0] = new InputFilter.LengthFilter(ConfigurationProvider.rules().spots_max_name_length);
        etNameSpot.setFilters(f);
        etNameSpot.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        etNameSpot.findFocus();
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
        mAdapter = new SpotsAdapter(this);
        mAdapter.initializeListeners(new FlexibleAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(int position) {
                try {
                    currentSpot = (Spot) mAdapter.getSpot(position).requireLocalId();
                } catch (CannotSaveModelException e) {
                }
                finishActivityResult(currentSpot);
                return false;
            }
        });
        mAdapter.initializeListeners(new FlexibleAdapter.OnUpdateListener() {
            @Override
            public void onUpdateEmptyView(int size) {

            }
        });
        spotsRv.setAdapter(mAdapter);
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
                spotsRv.scrollToPosition(0);
                currentSpot.setName(newText);
                if (mAdapter.hasNewSearchText(newText)) {
                    Log.d(TAG, "onQueryTextChange newText: " + newText);
                    mAdapter.setSearchText(newText);
                    //Fill and Filter mItems with your custom list and automatically animate the changes
                    //Watch out! The original list must be a copy
                    mAdapter.filterItems(mAdapter.getItemsCopy(), 200L);
                    if(mAdapter.getItemCount() == 0) {
                        noDataView.setVisibility(View.VISIBLE);
                    }
                }

                setButtonValidation();
            }
        });
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
            //requestReverseGeocoding(LocationManager.getLastLocation());
        }
    }

    private void setCategory(SpotCategory spotCategory) {
        currentSpot.setCategory(spotCategory);
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


    /*
    private void requestReverseGeocoding(Location location){
        if (mAddressResultReceiver == null){
            mAddressResultReceiver = new AddressResultReceiver();
        }
        currentSpot.setLocation(location);
        ReverseGeocodingHelper.request(AddSpotActivity.this, location, mAddressResultReceiver);
    }
    */
    // =============================================================================================

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        //requestReverseGeocoding(newLocation);

        if (lastLocation == null){
            mDataLoader.loadNextPage();
        }
        else {
            Toast.makeText(this, R.string.user_location_changed_reload_data, Toast.LENGTH_LONG).show();
            mSwipeAndRefreshLayout.setEnabled(true);
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
        // If we need more logic
        mAdapter.createItemsCopy();
    }

    @Override
    public void onLoadError(Throwable error, PaginateDataLoader.PaginateRequestInfo info) {
        // If we need more logic
    }

    // =============================================================================================
/*
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
    }*/
}
