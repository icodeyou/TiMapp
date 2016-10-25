package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SelectFriendsAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.UserItem;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.entities.UserInvitationFeedback;
import com.timappweb.timapp.data.loader.FriendsLoaderFactory;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateDataLoader;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserFriend;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.NetworkErrorCallback;
import com.timappweb.timapp.rest.callbacks.PublishInEventCallback;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import retrofit2.Call;
import retrofit2.Response;

public class InviteFriendsActivity extends BaseActivity
        implements FlexibleAdapter.OnItemClickListener, CursorPaginateDataLoader.Callback<UserFriend> {

    private String              TAG                         = "InviteFriendsActivity";

    // ---------------------------------------------------------------------------------------------
    private Menu menu;

    private RecyclerView                recyclerView;
    private SelectFriendsAdapter        mAdapter;
    private Event                       event;
    private SwipeRefreshLayout          mSwipeRefreshLayout;
    private View                        progressview;
    private View                        shareButton;
    private View                        noFriendsView;
    private List<EventsInvitation>      _cachedInvitations;
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!IntentsUtils.requireLogin(this, false)) {
            return;
        }
        event = IntentsUtils.extractEvent(getIntent());
        if (event == null){
            IntentsUtils.getBackToParent(this);
            return;
        }

        setContentView(R.layout.activity_invite_friends);
        shareButton = findViewById(R.id.share_button);
        noFriendsView = findViewById(R.id.no_data_view_layout);

        try {
            event = (Event) event.requireLocalId();
            this.initToolbar(true);
            mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
            recyclerView = (RecyclerView) findViewById(R.id.rv_friends);
            progressview = findViewById(R.id.progress_view);
            initAdapterListFriends();

            FriendsLoaderFactory.manager(this, mAdapter)
                    .setSwipeRefreshLayout(mSwipeRefreshLayout)
                    .setCallback(this)
                    .setNoDataView(noFriendsView)
                    .load();

        } catch (CannotSaveModelException e) {
            IntentsUtils.getBackToParent(this);
            return;
        }

        initListener();
    }

    private void initListener() {
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.actionShareApp(InviteFriendsActivity.this);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invite, menu);
        this.menu = menu;
        menu.findItem(R.id.action_invite).setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_all:
                mAdapter.selectAll();
                return true;
            case R.id.action_delete:
                mAdapter.clearSelection();
                initializeSelection();
                return true;
            case R.id.action_invite:
                sendInvites();
                return true;
            case R.id.action_get_friends:
                IntentsUtils.actionShareApp(this);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

        /*
    private void initializeActionModeHelper() {
        mActionModeHelper = new ActionModeHelper(mAdapter, R.menu.menu_invite, this) {
            //Override to customize the title
            @Override
            public void updateContextTitle(int count) {
                //You can use the internal mActionMode instance
                if (mActionMode != null) {
                    mActionMode.setTitle(getResources().getQuantityString(R.plurals.action_friend_selected, count));
                }
            }
        }.withDefaultMode(SelectableAdapter.MODE_MULTI);
        this.startSupportActionMode(mActionModeHelper);
    }
    */


    private void initializeSelection(){
        // Preselect every user already invited.
        if (_cachedInvitations == null){
            _cachedInvitations = event.getSentInvitationsByUser(MyApplication.getCurrentUser());
        }
        for (EventsInvitation invite: _cachedInvitations){
            int position = mAdapter.getGlobalPositionOf(new UserItem(invite.getUser()));
            AbstractFlexibleItem item = mAdapter.getItem(position);
            if (item instanceof UserItem){
                UserItem userItem = (UserItem) item;
                userItem.setSelectable(true);
                mAdapter.toggleSelection(position);
                userItem.setSelectable(false);
            }
        }
    }

    private void initAdapterListFriends() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SelectFriendsAdapter(this);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setMode(SelectableAdapter.MODE_MULTI);
    }

    @Override
    public boolean onItemClick(int position) {
        if (position != RecyclerView.NO_POSITION) {
            // If user is already invited
            if (!mAdapter.getItem(position).isSelectable()){
                Toast.makeText(InviteFriendsActivity.this, R.string.friend_already_invited, Toast.LENGTH_LONG).show();
                return true;
            }
            // New invite
            else{
                mAdapter.toggleSelection(position);
                menu.findItem(R.id.action_invite).setEnabled(mAdapter.countNewSelectedUser() >= 1);
                //mAdapter.notifyDataSetChanged(); // Useless ?
                return true;
            }
        } else {
            return false;
        }
    }


    private void sendInvites(){
        // Get user ids for each new invites
        List<Long> ids = mAdapter.getNewSelectedUserIds();
        if (ids.size() == 0){
            return;
        }
        progressview.setVisibility(View.VISIBLE);
        Call<List<UserInvitationFeedback>> call = RestClient.service().sendInvite(event.remote_id, ids);
        RestClient.buildCall(call)
                .onResponse(new PublishInEventCallback(event, MyApplication.getCurrentUser(), QuotaType.INVITE_FRIEND))
                .onResponse(new HttpCallback<List<UserInvitationFeedback>>() {
                    @Override
                    public void successful(List<UserInvitationFeedback> feedbackList) {
                        Toast.makeText(getApplicationContext(), R.string.toast_thanks_for_sharing, Toast.LENGTH_LONG).show();
                        for (UserInvitationFeedback feedback: feedbackList){
                            Log.v(TAG, feedback.toString());
                            if (feedback.success){
                                if (feedback.invitation != null){
                                    feedback.invitation.event = event;
                                    feedback.invitation.user_source = MyApplication.getCurrentUser();
                                    feedback.invitation.user_target = User.queryByRemoteId(User.class, feedback.user_id).executeSingle();
                                    feedback.invitation.mySaveSafeCall();
                                }
                            }
                            else{
                                Log.e(TAG, "Cannot invite user with id: " + feedback.user_id);
                            }
                        }
                        finishActivityResult();
                    }


                    @Override
                    public void notSuccessful() {
                        progressview.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), R.string.action_performed_not_successful, Toast.LENGTH_LONG).show();
                    }

                })
                .onError(new NetworkErrorCallback(this))
                .onFinally(new HttpCallManager.FinallyCallback() {
                    @Override
                    public void onFinally(Response response, Throwable error) {
                        progressview.setVisibility(View.GONE);
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

    // ---------------------------------------------------------------------------------------------

    @Override
    public void onLoadEnd(List<UserFriend> data, CursorPaginateDataLoader.LoadType type, boolean overwrite) {
        initializeSelection();
    }

    @Override
    public void onLoadError(Throwable error, CursorPaginateDataLoader.LoadType type) {

    }

    @Override
    public void onLoadStart(CursorPaginateDataLoader.LoadType type) {

    }

    // ---------------------------------------------------------------------------------------------
/*
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        this.menu = menu;
        menu.findItem(R.id.action_invite).setEnabled(false);
        mAdapter.setMode(SelectableAdapter.MODE_MULTI);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_all:
                mAdapter.selectAll();
                return true;
            case R.id.action_delete:
                mAdapter.clearSelection();
                return true;
            case R.id.action_invite:
                sendInvites();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mAdapter.setMode(SelectableAdapter.MODE_IDLE);
        mActionModeHelper = null;
    }

*/
}
