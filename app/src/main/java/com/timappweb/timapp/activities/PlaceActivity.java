package com.timappweb.timapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.PlaceAdapter;
import com.timappweb.timapp.entities.Tag;

import java.util.ArrayList;

public class PlaceActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Import results into the vertical ListView
        //////////////////////////////////////////////////////////////////////////////
        //Find listview in XML
        ListView lv = (ListView) findViewById(R.id.tags);

        // pass context and data to the custom adapter
        PlaceAdapter placeAdapter = new PlaceAdapter(this,generateData());

        //Set adapter
        lv.setAdapter(placeAdapter);
    }

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

    private ArrayList<Tag> generateData(){
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(new Tag("#friteschezjojo",1587));
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

    public void onPeopleClick(View view) {
        Intent intent = new Intent(this,PeopleActivity.class);
        startActivity(intent);
    }

    public void onCommentClick(View view) {
        Intent intent = new Intent(this,CommentActivity.class);
        startActivity(intent);
    }
}