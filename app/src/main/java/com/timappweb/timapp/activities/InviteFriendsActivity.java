package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.entities.UserInvitationFeedback;
import com.timappweb.timapp.data.loader.FriendsLoader;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.PublishInEventCallback;
import com.timappweb.timapp.rest.managers.HttpCallManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Response;

public class InviteFriendsActivity extends BaseActivity {

    private String              TAG                         = "InviteFriendsActivity";
    private static final int    LOADER_ID_FRIENDS_LIST      = 0;

    // ---------------------------------------------------------------------------------------------
    private Menu menu;

    private RecyclerView                recyclerView;
    private SelectFriendsAdapter        adapter;
    private Event                       event;
    private FriendsLoader               mFriendsLoader;
    private WaveSwipeRefreshLayout          mSwipeRefreshLayout;
    private View                        progressview;
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

            mSwipeRefreshLayout = (WaveSwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
            mSwipeRefreshLayout.setWaveColor(ContextCompat.getColor(this,R.color.colorRefresh));
            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            progressview = findViewById(R.id.progress_view);

            initAdapterListFriends();

            mFriendsLoader = new FriendsLoader(this, adapter, mSwipeRefreshLayout);
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
        //setButtonValidation();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_invite:
                sendInvites();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                boolean enableInviteButton = adapter.count(true, true) > 0;
                menu.findItem(R.id.action_invite).setEnabled(enableInviteButton);
                adapter.notifyDataSetChanged();
            }
        });

        List<EventsInvitation> invitations = event.getSentInvitationsByUser(MyApplication.getCurrentUser());
        for (EventsInvitation invitation: invitations){
            adapter.setSelected(invitation.user_target, true);
            adapter.setEditable(invitation.user_target, false);
        }
    }


    private void sendInvites(){
        // Encoding data:
        ArrayList<Integer> ids = new ArrayList<>();
        for (Map.Entry<User, SelectFriendsAdapter.InviteInfo> friendEntry: adapter.getInviteInfo().entrySet()){
            if (friendEntry.getValue().selected && friendEntry.getValue().editable){
                ids.add(friendEntry.getKey().remote_id);
            }
        }
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

}
