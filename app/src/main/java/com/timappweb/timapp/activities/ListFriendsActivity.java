package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.FriendsAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.UserItem;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.FriendsLoaderFactory;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

public class ListFriendsActivity extends BaseActivity{

    private String               TAG                    = "ListFriendsActivity";

    // =============================================================================================

    private RecyclerView mRecyclerView;
    private FriendsAdapter mAdapter;
    private View noFriendsView;
    private View progressView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View shareButton;

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
        noFriendsView = findViewById(R.id.no_data_view_layout);
        shareButton = findViewById(R.id.share_button);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mToolbar.setTitle(R.string.title_activity_list_friends);
        //progressView = findViewById(R.id.progress_view);

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
        //progressView.setVisibility(View.VISIBLE);
        Util.appAssert(mAdapter != null, TAG, "Adapter must be initialized before calling this method");
        Util.appAssert(mSwipeRefreshLayout != null, TAG, "SwipeAnRefreshLayout must be initialized before calling this method");

        FriendsLoaderFactory.manager(this, mAdapter, mSwipeRefreshLayout)
                //.setCallback(this)
                .setClearOnRefresh(true)
                .setNoDataView(noFriendsView)
                .load();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_friends, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_get_friends:
                IntentsUtils.actionShareApp(this);
                return true;
        }
        return false;
    }

}
