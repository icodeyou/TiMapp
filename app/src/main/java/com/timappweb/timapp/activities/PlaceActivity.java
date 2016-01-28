package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.MyPagerAdapter;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.fragments.PlacePostsFragment;
import com.timappweb.timapp.fragments.PlaceTagsFragment;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.rest.model.RestError;
import com.timappweb.timapp.rest.model.RestFeedback;

import java.util.List;
import java.util.Vector;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class PlaceActivity extends BaseActivity{
    private String TAG = "PlaceActivity";
    private ListView tagsListView;
    private ListView placeListView;
    private MyPagerAdapter pagerAdapter;
    private Place place;
    private int placeId;
    private Button comingButton;
    private Button addPostButton;
    private View   progressView;
    private Activity currentActivity;

    private ShareActionProvider shareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentActivity = this;

        this.place = IntentsUtils.extractPlace(getIntent());
        placeId = getIntent().getIntExtra("id", -1);
        if (place == null && placeId == -1){
            IntentsUtils.home(this);
        }

        setContentView(R.layout.activity_place);
        initToolbar(true);

        //Initialize
        tagsListView = (ListView) findViewById(R.id.tags_lv);
        placeListView = (ListView) findViewById(R.id.place_lv);
        comingButton = (Button) findViewById(R.id.button_coming);
        comingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO fine location
                if (!MyApplication.hasLastLocation()){
                    Toast.makeText(getApplicationContext(), R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
                    return;
                }
                QueryCondition conditions = new QueryCondition();
                conditions.setPlaceId(placeId);
                conditions.setAnonymous(false);
                conditions.setUserLocation(MyApplication.getLastLocation());
                RestClient.service().placeComing(conditions.toMap(), new RestCallback<RestFeedback>(currentActivity) {
                    @Override
                    public void success(RestFeedback restFeedback, Response response) {
                        if (restFeedback.success){
                            Log.d(TAG, "Success register here for user");
                        }
                        else{
                            Log.d(TAG, "Fail register here for user");
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        super.failure(error);
                        Log.d(TAG, "Fail register coming for user");
                    }
                });
            }
        });

        addPostButton = (Button) findViewById(R.id.button_add_some_tags);
        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO fine location
                if (!MyApplication.hasLastLocation()){
                    Toast.makeText(getApplicationContext(), R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
                    return;
                }

                QueryCondition conditions = new QueryCondition();
                conditions.setPlaceId(placeId);
                conditions.setAnonymous(false);
                conditions.setUserLocation(MyApplication.getLastLocation());
                RestClient.service().placeComing(conditions.toMap(), new RestCallback<RestFeedback>(currentActivity) {
                    @Override
                    public void success(RestFeedback restFeedback, Response response) {
                        if (restFeedback.success){
                            Log.d(TAG, "Success register here for user");
                        }
                        else{
                            Log.d(TAG, "Fail register here for user");
                        }
                        // TODO jean:  google map
                    }

                });

                IntentsUtils.addPostStepTags(currentActivity, place);
            }
        });

        progressView = findViewById(R.id.progress_view);

        initFragments();
        initBottomButton();

        if (place != null){
            placeId = place.id;
            this.notifyPlaceLoaded();
        }
        else{
            loadPlace(placeId);
        };
    }



    private void initBottomButton() {
        if(MyApplication.hasLastLocation()) {
            double latitude =  MyApplication.getLastLocation().getLatitude();
            double longitude = MyApplication.getLastLocation().getLongitude();
        }
        // TODO steph: display the right button. ex: comingButton.setVisibility(View.VISIBLE);
    }

    private void initFragments() {
        // Création de la liste de Fragments que fera défiler le PagerAdapter
        List fragments = new Vector();

        // Ajout des Fragments dans la liste
        fragments.add(Fragment.instantiate(this, PlaceTagsFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, PlacePostsFragment.class.getName()));

        // Création de l'adapter qui s'occupera de l'affichage de la liste de
        // Fragments
        this.pagerAdapter = new MyPagerAdapter(super.getSupportFragmentManager(), fragments);

        ViewPager pager = (ViewPager) super.findViewById(R.id.place_viewpager);
        // Affectation de l'adapter au ViewPager
        pager.setAdapter(this.pagerAdapter);
    }

    private void loadPlace(int placeId) {
        final PlaceActivity that = this;
        RestClient.service().viewPlace(placeId, new RestCallback<Place>(this) {
            @Override
            public void success(Place p, Response response) {
                place = p;
                notifyPlaceLoaded();
            }

            @Override
            public void failure(RetrofitError error) {
                super.failure(error);
                Toast.makeText(that, "This place does not exists anymore", Toast.LENGTH_LONG).show();
                // TODO cannot load place (invalid ? )
                IntentsUtils.home(that);
            }
        });
    }



    private void notifyPlaceLoaded() {
        progressView.setVisibility(View.GONE);
        initPlaceAdapters();
        this.updateBtnVisible();

        if (!MyApplication.hasLastLocation()) {
            Log.d(TAG, "There is no last known location");
            this.initLocationProvider(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    MyApplication.setLastLocation(location);
                    updateBtnVisible();
                }
            });
        }
    }

    /**
     * Show or hide add post or comming button according to user location
     */
    private void updateBtnVisible(){
        Log.d(TAG, "PlaceActivity.updateBtnVisible()");
        // Check if the user can post in this place
        if (!MyApplication.hasLastLocation()) {
            addPostButton.setVisibility(View.GONE);
            comingButton.setVisibility(View.GONE);
        }
        else if (place.isReachable()){
            addPostButton.setVisibility(View.VISIBLE);
            comingButton.setVisibility(View.GONE);
        }
        else{
            comingButton.setVisibility(View.VISIBLE);
            addPostButton.setVisibility(View.GONE);
        }
    }

    //Menu Action Bar
    //////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_share:
                setDefaultShareIntent();
                return true;
            case R.id.action_reload:
                IntentsUtils.reload(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setDefaultShareIntent() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_place_text));
        startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    private void initPlaceAdapters() {
         //PlacesAdapter
        PlacesAdapter placesAdapter = new PlacesAdapter(this, false);
        placesAdapter.add(place);
        placeListView.setAdapter(placesAdapter);
    }

    public Place getPlace() {
        return place;
    }

    public int getPlaceId() {
        return placeId;
    }
}