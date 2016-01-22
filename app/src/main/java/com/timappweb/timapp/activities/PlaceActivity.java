package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.MyPagerAdapter;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.fragments.PlacePeopleFragment;
import com.timappweb.timapp.fragments.PlaceTagsFragment;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.IntentsUtils;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.place = IntentsUtils.extractPlace(getIntent());
        int placeId = getIntent().getIntExtra("id", -1);
        if (place == null && placeId == -1){
            IntentsUtils.home(this);
        }


        setContentView(R.layout.activity_place);


        this.initToolbar(true);

        //Initialize
        tagsListView = (ListView) findViewById(R.id.tags_lv);
        placeListView = (ListView) findViewById(R.id.place_lv);

        //initAdapters();

        // Création de la liste de Fragments que fera défiler le PagerAdapter
        List fragments = new Vector();

        // Ajout des Fragments dans la liste
        fragments.add(Fragment.instantiate(this, PlaceTagsFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, PlacePeopleFragment.class.getName()));

        // Création de l'adapter qui s'occupera de l'affichage de la liste de
        // Fragments
        this.pagerAdapter = new MyPagerAdapter(super.getSupportFragmentManager(), fragments);

        ViewPager pager = (ViewPager) super.findViewById(R.id.place_viewpager);
        // Affectation de l'adapter au ViewPager
        pager.setAdapter(this.pagerAdapter);

        if (place != null){
            this.notifyPlaceLoaded();
        }
        else{
            loadPlace(placeId);
        };
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
                Toast.makeText(that, "This place does not exists anymore", Toast.LENGTH_LONG);
                // TODO cannot load place (invalid ? )
                IntentsUtils.home(that);
            }
        });
    }

    private void notifyPlaceLoaded() {
        // called when the place is loaded
        // this.place is the place loaded
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
                /////Handle share actions here
                return true;
            case R.id.action_RT:
                /////Handle RT actions here
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void initAdapters() {
        // //PlacesAdapter
        //PlacesAdapter placesAdapter = new PlacesAdapter(this);
        //placesAdapter.add(currentPlace);
        //placeListView.setAdapter(placesAdapter);

        // TagsAndCountersAdapter
        TagsAndCountersAdapter tagsAndCountersAdapter = new TagsAndCountersAdapter(this);
        tagsAndCountersAdapter.add(Tag.createDummy());
        tagsAndCountersAdapter.notifyDataSetChanged();

        //Set adapter
        tagsListView.setAdapter(tagsAndCountersAdapter);
    }
}