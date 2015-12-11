package com.timappweb.timapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Post;

public class PostActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String TAG = "PostActivity";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //------------------------------------------------------------------------------------------
        Post post = (Post) getIntent().getSerializableExtra("post");
        if (post == null){
            Log.i(TAG, "The post is null");
            // TODO redirst to home ?
            return;
        }

        //------------------------------------------------------------------------------------------
        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //------------------------------------------------------------------------------------------
        ListView listViewTags = (ListView) findViewById(R.id.list_tags_place);
        TextView textViewCreated = (TextView) findViewById(R.id.post_created);
        TextView textViewComment = (TextView) findViewById(R.id.post_comment);
        TextView textViewUsername = (TextView) findViewById(R.id.post_username);
        TextView textViewPostName = (TextView) findViewById(R.id.post_name);

        textViewComment.setText(post.comment);
        textViewCreated.setText(post.getCreatedDate());
        textViewUsername.setText(post.user != null ? post.user.username : "User unactivated");
        textViewPostName.setText("Spot name to do ");

        //Example of tags :
        String[] tags_ex = post.getTagsToStringArray();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                tags_ex);

        listViewTags.setAdapter(arrayAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spot, menu);
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

    public void onProfileCLick(View view) {
        Intent intent = new Intent(this,ProfileActivity.class);
        startActivity(intent);
    }
}
