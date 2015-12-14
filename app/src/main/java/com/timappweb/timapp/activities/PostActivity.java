package com.timappweb.timapp.activities;

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
import com.timappweb.timapp.utils.IntentsUtils;

public class PostActivity extends BaseActivity {

    private static final String TAG = "PostActivity" ;
    Post currentPost = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String TAG = "PostActivity";

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //------------------------------------------------------------------------------------------
        currentPost = (Post) getIntent().getSerializableExtra("post");
        if (currentPost == null){
            Log.e(TAG, "The post is null");
            IntentsUtils.home(this);
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

        // Hide the comment container if there isn't one
        if (currentPost.comment != null && currentPost.comment.length() > 0){
            textViewComment.setText(currentPost.comment);
        }
        else{
            textViewComment.setVisibility(View.INVISIBLE);
        }

        textViewCreated.setText(currentPost.getPrettyTimeCreated());
        textViewUsername.setText(currentPost.user != null ? currentPost.user.username : "User unactivated");
        textViewPostName.setText(currentPost.getName());

        //Example of tags :
        String[] tags_ex = currentPost.getTagsToStringArray();

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
        Log.d(TAG, "PostActivity.onProfileCLick()");
        if (currentPost.user != null){
            IntentsUtils.profile(this, currentPost.user.username);
        }
    }
}
