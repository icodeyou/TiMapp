package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.timappweb.timapp.Managers.SearchAndSelectTagManager;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.utils.IntentsUtils;
import com.timappweb.timapp.views.HorizontalRecyclerView;
import com.timappweb.timapp.views.FilledRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends BaseActivity {
    String TAG = "FilterActivity";
    private SearchAndSelectTagManager searchAndSelectTagManager;
    private SearchView  searchView;
    private Menu menu;
    private Activity activity=this;

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
        this.menu = menu;

        setSearchview();

        FilledRecyclerView filledRecyclerView = (FilledRecyclerView) findViewById(R.id.rv_suggested_tags_filter);
        HorizontalRecyclerView selectedTagsRecyclerView = (HorizontalRecyclerView) findViewById(R.id.rv_selected_tags);
        searchAndSelectTagManager = new SearchAndSelectTagManager(this,
                searchView, filledRecyclerView, selectedTagsRecyclerView);

        return true;
    }

    private void setSearchview() {
        //Set search item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.expandActionView();

        //Always display the searchview expanded in the action bar
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                NavUtils.navigateUpFromSameTask(activity);
                return false;
            }
        });

        //set searchView
        searchView = (SearchView) searchItem.getActionView();
        //set hint for searchview
        searchView.setQueryHint(activity.getString(R.string.hint_searchview_add_post));
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

    public List<Tag> addDataToAdapter(String newData, HorizontalTagsAdapter adapter) {
        List<Tag> data = adapter.getData();
        data.add(new Tag(newData, 0));
        adapter.notifyDataSetChanged();
        return data;
    }

}
