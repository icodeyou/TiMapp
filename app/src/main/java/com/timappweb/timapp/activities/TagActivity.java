package com.timappweb.timapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.timappweb.timapp.managers.SearchAndSelectTagManager;
import com.timappweb.timapp.R;
import com.timappweb.timapp.views.FilledRecyclerView;
import com.timappweb.timapp.views.HorizontalRecyclerView;

public class TagActivity extends BaseActivity{

    private String TAG = "TagActivity";
    private InputMethodManager              imm;

    //Views
    private SearchView                      searchView;
    private HorizontalRecyclerView          selectedTagsRV;
    private FilledRecyclerView              suggestedTagsRV;

    //others
    private SearchAndSelectTagManager searchAndSelectTagManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        this.initToolbar(true);

        //Initialize variables
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        searchView = (SearchView) findViewById(R.id.searchview);
        selectedTagsRV = (HorizontalRecyclerView) findViewById(R.id.rv_search_selected_tags);
        suggestedTagsRV = (FilledRecyclerView) findViewById(R.id.rv_search_suggested_tags);

        //set searchview
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.hint_searchview_activity_tag));

        //Set the manager for inputs and suggestions of tags
        searchAndSelectTagManager = new SearchAndSelectTagManager(this,
                searchView, suggestedTagsRV, selectedTagsRV);
    }

    //----------------------------------------------------------------------------------------------
    //Private methods

    //----------------------------------------------------------------------------------------------
    //Public methods

    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    //----------------------------------------------------------------------------------------------
    //Miscellaneous
    public void testClick(View view) {
        Intent intent = new Intent(this,PublishActivity.class);
        startActivity(intent);
    }
}
