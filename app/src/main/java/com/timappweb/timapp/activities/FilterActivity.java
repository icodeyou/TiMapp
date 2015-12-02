package com.timappweb.timapp.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Tag;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends BaseActivity {

    ////////////////////////////////////////////////////////////////////////////////
    //// onCreate
    ////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        /////////////////Saved tags Recycler view//////////////////////////////////////
        // Get recycler view
        RecyclerView rv_savedTagsList = (RecyclerView) findViewById(R.id.lv_saved_tags);

        //Create and set adapter
        //SavedTagsAdapter SavedTagsAdapter = new SavedTagsAdapter(this, generateData());
        //rv_savedTagsList.setAdapter(SavedTagsAdapter);

        //Set LayoutManager
        GridLayoutManager manager = new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false);
        rv_savedTagsList.setLayoutManager(manager);

        //////////////////Import results into the vertical ListView////////////////////
        //Find listview in XML
        ListView lv_suggestedTags = (ListView) findViewById(R.id.suggested_tags);

        //Example of tags :
        String[] tags_ex = {"hilarious", "despicable", "OKLM", "yeah",
        "whynot","ridiculous","good","awful","sexdrugsandrocknroll", "endofworld", "godsavethequeen"};

        // Array adapter( *activity*, *type of list view*, *my_array*)
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                tags_ex);

        //Set adapter
        lv_suggestedTags.setAdapter(arrayAdapter);
}
////////////////////////////////////////////////////////////////////////////////
    //// Action bar (Searchview) + onBackPressed
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_filter, menu);

        //Set search item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent updateMap = new Intent(this,DrawerActivity.class);
                startActivity(updateMap);
                return true;
            case R.id.action_search:
                /////Handle search actions here
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    public void onUpdateClick(View view) {
        Intent updateMap = new Intent(this,DrawerActivity.class);
        startActivity(updateMap);
    }

    public List<Tag> generateData() {
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag("friteschezjojo",0));
        tags.add(new Tag("#boeing",747));
        tags.add(new Tag("#airbus",380));
        tags.add(new Tag("#lolilol",185));
        tags.add(new Tag("#whatever",184));
        tags.add(new Tag("#salt",154));
        tags.add(new Tag("#beer",146));
        tags.add(new Tag("#idontknowwhattosay",130));
        tags.add(new Tag("#nowords",114));
        tags.add(new Tag("#amazing",104));
        tags.add(new Tag("#wtf",85));
        tags.add(new Tag("#youhavetoseeittobelieveit",55));
        tags.add(new Tag("#ohmygod",30));
        tags.add(new Tag("#thisissofunny",21));
        tags.add(new Tag("#beach",14));
        return tags;
    }

}
