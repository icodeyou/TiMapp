package com.timappweb.timapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Spot;
import com.timappweb.timapp.listeners.LoadingListener;
import com.timappweb.timapp.rest.ApiCallFactory;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.views.SpotView;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class AddSpotActivity extends BaseActivity implements LoadingListener {

    private SpotView spotView;
    private ImageView showCategoriesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);

        //Toolbar
        this.initToolbar(true);

        //Initialize
        spotView = (SpotView) findViewById(R.id.spot_view);
        spotView.getRvSpotTags().getAdapter().setDummyData();
        showCategoriesButton = (ImageView) findViewById(R.id.button_show_categories_spot);

        setListeners();

        loadData();
    }

    private void setListeners() {
        final Activity activity = this;
        spotView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.addPlace(activity);
            }
        });

        showCategoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategories();
            }
        });
    }

    public void loadData(){
        Map<String, String> conditions = null;
        Call call = RestClient.service().spotReachable(conditions);
        ApiCallFactory.build(call, new RestCallback<List<Spot>>(this){

            @Override
            public void onResponse200(Response<List<Spot>> response) {
                List<Spot> spots = response.body();
                // TODO JACK: add data in adapter HERE!
            }

        }, this);
    }

    private void showCategories() {

    }

    private void hideCategories() {

    }

    @Override
    public void onLoadStart() {
        // TODO JACK
    }

    @Override
    public void onLoadEnd() {
        // TODO JACK
    }
}
