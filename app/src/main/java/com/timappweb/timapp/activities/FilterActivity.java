package com.timappweb.timapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.greenfrvr.hashtagview.HashtagView;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.utils.IntentsUtils;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.List;

public class FilterActivity extends BaseActivity {
    String TAG = "FilterActivity";
    private SearchAndSelectTagManager searchAndSelectTagManager;
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
        getMenuInflater().inflate(R.menu.menu_search_tags, menu);

        setSearchview(menu);

        //set hint for searchview
        searchView.setQueryHint(getString(R.string.hint_searchview_add_post));

        HashtagView hashtagView = (HashtagView) findViewById(R.id.rv_suggested_tags_filter);
        HorizontalTagsRecyclerView selectedTagsRecyclerView = (HorizontalTagsRecyclerView) findViewById(R.id.rv_selected_tags);
        searchAndSelectTagManager = new SearchAndSelectTagManager(this,
                searchView, hashtagView, selectedTagsRecyclerView);

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

    public List<Tag> addDataToAdapter(String newData, HorizontalTagsAdapter adapter) {
        List<Tag> data = adapter.getData();
        data.add(new Tag(newData, 0));
        adapter.notifyDataSetChanged();
        return data;
    }

}
