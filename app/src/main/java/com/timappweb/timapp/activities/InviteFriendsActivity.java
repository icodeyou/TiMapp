package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SelectFriendsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.entities.UserInvitationFeedback;
import com.timappweb.timapp.data.loader.MultipleEntryLoaderCallback;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.MyModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.PublishInEventCallback;
import com.timappweb.timapp.sync.DataSyncAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

public class InviteFriendsActivity extends BaseActivity {

    private String              TAG                         = "InviteFriendsActivity";
    private static final int    LOADER_ID_FRIENDS_LIST      = 0;

    // ---------------------------------------------------------------------------------------------

    private RecyclerView                recyclerView;
    private SelectFriendsAdapter        adapter;
    private View                        inviteButton;
    private Event                       event;
    private FriendsLoader               mLoader;
    private SwipeRefreshLayout          mSwipeRefreshLayout;

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);

        if (!IntentsUtils.requireLogin(this, false)) {
            return;
        }
        event = IntentsUtils.extractEvent(getIntent());
        if (event == null){
            IntentsUtils.home(this);
            finish();
        }

        this.initToolbar(true);

        inviteButton = findViewById(R.id.invite_button);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        initAdapterListFriends();
        initInviteButton();

        mLoader = new FriendsLoader(MyApplication.getCurrentUser());
        getSupportLoaderManager().initLoader(LOADER_ID_FRIENDS_LIST, null, mLoader);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSupportLoaderManager().getLoader(LOADER_ID_FRIENDS_LIST).forceLoad();
            }
        });
    }

    private void initAdapterListFriends() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SelectFriendsAdapter(this);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                User friend = adapter.getData().get(position);
                SelectFriendsAdapter.InviteInfo info = adapter.getInviteInfo(friend);

                if (info == null){
                    adapter.setSelected(friend, true);
                }
                else if (!info.editable){
                    if (info.selected){
                        Toast.makeText(InviteFriendsActivity.this, R.string.friend_already_invited, Toast.LENGTH_LONG).show();
                    }
                    // not editable, do nothing
                }
                else {
                    adapter.setSelected(friend, !info.selected);
                }
                inviteButton.setVisibility(adapter.count(true, true) > 0 ? View.VISIBLE : View.INVISIBLE);
                adapter.notifyDataSetChanged();
            }
        });

        List<EventsInvitation> invitations = event.getSentInvitationsByUser(MyApplication.getCurrentUser());
        for (EventsInvitation invitation: invitations){
            adapter.setSelected(invitation.user_target, true);
            adapter.setEditable(invitation.user_target, false);
        }
    }

    private void initInviteButton() {
        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInvites();
            }
        });
    }

    private void sendInvites(){
        // Encoding data:
        ArrayList<Integer> ids = new ArrayList<>();
        for (Map.Entry<User, SelectFriendsAdapter.InviteInfo> friendEntry: adapter.getInviteInfo().entrySet()){
            if (friendEntry.getValue().selected && friendEntry.getValue().editable){
                ids.add(friendEntry.getKey().remote_id);
            }
        }
        Call<List<UserInvitationFeedback>> call = RestClient.service().sendInvite(event.remote_id, ids);
        RestClient.buildCall(call)
                .onResponse(new PublishInEventCallback(event, MyApplication.getCurrentUser(), QuotaType.INVITE_FRIEND))
                .onResponse(new HttpCallback<List<UserInvitationFeedback>>() {
                    @Override
                    public void successful(List<UserInvitationFeedback> feedbackList) {
                        Toast.makeText(getApplicationContext(), R.string.toast_thanks_for_sharing, Toast.LENGTH_LONG).show();
                        for (UserInvitationFeedback feedback: feedbackList){
                            if (feedback.success){
                                if (feedback.invitation != null){
                                    MyModel savedModel = feedback.invitation.mySave();
                                }
                            }
                            else{
                                Log.e(TAG, "Cannot invite user=" + feedback.user_id);
                            }
                        }
                        finishActivityResult();
                    }

                    @Override
                    public void notSuccessful() {
                        Toast.makeText(getApplicationContext(), R.string.action_performed_not_successful, Toast.LENGTH_LONG).show();
                    }
                })
                .perform();
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

        public FriendsLoader(User user) {
            super(InviteFriendsActivity.this, 3600 * 24 * 1000, DataSyncAdapter.SYNC_TYPE_FRIENDS, user.getFriendsQuery());
            this.setSwipeAndRefreshLayout(mSwipeRefreshLayout);
        }

        @Override
        public void onLoadFinished(Loader loader, List data) {
            super.onLoadFinished(loader, data);
            adapter.clear();
            adapter.setData(data);
            adapter.notifyDataSetChanged();
            Log.d(TAG, "Loaded " + data.size() + " friends for the user");
        }

        @Override
        public void onLoaderReset(Loader loader) {
            super.onLoaderReset(loader);
            adapter.clear();
            adapter.notifyDataSetChanged();
        }
    }
}
