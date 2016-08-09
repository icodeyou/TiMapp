package com.timappweb.timapp.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.EventCategoriesAdapter;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.Constants;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.EventStatusManager;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.databinding.ActivityAddEventBinding;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.ResourceUrlMapping;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.AutoMergeCallback;
import com.timappweb.timapp.rest.callbacks.FormErrorsCallback;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.io.serializers.AddEventMapper;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.location.ReverseGeocodingHelper;
import com.timappweb.timapp.views.CategorySelectorView;


public class AddEventActivity extends BaseActivity implements LocationManager.LocationListener, OnMapReadyCallback {

    private String                  TAG                                 = "AddEventActivity";
    private static final float      ZOOM_LEVEL_CENTER_MAP               = 14.0f;
    private static final int        NUMBER_OF_MAIN_CATEGORIES           = 4;
    private static final int        CATEGORIES_COLUMNS                  = 4;

    //----------------------------------------------------------------------------------------------

    private InputMethodManager          imm;
    private EditText                    eventNameET;
    private EventCategory               eventCategorySelected;
    private View                        progressView;
    private EditText                    descriptionET;
    // Data
    private MapView                     mapView = null;
    private GoogleMap                   gMap;
    private ActivityAddEventBinding     mBinding;
    private View                        mBtnAddSpot;
    private View                        mSpotContainer;
    private AddressResultReceiver       mAddressResultReceiver;
    private View.OnClickListener        displayHideCategories;

    private Menu                        menu;
    private CategorySelectorView        categorySelector;
    //private View eventLocation;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_event);

        this.initToolbar(true);
        this.setStatusBarColor(R.color.colorSecondaryDark);
        this.extractSpot(savedInstanceState);

        //Initialize
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        descriptionET = (EditText)  findViewById(R.id.description_edit_text);
        eventNameET = (EditText) findViewById(R.id.event_name);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(ConfigurationProvider.rules().places_max_name_length);
        eventNameET.setFilters(filters);
        eventNameET.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        eventNameET.requestFocus();

        categorySelector = (CategorySelectorView) findViewById(R.id.category_selector);

        progressView = findViewById(R.id.progress_view);
        mapView = (MapView) findViewById(R.id.map);
        //mButtonAddPicture = findViewById(R.id.button_take_picture);
        mBtnAddSpot = findViewById(R.id.button_add_spot);
        mSpotContainer = findViewById(R.id.spot_container);

        Event event = new Event();
        if (LocationManager.hasLastLocation()){
            event.setLocation(LocationManager.getLastLocation());
        }
        mBinding.setEvent(event);

