package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnFriendsListener;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.FriendsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.List;

public class ListFriendsActivity extends BaseActivity{

    private String TAG = "ListFriendsActivity";
    private List<Profile> allFbFriends;
    private RecyclerView recyclerView;
    private FriendsAdapter adapter;
    private SimpleFacebook mSimpleFacebook;
    private View noFriendsView;

    private OnFriendsListener onFriendsListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating ListFriendsActivity");
        setContentView(R.layout.activity_list_friends);
        this.initToolbar(true);
        //initOnFriendsLoadedListener();
        //this.getFriends(onFriendsListener);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        noFriendsView = findViewById(R.id.no_friends_view);

        initAdapterListFriends();
    }

    private void initAdapterListFriends() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        adapter = new FriendsAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                itemListClicked(position);
            }
        });
    }

    private void itemListClicked(int position) {
        Profile friend = allFbFriends.get(position);
        //TODO : redirect on right profile
        IntentsUtils.profile(this, MyApplication.getCurrentUser());
    }
}
