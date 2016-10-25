package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SpotCategoriesAdapter;
import com.timappweb.timapp.adapters.SpotsAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.SpotItem;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.RecyclerViewManager;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateDataLoader;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateManager;
import com.timappweb.timapp.data.loader.paginate.PaginateDataLoader;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.utils.SerializeHelper;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.CategorySelectorView;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

public class AddSpotActivity extends BaseActivity implements
        //LocationManager.LocationListener,
        OnMapReadyCallback, CursorPaginateDataLoader.Callback<Spot> {

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
    private CursorPaginateDataLoader<Spot, Spot> mDataLoader;

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

        initEditText();
        initAdapters();
        setListeners();
        initDataLoader();

        Util.appAssert(LocationManager.hasLastLocation(), TAG, "A last location must be set to add a spot");
    }


    private void initDataLoader() {
        LatLngBounds bounds = LocationManager.generateBoundsAroundLocation(
                LocationManager.getLastLocation(),
                ConfigurationProvider.rules().place_max_reachable
        );
        RestQueryParams options = new RestQueryParams().setBounds(bounds);

        mDataLoader = CursorPaginateDataLoader.<Spot,Spot>create(
                    "spots/index",
                    Spot.class
                )
                .setQueryParams(options.toMap())
                .addFilter(CursorPaginateDataLoader.PaginateFilter.createCreatedFilter())
                .addFilter(CursorPaginateDataLoader.PaginateFilter.createIdFilter());
                //.setLimit(LOCAL_LOAD_LIMIT);

        new CursorPaginateManager<>(this, mAdapter, mDataLoader)
                .setItemTransformer(new RecyclerViewManager.ItemTransformer<Spot>() {
                    @Override
                    public AbstractFlexibleItem createItem(Spot data) {
                        return new SpotItem(data);
                    }
                })
                .setClearOnRefresh(true)
                .setCallback(this)
                .setMinDelayForceRefresh(MIN_DELAY_FORCE_REFRESH)
                .setNoDataView(noDataView)
                .setSwipeRefreshLayout(mSwipeAndRefreshLayout)
                .enableEndlessScroll()
                .load();
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
                //setButtonValidation();
            }
        });

        spotCategoriesAdapterAll.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                setCategory(spotCategoriesAdapterAll.getCategory(position));
                //setButtonValidation();
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
        spotsRv.setAdapter(mAdapter);
    }

    private void setListeners() {
        //If click on editText when Not Focused
        etNameSpot.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                categorySelector.lowerView();
            }
        });

        //If click on editText when Focused
        etNameSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categorySelector.lowerView();
            }
        });

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

                //setButtonValidation();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_add_spot, menu);
        getSpotAndBind();
        //setButtonValidation();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_create:
                if(!Spot.isValidName(currentSpot.getName())) {
                    Toast.makeText(AddSpotActivity.this, R.string.error_no_name_spot, Toast.LENGTH_SHORT).show();
                    return true;
                }
                if(currentSpot.getCategory() == null) {
                    Toast.makeText(AddSpotActivity.this, R.string.error_no_category_spot, Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (spotAlreadyExist()){
                    Toast.makeText(AddSpotActivity.this, R.string.cannot_create_same_spot, Toast.LENGTH_SHORT).show();
                    return true;
                }
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

    public int getNumberOfMainCategories() {
        return NUMBER_OF_MAIN_CATEGORIES;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public boolean spotAlreadyExist() {
        for (int i=0; i < mAdapter.getItemCount(); i++){
            Spot spot = mAdapter.getSpot(i);
            if (spot.name.equals(currentSpot.name)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onLoadEnd(List<Spot> data, CursorPaginateDataLoader.LoadType type, boolean overwrite) {
        mAdapter.createItemsCopy();
    }

    @Override
    public void onLoadError(Throwable error, CursorPaginateDataLoader.LoadType type) {

    }

    @Override
    public void onLoadStart(CursorPaginateDataLoader.LoadType type) {

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
