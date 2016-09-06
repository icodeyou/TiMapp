package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.InvitationsAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.InvitationItem;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.PaginatedDataProviderInterface;
import com.timappweb.timapp.data.loader.PaginatedDataLoader;
import com.timappweb.timapp.data.loader.SectionContainer;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.SectionHistory;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;
import com.timappweb.timapp.views.RefreshableRecyclerView;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class InvitationsActivity extends BaseActivity implements
        PaginatedDataLoader.Callback<EventsInvitation>,
        WaveSwipeRefreshLayout.OnRefreshListener,
        FlexibleAdapter.EndlessScrollListener {

    private static final long MIN_DELAY_AUTO_REFRESH = 10 * 60 * 1000;
    private static final int MIN_DELAY_FORCE_REFRESH = 30 * 1000;
    private static final int LOCAL_LOAD_LIMIT = 3;
    private static final int REMOTE_LOAD_LIMIT = 3;

    private String TAG = "ListFriendsActivity";
    private RecyclerView recyclerView;
    private InvitationsAdapter adapter;
    private View noInvitationsView;
    private WaveSwipeRefreshLayout mSwipeRefreshLayout;
    private PaginatedDataLoader mDataLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!IntentsUtils.requireLogin(this, false)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_invitations);
        this.initToolbar(true);

        recyclerView = (RefreshableRecyclerView) findViewById(R.id.recyclerView);
        noInvitationsView = findViewById(R.id.no_invitations_view);
        mSwipeRefreshLayout = (WaveSwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setWaveColor(ContextCompat.getColor(this, R.color.colorRefresh));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        initAdapterListFriends();

        PaginatedDataProviderInterface<EventsInvitation> mDataProvider = new PaginatedDataProviderInterface() {
            @Override
            public HttpCallManager<ResponseSyncWrapper<EventsInvitation>> remoteLoad(SectionContainer.PaginatedSection section) {
                RestQueryParams options = RestClient.buildPaginatedOptions(section).setLimit(REMOTE_LOAD_LIMIT);
                return RestClient.buildCall(RestClient.service().inviteReceived(options.toMap()));
            }

        };

        // TODO use a factory
        mDataLoader = new PaginatedDataLoader<EventsInvitation>()
                .setCallback(this)
                .setFormatter(new PaginatedDataLoader.SectionBoundsFormatter<EventsInvitation>() {
                    @Override
                    public long format(EventsInvitation data) {
                        return data.getRemoteId();
                    }
                })
                .setOrder(SectionContainer.PaginateDirection.ASC)
                .setMinDelayRefresh(MIN_DELAY_FORCE_REFRESH)
                .setCacheEngine(new PaginatedDataLoader.CacheEngine<EventsInvitation>(){

                    private String getHashKey(){
                        return "EventInvitationUser" + MyApplication.getCurrentUser().hashHistoryKey();
                    }

                    @Override
                    public void add(SectionContainer.PaginatedSection<EventsInvitation> section, List<EventsInvitation> data) {
                        // TODO persite
                        /*
                        try {
                            new MultipleEntriesSyncPerformer<EventsInvitation, ResponseSyncWrapper<EventsInvitation>>()
                                    .setLocalEntries(MyApplication.getCurrentUser().getInviteReceived())
                                    .setRemoteEntries(data)
                                    .setCallback(new InvitationSyncCallback())
                                    .perform();
                        } catch (Exception e) {
                            Log.e(TAG, "Cannot sync data: " + e.getMessage());
                            e.printStackTrace();
                        }*/

                        SectionHistory.add(this.getHashKey(), section);
                    }

                    @Override
                    public boolean contains(SectionContainer.PaginatedSection<EventsInvitation> section) {
                        return SectionHistory.contains(this.getHashKey(), section);
                    }

                    @Override
                    public List<EventsInvitation> get(SectionContainer.PaginatedSection<EventsInvitation> section) {
                        return SyncBaseModel.selectIdsRange(EventsInvitation.class, section.start, section.end)
                                .execute();
                    }

                })
                .useCache(false)
                .setDataProvider(mDataProvider);
    }

    private void initAdapterListFriends() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InvitationsAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.initializeListeners(new FlexibleAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(int position) {
                AbstractFlexibleItem item = adapter.getItem(position);
                if (item instanceof InvitationItem) {
                    InvitationItem invitationWrapper = (InvitationItem) adapter.getItem(position);
                    IntentsUtils.viewSpecifiedEvent(InvitationsActivity.this, invitationWrapper.getInvitation().event);
                }
                return true;
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onLoadMore() {
        if (!mDataLoader.loadMore()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        if (!mDataLoader.loadNewest()){
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onLoadEnd(SectionContainer.PaginatedSection section, List<EventsInvitation> data) {
        mSwipeRefreshLayout.setRefreshing(false);

        List<AbstractFlexibleItem> items = new LinkedList<>();
        if (data != null){
            for (EventsInvitation invitation : data) {
                items.add(new InvitationItem(invitation));
            }
        }

        switch (section.getLoadType()) {
            case MORE:
                adapter.onLoadMoreComplete(items);
                break;
            case NEWEST:
                adapter.addItems(0, items);
                break;
            case UPDATE:
                // TODO
                break;
        }
        noInvitationsView.setVisibility(adapter.getItemCount() == 0
                ? View.VISIBLE
                : View.GONE);

    }

    @Override
    public void onLoadError(Throwable error, SectionContainer.PaginatedSection section) {
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);

        if (error instanceof IOException) {
            Toast.makeText(InvitationsActivity.this, R.string.no_internet_connection_message, Toast.LENGTH_LONG).show();
        } else if (error instanceof CannotSyncException) {
            Toast.makeText(InvitationsActivity.this, ((CannotSyncException) error).getUserFeedback(), Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(InvitationsActivity.this, R.string.error_server_unavailable, Toast.LENGTH_LONG).show();
        }
        noInvitationsView.setVisibility(adapter.getItemCount() == 0
                ? View.VISIBLE
                : View.GONE);
    }

}
