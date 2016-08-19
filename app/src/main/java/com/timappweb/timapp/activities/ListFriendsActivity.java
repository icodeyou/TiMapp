package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.FriendsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.FriendsLoader;
import com.timappweb.timapp.data.models.UserFriend;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.utils.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class ListFriendsActivity extends BaseActivity {

    private static final int    LOADER_ID_FRIENDS       = 0;
    private String               TAG                    = "ListFriendsActivity";

    // =============================================================================================

    private RecyclerView mRecyclerView;
    private FriendsAdapter mAdapter;
    private View noFriendsView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
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
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        noFriendsView = findViewById(R.id.no_friends_layout);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mToolbar.setTitle(R.string.title_activity_list_friends);

        initAdapter();
        initLoader();
    }
    private void initAdapter() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new FriendsAdapter(this);
        mAdapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                IntentsUtils.profile(ListFriendsActivity.this, mAdapter.getData().get(position));
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initLoader() {
        Util.appAssert(mAdapter != null, TAG, "Adapter must be initialized before calling this method");
        Util.appAssert(mSwipeRefreshLayout != null, TAG, "SwipeAnRefreshLayout must be initialized before calling this method");
        mFriendsLoader = new FriendsLoader(this, mAdapter, mSwipeRefreshLayout){
            @Override
            public void onFinish(List<UserFriend> data) {
                noFriendsView.setVisibility (mAdapter.getItemCount() > 0
                        ? View.GONE
                        : View.VISIBLE);
            }
        };
        getSupportLoaderManager().initLoader(LOADER_ID_FRIENDS, null, mFriendsLoader);
        mSwipeRefreshLayout.setOnRefreshListener(mFriendsLoader);
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
