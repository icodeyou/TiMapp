package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.config.IntentsUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class ProfileActivity extends BaseActivity{

    String TAG = "ProfileActivity";

    private User mUser = null;

    private TextView tvUsername;
    private TextView tvCountTags;
    private TextView tvCountPlaces;
    private TextView tvTag1;
    private TextView tvTag2;
    private TextView tvTag3;

    private ListView placeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Toolbar
        this.initToolbar(true);

        //Initialize
        tvUsername = (TextView) findViewById(R.id.tv_profile_username);
        tvCountTags = (TextView) findViewById(R.id.tags_counter);
        tvCountPlaces = (TextView) findViewById(R.id.places_counter);
        placeView = (ListView) findViewById(R.id.place_lv);
        tvTag1 = (TextView) findViewById(R.id.tv_tag1);
        tvTag2 = (TextView) findViewById(R.id.tv_tag2);
        tvTag3 = (TextView) findViewById(R.id.tv_tag3);

        initAdapter();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_place, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_profile:
                IntentsUtils.editProfile(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initAdapter() {
        PlacesAdapter placesAdapter = new PlacesAdapter(this);
        //TODO : find last place
        Place place = new Place(1, 0, 0, "test");
        placesAdapter.add(place);
        placeView.setAdapter(placesAdapter);
    }

    private void loadUser(final String username){
        Call<User> call = RestClient.service().profile(username);
        call.enqueue(new RestCallback<User>(this) {
            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
            }

            @Override
            public void onResponse(Response<User> response) {
                super.onResponse(response);
                if (response.isSuccess()){
                    User user = response.body();
                    Log.i(TAG, user + " loaded");
                    mUser = user;
                    tvUsername.setText(mUser.username);
                    tvCountTags.setText(String.valueOf(mUser.count_posts));
                    tvCountPlaces.setText(String.valueOf(mUser.count_places));

                    // Setting the last post
                    if (mUser.posts != null && mUser.posts.size() > 0) {
                        Post post = mUser.posts.getFirst();
                        ArrayList<String> arrayTags = post.getTagsToStringArray();
                    /*
                    for (Tag tag : arrayTags) {
                        String tag1 = "#" + arrayTags.get(1);

                    }*/
                    String tag1 = "#" + arrayTags.get(1);
                    String tag2 = "#" + arrayTags.get(2);
                    String tag3 = "#" + arrayTags.get(3);
                    tvTag1.setText(tag1);
                    tvTag2.setText(tag2);
                    tvTag3.setText(tag3);

                    //TODO : Loader
                    }
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
            IntentsUtils.viewPost(this, post);
        }
    }

}
