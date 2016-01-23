package com.timappweb.timapp.activities;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.CategoriesAdapter;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.managers.SpanningGridLayoutManager;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.utils.IntentsUtils;
import com.timappweb.timapp.utils.Util;

import retrofit.client.Response;

public class AddPlaceActivity extends BaseActivity {
    private String TAG = "PublishActivity";
    private InputMethodManager imm;

    //Views
    private EditText groupNameET;
    RecyclerView categoriesRV;
    CategoriesAdapter categoriesAdapter;
    private Category categorySelected;
    private Location currentLocation = null;

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
        initLocationListener();
    }

    //----------------------------------------------------------------------------------------------
    //Private methods

    /**
     * Load places once user name is known
     */
    private void initLocationListener() {
        initLocationProvider(new LocationListener() {
            @Override
            public void onLocationChanged(Location l) {
                Log.i(TAG, "Location has changed: " + Util.print(l));
                currentLocation = l;
            }
        });
    }

    private void initAdapterAndManager() {
        categoriesAdapter = new CategoriesAdapter(this);
        categoriesRV.setAdapter(categoriesAdapter);
        GridLayoutManager manager = new SpanningGridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false);
        categoriesRV.setLayoutManager(manager);
    }

    private void submitPlace(final Place place){
        Context context = this;
        Log.d(TAG, "Submit place " + place.toString());
        RestClient.service().addPlace(place, new RestCallback<RestFeedback>(this) {
            @Override
            public void success(RestFeedback restFeedback, Response response) {
                if (restFeedback.success){
                    Log.d(TAG, "Place has been saved: " + place);
                    Post post = new Post();
                    post.latitude = currentLocation.getLatitude();
                    post.longitude = currentLocation.getLongitude();
                    place.id = Integer.parseInt(restFeedback.data.get("id"));
                    IntentsUtils.addPostStepTags(this.context, place, post);
                }
                else{
                    Log.d(TAG, "Cannot save viewPlace: " + place);
                    Toast.makeText(context, "We cannot save your place right now. Please try again later", Toast.LENGTH_LONG).show();
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
        if (currentLocation != null){
            final Place place = new Place(currentLocation.getLatitude(), currentLocation.getLongitude(), groupNameET.getText().toString(), categorySelected);
            // TODO validate place
            this.submitPlace(place);
        }
        else {
            Log.d(TAG, "Click on add place before having a user location");
            Toast.makeText(this, "We don't have your position yet. Please wait", Toast.LENGTH_LONG).show();
        }
    }

    //----------------------------------------------------------------------------------------------
    //Miscellaneous
}
