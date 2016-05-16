package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
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
import android.widget.ImageView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SpotCategoriesAdapter;
import com.timappweb.timapp.adapters.SpotsAdapter;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.listeners.LoadingListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.ApiCallFactory;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.PaginationResponse;
import com.timappweb.timapp.utils.location.LocationManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class AddSpotActivity extends BaseActivity implements LoadingListener {

    private static final String TAG = "AddSpotActivity";
    private ImageView showCategoriesButton;
    private View createPlaceButton;
    private EditText etCustomPlace;
    private RecyclerView spotCategoriesRv;
    private RecyclerView spotsRv;
    private SpotsAdapter spotsAdapter;
    private AddSpotActivity activity;

    private SpotCategory categorySelected;
    private SpotCategoriesAdapter spotCategoriesAdapter;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);
        activity = this;

        //Toolbar
        this.initToolbar(true);

        //Initialize
        showCategoriesButton = (ImageView) findViewById(R.id.button_show_categories_spot);
        createPlaceButton = findViewById(R.id.create_spot_button);
        spotsRv = (RecyclerView) findViewById(R.id.spots_rv);
        spotCategoriesRv = (RecyclerView) findViewById(R.id.spot_categories_rv);
        etCustomPlace = (EditText) findViewById(R.id.edittext);

        initEt();
        initAdapters();
        setListeners();

        loadData();

    }

    private void initEt() {
        etCustomPlace.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

    private void initAdapters() {
        //RV
        spotCategoriesRv.setLayoutManager(new GridLayoutManager(this, 3));
        spotsRv.setLayoutManager(new LinearLayoutManager(this));

        //Adapter
        spotCategoriesAdapter = new SpotCategoriesAdapter(this);
        spotCategoriesRv.setAdapter(spotCategoriesAdapter);
        spotCategoriesAdapter.add(SpotCategory.createDummy());

        spotsAdapter = new SpotsAdapter(this);
        spotsRv.setAdapter(spotsAdapter);
    }

    private void setListeners() {
        final Activity activity = this;

        createPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spot spot = new Spot(etCustomPlace.getText().toString(), categorySelected);
                finishActivityResult(spot);
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
        etCustomPlace.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    spotCategoriesRv.setVisibility(View.VISIBLE);
                }
            }
        });

        spotCategoriesAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                if (categorySelected == spotCategoriesAdapter.getCategory(position)) {
                    categorySelected = null;
                    setButtonValidation();
                } else {
                    categorySelected = spotCategoriesAdapter.getCategory(position);
                    setButtonValidation();
                }
                spotCategoriesAdapter.notifyDataSetChanged();
            }
        });

        showCategoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spotCategoriesRv.getVisibility() == View.GONE) {
                    spotCategoriesRv.setVisibility(View.VISIBLE);
                } else {
                    spotCategoriesRv.setVisibility(View.GONE);
                }
            }
        });

        spotsAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                finishActivityResult(spotsAdapter.getItem(position));
            }
        });
    }

    private void finishActivityResult(Spot spot){
        Log.d(TAG, "Spot chose: " + spot);
        Intent intent = new Intent(activity, AddPlaceActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("spot", spot);
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        finish();
        //NavUtils.navigateUpTo(activity, intent);
    }

    private void setButtonValidation() {
        //TODO : Get rules from server configuration
        if(etCustomPlace.length()>=2 && getCategorySelected()!=null) {
            createPlaceButton.setVisibility(View.VISIBLE);
            showCategoriesButton.setVisibility(View.GONE);
        } else {
            createPlaceButton.setVisibility(View.GONE);
            showCategoriesButton.setVisibility(View.VISIBLE);
        }
    }

    public void loadData(){
        Location location = LocationManager.getLastLocation();
        if (location == null){
            // TODO wait for location
            Log.e(TAG, "User does not have a location");
            return;
        }
        Call call = RestClient.service().spotReachable(location.getLatitude(), location.getLongitude());

        ApiCallFactory.build(call, new RestCallback<PaginationResponse<Spot>>(this) {

            @Override
            public void onResponse200(Response<PaginationResponse<Spot>> response) {
                List<Spot> spots = response.body().items;
                spotsAdapter.setData(spots);
            }

        }, this);
    }

    @Override
    public void onBackPressed() {
        if(spotCategoriesRv.getVisibility()==View.VISIBLE) {
            spotCategoriesRv.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onLoadStart() {
        // TODO JACK
    }

    @Override
    public void onLoadEnd() {
        // TODO JACK
    }

    public SpotCategory getCategorySelected() {
        return categorySelected;
    }
}
