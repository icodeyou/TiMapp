package com.timappweb.timapp.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.CategoriesAdapter;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.managers.SpanningGridLayoutManager;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.utils.IntentsUtils;

import retrofit.client.Response;

public class AddPlaceActivity extends BaseActivity {
    private String TAG = "PublishActivity";
    private InputMethodManager imm;

    //Views
    private EditText groupNameET;
    RecyclerView categoriesRV;
    CategoriesAdapter categoriesAdapter;
    private Category categorySelected;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        this.initToolbar(true);

        //Initialize
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        groupNameET = (EditText) findViewById(R.id.place_name_edit_text);
        categoriesRV = (RecyclerView) findViewById(R.id.rv_categories);

        initAdapterAndManager();
    }

    //----------------------------------------------------------------------------------------------
    //Private methods
    private void initAdapterAndManager() {
        categoriesAdapter = new CategoriesAdapter(this);
        categoriesRV.setAdapter(categoriesAdapter);
        GridLayoutManager manager = new SpanningGridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false);
        categoriesRV.setLayoutManager(manager);
    }

    private void submitPlace(final Place place){
        Log.d(TAG, "Submit place " + place.toString());
        RestClient.service().addPlace(place, new RestCallback<RestFeedback>(this) {
            @Override
            public void success(RestFeedback restFeedback, Response response) {
                if (restFeedback.success){
                    Log.d(TAG, "Place has been saved: " + place);
                    IntentsUtils.addPlace(this.context);
                }
                else{
                    Log.d(TAG, "Cannot save viewPlace: " + place);
                    // TODO display message
                }
            }
        });
    }
    //----------------------------------------------------------------------------------------------
    //Public methods
    public RecyclerView getCategoriesRV() {
        return categoriesRV;
    }

    // ----------------------------------------------------------------------------------------------
    //Inner classes

    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    public void setCategory(Category category) {
        categorySelected = category;
    }

    public void onCreatePlaceClick(View view) {
        final Place place = new Place(0, 0, groupNameET.getText().toString(), categorySelected);
        // TODO validate place
        this.submitPlace(place);
    }

    //----------------------------------------------------------------------------------------------
    //Miscellaneous
}
