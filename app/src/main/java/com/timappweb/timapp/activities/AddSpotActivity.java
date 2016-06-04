package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SpotCategoriesAdapter;
import com.timappweb.timapp.adapters.SpotsAdapter;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.Constants;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.databinding.ActivityAddSpotBinding;
import com.timappweb.timapp.listeners.LoadingListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.ApiCallFactory;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.PaginatedResponse;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.location.ReverseGeocodingHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class AddSpotActivity extends BaseActivity implements LocationManager.LocationListener, OnMapReadyCallback {

    private static final String TAG = "AddSpotActivity";
    private static final float ZOOM_LEVEL_CENTER_MAP = 15.0f;

    //private ImageView showCategoriesButton;
    private View createPlaceButton;
    private EditText etCustomPlace;
    private RecyclerView spotCategoriesRv;

    private SpotCategoriesAdapter spotCategoriesAdapter;
    private InputMethodManager imm;
    private MapView mapView;
    private GoogleMap gMap;
    private ActivityAddSpotBinding mBinding;
    private AddressResultReceiver mAddressResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_spot);

        //Toolbar
        this.initToolbar(true);

        //Initialize
        //showCategoriesButton = (ImageView) findViewById(R.id.button_show_categories_spot);
        createPlaceButton = findViewById(R.id.create_spot_button);
        spotCategoriesRv = (RecyclerView) findViewById(R.id.spot_categories_rv);
        etCustomPlace = (EditText) findViewById(R.id.edittext);
        mapView = (MapView) findViewById(R.id.map);

        initEt();
        initAdapters();
        setListeners();
        initMap();
        LocationManager.addOnLocationChangedListener(this);

        mBinding.setSpot(IntentsUtils.extractSpot(getIntent()));
        if (mBinding.getSpot() == null){
            mBinding.setSpot(new Spot());
        }
        if (LocationManager.hasLastLocation()){
            requestReverseGeocoding(LocationManager.getLastLocation());
        }
    }

    private void initEt() {
        etCustomPlace.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

    private void initAdapters() {
        spotCategoriesRv.setLayoutManager(new GridLayoutManager(this, 3));
        spotCategoriesAdapter = new SpotCategoriesAdapter(this);
        spotCategoriesRv.setAdapter(spotCategoriesAdapter);
        spotCategoriesAdapter.addAll(ConfigurationProvider.spotCategories());
    }

    private void setListeners() {
        createPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivityResult(mBinding.getSpot());
            }
        });

        etCustomPlace.addTextChangedListener(new TextWatcher() {
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

        spotCategoriesAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Spot spot = mBinding.getSpot();
                if (spot.hasCategory(spotCategoriesAdapter.getCategory(position))) {
                    spot.setCategory(null);
                } else {
                    spot.setCategory(spotCategoriesAdapter.getCategory(position));
                }
                setButtonValidation();
                spotCategoriesAdapter.notifyDataSetChanged();
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
        createPlaceButton.setActivated(mBinding.getSpot().isValid());
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
    }


    private void requestReverseGeocoding(Location location){
        if (mAddressResultReceiver == null){
            mAddressResultReceiver = new AddressResultReceiver();
        }
        ReverseGeocodingHelper.request(AddSpotActivity.this, location, mAddressResultReceiver);
    }
    // =============================================================================================

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        requestReverseGeocoding(newLocation);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public SpotCategory getCategorySelected() {
        return mBinding.getSpot().getCategory();
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
                mBinding.getSpot().setAddress(addressOutput);
            }
        }
    }

}
