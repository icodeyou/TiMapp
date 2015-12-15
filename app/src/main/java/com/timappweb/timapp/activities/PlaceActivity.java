package com.timappweb.timapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.ListTagAdapter;
import com.timappweb.timapp.entities.Tag;

import java.util.ArrayList;

public class PlaceActivity extends BaseActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        this.initToolbar(true);

        //Create ListView
        //////////////////////////////////////////////////////////////////////////////
        //Find listview in XML
        ListView lvTags = (ListView) findViewById(R.id.list_tags_place);

        // pass context and data to the custom adapter
        ListTagAdapter listTagAdapter = new ListTagAdapter(this,generateData());

        //Set adapter
        lvTags.setAdapter(listTagAdapter);
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


    //Generate Data for ListView
    //////////////////////////////////////////////////////////////////////////////
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