package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import com.timappweb.timapp.data.loader.DynamicListLoader;
import com.timappweb.timapp.data.loader.PaginatedDataProviderInterface;
import com.timappweb.timapp.data.loader.PaginatedDataLoader;
import com.timappweb.timapp.data.loader.SectionContainer;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.sync.callbacks.InvitationSyncCallback;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;
import com.timappweb.timapp.views.RefreshableRecyclerView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class InvitationsActivity extends BaseActivity implements
        PaginatedDataLoader.Callback<EventsInvitation>{

    private String              TAG                             = "ListFriendsActivity";
    private static final int    MIN_DELAY_FORCE_REFRESH         = 30 * 1000;
    private static final int    LOCAL_LOAD_LIMIT                = 3;
    private static final int    REMOTE_LOAD_LIMIT               = 3;

    // ---------------------------------------------------------------------------------------------

    private RecyclerView recyclerView;
    private InvitationsAdapter adapter;
    private View noInvitationsView;
    private WaveSwipeRefreshLayout mSwipeRefreshLayout;
    private PaginatedDataLoader mDataLoader;

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
        mSwipeRefreshLayout = (WaveSwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setWaveColor(ContextCompat.getColor(this, R.color.colorRefresh));

        initAdapterListFriends();
        initDataLoader();

        new DynamicListLoader(this, adapter, mDataLoader)
                .setItemTransformer(new DynamicListLoader.ItemTransformer<EventsInvitation>() {
                    @Override
                    public AbstractFlexibleItem createItem(EventsInvitation data) {
                        return new InvitationItem(data);
                    }
                })
                .setNoDataView(noInvitationsView)
                .setCallback(this)
                .setSwipeRefreshLayout(mSwipeRefreshLayout)
                .setEndlessScrollListener()
                .firstLoad();
    }

    private void initDataLoader() {
        PaginatedDataProviderInterface<EventsInvitation> mDataProvider = new PaginatedDataProviderInterface() {
            @Override
            public HttpCallManager<ResponseSyncWrapper<EventsInvitation>> remoteLoad(SectionContainer.PaginatedSection section) {
                RestQueryParams options = RestClient.buildPaginatedOptions(section).setLimit(REMOTE_LOAD_LIMIT);
                return RestClient.buildCall(RestClient.service().inviteReceived(options.toMap()));
            }

        };
        mDataLoader = new PaginatedDataLoader<EventsInvitation>()
                .setFormatter(SyncBaseModel.getPaginatedFormater())
                .setOrder(SectionContainer.PaginateDirection.ASC)
                .setMinDelayRefresh(MIN_DELAY_FORCE_REFRESH)
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

        mDataLoader.firstLoad();
    }

    private void initAdapterListFriends() {
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
