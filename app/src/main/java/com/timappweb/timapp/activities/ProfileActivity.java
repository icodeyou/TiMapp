package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.DisplayedTagsAdapter;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.IntentsUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import retrofit.client.Response;

public class ProfileActivity extends BaseActivity{

    String TAG = "ProfileActivity";

    private DisplayedTagsAdapter displayedTagsAdapter = null;
    private User mUser = null;
    private TextView tvUsername = null;
    private TextView tvDateCreated = null;
    private TextView tvPostName = null;
    private TextView tvCountPosts = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Toolbar
        this.initToolbar(true);

        /////////////////Fetch tags for Recycler view in LastPost box ! //////////////////////////////////////
        final RecyclerView rv_lastPostTags = (RecyclerView) findViewById(R.id.rv_displayed_tags);
        tvUsername = (TextView) findViewById(R.id.tv_profile_username);
        tvCountPosts = (TextView) findViewById(R.id.tv_profile_count_posts);
        tvDateCreated = (TextView) findViewById(R.id.tv_last_post_created);
        tvPostName = (TextView) findViewById(R.id.tv_last_post_name);

        //set Adapter
        displayedTagsAdapter = new DisplayedTagsAdapter(this, new LinkedList<Tag>());
        rv_lastPostTags.setAdapter(displayedTagsAdapter);

        //Set LayoutManager
        GridLayoutManager manager_savedTags = new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false);
        rv_lastPostTags.setLayoutManager(manager_savedTags);

        // Get data
        String username = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            username = extras.getString("username");
        }
        else if (MyApplication.isLoggedIn()){
            username = MyApplication.getCurrentUser().username;
        }
        if (username == null){
            // Redirect to login activity
            Log.e(TAG, "Username should be set to see profile activity.");
            MyApplication.redirectLogin(this);
        }
        this.loadUser(username); // TODO get username by bundle of current username if none
    }

    private void loadUser(final String username){

        RestClient.service().profile(username, new RestCallback<User>(this) {
            @Override
            public void success(User user, Response response) {
                Log.i(TAG, user + " loaded");
                mUser = user;
                tvUsername.setText(mUser.username);
                tvCountPosts.setText(String.valueOf(mUser.count_posts));

                // Setting the last post
                if (mUser.posts != null && mUser.posts.size() > 0){
                    Post post = mUser.posts.getFirst();
                    displayedTagsAdapter.setData(post.tags);
                    tvDateCreated.setText(post.getPrettyTimeCreated());
                    tvPostName.setText(post.getAdress());
                    View layout = findViewById(R.id.box_profile_last_post);
                    layout.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    ///////// Generate pre-selected tags here/////////////////////
    public List<Tag> generateDummyData() {
        List<Tag> data = new ArrayList<>();
        data.add(new Tag("sexygirls", 0));
        data.add(new Tag("smimmingpool", 0));
        data.add(new Tag("swimsuit", 0));
        data.add(new Tag("beautifulplace", 0));
        return data;
    }

    public void onLastPostClick(View view) {
        if (mUser != null && mUser.posts.size() > 0){
            Post post = mUser.posts.getFirst();
            post.user = mUser;
            IntentsUtils.post(this, post);
        }
    }
}
