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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SavedTagsAdapter;
import com.timappweb.timapp.entities.Tag;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends BaseActivity {
    String TAG = "FilterActivity_TAG";

    ////////////////////////////////////////////////////////////////////////////////
    //// onCreate
    ////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Log.i(TAG,"ACtivity created");
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        /////////////////Saved tags Recycler view//////////////////////////////////////
        // Get recycler view
        RecyclerView rv_savedTagsList = (RecyclerView) findViewById(R.id.rv_savedTags_filter);

        Log.i(TAG,"generate data");
        final SavedTagsAdapter savedTagsAdapter = new SavedTagsAdapter(this, generateData());
        rv_savedTagsList.setAdapter(savedTagsAdapter);

        //Set LayoutManager
        GridLayoutManager manager = new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false);
        rv_savedTagsList.setLayoutManager(manager);

        //////////////////Import examples into the vertical ListView////////////////////
        //Find listview in XML
        ListView lv_suggestedTags = (ListView) findViewById(R.id.suggested_tags_filter);

        //Example of tags :
        String[] tags_ex = {"blablacar", "blablacar2", "OKLM", "yeah",
        "whynot","blablacar","good","blablacar","blablacar", "blablacar", "godsavethequeen"};

        // Array adapter( *activity*, *type of list view*, *my_array*)
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                tags_ex);

        //Set adapter
        lv_suggestedTags.setAdapter(arrayAdapter);

        //set onClickListener
        lv_suggestedTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedTag = String.valueOf(parent.getItemAtPosition(position));
                addDataToAdapter(selectedTag, savedTagsAdapter);
            }
        });
}
////////////////////////////////////////////////////////////////////////////////
    //// onCreateOptionsMenu
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

        final SearchView finalSearchView = searchView;
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Get recycler view
                RecyclerView rv_savedTagsList = (RecyclerView) findViewById(R.id.rv_savedTags_filter);
                //Get adapter
                RecyclerView.Adapter adapter = rv_savedTagsList.getAdapter();
                SavedTagsAdapter savedTagsAdapter = (SavedTagsAdapter) adapter;
                //Set new values
                addDataToAdapter(query, savedTagsAdapter);

                finalSearchView.setIconified(true);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Get the text each time the value is change in the searchbox
                return false;
            }
        };

        if (searchView != null) {
            //Always display the searchview expanded in the action bar
            searchView.setIconifiedByDefault(false);

            //focus on searchBar and open keyboard
            searchView.requestFocus();
            ImageView magImage = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
            magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));

            //setSearchableInfo & Listener
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(queryTextListener);
        }

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //// onOptionsItemSelected
    ////////////////////////////////////////////////////////////////////////////////
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

    /////////GENERATE DATA/////////////////////
    public List<Tag> generateData() {
        List<Tag> data = new ArrayList<>();
        data.add(new Tag("bar", 0));
        return data;
    }
    public List<Tag> addDataToAdapter(String newData, SavedTagsAdapter adapter) {
        List<Tag> data = adapter.getData();
        data.add(new Tag(newData, 0));
        adapter.notifyDataSetChanged();
        return data;
    }

}
