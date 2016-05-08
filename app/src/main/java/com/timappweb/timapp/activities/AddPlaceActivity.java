package com.timappweb.timapp.activities;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.AddEventCategoriesAdapter;
import com.timappweb.timapp.adapters.EventCategoryPagerAdapter;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.listeners.OnSpotClickListener;
import com.timappweb.timapp.managers.SpanningGridLayoutManager;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.BackCatchEditText;
import com.timappweb.timapp.views.SpotView;

import retrofit2.Call;


public class AddPlaceActivity extends BaseActivity {
    private String TAG = "AddPlaceActivity";

    private InputMethodManager imm;
    private String description;

    //Views
    private BackCatchEditText eventNameET;
    RecyclerView categoriesRV;
    AddEventCategoriesAdapter categoriesAdapter;
    private EventCategory eventCategorySelected;
    private View createButton;
    private View commentButton;
    private View progressView;
    private TextView nameCategoryTV;
    private View pinView;
    private ViewPager viewPager;
    private SpotView spotView;
    private TextView commentView;
    private View buttonsView;
    // Data
    private Spot spot = null;
    private AddPlaceActivity context;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        context = this;

        this.initToolbar(true);
        this.extractSpot(savedInstanceState);

        //Initialize
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        eventNameET = (BackCatchEditText) findViewById(R.id.event_name);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(ConfigurationProvider.rules().places_max_name_length);
        eventNameET.setFilters(filters);
        eventNameET.requestFocus();

        buttonsView = findViewById(R.id.buttons);
        categoriesRV = (RecyclerView) findViewById(R.id.rv_categories);
        createButton = findViewById(R.id.create_button);
        commentButton = findViewById(R.id.comment_button);
        progressView = findViewById(R.id.progress_view);
        nameCategoryTV = (TextView) findViewById(R.id.category_name);
        pinView = findViewById(R.id.no_spot_view);
        //pinnedSpot = findViewById(R.remote_id.pinned_spot);
        spotView = (SpotView) findViewById(R.id.spot_view);
        commentView = (TextView) findViewById(R.id.comment_text);

        initKeyboard();
        setListeners();
        initAdapterAndManager();
        initViewPager();
        initLocationListener();
        setButtonValidation();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        eventNameET.clearFocus();
        commentView.setVisibility(View.VISIBLE);
        switch (requestCode) {
            case IntentsUtils.ACTIVITY_RESULT_PICK_SPOT:
                if(resultCode == RESULT_OK){
                    Log.d(TAG, "extracting bundle Spot");
                    extractSpot(data.getExtras());
                }
                break;
            case IntentsUtils.ACTIVITY_RESULT_COMMENT:
                if(resultCode == RESULT_OK){
                    Log.d(TAG, "extracting bundle Comment");
                    extractComment(data.getExtras());
                }
                break;
        }
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
        eventNameET.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
    }

    private void initAdapterAndManager() {
        categoriesAdapter = new AddEventCategoriesAdapter(this);
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
        viewPager = (ViewPager) findViewById(R.id.addplace_viewpager);
        final EventCategoryPagerAdapter eventCategoryPagerAdapter = new EventCategoryPagerAdapter(this);
        viewPager.setAdapter(eventCategoryPagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        eventCategorySelected = categoriesAdapter.getCategory(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                EventCategory newEventCategory = categoriesAdapter.getCategory(position);
                categoriesAdapter.setIconNewCategory(context, newEventCategory);
                eventCategorySelected = newEventCategory;
            }
        });
    }

    private void submitPlace(final Place place){
        Log.d(TAG, "Submit place " + place.toString());
        Call call = RestClient.service().addPlace(place);
        call.enqueue(new RestFeedbackCallback(this) {

            @Override
            public void onActionSuccess(RestFeedback feedback) {
                place.remote_id = Integer.parseInt(feedback.data.get("place_id")); // TODO handle exception if invalid int
                Log.i(TAG, "User created the event with id: " + place.remote_id);
                IntentsUtils.viewPlaceFromPublish(context, place.remote_id);
                setProgressView(false);
            }

            @Override
            public void onFinish() {
                setProgressView(false);
            }

            @Override
            public void onActionFail(RestFeedback feedback) {
                Toast.makeText(context, feedback.message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setButtonValidation() {
        String textAfterChange = eventNameET.getText().toString().trim();
//        Log.d(TAG,"textafterchange : "+textAfterChange);
//        Log.d(TAG,"textafterchange Length: "+textAfterChange.length());
        if (eventCategorySelected !=null && Place.isValidName(textAfterChange)) {
            buttonsView.setVisibility(View.VISIBLE);
        } else {
            buttonsView.setVisibility(View.GONE);
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

    public void setCategory(EventCategory eventCategory) {
        eventCategorySelected = eventCategory;
    }

    public EventCategory getEventCategorySelected() {
        return eventCategorySelected;
    }

    private void setListeners() {
        pinView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.pinSpot(context);
            }
        });

        eventNameET.addTextChangedListener(new TextWatcher() {
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

        eventNameET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    commentView.setVisibility(View.GONE);
                }
            }
        });

        eventNameET.setHandleDismissingKeyboard(new BackCatchEditText.HandleDismissingKeyboard() {
            @Override
            public void dismissKeyboard() {
                imm.hideSoftInputFromWindow(eventNameET.getWindowToken(), 0);   //Hide keyboard
                commentView.setVisibility(View.VISIBLE);
                eventNameET.clearFocus();
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.comment(context);
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyApplication.hasFineLocation(ConfigurationProvider.rules().gps_min_accuracy_add_place)) {
                    setProgressView(true);
                    final Place place = new Place(MyApplication.getLastLocation(),
                            eventNameET.getText().toString(), eventCategorySelected, context.spot, description);
                    submitPlace(place);
                } else if (MyApplication.hasLastLocation()) {
                    Toast.makeText(getBaseContext(), "We don't have a fine location. Make sure your gps is enabled.", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "Click on add place before having a user location");
                    Toast.makeText(getBaseContext(), "Please wait we are getting your location...", Toast.LENGTH_LONG).show();
                }
            }
        });

        spotView.setOnSpotClickListener(new OnSpotClickListener() {
            @Override
            public void onEditClick() {
                IntentsUtils.pinSpot(context);
            }

            @Override
            public void onRemoveClick() {
                spot = null;
                spotView.setVisibility(View.GONE);
                pinView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void extractSpot(Bundle bundle){
        if(bundle!=null) {
            spot = (Spot) bundle.getSerializable("spot");
            if (spot != null){
                Log.v(TAG, "Spot is selected: " + spot);
                spotView.setSpot(spot);
                spotView.setVisibility(View.VISIBLE);
                pinView.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "spot is null");
            }
        }
    }

    private void extractComment(Bundle bundle){
        if(bundle!=null) {
            description = (String) bundle.getSerializable("description");
            if (description != null){
                Log.v(TAG, "Comment is selected: " + description);
                commentView.setText(description);
            } else {
                Log.d(TAG, "description is null");
            }
        }
    }
}
