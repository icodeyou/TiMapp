package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dpizarro.autolabel.library.AutoLabelUI;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.FriendsAdapter;
import com.timappweb.timapp.adapters.SelectFriendsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Friend;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListFriendsActivity extends BaseActivity{

    private String TAG = "ListFriendsActivity";
    private List<Friend> mPersonList;
    private RecyclerView recyclerView;
    private FriendsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating ListFriendsActivity");
        setContentView(R.layout.activity_list_friends);
        this.initToolbar(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        initRv();
    }

    private void initRv() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        mPersonList = new ArrayList<>();

        //Populate fake list
        List<String> names = Arrays.asList(getResources().getStringArray(R.array.names));
        int[] ages = getResources().getIntArray(R.array.ages);
        TypedArray photos = getResources().obtainTypedArray(R.array.photos);

        for (int i = 0; i < names.size(); i++) {
            mPersonList.add(new Friend(names.get(i), ages[i], photos.getResourceId(i, -1), false));
        }

        photos.recycle();

        adapter = new FriendsAdapter(this);
        adapter.setData(mPersonList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                itemListClicked(position);
            }
        });
    }

    private void itemListClicked(int position) {
        User user = mPersonList.get(position);
        IntentsUtils.profile(this,user);
    }
}
