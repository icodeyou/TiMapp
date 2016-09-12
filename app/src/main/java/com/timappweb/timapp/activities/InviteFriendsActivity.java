package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
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
import com.timappweb.timapp.data.loader.FriendsLoader;
import com.timappweb.timapp.data.loader.SyncDataLoader;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserFriend;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.PublishInEventCallback;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

import retrofit2.Call;
import retrofit2.Response;

public class InviteFriendsActivity extends BaseActivity
        implements FlexibleAdapter.OnItemClickListener, SyncDataLoader.Callback<UserFriend> {

    private String              TAG                         = "InviteFriendsActivity";
    private static final int    LOADER_ID_FRIENDS_LIST      = 0;

    // ---------------------------------------------------------------------------------------------
    private Menu menu;

    private RecyclerView                recyclerView;
    private SelectFriendsAdapter        mAdapter;
    private Event                       event;
    private FriendsLoader               mFriendsLoader;
    private SwipeRefreshLayout          mSwipeRefreshLayout;
    private View                        progressview;
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
            IntentsUtils.home(this);
            finish();
        }

        setContentView(R.layout.activity_invite_friends);

        try {
            event = (Event) event.requireLocalId();
            this.initToolbar(true);
            mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
            recyclerView = (RecyclerView) findViewById(R.id.rv_friends);
            progressview = findViewById(R.id.progress_view);
            initAdapterListFriends();
            mFriendsLoader = new FriendsLoader(this, mAdapter, mSwipeRefreshLayout)
                .setCallback(this);

            getSupportLoaderManager().initLoader(LOADER_ID_FRIENDS_LIST, null, mFriendsLoader);
        } catch (CannotSaveModelException e) {
            IntentsUtils.home(this);
            finish();
        }
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
            case android.R.id.home:
                finish();
                //TODO : Workaround. navigateUpFromSameTask redirects to map
                //NavUtils.navigateUpFromSameTask(this);
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

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(mFriendsLoader);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(mFriendsLoader);
        super.onStop();
    }

    @Override
    public void onLoadEnd(List<UserFriend> data) {
        mAdapter.setData(data);
        if (data != null && data.size() > 0){
            initializeSelection();
        }
    }

    @Override
    public void onLoadError(Throwable error) {
        // TODO
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
