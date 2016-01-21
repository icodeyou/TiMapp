package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.MyPagerAdapter;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.fragments.PlacePeopleFragment;
import com.timappweb.timapp.fragments.PlaceTagsFragment;

import java.util.List;
import java.util.Vector;

public class PlaceActivity extends BaseActivity{
    private String TAG = "PlaceActivity";
    private ListView tagsListView;
    private ListView placeListView;
    private MyPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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