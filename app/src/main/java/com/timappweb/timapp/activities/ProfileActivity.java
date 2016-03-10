package com.timappweb.timapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class ProfileActivity extends BaseActivity{

    String TAG = "ProfileActivity";

    private User mUser = null;

    private TextView tvUsername;
    private TextView tvAge;
    private TextView tvCountTags;
    private TextView tvCountPlaces;

    // TODO jean: remplace ca par un adapter. Les tags de l'utilsateur: MyApplication.getCurrentUser().tags
    private TextView tvTag1;
    private TextView tvTag2;
    private TextView tvTag3;
    private ListView placeView;
    private View loadingView;
    private View mainView;
    private LinearLayout layoutTagsProfile;
    private View noConnectionView;
    private List<Tag> tags;
    private ImageView profilePicture;
    private View progressView1;
    private View progressView2;


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
        placeView = (ListView) findViewById(R.id.place_lv);
        tvTag1 = (TextView) findViewById(R.id.tv_tag1);
        tvTag2 = (TextView) findViewById(R.id.tv_tag2);
        tvTag3 = (TextView) findViewById(R.id.tv_tag3);
        //loadingView = findViewById(R.id.loading_view);
        mainView = findViewById(R.id.main_view);
        noConnectionView = findViewById(R.id.no_connection_view);
        layoutTagsProfile = (LinearLayout) findViewById(R.id.layout_tags_profile);
        profilePicture = (ImageView) findViewById(R.id.profile_picture);
        progressView1 = findViewById(R.id.progress_view1);
        progressView2 = findViewById(R.id.progress_view2);

        initAdapter();
        setListeners();

        // Get data
        int userId = IntentsUtils.extractUserId(getIntent());
        if (userId == -1 && MyApplication.isLoggedIn()){
            userId = MyApplication.getCurrentUser().id;
        }
        else{
            Log.e(TAG, "Username should be set to see profile activity.");
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

    private void initAdapter() {
        PlacesAdapter placesAdapter = new PlacesAdapter(this);
        //TODO : find last place
        placeView.setAdapter(placesAdapter);
    }

    private void setListeners() {
        final Activity activity = this;

        setMyTouchListener(layoutTagsProfile,R.color.colorAccentLight);
        layoutTagsProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.editProfile(activity, mUser);
            }
        });
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
                    tags = mUser.tags;
                    //setTags(tags);
                    progressView1.setVisibility(View.GONE);
                    tvCountTags.setText(String.valueOf(mUser.count_posts));
                    tvCountTags.setVisibility(View.VISIBLE);
                    progressView2.setVisibility(View.GONE);
                    tvCountPlaces.setText(String.valueOf(mUser.count_places));
                    tvCountPlaces.setVisibility(View.VISIBLE);

                    // Setting the last post
                    if (mUser.posts != null && mUser.posts.size() > 0) {
                        Post post = mUser.posts.getFirst();
                    }
                    if (mUser.username.equals(MyApplication.getCurrentUser().username)) {
                        invalidateOptionsMenu();
                    }

                    Picasso.with(context).load(mUser.getProfilePictureUrl()).into(profilePicture);

                    //mainView.setVisibility(View.VISIBLE);
                    //loadingView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setTags(List<Tag> tags) {
        if(tags.size()==0) {
            tvTag2.setText("#Newbie");
        } else {
            tvTag1.setText(tags.get(0).name);
            tvTag2.setText(tags.get(1).name);
            tvTag3.setText(tags.get(2).name);
        }
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
