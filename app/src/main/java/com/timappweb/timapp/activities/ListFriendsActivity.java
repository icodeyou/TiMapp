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

    private static final long SYNC_UPDATE_DELAY = 30 * 1000;
    private String TAG = "ListFriendsActivity";

    private RecyclerView recyclerView;
    private FriendsAdapter adapter;

    private View noFriendsView;
    private View progressView;
    private FriendsLoader mLoader;
    private ListFriendsActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_list_friends);
        this.initToolbar(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        noFriendsView = findViewById(R.id.no_friends_view);
        progressView = findViewById(R.id.loading_friends);

        initAdapterListFriends();

        mLoader = new FriendsLoader();
        getSupportLoaderManager().initLoader(0, null, mLoader);


    }

    private void refreshItems() {
        Log.v(TAG, "Request refresh friend sync");
        SyncBaseModel.getRemoteEntries(context, DataSyncAdapter.SYNC_TYPE_FRIENDS);
    }

    private void initAdapterListFriends() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendsAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                IntentsUtils.profile(context, adapter.getData().get(position));
            }
        });
    }

    //  ============================================================================================


    class FriendsLoader extends MultipleEntryLoaderCallback
    {

        public FriendsLoader() {
            super(ListFriendsActivity.this, DataSyncAdapter.SYNC_TYPE_FRIENDS, 3600 * 24 * 1000, MyApplication.getCurrentUser().getFriendsQuery());
            this.setSwipeAndRefreshLayout();
        }

        @Override
        public void onLoadFinished(Loader loader, List data) {
            super.onLoadFinished(loader, data);
            adapter.clear();
            adapter.setData(data);
            adapter.notifyDataSetChanged();
            //setProgressBarIndeterminateVisibility(false);
            Log.i(TAG, "Loaded " + data.size() + " friends for the user");
            //noFriendsView.setVisibility(data.size() == 0 ? View.VISIBLE : View.INVISIBLE);
            progressView.setVisibility(View.GONE);
        }

        @Override
        public void onLoaderReset(Loader loader) {
            super.onLoaderReset(loader);
            adapter.clear();
            adapter.notifyDataSetChanged();
        }
    }
}
