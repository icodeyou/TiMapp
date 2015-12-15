package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.timappweb.timapp.Managers.SearchAndSelectTagManager;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SelectedTagsAdapter;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.utils.IntentsUtils;
import com.timappweb.timapp.views.SelectedTagRecyclerView;
import com.timappweb.timapp.views.SuggestedTagRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends BaseActivity {
    String TAG = "FilterActivity";
    private SearchAndSelectTagManager searchAndSelectTagManager;

    ////////////////////////////////////////////////////////////////////////////////
    //// onCreate
    ////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        this.initToolbar(true);
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

        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            if (searchView != null){

                SuggestedTagRecyclerView suggestedTagRecyclerView = (SuggestedTagRecyclerView) findViewById(R.id.rv_suggested_tags_filter);
                SelectedTagRecyclerView selectedTagsRecyclerView = (SelectedTagRecyclerView) findViewById(R.id.rv_selected_tags_filter);
                searchAndSelectTagManager = new SearchAndSelectTagManager(this,
                        searchView, suggestedTagRecyclerView, selectedTagsRecyclerView);
            }
        }
/*
        if (searchView != null) {
            //Always display the searchview expanded in the action bar
            searchView.setIconifiedByDefault(false);
            //focus on searchBar and open keyboard
            searchView.requestFocus();
            ImageView magImage = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
            magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            //setSearchableInfo & Listener
        }
        */

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
                IntentsUtils.home(this);
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
        IntentsUtils.home(this);
    }

    /////////GENERATE DATA/////////////////////
    public List<Tag> generateData() {
        List<Tag> data = new ArrayList<>();
        data.add(new Tag("test", 0));
        return data;
    }

    public List<Tag> addDataToAdapter(String newData, SelectedTagsAdapter adapter) {
        List<Tag> data = adapter.getData();
        data.add(new Tag(newData, 0));
        adapter.notifyDataSetChanged();
        return data;
    }

}
