package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.activeandroid.query.From;
import com.google.gson.JsonArray;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SelectFriendsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.MultipleEntryLoaderCallback;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.utils.loaders.ModelLoader;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class InviteFriendsActivity extends BaseActivity {

    private String              TAG                         = "InviteFriendsActivity";
    private static final int    LOADER_ID_FRIENDS_LIST      = 0;

    //private AutoLabelUI                 mAutoLabel;
    private RecyclerView                recyclerView;
    private SelectFriendsAdapter        adapter;

    private View                        inviteButton;
    private View                        noFriendsView;

    private List<User>                  friendsSelected;
    private List<User>                  allFbFriends;
    private Place                       place;
    private FriendsLoader               mLoader;
    private ContentResolver             mResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        place = IntentsUtils.extractPlace(getIntent());
        if (place == null){
            IntentsUtils.home(this);
            finish();
        }

        this.initToolbar(true);

        inviteButton = findViewById(R.id.invite_button);
        noFriendsView = findViewById(R.id.no_friends_view);
        //mAutoLabel = (AutoLabelUI) findViewById(R.remote_id.label_view);
        //mAutoLabel.setBackgroundResource(R.drawable.round_corner_background);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        initSelectedFriends();
        initAdapterListFriends();
        //initAutoLabel();
        initInviteButton();

        mLoader = new FriendsLoader();
        getSupportLoaderManager().initLoader(LOADER_ID_FRIENDS_LIST, null, mLoader);
    }

    private void initSelectedFriends() {
        friendsSelected = new ArrayList<>();
        //TODO : Get invited users from placeActivity and add them to the list
    }

    private void initAdapterListFriends() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        adapter = new SelectFriendsAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                User friend = allFbFriends.get(position);
                boolean success;
                if (friendsSelected.contains(friend)) {
                    success = true;//mAutoLabel.removeLabel(position);
                    friendsSelected.remove(friend);
                } else {
                    success = true;// mAutoLabel.addLabel(friend.getUsername(), position);
                    friendsSelected.add(friend);
                }

                inviteButton.setVisibility(friendsSelected.size() > 0 ? View.VISIBLE : View.INVISIBLE);
                adapter.notifyDataSetChanged();
            }
        });
    }
/*
    private void initAutoLabel() {
        int maxLabels = ConfigurationProvider.rules().max_invite_per_request;
        //TODO : Replace 20 by maxLabels;
        mAutoLabel.setMaxLabels(20);
        Log.d(TAG, "Max labels : " + maxLabels);

        mAutoLabel.setOnLabelsCompletedListener(new AutoLabelUI.OnLabelsCompletedListener() {
            @Override
            public void onLabelsCompleted() {
                Toast.makeText(InviteFriendsActivity.this, R.string.cannot_add_more_friends,
                        Toast.LENGTH_SHORT).show();
            }
        });

        mAutoLabel.setOnRemoveLabelListener(new AutoLabelUI.OnRemoveLabelListener() {
            @Override
            public void onRemoveLabel(View view, int position) {
                friendsSelected.remove(position);
                adapter.notifyDataSetChanged();
            }
        });

        mAutoLabel.setOnLabelsEmptyListener(new AutoLabelUI.OnLabelsEmptyListener() {
            @Override
            public void onLabelsEmpty() {
                updateButtonVisibility();
            }
        });

        mAutoLabel.setOnLabelClickListener(new AutoLabelUI.OnLabelClickListener() {
            @Override
            public void onClickLabel(View v) {

            }
        });
    }
*/
    private void initInviteButton() {
        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Number of friends invited : " + friendsSelected.size());
                sendInvites();
            }
        });
    }

    private void sendInvites(){
        // Encoding data:
        ArrayList<Integer> ids = new ArrayList<>();
        for (User user: friendsSelected){
            ids.add(user.remote_id);
        }
        Call<JsonArray> call = RestClient.service().sendInvite(place.remote_id, ids);
        call.enqueue(new RestCallback<JsonArray>(this) {
            @Override
            public void onResponse200(Response<JsonArray> response) {
                Toast.makeText(context, R.string.toast_thanks_for_sharing, Toast.LENGTH_LONG).show();
                finishActivityResult();
            }

        });
        this.apiCalls.add(call);
    }

    public List<User> getFriendsSelected() {
        return friendsSelected;
    }


    private void finishActivityResult(){
        Intent intent = NavUtils.getParentActivityIntent(this);
        Bundle bundle = new Bundle();
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }


    // LOADER FUNCTIONS ============================================================================

    class FriendsLoader extends MultipleEntryLoaderCallback
    {

        public FriendsLoader() {
            super(InviteFriendsActivity.this, 3600 * 24 * 1000, DataSyncAdapter.SYNC_TYPE_FRIENDS, MyApplication.getCurrentUser().getFriendsQuery());
            this.setSwipeAndRefreshLayout((SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout));
        }

        @Override
        public void onLoadFinished(Loader loader, List data) {
            super.onLoadFinished(loader, data);
            allFbFriends = data;
            adapter.clear();
            adapter.setData(data);
            adapter.notifyDataSetChanged();
            //setProgressBarIndeterminateVisibility(false);
            Log.i(TAG, "Loaded " + data.size() + " friends for the user");
            //noFriendsView.setVisibility(data.size() == 0 ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public void onLoaderReset(Loader loader) {
            super.onLoaderReset(loader);
            adapter.clear();
            adapter.notifyDataSetChanged();
        }
    }
}
