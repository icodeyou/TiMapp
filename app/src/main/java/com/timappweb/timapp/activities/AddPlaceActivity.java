package com.timappweb.timapp.activities;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.AddPlaceCategoriesAdapter;
import com.timappweb.timapp.adapters.CategoriesAdapter;
import com.timappweb.timapp.adapters.CategoryPagerAdapter;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.listeners.ColorSquareOnTouchListener;
import com.timappweb.timapp.managers.SpanningGridLayoutManager;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.utils.Util;


public class AddPlaceActivity extends BaseActivity {
    private String TAG = "PublishActivity";
    private InputMethodManager imm;

    //Views
    private EditText groupNameET;
    RecyclerView categoriesRV;
    AddPlaceCategoriesAdapter categoriesAdapter;
    private Category categorySelected;
    private View createButton;
    private TextView textCreateButton;
    private View progressView;
    private TextView nameCategoryTV;

    private ViewPager viewPager;

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
        createButton = findViewById(R.id.create_button);
        textCreateButton = (TextView) findViewById(R.id.text_create_button);
        progressView = findViewById(R.id.progress_view);
        nameCategoryTV = (TextView) findViewById(R.id.category_name);

        initKeyboard();
        setListeners();
        initAdapterAndManager();
        initViewPager();
        initLocationListener();
        setButtonValidation();
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
        groupNameET.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

    private void initAdapterAndManager() {
        categoriesAdapter = new AddPlaceCategoriesAdapter(this);
        categoriesRV.setAdapter(categoriesAdapter);
        GridLayoutManager manager = new SpanningGridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false);
        categoriesRV.setLayoutManager(manager);
    }

    private void setProgressView(boolean bool) {
        if(bool) {
            progressView.setVisibility(View.VISIBLE);
            getSupportActionBar().hide();
        }
        else {
            progressView.setVisibility(View.GONE);
            getSupportActionBar().show();
        }
    }

    private void initViewPager() {
        final AddPlaceActivity that = this;
        viewPager = (ViewPager) findViewById(R.id.addplace_viewpager);
        final CategoryPagerAdapter categoryPagerAdapter = new CategoryPagerAdapter(this);
        viewPager.setAdapter(categoryPagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        categorySelected = categoriesAdapter.getCategory(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                Category newCategory = categoriesAdapter.getCategory(position);
                categoriesAdapter.setIconNewCategory(that, newCategory);
                categorySelected = newCategory;
            }
        });
    }

    private void submitPlace(final Place place){
        Context context = this;
        Log.d(TAG, "Submit place " + place.toString());
        IntentsUtils.addPostStepTags(context, place);
        setProgressView(false);
        /*
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
            public void failure(RetrofitError error) {
                super.failure(error);
                setProgressView(false);
            }
        });
        */
    }

    public void setButtonValidation() {
        String textAfterChange = groupNameET.getText().toString();

        if (categorySelected!=null && Place.isValidName(textAfterChange)) {
            createButton.setVisibility(View.VISIBLE);
        } else {
            createButton.setVisibility(View.GONE);
        }
    }

    //----------------------------------------------------------------------------------------------
    //Public methods
    public RecyclerView getCategoriesRV() {
        return categoriesRV;
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    // ----------------------------------------------------------------------------------------------
    //Inner classes

    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    public void setCategory(Category category) {
        categorySelected = category;
    }

    public Category getCategorySelected() {
        return categorySelected;
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
                if (MyApplication.hasFineLocation(MyApplication.getApplicationRules().gps_min_accuracy_add_place)) {
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
}
