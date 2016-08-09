package com.timappweb.timapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.activeandroid.query.From;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.FriendsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.MultipleEntryLoaderCallback;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.utils.loaders.ModelLoader;

import java.util.List;

public class ListFriendsActivity extends BaseActivity{

    private static final long   SYNC_UPDATE_DELAY       = 3600 * 1000;
    private static final int    LOADER_ID_FRIENDS       = 0;
    private String               TAG                    = "ListFriendsActivity";

    // =============================================================================================

    private RecyclerView recyclerView;
    private FriendsAdapter adapter;
    private View noFriendsView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friends);
        this.initToolbar(true);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        noFriendsView = findViewById(R.id.no_friends_layout);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        initAdapterListFriends();

        getSupportLoaderManager().initLoader(LOADER_ID_FRIENDS, null, new FriendsLoader());
        mToolbar.setTitle(R.string.title_activity_list_friends);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSupportLoaderManager().getLoader(LOADER_ID_FRIENDS).forceLoad();
            }
        });
    }

    private void initAdapterListFriends() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendsAdapter(this);
        adapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                IntentsUtils.profile(ListFriendsActivity.this, adapter.getData().get(position));
            }
        });
        recyclerView.setAdapter(adapter);
    }

    //  ============================================================================================


    class FriendsLoader extends MultipleEntryLoaderCallback
    {

        public FriendsLoader() {
            super(ListFriendsActivity.this, SYNC_UPDATE_DELAY, DataSyncAdapter.SYNC_TYPE_FRIENDS, MyApplication.getCurrentUser().getFriendsQuery());
            this.setSwipeAndRefreshLayout(mSwipeRefreshLayout);
        }

        @Override
        public void onLoadFinished(Loader loader, List data) {
            super.onLoadFinished(loader, data);
            Log.i(TAG, "Loaded " + data.size() + " friends for the user");
            adapter.clear();
            adapter.setData(data);
            adapter.notifyDataSetChanged();
            noFriendsView.setVisibility(data.size() > 0 ? View.GONE : View.VISIBLE);
        }

        /*
        @Override
        public void onLoaderReset(Loader loader) {
            super.onLoaderReset(loader);
            adapter.clear();
            adapter.notifyDataSetChanged();
        }*/
    }
}
