package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SpotCategoriesAdapter;
import com.timappweb.timapp.adapters.SpotsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Spot;
import com.timappweb.timapp.entities.SpotCategory;
import com.timappweb.timapp.listeners.LoadingListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.ApiCallFactory;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.PaginationResponse;
import com.timappweb.timapp.views.SpotView;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class AddSpotActivity extends BaseActivity implements LoadingListener {

    private ImageView showCategoriesButton;
    private View createPlaceButton;
    private EditText etCustomPlace;
    private RecyclerView spotCategoriesRv;
    private RecyclerView spotsRv;
    private SpotsAdapter spotsAdapter;

    private SpotCategory categorySelected;
    private SpotCategoriesAdapter spotCategoriesAdapter;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);

        //Toolbar
        this.initToolbar(true);

        //Keyboard
        imm = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        //Initialize
        showCategoriesButton = (ImageView) findViewById(R.id.button_show_categories_spot);
        createPlaceButton = findViewById(R.id.create_place_button);
        spotsRv = (RecyclerView) findViewById(R.id.spots_rv);
        spotCategoriesRv = (RecyclerView) findViewById(R.id.spot_categories_rv);
        etCustomPlace = (EditText) findViewById(R.id.edittext);

        initAdapters();
        setListeners();

        loadData();
    }

    private void initAdapters() {
        final Activity activity = this;
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
                //TODO : get back to AddPlaceActivity
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

        etCustomPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!imm.isAcceptingText()) {
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
        Map<String, String> conditions = null;
        Call call = RestClient.service().spotReachable(conditions);
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
