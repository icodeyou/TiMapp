package com.timappweb.timapp.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.InvitationsAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.InvitationItem;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.DataLoader;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.utils.loaders.ModelLoader;
import com.timappweb.timapp.views.RefreshableRecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

public class InvitationsActivity extends BaseActivity{

    private static final long   MIN_DELAY_AUTO_REFRESH      = 10 * 60 * 1000;
    private static final int    MIN_DELAY_FORCE_REFRESH     = 30 * 1000;
    private static final int    LOADER_ID_FRIENDS_LIST      = 0;
    private static final int    LOCAL_LOAD_LIMIT = 3;
    private static final int    REMOTE_LOAD_LIMIT = 3;

    private String TAG = "ListFriendsActivity";
    private RecyclerView recyclerView;
    private InvitationsAdapter adapter;
    private View noInvitationsView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private InvitationLoader mInvitationLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!IntentsUtils.requireLogin(this, false)){
            finish();
            return;
        }
        setContentView(R.layout.activity_invitations);
        this.initToolbar(true);

        recyclerView = (RefreshableRecyclerView) findViewById(R.id.recyclerView);
        noInvitationsView = findViewById(R.id.no_invitations_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        initAdapterListFriends();

        mInvitationLoader = (InvitationLoader) new InvitationLoader(this)
            .setMinDelayAutoRefresh(MIN_DELAY_AUTO_REFRESH)
            .setMinDelayForceRefresh(MIN_DELAY_FORCE_REFRESH)
            .setSwipeAndRefreshLayout(mSwipeRefreshLayout)
            .setHistoryItemInterface(MyApplication.getCurrentUser())
            .setEnlessLoading(adapter)
            .setSyncOptions(new SyncAdapterOption()
                .setType(DataSyncAdapter.SYNC_TYPE_INVITE_RECEIVED)
                .setHashId(MyApplication.getCurrentUser())
                .setLimit(REMOTE_LOAD_LIMIT));


        getSupportLoaderManager().initLoader(LOADER_ID_FRIENDS_LIST, null, mInvitationLoader);
    }

    private void initAdapterListFriends() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvitationsAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.initializeListeners(new FlexibleAdapter.OnItemClickListener(){
            @Override
            public boolean onItemClick(int position) {
                AbstractFlexibleItem item = adapter.getItem(position);
                if (item instanceof InvitationItem){
                    InvitationItem invitationWrapper = (InvitationItem)adapter.getItem(position);
                    IntentsUtils.viewSpecifiedEvent(InvitationsActivity.this, invitationWrapper.getInvitation().event);
                }
                return true;
            }
        });
    }



    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(mInvitationLoader);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(mInvitationLoader);
        super.onStop();
    }

    // =============================================================================================

    class InvitationLoader extends DataLoader<EventsInvitation>{

        public InvitationLoader(Context context) {
            super(context);
            //new Delete().from(EventsInvitation.class).execute();
            this.setLocalQuery(MyApplication.getCurrentUser().getInviteReceivedQuery().limit(LOCAL_LOAD_LIMIT));
        }

        @Override
        protected Loader<List<EventsInvitation>> buildModelLoader() {
            return new ModelLoader(InvitationsActivity.this, EventsInvitation.class, this.localQuery, false);
        }

        @Override
        public void onFinish(List<EventsInvitation> data) {
            super.onFinish(data);

            if (data == null || data.size() == 0){
                adapter.onLoadMoreComplete(null);
                return;
            }

            List<AbstractFlexibleItem> items = new LinkedList<>();
            for (EventsInvitation invitation: data){
                items.add(new InvitationItem(invitation));
            }
            adapter.onLoadMoreComplete(items);
            noInvitationsView.setVisibility(data == null || data.size() == 0
                    ? View.VISIBLE
                    : View.GONE);
        }

        @Override
        public long getMaxRemoteId() {
            return SyncBaseModel.getMaxRemoteId(EventsInvitation.class, "UserTarget = " + MyApplication.getCurrentUser().getId());
        }

        @Override
        public long getMinRemoteId() {
            return SyncBaseModel.getMinRemoteId(EventsInvitation.class, "UserTarget = " + MyApplication.getCurrentUser().getId());
        }


    }
}
