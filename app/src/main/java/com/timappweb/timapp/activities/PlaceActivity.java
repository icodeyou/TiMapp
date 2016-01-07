package com.timappweb.timapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.entities.Tag;

import java.util.ArrayList;

public class PlaceActivity extends BaseActivity{
    private String TAG = "PlaceActivity";
    private ListView lvTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        this.initToolbar(true);

        //Initialize
        lvTags = (ListView) findViewById(R.id.list_tags);

        initAdapter();
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


    private void initAdapter() {
        // pass context and data to the custom adapter
        TagsAndCountersAdapter tagsAndCountersAdapter = new TagsAndCountersAdapter(this);
        tagsAndCountersAdapter.generateDummyData();

        //Set adapter
        lvTags.setAdapter(tagsAndCountersAdapter);
    }

    //essayer de le faire programmatiquement
    public void onPeopleClick(View view) {
        Intent intent = new Intent(this,PlaceFriendsActivity.class);
        startActivity(intent);
    }

    public void onCommentClick(View view) {
        Intent intent = new Intent(this,PlacePostsActivity.class);
        startActivity(intent);
    }
}