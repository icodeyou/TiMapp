package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.InvitationsAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.InvitationItem;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.RecyclerViewManager;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateDataLoader;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateManager;
import com.timappweb.timapp.data.loader.paginate.PaginateFilterFactory;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.EventsInvitation_Table;
import com.timappweb.timapp.data.tables.EventInvitationsTable;
import com.timappweb.timapp.utils.DurationConstants;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.RefreshableRecyclerView;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

public class InvitationsActivity extends BaseActivity{

    private static final long CACHE_INVITE_VALIDITY = DurationConstants.DAY_MILLIS * 15;
    private String              TAG                             = "ListFriendsActivity";
    private static final int    MIN_DELAY_FORCE_REFRESH         = 30 * 1000;
    private static final int    MIN_DELAY_AUTO_REFRESH         = 2 * 3600 * 1000;
    public static int           LOCAL_LOAD_LIMIT                = 5;
    private static final int    REMOTE_LOAD_LIMIT               = LOCAL_LOAD_LIMIT;

    // ---------------------------------------------------------------------------------------------

    private RecyclerView recyclerView;
    private InvitationsAdapter adapter;
    private View noInvitationsView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CursorPaginateDataLoader<EventsInvitation, EventsInvitation> mDataLoader;
    private CursorPaginateManager<EventsInvitation> mRecyclerViewManager;

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!IntentsUtils.requireLogin(this, false)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_invitations);
        this.initToolbar(true);

        recyclerView = (RefreshableRecyclerView) findViewById(R.id.rv_invitations);
        noInvitationsView = findViewById(R.id.no_invitations_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        initInvitationAdapter();
        initDataLoader();

    }


    @Override
    protected void onStart() {
        super.onStart();
        LocationManager.start(this);
    }

    @Override
    protected void onStop() {
        LocationManager.stop(this);
        super.onStop();
    }



    private void initDataLoader() {
        mDataLoader = CursorPaginateDataLoader.<EventsInvitation, EventsInvitation>create(
                    "PlacesInvitations/received",
                    EventsInvitation.class
                )
                .initCache("UserInvitation" + MyApplication.getCurrentUser().id, CACHE_INVITE_VALIDITY)
                .setCacheCallback(new CursorPaginateDataLoader.CacheCallback<EventsInvitation, EventsInvitation>() {
                    @Override
                    public EventsInvitation beforeSaveModel(EventsInvitation model) {
                        model.user_target = MyApplication.getCurrentUser();
                        return model;
                    }
                })
                .setLocalQuery(EventInvitationsTable.inviteReceived(MyApplication.getCurrentUser()))
                .addFilter(PaginateFilterFactory.createCreatedFilter(EventsInvitation_Table.created))
                .addFilter(PaginateFilterFactory.createSyncIdFilter(EventsInvitation_Table.id))
                .setLimit(LOCAL_LOAD_LIMIT);

        this.mRecyclerViewManager = new CursorPaginateManager<EventsInvitation>(this, adapter, mDataLoader)
                .setItemTransformer(new RecyclerViewManager.ItemTransformer<EventsInvitation>() {
                    @Override
                    public AbstractFlexibleItem createItem(EventsInvitation data) {
                        return new InvitationItem(InvitationsActivity.this, data);
                    }
                })
                .setMinDelayForceRefresh(MIN_DELAY_FORCE_REFRESH)
                .setNoDataView(noInvitationsView)
                .setSwipeRefreshLayout(mSwipeRefreshLayout)
                .enableEndlessScroll()
                .load();
    }


    private void initInvitationAdapter() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvitationsAdapter(this);
        adapter.initializeListeners(new FlexibleAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(int position) {
                Log.d(TAG, "Click on item at position: "+ position);
                AbstractFlexibleItem item = adapter.getItem(position);
                if (item instanceof InvitationItem) {
                    InvitationItem invitationWrapper = (InvitationItem) adapter.getItem(position);
                    IntentsUtils.viewSpecifiedEvent(InvitationsActivity.this, invitationWrapper.getInvitation().event);
                    return true;
                }
                return false;
            }
        });
        recyclerView.setAdapter(adapter);
    }

}
