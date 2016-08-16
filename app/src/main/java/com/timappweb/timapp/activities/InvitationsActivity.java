package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.EventsAdapter;
import com.timappweb.timapp.adapters.InvitationsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.MultipleEntryLoaderCallback;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.views.RefreshableRecyclerView;

import java.util.List;

public class InvitationsActivity extends BaseActivity{

    private static final long SYNC_UPDATE_DELAY = 6 * 3600 * 1000;
    private static final int LOADER_ID_FRIENDS_LIST = 0;

    private String TAG = "ListFriendsActivity";
    private List<EventsInvitation> invitations;
    private RecyclerView recyclerView;
    private InvitationsAdapter adapter;
    private View noInvitationsView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating ListFriendsActivity");
        setContentView(R.layout.activity_invitations);
        this.initToolbar(true);

        recyclerView = (RefreshableRecyclerView) findViewById(R.id.recyclerView);
        noInvitationsView = findViewById(R.id.no_invitations_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        initAdapterListFriends();

        getSupportLoaderManager().initLoader(LOADER_ID_FRIENDS_LIST, null, new InvitationLoader());
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSupportLoaderManager().getLoader(LOADER_ID_FRIENDS_LIST).forceLoad();
            }
        });
    }

    private void initAdapterListFriends() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        adapter = new InvitationsAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                onItemListClicked(position);
            }
        });
    }


    private void updateView(List<EventsInvitation> items){
        invitations = items;
        if(invitations.size()==0) {
            noInvitationsView.setVisibility(View.VISIBLE);
        } else {
            noInvitationsView.setVisibility(View.GONE);
            adapter.setData(items);
        }
    }

    private void onItemListClicked(int position) {
        EventsInvitation invitation = invitations.get(position);
        IntentsUtils.viewSpecifiedEvent(this, invitation.event);
    }

    // =============================================================================================

    class InvitationLoader extends MultipleEntryLoaderCallback{

        public InvitationLoader() {
            super(InvitationsActivity.this,
                    SYNC_UPDATE_DELAY,
                    DataSyncAdapter.SYNC_TYPE_INVITE_RECEIVED,
                    MyApplication.getCurrentUser().getInviteReceivedQuery(),
                    EventsInvitation.class);
            this.setSwipeAndRefreshLayout(mSwipeRefreshLayout);
        }

        @Override
        public void onLoadFinished(Loader loader, List data) {
            super.onLoadFinished(loader, data);
            updateView(data);
        }
    }
}