        initKeyboard();
        initAdapterAndManager();
        setListeners();
        //initViewPager();
        initLocationListener();
        initEvents();
        initMap();
    }

    private void initEvents() {
        /*
        mButtonAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.addPicture(AddEventActivity.this);
            }
        });*/
    }

    @Override
    public void onBackPressed() {
        finish();
    }
    //----------------------------------------------------------------------------------------------
    //Private methods

    /**
     * Load places once user name is known
     */
    private void initLocationListener() {
        LocationManager.addOnLocationChangedListener(this);
        LocationManager.start(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        LocationManager.stop();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_event, menu);
        this.menu = menu;
        setButtonValidation();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_post:
                if (LocationManager.hasFineLocation(ConfigurationProvider.rules().gps_min_accuracy_add_place)) {
                    setProgressView(true);
                    Event event = mBinding.getEvent();
                    event.setCategory(eventCategorySelected);
                    submitEvent(event);
                } else if (LocationManager.hasLastLocation()) {
                    Toast.makeText(getBaseContext(), R.string.no_fine_location, Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "Click on add event before having a user location");
                    Toast.makeText(getBaseContext(), R.string.waiting_for_location, Toast.LENGTH_LONG).show();
                }
                return true;
            case android.R.id.home:
                //Action to do in case of home button pressed
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initKeyboard() {
        eventNameET.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

    private void initAdapterAndManager() {
        //Main categories
        final EventCategoriesAdapter mainAdapter = new EventCategoriesAdapter(this,false);
        final EventCategoriesAdapter allAdapter = new EventCategoriesAdapter(this,true);

        mainAdapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                setCategory(mainAdapter.getCategory(position));
            }
        });
        allAdapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                setCategory(allAdapter.getCategory(position));
            }
        });

        categorySelector.setAdapters(mainAdapter, allAdapter);
    }

    private void setProgressView(boolean bool) {
        if(bool) {
            progressView.setVisibility(View.VISIBLE);
        }
        else {
            progressView.setVisibility(View.GONE);
        }
    }

    private void submitEvent(final Event event){
        Log.d(TAG, "Submit event " + event.toString());
        RestClient
            .post(ResourceUrlMapping.MODEL_EVENT, AddEventMapper.toJson(event))
                .onResponse(new AutoMergeCallback(event))
                .onResponse(new FormErrorsCallback(mBinding))
                .onResponse(new HttpCallback<JsonObject>() {
                    @Override
                    public void successful(JsonObject feedback) {
                        Log.d(TAG, "Event has been successfully added");
                        event.setAuthor(MyApplication.getCurrentUser());
                        event.mySave();
                        if (event.hasLocalId()){
                            try{
                                long syncId = feedback.get("places_users").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsLong();
                                EventStatusManager.instance().addLocally(syncId, event, UserPlaceStatusEnum.HERE);
                            }
                            catch (Exception ex){
                                Log.e(TAG, "Cannot get EventUser id from server response");
                            }
                        }
                        IntentsUtils.viewEventFromId(AddEventActivity.this, event.remote_id);
                    }
                })
                .perform();
    }

    public void setButtonValidation() {
        String textAfterChange = eventNameET.getText().toString().trim();
        boolean isValid = eventCategorySelected != null && Event.isValidName(textAfterChange);
        menu.findItem(R.id.action_post).setEnabled(isValid);
    }

    //----------------------------------------------------------------------------------------------
    //Public methods

    public int getNumberOfMainCategories() {
        return NUMBER_OF_MAIN_CATEGORIES;
    }

    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    public void setCategory(EventCategory eventCategory) {
        eventCategorySelected = eventCategory;
        categorySelector.selectCategoryUI(eventCategory.getName(),eventCategory.getIconWhiteResId());
        setButtonValidation();
    }

    public EventCategory getEventCategorySelected() {
        return eventCategorySelected;
    }

    private void setListeners() {

        //If click on editText when Not Focused
        View.OnFocusChangeListener onEtFocus = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hideCategories();
            }
        };
        //If click on editText when Focused
        View.OnClickListener onEtClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideCategories();
            }
        };
        eventNameET.setOnFocusChangeListener(onEtFocus);
        eventNameET.setOnClickListener(onEtClick);
        descriptionET.setOnFocusChangeListener(onEtFocus);
        descriptionET.setOnClickListener(onEtClick);

        mBtnAddSpot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.pinSpot(AddEventActivity.this);
            }
        });

        eventNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setButtonValidation();
            }
        });

        /*eventNameET.setHandleDismissingKeyboard(new BackCatchEditText.HandleDismissingKeyboard() {
            @Override
            public void dismissKeyboard() {
                imm.hideSoftInputFromWindow(eventNameET.getWindowToken(), 0);   //Hide keyboard
                eventNameET.clearFocus();
            }
        });*/

        mSpotContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spot spot = mBinding.getEvent().getSpot();
                if (spot != null && spot.isNew()) {
                    IntentsUtils.pinSpot(AddEventActivity.this, spot);
                }
            }
        });

