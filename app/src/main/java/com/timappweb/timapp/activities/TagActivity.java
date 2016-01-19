package com.timappweb.timapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.greenfrvr.hashtagview.HashtagView;
import com.timappweb.timapp.adapters.DataTransformTag;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;
import com.timappweb.timapp.R;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.LinkedList;

public class TagActivity extends BaseActivity{

    private String TAG = "TagActivity";

    //Views
    private HorizontalTagsRecyclerView selectedTagsRV;
    private HashtagView suggestedTagsRV;
    private View progressBarView;

    // @Bind(R.id.hashtags1)
    protected HashtagView suggestedTagsView;

    //others
    private SearchAndSelectTagManager searchAndSelectTagManager;
    private View selectedTagsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        this.initToolbar(true);

        //Initialize variables
        selectedTagsRV = (HorizontalTagsRecyclerView) findViewById(R.id.rv_selected_tags);
        selectedTagsView = findViewById(R.id.rv_selected_tags);
        suggestedTagsView = (HashtagView) findViewById(R.id.rv_search_suggested_tags);
        this.progressBarView = findViewById(R.id.progressbar);

        setSelectedTagsViewGONE();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_tags, menu);

        setSearchview(menu);
        searchView.clearFocus();

        //set hint for searchview
        searchAndSelectTagManager = new SearchAndSelectTagManager(this,
                searchView, suggestedTagsView, selectedTagsRV);

        suggestedTagsView.setData(new LinkedList<Tag>(), new DataTransformTag());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        return true;
    }

    //----------------------------------------------------------------------------------------------
    //Private methods

    //----------------------------------------------------------------------------------------------
    //Public methods
    public void setSelectedTagsViewGONE() {
        findViewById(R.id.top_line_hrv).setVisibility(View.GONE);
        selectedTagsView.setVisibility(View.GONE);
        findViewById(R.id.bottom_line_hrv).setVisibility(View.GONE);
    }

    public void setSelectedTagsViewVISIBLE() {
        findViewById(R.id.top_line_hrv).setVisibility(View.VISIBLE);
        selectedTagsView.setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_line_hrv).setVisibility(View.VISIBLE);
    }


    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    public View getProgressBarView() {
        return progressBarView;
    }

    //----------------------------------------------------------------------------------------------
    //Miscellaneous
    public void testClick(View view) {
        Intent intent = new Intent(this,PublishActivity.class);
        startActivity(intent);
    }
}
