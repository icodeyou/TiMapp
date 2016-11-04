package com.timappweb.timapp.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.TagsProfileAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.SyncOneEntryLoader;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.databinding.ActivityProfileBinding;
import com.timappweb.timapp.sync.data.DataSyncAdapter;

import java.util.List;

public class ProfileActivity extends BaseActivity  {

    public static final String TAG = "ProfileActivity";
    public static final int ACTIVITY_RESULT_EDIT_PROFILE = 1;

    private User mUser = null;
    private long userId;
    private ListView tagsListView;
    private View layoutTagsProfile;
    private SimpleDraweeView profilePicture;
    private UserLoader mLoader;
    private ActivityProfileBinding mBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        //Toolbar
        this.initToolbar(true);

        tagsListView = (ListView) findViewById(R.id.listview_usertags);
        //loadingView = findViewById(R.id.loading_view);
        layoutTagsProfile = findViewById(R.id.layout_tags_profile);
        profilePicture = (SimpleDraweeView) findViewById(R.id.profile_picture);

        initUserTagsAdapter();

        // Get data
        userId = IntentsUtils.extractUserId(getIntent());
        if (userId == -1 && MyApplication.isLoggedIn()){
            userId = MyApplication.getCurrentUser().id;
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
        if (MyApplication.isLoggedIn()){
            if (mUser != null && mUser.getRemoteId() == MyApplication.getCurrentUser().getRemoteId()) {
                item.setVisible(true);
            }
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
        mBinding.setUser(mUser);

        initUserTagsAdapter();

        TagsProfileAdapter tagsAdapter = (TagsProfileAdapter) tagsListView.getAdapter();
        if(mUser.hasTags()){
            Log.v(TAG, "User has a: " + mUser.getTags().size() + " tag(s)");
            tagsAdapter.clear();
            tagsAdapter.addAll(mUser.getTags());
        }
        else {
            Tag defaultTag = new Tag(getString(MyApplication.isCurrentUser(mUser)
                    ? R.string.define_yourself_tag
                    : R.string.newbie_tag));
            tagsAdapter.add(defaultTag);
        }
        tagsAdapter.notifyDataSetChanged();

        if (MyApplication.isCurrentUser(mUser)) {
            invalidateOptionsMenu();
            setTagsListeners();
        }

        String photoUrl = mUser.getProfilePictureUrl();
        Uri uri = Uri.parse(photoUrl);
        profilePicture.setImageURI(uri);
    }


    private void setTagsListeners() {
        layoutTagsProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.editProfile(ProfileActivity.this, mUser);
            }
        });
        tagsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IntentsUtils.editProfile(ProfileActivity.this, mUser);
            }
        });
    }


    private void initUserTagsAdapter() {
        TagsProfileAdapter tagsProfileAdapter = new TagsProfileAdapter(this);
        tagsListView.setAdapter(tagsProfileAdapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK){
            return;
        }
        switch (requestCode){
            case ACTIVITY_RESULT_EDIT_PROFILE:
                updateView();
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    // =============================================================================================

    class UserLoader extends SyncOneEntryLoader<User> {

        public UserLoader() {
            super(ProfileActivity.this, userId, User.class, DataSyncAdapter.SYNC_TYPE_USER);
            this.setSwipeAndRefreshLayout();
        }

        @Override
        public void onLoadFinished(Loader<List<User>> loader, List<User> data) {
            super.onLoadFinished(loader, data);
            if (data.size() > 0){
                mUser = data.get(0);
                updateView();
            }
        }
    }

}
