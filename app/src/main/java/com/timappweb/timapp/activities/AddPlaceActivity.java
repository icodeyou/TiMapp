package com.timappweb.timapp.activities;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.CategoriesAdapter;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.managers.SpanningGridLayoutManager;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.RestError;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.config.IntentsUtils;
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
    private Button createButton;
    private View progressView;
    private TextView nameCategoryTV;

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
        createButton = (Button) findViewById(R.id.create_place_button);
        progressView = findViewById(R.id.progress_view);
        nameCategoryTV = (TextView) findViewById(R.id.category_name);

        initKeyboard();
        setListeners();
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
                MyApplication.setLastLocation(l);
            }
        });
    }

    private void initKeyboard() {
        groupNameET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }

    private void initAdapterAndManager() {
        categoriesAdapter = new CategoriesAdapter(this);
        categoriesRV.setAdapter(categoriesAdapter);
        GridLayoutManager manager = new SpanningGridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false);
        categoriesRV.setLayoutManager(manager);
    }

    private void setProgressView(boolean bool) {
        if(bool) {
            progressView.setVisibility(View.VISIBLE);
            createButton.setVisibility(View.GONE);
        }
        else {
            progressView.setVisibility(View.GONE);
            createButton.setVisibility(View.VISIBLE);
        }
    }

    private void submitPlace(final Place place){
        Context context = this;
        Log.d(TAG, "Submit place " + place.toString());
        RestClient.service().addPlace(place, new RestCallback<RestFeedback>(this) {
            @Override
            public void success(RestFeedback restFeedback, Response response) {
                if (restFeedback.success) {
                    Log.d(TAG, "Place has been saved: " + place);
                    Post post = new Post();
                    post.latitude = MyApplication.getLastLocation().getLatitude();
                    post.longitude = MyApplication.getLastLocation().getLongitude();
                    place.id = Integer.parseInt(restFeedback.data.get("id"));
                    IntentsUtils.addPostStepTags(this.context, place, post);
                } else {
                    Log.d(TAG, "Cannot save viewPlaceFromPublish: " + place);
                    Toast.makeText(context, "We cannot save your place right now. Please try again later", Toast.LENGTH_LONG).show();
                    setProgressView(false);
                }
            }

            @Override
            public void failure(RestError error) {
                super.failure(error);
                setProgressView(true);
            }
        });
    }

    public void setButtonValidation() {
        String textAfterChange = groupNameET.getText().toString();

        if (categorySelected!=null && !textAfterChange.equals("")) {
            createButton.setEnabled(true);
            createButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        } else {
            createButton.setEnabled(false);
            createButton.setBackgroundColor(ContextCompat.getColor(this, R.color.LightGrey));
        }
    }

    public void setNameCategoryTV(String name) {
        nameCategoryTV.setText(name);
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

    private void setListeners() {
        groupNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setButtonValidation();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyApplication.hasFineLocation()) {
                    setProgressView(true);
                    final Place place = new Place(MyApplication.getLastLocation().getLatitude(), MyApplication.getLastLocation().getLongitude(), groupNameET.getText().toString(), categorySelected);
                    submitPlace(place);
                } else if (MyApplication.hasLastLocation()){
                    Toast.makeText(getBaseContext(), "We don't have a fine location. Make sure your gps is enabled.", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "Click on add place before having a user location");
                    Toast.makeText(getBaseContext(), "Please wait we are getting your location...", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    //Miscellaneous
}
