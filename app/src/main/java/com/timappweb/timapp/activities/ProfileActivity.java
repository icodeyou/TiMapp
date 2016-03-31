package com.timappweb.timapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.UserTagsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.listeners.ColorButtonOnTouchListener;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import retrofit2.Call;
import retrofit2.Response;


public class ProfileActivity extends BaseActivity{

    String TAG = "ProfileActivity";

    private User mUser = null;

    private TextView tvUsername;
    private TextView tvAge;
    private TextView tvCountTags;
    private TextView tvCountPlaces;
    private ListView lastPostListView;
    private ListView tagsListView;
    private View loadingView;
    private View mainView;
    private View layoutTagsProfile;
    private View noConnectionView;
    private CircularImageView profilePicture;
    private View progressView1;
    private View progressView2;
    private View lastPostContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Toolbar
        this.initToolbar(true);

        //Initialize
        tvUsername = (TextView) findViewById(R.id.tv_profile_username);
        tvAge = (TextView) findViewById(R.id.text_age);
        tvCountTags = (TextView) findViewById(R.id.tags_counter);
        tvCountPlaces = (TextView) findViewById(R.id.places_counter);
        lastPostListView = (ListView) findViewById(R.id.profile_last_post);
        tagsListView = (ListView) findViewById(R.id.listview_usertags);
        //loadingView = findViewById(R.id.loading_view);
        mainView = findViewById(R.id.main_view);
        noConnectionView = findViewById(R.id.no_connection_view);
        layoutTagsProfile = findViewById(R.id.layout_tags_profile);
        profilePicture = (CircularImageView) findViewById(R.id.profile_picture);
        progressView1 = findViewById(R.id.progress_view1);
        progressView2 = findViewById(R.id.progress_view2);
        lastPostContainer = findViewById(R.id.profile_last_post_container);

        initUserTagsAdapter();
        setListeners();

        // Get data
        int userId = IntentsUtils.extractUserId(getIntent());
        if (userId == -1 && MyApplication.isLoggedIn()){
            userId = MyApplication.getCurrentUser().id;
        }

        if (userId == -1){
            Log.e(TAG, "User id should be set to see profile activity.");
            MyApplication.redirectLogin(this);
            return;
        }

        this.loadUser(userId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_edit_profile);

        //Check that the user is loaded
        if(mUser!=null) {
            item.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_profile:
                IntentsUtils.editProfile(this, mUser);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initLastPostAdapter() {
        //PostsAdapter postsAdapter = new PostsAdapter(this);
        //lastPostListView.setAdapter(postsAdapter);
    }

    private void setListeners() {
        final Activity activity = this;

        if(mUser.username.equals(MyApplication.getCurrentUser().username)) {
            layoutTagsProfile.setOnTouchListener(new ColorButtonOnTouchListener());

            layoutTagsProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.editProfile(activity, mUser);
                }
            });
        }
    }

    private void loadUser(int userId){
        Call<User> call = RestClient.service().profile(userId);
        call.enqueue(new RestCallback<User>(this) {
            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                //loadingView.setVisibility(View.GONE);
                mainView.setVisibility(View.GONE);
                noConnectionView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(Response<User> response) {
                super.onResponse(response);
                if (response.isSuccess()) {
                    User user = response.body();
                    Log.i(TAG, user + " loaded");
                    mUser = user;
                    tvUsername.setText(mUser.username);
                    tvAge.setText("100 years old");
                    progressView1.setVisibility(View.GONE);
                    tvCountTags.setText(String.valueOf(mUser.count_posts));
                    tvCountTags.setVisibility(View.VISIBLE);
                    progressView2.setVisibility(View.GONE);
                    tvCountPlaces.setText(String.valueOf(mUser.count_places));
                    tvCountPlaces.setVisibility(View.VISIBLE);

                    initLastPostAdapter();
                    initUserTagsAdapter();

                    // Setting the last post
                    /*
                    if (mUser.posts != null && mUser.posts.size() > 0) {
                        Post post = mUser.posts.getFirst();
                        post.user = mUser;
                        Log.v(TAG, "User has a last post: " + post);
                        PostsAdapter adapter = (PostsAdapter) lastPostListView.getAdapter();
                        adapter.add(post);
                        adapter.notifyDataSetChanged();
                        lastPostContainer.setVisibility(View.VISIBLE);
                    }
                    else{
                        lastPostContainer.setVisibility(View.GONE);
                    }*/
                    // Setting tags
                    if (mUser.tags != null) {
                        UserTagsAdapter adapter = (UserTagsAdapter) tagsListView.getAdapter();
                        if(mUser.tags.size() > 0){
                            Log.v(TAG, "User has a: " + mUser.tags.size() + " tag(s)");
                            adapter.clear();
                            adapter.addAll(mUser.tags);
                            adapter.notifyDataSetChanged();
                        }
                        else {
                            adapter.add(new Tag(getString(R.string.define_yourself_tag)));
                            adapter.notifyDataSetChanged();
                        }
                    }

                    if (mUser.username.equals(MyApplication.getCurrentUser().username)) {
                        invalidateOptionsMenu();
                    }
                    String photoUrl = mUser.getProfilePictureUrl();
                    Picasso.with(context).load(photoUrl).into(profilePicture);

                    //mainView.setVisibility(View.VISIBLE);
                    //loadingView.setVisibility(View.GONE);
                }
            }
        });
        apiCalls.add(call);
    }

    private void initUserTagsAdapter() {
        UserTagsAdapter userTagsAdapter= new UserTagsAdapter(this);
        tagsListView.setAdapter(userTagsAdapter);
    }

    ///////// Generate pre-selected tags here/////////////////////
    public void onLastPostClick(View view) {
        if (mUser != null && mUser.posts.size() > 0){
            Post post = mUser.posts.getFirst();
            post.user = mUser;
            IntentsUtils.viewPost(this, post);
        }
    }

}
