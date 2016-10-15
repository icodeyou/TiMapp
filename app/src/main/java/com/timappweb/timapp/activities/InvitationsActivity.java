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
import com.timappweb.timapp.data.DBCacheEngine;
import com.timappweb.timapp.data.loader.RecyclerViewManager;
import com.timappweb.timapp.data.loader.sections.SectionContainer;
import com.timappweb.timapp.data.loader.sections.SectionDataLoader;
import com.timappweb.timapp.data.loader.sections.SectionDataProviderInterface;
import com.timappweb.timapp.data.loader.sections.SectionRecyclerViewManager;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.sync.callbacks.InvitationSyncCallback;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.RefreshableRecyclerView;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

public class InvitationsActivity extends BaseActivity implements
        SectionDataLoader.Callback<EventsInvitation>{

    private String              TAG                             = "ListFriendsActivity";
    private static final int    MIN_DELAY_FORCE_REFRESH         = 30 * 1000;
    private static final int    MIN_DELAY_AUTO_REFRESH         = 2 * 3600 * 1000;
    public static int    LOCAL_LOAD_LIMIT                = 5;
    private static final int    REMOTE_LOAD_LIMIT               = LOCAL_LOAD_LIMIT;

    // ---------------------------------------------------------------------------------------------

    private RecyclerView recyclerView;
    private InvitationsAdapter adapter;
    private View noInvitationsView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SectionDataLoader mDataLoader;
    private SectionRecyclerViewManager mRecyclerViewManager;

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

    @Override
    protected void onPause() {
        //LocationManager.removeLocationListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //LocationManager.addOnLocationChangedListener(this);
    }


    private void initDataLoader() {
        SectionDataProviderInterface<EventsInvitation> mDataProvider = new SectionDataProviderInterface() {
            @Override
            public HttpCallManager<ResponseSyncWrapper<EventsInvitation>> remoteLoad(SectionContainer.PaginatedSection section) {
                RestQueryParams options = RestClient.buildPaginatedOptions(section).setLimit(REMOTE_LOAD_LIMIT);
                return RestClient.buildCall(RestClient.service().inviteReceived(options.toMap()));
            }

        };
        mDataLoader = new SectionDataLoader<EventsInvitation>()
                .setFormatter(SyncBaseModel.getPaginatedFormater())
                .setOrder(SectionContainer.PaginateDirection.ASC)
                .setMinDelayAutoRefresh(MIN_DELAY_AUTO_REFRESH)
                .setMinDelayForceRefresh(MIN_DELAY_FORCE_REFRESH)
                .setCacheEngine(new DBCacheEngine<EventsInvitation>(EventsInvitation.class){
                    @Override
                    protected String getHashKey() {
                        return "EventInvitation" + MyApplication.getCurrentUser().getRemoteId();
                    }

                    @Override
                    protected void persist(List<EventsInvitation> data) throws Exception {
                        new MultipleEntriesSyncPerformer<EventsInvitation, ResponseSyncWrapper<EventsInvitation>>()
                                .setLocalEntries(MyApplication.getCurrentUser().getInviteReceived())
                                .setRemoteEntries(data)
                                .setCallback(new InvitationSyncCallback())
                                .perform();
                    }
                })
                .useCache(false)
                .setDataProvider(mDataProvider);

        this.mRecyclerViewManager = new SectionRecyclerViewManager(this, adapter, mDataLoader)
                .setItemTransformer(new RecyclerViewManager.ItemTransformer<EventsInvitation>() {
                    @Override
                    public AbstractFlexibleItem createItem(EventsInvitation data) {
                        return new InvitationItem(InvitationsActivity.this, data);
                    }
                })
                .setNoDataView(noInvitationsView)
                .setCallback(this)
                .setSwipeRefreshLayout(mSwipeRefreshLayout)
                .enableEndlessScroll()
                .firstLoad();
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

    @Override
    public void onLoadEnd(SectionContainer.PaginatedSection section, List<EventsInvitation> data) {

    }

    @Override
    public void onLoadError(Throwable error, SectionContainer.PaginatedSection section) {

    }
}
