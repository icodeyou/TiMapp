package com.timappweb.timapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
    private RecyclerView spotCategoriesRv;
    private RecyclerView spotsRv;
    private SpotsAdapter spotsAdapter;

    private SpotCategory categorySelected;
    private SpotCategoriesAdapter spotCategoriesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);

        //Toolbar
        this.initToolbar(true);

        //Initialize
        showCategoriesButton = (ImageView) findViewById(R.id.button_show_categories_spot);
        spotsRv = (RecyclerView) findViewById(R.id.spots_rv);
        spotCategoriesRv = (RecyclerView) findViewById(R.id.spot_categories_rv);

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

        spotCategoriesAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                categorySelected = spotCategoriesAdapter.getCategory(position);
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
