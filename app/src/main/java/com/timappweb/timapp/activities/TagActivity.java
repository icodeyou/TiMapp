package com.timappweb.timapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
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
        selectedTagsRV = (HorizontalRecyclerView) findViewById(R.id.rv_selected_tags);
        suggestedTagsRV = (FilledRecyclerView) findViewById(R.id.rv_suggested_tags);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_tags, menu);

        setSearchview(menu);

        //set hint for searchview
        searchView.setQueryHint(getString(R.string.hint_searchview_activity_tag));

        searchAndSelectTagManager = new SearchAndSelectTagManager(this,
                searchView, suggestedTagsRV, selectedTagsRV);

        return true;
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
