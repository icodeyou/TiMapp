package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.widget.EditText;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.CategoriesAdapter;
import com.timappweb.timapp.views.HorizontalRecyclerView;

public class AddPlaceActivity extends BaseActivity {
    private String TAG = "PublishActivity";

    //Views
    private EditText locationET;
    HorizontalRecyclerView categoriesRV;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        this.initToolbar(true);

        //Initialize
        locationET = (EditText) findViewById(R.id.location_edit_text);
        categoriesRV = (HorizontalRecyclerView) findViewById(R.id.categories_scroll);

        initAdapter();
    }

    private void initAdapter() {
        categoriesRV.setAdapter(new CategoriesAdapter(this));
    }

    //----------------------------------------------------------------------------------------------
    //Private methods

    //----------------------------------------------------------------------------------------------
    //Inner classes

    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    //----------------------------------------------------------------------------------------------
    //Miscellaneous
}
