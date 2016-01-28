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
import com.timappweb.timapp.config.IntentsUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class PostActivity extends BaseActivity {

    private static final String TAG = "PostActivity" ;
    private Post currentPost = null;
    private ArrayAdapter<String> tagsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //------------------------------------------------------------------------------------------
        ListView listViewTags = (ListView) findViewById(R.id.tags_lv);
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
        Call<List<Tag>> call = RestClient.service().loadTagsFromPost(currentPost.getId());
        call.enqueue(new RestCallback<List<Tag>>(this) {
            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
            }

            @Override
            public void onResponse(Response<List<Tag>> response) {
                super.onResponse(response);
                if (response.isSuccess()){
                    List<Tag> tags = response.body();
                    currentPost.setTags(tags);
                    fetchDataToView();
                }
            }
        });
    }

    private void loadPost(int postId) {
        Call<Post> call = RestClient.service().viewPost(currentPost.getId());
        call.enqueue(new RestCallback<Post>(this) {
            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
            }

            @Override
            public void onResponse(Response<Post> response) {
                super.onResponse(response);
                if (response.isSuccess()){
                    Post post = response.body();
                    currentPost = post;
                    fetchDataToView();
                }
            }
        });
    }

    private void fetchDataToView(){
        TextView textViewCreated = (TextView) findViewById(R.id.post_created);
        TextView textViewUsername = (TextView) findViewById(R.id.post_username);
        TextView textViewPostName = (TextView) findViewById(R.id.post_name);

        textViewCreated.setText(currentPost.getPrettyTimeCreated());
        textViewUsername.setText(currentPost.getUsername());
        textViewPostName.setText(currentPost.getAddress());

        if (currentPost.hasTagsLoaded()){
            tagsAdapter.addAll(currentPost.getTagsToStringArray());
            tagsAdapter.notifyDataSetChanged();
        }

    }

    public void onProfileCLick(View view) {
        Log.d(TAG, "PostActivity.onProfileCLick()");
        if (currentPost.user != null){
            IntentsUtils.profile(this, currentPost.user.username);
        }
    }
}
