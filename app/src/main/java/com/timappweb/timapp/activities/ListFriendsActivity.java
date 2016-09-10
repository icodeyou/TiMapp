package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.FriendsAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.UserItem;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.FriendsLoader;
import com.timappweb.timapp.data.loader.SyncDataLoader;
import com.timappweb.timapp.data.models.UserFriend;
import com.timappweb.timapp.utils.Util;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class ListFriendsActivity extends BaseActivity implements SyncDataLoader.Callback<UserFriend> {

    private static final int    LOADER_ID_FRIENDS       = 0;
    private String               TAG                    = "ListFriendsActivity";

    // =============================================================================================

    private RecyclerView mRecyclerView;
    private FriendsAdapter mAdapter;
    private View noFriendsView;
    private WaveSwipeRefreshLayout mSwipeRefreshLayout;
    private FriendsLoader mFriendsLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!IntentsUtils.requireLogin(this, false)){
            finish();
            return;
        }

        setContentView(R.layout.activity_list_friends);
        this.initToolbar(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_friends);
        noFriendsView = findViewById(R.id.no_friends_layout);
        mSwipeRefreshLayout = (WaveSwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setWaveColor(ContextCompat.getColor(this,R.color.colorRefresh));
        mToolbar.setTitle(R.string.title_activity_list_friends);

        initAdapter();
        initLoader();

    }

    private void initAdapter() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new FriendsAdapter(this);
        mAdapter.initializeListeners(new FlexibleAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(int position) {
                AbstractFlexibleItem item = mAdapter.getItem(position);
                if (item instanceof UserItem){
                    IntentsUtils.profile(ListFriendsActivity.this, ((UserItem) item).getUser());
                }
                return true;
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initLoader() {
        Util.appAssert(mAdapter != null, TAG, "Adapter must be initialized before calling this method");
        Util.appAssert(mSwipeRefreshLayout != null, TAG, "SwipeAnRefreshLayout must be initialized before calling this method");
        mFriendsLoader = new FriendsLoader(this, mAdapter, mSwipeRefreshLayout)
                .setCallback(this);
        mFriendsLoader.setSwipeAndRefreshLayout(mSwipeRefreshLayout);
        getSupportLoaderManager().initLoader(LOADER_ID_FRIENDS, null, mFriendsLoader);
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
        noFriendsView.setVisibility (mAdapter.hasData()
                ? View.GONE
                : View.VISIBLE);
    }

    @Override
    public void onLoadError(Throwable error) {
        // TODO
    }
}
