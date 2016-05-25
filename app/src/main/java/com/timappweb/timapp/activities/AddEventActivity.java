package com.timappweb.timapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.AddEventCategoriesAdapter;
import com.timappweb.timapp.adapters.EventCategoryPagerAdapter;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.listeners.BinaryActionListener;
import com.timappweb.timapp.listeners.OnSpotClickListener;
import com.timappweb.timapp.managers.SpanningGridLayoutManager;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.BackCatchEditText;
import com.timappweb.timapp.views.SpotView;

import org.w3c.dom.Text;

import java.util.HashMap;

import retrofit2.Call;


public class AddEventActivity extends BaseActivity {
    private String TAG = "AddEventActivity";

    private InputMethodManager imm;
    private String description;

    //Views
    private BackCatchEditText eventNameET;
    RecyclerView categoriesRV;
    AddEventCategoriesAdapter categoriesAdapter;
    private EventCategory eventCategorySelected;
    private View createButton;
    private View progressView;
    private TextView nameCategoryTV;
    //private View pinView;
    private ViewPager viewPager;
    //private SpotView spotView;
    // Data
    private Spot spot = null;
    private AddEventActivity context;

    private MapView mapView = null;
    private GoogleMap gMap;
    private View eventLocation;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        context = this;

        this.initToolbar(true);
        this.extractSpot(savedInstanceState);

        //Initialize
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        eventNameET = (BackCatchEditText) findViewById(R.id.event_name);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(ConfigurationProvider.rules().places_max_name_length);
        eventNameET.setFilters(filters);
        eventNameET.requestFocus();

        categoriesRV = (RecyclerView) findViewById(R.id.rv_categories);
        createButton = findViewById(R.id.create_button);
        progressView = findViewById(R.id.progress_view);
        nameCategoryTV = (TextView) findViewById(R.id.category_name);
        //pinView = findViewById(R.id.no_spot_view);
        //pinnedSpot = findViewById(R.remote_id.pinned_spot);
        eventLocation = (TextView) findViewById(R.id.event_location);
        //spotView = (SpotView) findViewById(R.id.spot_view);
        mapView = (MapView) findViewById(R.id.map);

        initKeyboard();
        setListeners();
        initAdapterAndManager();
        initViewPager();
        initLocationListener();
        setButtonValidation();
        initMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        eventNameET.clearFocus();
        switch (requestCode) {
            case IntentsUtils.REQUEST_PICK_SPOT:
                if(resultCode == RESULT_OK){
                    Log.d(TAG, "extracting bundle Spot");
                    extractSpot(data.getExtras());
                }
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    //Private methods

    /**
     * Load places once user name is known
     */
    private void initLocationListener() {
        /*
        LocationManager.addOnLocationChangedListener(new LocationManager.LocationListener() {
            @Override
            public void onLocationChanged(Location newLocation, Location lastLocation) {
            }
        });*/
        LocationManager.start(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initKeyboard() {
        eventNameET.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

    private void initAdapterAndManager() {
        categoriesAdapter = new AddEventCategoriesAdapter(this);
        categoriesRV.setAdapter(categoriesAdapter);
        GridLayoutManager manager = new SpanningGridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false);
        categoriesRV.setLayoutManager(manager);
    }

    private void setProgressView(boolean bool) {
        if(bool) {
            progressView.setVisibility(View.VISIBLE);
            getSupportActionBar().hide();
        }
        else {
            progressView.setVisibility(View.GONE);
            getSupportActionBar().show();
        }
    }

    private void initViewPager() {
        viewPager = (ViewPager) findViewById(R.id.addplace_viewpager);
        final EventCategoryPagerAdapter eventCategoryPagerAdapter = new EventCategoryPagerAdapter(this);
        viewPager.setAdapter(eventCategoryPagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        eventCategorySelected = categoriesAdapter.getCategory(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                EventCategory newEventCategory = categoriesAdapter.getCategory(position);
                categoriesAdapter.setIconNewCategory(context, newEventCategory);
                eventCategorySelected = newEventCategory;
            }
        });
    }

    private void submitPlace(final Event event){
        Log.d(TAG, "Submit event " + event.toString());
        Call call = RestClient.service().addPlace(event);
        event.saveRemoteEntry(this, call, new BinaryActionListener() {

            @Override
            public void onSuccess() {
                IntentsUtils.viewEventFromId(context, event.remote_id);
                // TODO update sync to prevent reloading
            }

            @Override
            public void onFailure() { }

            @Override
            public void onFinish() {
                setProgressView(false);
            }
        });

    }

    public void setButtonValidation() {
        String textAfterChange = eventNameET.getText().toString().trim();
//        Log.d(TAG,"textafterchange : "+textAfterChange);
//        Log.d(TAG,"textafterchange Length: "+textAfterChange.length());
        if (eventCategorySelected !=null && Event.isValidName(textAfterChange)) {
            createButton.setVisibility(View.VISIBLE);
        } else {
            createButton.setVisibility(View.GONE);
        }
    }

    //----------------------------------------------------------------------------------------------
    //Public methods
    public RecyclerView getCategoriesRV() {
        return categoriesRV;
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    // ----------------------------------------------------------------------------------------------
    //Inner classes

    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    public void setCategory(EventCategory eventCategory) {
        eventCategorySelected = eventCategory;
    }

    public EventCategory getEventCategorySelected() {
        return eventCategorySelected;
    }

    private void setListeners() {
        eventLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.pinSpot(context);
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

        eventNameET.setHandleDismissingKeyboard(new BackCatchEditText.HandleDismissingKeyboard() {
            @Override
            public void dismissKeyboard() {
                imm.hideSoftInputFromWindow(eventNameET.getWindowToken(), 0);   //Hide keyboard
                eventNameET.clearFocus();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LocationManager.hasFineLocation(ConfigurationProvider.rules().gps_min_accuracy_add_place)) {
                    setProgressView(true);
                    final Event event = new Event(LocationManager.getLastLocation(),
                            eventNameET.getText().toString(), eventCategorySelected, context.spot, description);
                    submitPlace(event);
                } else if (LocationManager.hasLastLocation()) {
                    Toast.makeText(getBaseContext(), "We don't have a fine location. Make sure your gps is enabled.", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "Click on add event before having a user location");
                    Toast.makeText(getBaseContext(), R.string.please_wait_location, Toast.LENGTH_LONG).show();
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

    private void extractSpot(Bundle bundle){
        if(bundle!=null) {
            spot = (Spot) bundle.getSerializable("spot");
            if (spot != null){
                Log.v(TAG, "Spot is selected: " + spot);
                //spotView.setSpot(spot);
                //spotView.setVisibility(View.VISIBLE);
                //pinView.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "spot is null");
            }
        }
    }

    private void initMap(){
        mapView.onCreate(null);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "Map is now ready!");
                gMap = googleMap;
            }
        });
        gMap = mapView.getMap();
        gMap.setIndoorEnabled(true);
    }
}
