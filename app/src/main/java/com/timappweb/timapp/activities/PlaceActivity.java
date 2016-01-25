package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.MyPagerAdapter;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.fragments.PlacePostsFragment;
import com.timappweb.timapp.fragments.PlaceTagsFragment;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.config.IntentsUtils;

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
    private Button addTagsButton;
    private View   progressView;

    private ShareActionProvider shareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        addTagsButton = (Button) findViewById(R.id.button_add_some_tags);
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
        if(MyApplication.lastLocation!=null) {
            double latitude =  MyApplication.lastLocation.getLatitude();
            double longitude = MyApplication.lastLocation.getLongitude();
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
        // called when the place is loaded
        // this.place is the place loaded

        progressView.setVisibility(View.GONE);
        initPlaceAdapters();
    }

    //Menu Action Bar
    //////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        //shareActionProvider =
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_share:
                /////Handle share actions here
                return true;
            case R.id.action_reload:
                IntentsUtils.reload(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    private void initPlaceAdapters() {
         //PlacesAdapter
        PlacesAdapter placesAdapter = new PlacesAdapter(this);
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