package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.IntentsUtils;

import java.util.ArrayList;

import retrofit.client.Response;

public class PostActivity extends BaseActivity {

    private static final String TAG = "PostActivity" ;
    private Post currentPost = null;
    private ArrayAdapter<String> tagsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //------------------------------------------------------------------------------------------
        ListView listViewTags = (ListView) findViewById(R.id.list_tags);
        tagsAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<String>());
        listViewTags.setAdapter(tagsAdapter);
        //------------------------------------------------------------------------------------------
        // 2 cases:
        //  - we gave the full post => we just display data
        //  - we gave the post id => we need to request the server to have extra informations
        currentPost = (Post) getIntent().getSerializableExtra("post");
        int postId = (int) getIntent().getExtras().getInt("post.id", -1);

        if (currentPost == null && postId <= 0){
            Log.e(TAG, "The post is null");
            IntentsUtils.home(this);
            return;
        }
        else if (postId > 0){
            Log.d(TAG, "Loading post from post id " + postId);
            this.loadPost(postId);
        }
        else {
            Log.d(TAG, "Using post given in extras " + currentPost);
            this.fetchDataToView();
            if (!currentPost.hasTagsLoaded()){
                this.loadTagsForPost();
            }
        }
        //------------------------------------------------------------------------------------------

        this.initToolbar(true);
    }

    private void loadTagsForPost() {
        RestClient.service().loadTagsFromPost(currentPost.getId(), new RestCallback<ArrayList<Tag>>(this) {
            @Override
            public void success(ArrayList<Tag> tags, Response response) {
                currentPost.tags = tags;
                fetchDataToView();
            }
        });
    }

    private void loadPost(int postId) {
        RestClient.service().viewPost(postId, new RestCallback<Post>(this) {
            @Override
            public void success(Post post, Response response) {
                if(post == null){
                    // TODO
                    return;
                }
                currentPost = post;
                fetchDataToView();
            }
        });
    }

    private void fetchDataToView(){
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
        textViewUsername.setText(currentPost.getUsername());
        textViewPostName.setText(currentPost.getAdress());

        if (currentPost.hasTagsLoaded()){
            tagsAdapter.addAll(currentPost.getTagsToStringArray());
            tagsAdapter.notifyDataSetChanged();
        }

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
