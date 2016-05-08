package com.timappweb.timapp.activities;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.UserTagsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.listeners.ColorAllOnTouchListener;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.utils.loaders.ModelLoader;

import java.util.List;

public class ProfileActivity extends BaseActivity  {

    String TAG = "ProfileActivity";

    private User mUser = null;
    private int userId;

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
    private SimpleDraweeView profilePicture;
    private View progressView1;
    private View progressView2;
    private View lastPostContainer;
    private UserLoader mLoader;


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
        //loadingView = findViewById(R.remote_id.loading_view);
        mainView = findViewById(R.id.main_view);
        noConnectionView = findViewById(R.id.no_connection_view);
        layoutTagsProfile = findViewById(R.id.layout_tags_profile);
        profilePicture = (SimpleDraweeView) findViewById(R.id.profile_picture);
        progressView1 = findViewById(R.id.progress_view1);
        progressView2 = findViewById(R.id.progress_view2);
        lastPostContainer = findViewById(R.id.profile_last_post_container);

        initUserTagsAdapter();

        // Get data
        userId = IntentsUtils.extractUserId(getIntent());
        if (userId == -1 && MyApplication.isLoggedIn()){
            userId = MyApplication.getCurrentUser().remote_id;
        }

        if (userId == -1){
            Log.e(TAG, "User id should be set to see profile activity.");
            MyApplication.redirectLogin(this);
            return;
        }

        mLoader = new UserLoader();
        getSupportLoaderManager().initLoader(0, null, mLoader);
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

    private void updateView() {

        Log.i(TAG, mUser + " loaded");
        tvUsername.setText(mUser.username);
        tvAge.setText("100 years old");
        progressView1.setVisibility(View.GONE);
        tvCountTags.setText(String.valueOf(mUser.count_posts));
        tvCountTags.setVisibility(View.VISIBLE);
        progressView2.setVisibility(View.GONE);
        tvCountPlaces.setText(String.valueOf(mUser.count_places));
        tvCountPlaces.setVisibility(View.VISIBLE);

        initUserTagsAdapter();

        UserTagsAdapter adapter = (UserTagsAdapter) tagsListView.getAdapter();
        if(mUser.hasTags()){
            Log.v(TAG, "User has a: " + mUser.getTags().size() + " tag(s)");
            adapter.clear();
            adapter.addAll(mUser.getTags());
            adapter.notifyDataSetChanged();
        }
        else {
            Tag defaultTag = new Tag(getString(MyApplication.isCurrentUser(mUser.remote_id)
                    ? R.string.define_yourself_tag
                    : R.string.newbie_tag));
            adapter.add(defaultTag);
            adapter.notifyDataSetChanged();
        }

        if (MyApplication.isCurrentUser(mUser.remote_id)) {
            invalidateOptionsMenu();
            setTagsListeners();
        }
        String photoUrl = mUser.getProfilePictureUrl();
        Uri uri = Uri.parse(photoUrl);
        profilePicture.setImageURI(uri);
    }


    private void setTagsListeners() {
        final Activity activity = this;

        if(mUser.username.equals(MyApplication.getCurrentUser().username)) {
            layoutTagsProfile.setOnTouchListener(new ColorAllOnTouchListener());

            layoutTagsProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.editProfile(activity, mUser);
                }
            });
        }
    }


    private void initUserTagsAdapter() {
        UserTagsAdapter userTagsAdapter= new UserTagsAdapter(this);
        tagsListView.setAdapter(userTagsAdapter);
    }

    // =============================================================================================

    class UserLoader implements LoaderManager.LoaderCallbacks<List<User>>{

        @Override
        public Loader<List<User>> onCreateLoader(int id, Bundle args) {
            mUser = (User) SyncBaseModel.getRemoteEntry(User.class, ProfileActivity.this, userId, DataSyncAdapter.SYNC_TYPE_USER);
            if (mUser != null){
                updateView();
            }
            return new ModelLoader<User>(ProfileActivity.this, User.class, SyncBaseModel.queryByRemoteId(User.class, userId), false);
        }

        @Override
        public void onLoadFinished(Loader<List<User>> loader, List<User> data) {
            Log.d(TAG, "User loaded finish");
            if (data.size() > 0){
                mUser = data.get(0);
                updateView();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<User>> loader) {

        }
    }

}
