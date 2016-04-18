package com.timappweb.timapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.views.PlaceView;
import com.timappweb.timapp.views.SpotView;

public class AddSpotActivity extends BaseActivity{

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

    private void showCategories() {

    }

    private void hideCategories() {

    }

}
