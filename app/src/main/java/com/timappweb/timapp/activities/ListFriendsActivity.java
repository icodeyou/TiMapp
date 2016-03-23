package com.timappweb.timapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnFriendsListener;
import com.sromku.simple.fb.utils.Attributes;
import com.sromku.simple.fb.utils.PictureAttributes;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating ListFriendsActivity");
        setContentView(R.layout.activity_list_friends);
        this.initToolbar(true);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        noFriendsView = findViewById(R.id.no_friends_view);

        initAdapterListFriends();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
        setAllFbFriends();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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

    public void setAllFbFriends() {
        final OnFriendsListener onFriendsListener = new OnFriendsListener() {
            @Override
            public void onComplete(List<Profile> friends) {
                allFbFriends = friends;
                Log.i(TAG, "Number of friends = " + friends.size());
                if(allFbFriends.size()==0) {
                    noFriendsView.setVisibility(View.VISIBLE);
                } else {
                    noFriendsView.setVisibility(View.GONE);
                    adapter.setData(allFbFriends);
                }
            }
        };
        PictureAttributes pictureAttributes = Attributes.createPictureAttributes();
        pictureAttributes.setHeight(100);
        pictureAttributes.setWidth(100);
        pictureAttributes.setType(PictureAttributes.PictureType.SQUARE);

        // Set the properties that you want to get
        Profile.Properties properties = new Profile.Properties.Builder()
                .add(Profile.Properties.ID)
                .add(Profile.Properties.FIRST_NAME)
                .add(Profile.Properties.NAME)
                .add(Profile.Properties.PICTURE, pictureAttributes)
                .build();

        mSimpleFacebook.getFriends(properties, onFriendsListener);
    }
}
