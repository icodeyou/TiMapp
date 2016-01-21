package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.entities.Tag;

public class PlaceActivity extends BaseActivity{
    private String TAG = "PlaceActivity";
    private ListView tagsListView;
    private ListView placeListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        this.initToolbar(true);

        //Initialize
        tagsListView = (ListView) findViewById(R.id.tags_lv);
        placeListView = (ListView) findViewById(R.id.place_lv);

        initAdapters();
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