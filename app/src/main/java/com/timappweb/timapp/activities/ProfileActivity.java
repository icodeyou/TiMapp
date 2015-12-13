package com.timappweb.timapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.LastPostTagsAdapter;
import com.timappweb.timapp.adapters.SelectedTagsAdapter;
import com.timappweb.timapp.entities.Tag;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseActivity{

    String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        /////////////////Fetch tags for Recycler view in LastPost box ! //////////////////////////////////////
        // Get recycler view from XML
        final RecyclerView rv_lastPostTags = (RecyclerView) findViewById(R.id.rv_lastPostTags);

        //set Adapter
        final LastPostTagsAdapter lastPostTagsAdapter = new LastPostTagsAdapter(this, generateData());
        rv_lastPostTags.setAdapter(lastPostTagsAdapter);

        //Set LayoutManager
        GridLayoutManager manager_savedTags = new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false);
        rv_lastPostTags.setLayoutManager(manager_savedTags);
    }


    ///////// Generate pre-selected tags here/////////////////////
    public List<Tag> generateData() {
        List<Tag> data = new ArrayList<>();
        data.add(new Tag("sexygirls", 0));
        data.add(new Tag("smimmingpool", 0));
        data.add(new Tag("swimsuit", 0));
        data.add(new Tag("beautifulplace", 0));
        return data;
    }

    public void onLastPostClick(View view) {
        Intent intent = new Intent(this,PostActivity.class);
        startActivity(intent);
    }
}
