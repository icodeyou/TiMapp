package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.FriendsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.utils.loaders.ModelLoader;

import java.util.List;

public class ListFriendsActivity extends BaseActivity{

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

    class FriendsLoader implements LoaderManager.LoaderCallbacks<List<User>>
    {

        @Override
        public Loader<List<User>> onCreateLoader(int id, Bundle args)
        {
            progressView.setVisibility(View.VISIBLE);
            //setProgressBarIndeterminateVisibility(true);
            return new ModelLoader<User>(ListFriendsActivity.this, User.class, true);
        }


        @Override
        public void onLoadFinished(Loader<List<User>> loader, List<User> data) {
            adapter.clear();
            adapter.setData(data);
            adapter.notifyDataSetChanged();

            //setProgressBarIndeterminateVisibility(false);
            Log.i(TAG, "Loaded " + data.size() + " friends for the user");
            //noFriendsView.setVisibility(data.size() == 0 ? View.VISIBLE : View.INVISIBLE);
            progressView.setVisibility(View.GONE);
        }


        @Override
        public void onLoaderReset(Loader<List<User>> loader)
        {
            adapter.clear();
            adapter.notifyDataSetChanged();
        }

    }
}