/*
        spotView.setOnSpotClickListener(new OnSpotClickListener() {
            @Override
            public void onEditClick() {
                IntentsUtils.pinSpot(context);
            }

            @Override
            public void onRemoveClick() {
                spot = null;
                spotView.setVisibility(View.GONE);
                //pinView.setVisibility(View.VISIBLE);
            }
        });*/
    }

    private void hideCategories() {
        if(categorySelector.isExpandedView()) {
            categorySelector.lowerView();
            //TODO : Nettoyer ça : supprimer cette fonction et uniquement appeler la methode lowerview
        }
    }

    private void extractSpot(Bundle bundle){
        if(bundle!=null) {
            Spot spot = (Spot) bundle.getSerializable(IntentsUtils.KEY_SPOT);
            mBinding.getEvent().setSpot(spot);
            mBinding.notifyChange();

            //mClusterManagerSpot.addItem(spot);
            //mClusterManagerSpot.cluster();
        }
    }


    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
        Log.d(TAG, "ExploreMapFragment.onResume()");
        this.loadMapIfNeeded();
    }

    private void loadMapIfNeeded() {
        try {
            if (gMap == null){
                gMap = mapView.getMap();
            }
            Location location = LocationManager.getLastLocation();
            if (location != null){
                updateMapCenter(location);
                //gMap.addMarker(event.getMarkerOption());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMapCenter(Location location){
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_LEVEL_CENTER_MAP));
    }

    private void initMap(){
        mapView.onCreate(null);
        mapView.getMapAsync(this);
        gMap = mapView.getMap();
        gMap.setIndoorEnabled(true);
        gMap.setMyLocationEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(false);
        gMap.getUiSettings().setScrollGesturesEnabled(false);
        gMap.getUiSettings().setRotateGesturesEnabled(false);
        gMap.getUiSettings().setTiltGesturesEnabled(false);
        //setUpClusterer();
    }
/*

    private void setUpClusterer(){
        Log.i(TAG, "Setting up cluster!");
        // Initialize the manager with the context and the map.
        mClusterManagerSpot = new ClusterManager<Spot>(this, gMap);
        mClusterManagerSpot.setRenderer(new SpotClusterRenderer(this, gMap, mClusterManagerSpot));
        mClusterManagerSpot.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Spot>() {
            @Override
            public boolean onClusterClick(Cluster<Spot> cluster) {
                Log.d(TAG, "You clicked on a cluster");
                // TODO
                return true;
            }
        });
        mClusterManagerSpot.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Spot>() {
            @Override
            public boolean onClusterItemClick(Spot item) {
                Log.d(TAG, "You clicked on a cluster item: " + item);
                mBinding.getEvent().setSpot(item);
                return true;
            }

        });
        mClusterManagerSpot.setAlgorithm(new RemovableNonHierarchicalDistanceBasedAlgorithm<Spot>());
        gMap.setOnMarkerClickListener(mClusterManagerSpot);
    }*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        eventNameET.clearFocus();
        switch (requestCode){
            case IntentsUtils.REQUEST_PICK_SPOT:
                if(resultCode == RESULT_OK){
                    Log.d(TAG, "extracting bundle Spot");
                    extractSpot(data.getExtras());
                }
                break;
            case IntentsUtils.ACTION_ADD_EVENT_PICTURE:
                if(resultCode==RESULT_OK) {
                    // TODO
                    Log.d(TAG, "Result OK from AddEventPicture");
                }
                break;
            default:
                Log.e(TAG, "Unknown activity result: " + requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // =============================================================================================

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        Log.v(TAG, "User location changed!");
        updateMapCenter(newLocation);
        requestReverseGeocoding(newLocation);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is now ready!");
        gMap = googleMap;

        if (LocationManager.hasLastLocation()){
            updateMapCenter(LocationManager.getLastLocation());
        }
    }

    // =============================================================================================

    private void requestReverseGeocoding(Location location){
        if (mAddressResultReceiver == null){
            mAddressResultReceiver = new AddressResultReceiver();
        }
        ReverseGeocodingHelper.request(this, location, mAddressResultReceiver);
    }

    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver() {
            super(new Handler());
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(TAG, "Receive result from service: " + resultCode);
            if (resultCode == Constants.SUCCESS_RESULT) {
                mBinding.setAddress(resultData.getString(Constants.RESULT_DATA_KEY));
            }
        }
    }
}
