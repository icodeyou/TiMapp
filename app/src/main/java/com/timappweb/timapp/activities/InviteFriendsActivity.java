package com.timappweb.timapp.activities;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;

import com.dpizarro.autolabel.library.AutoLabelUI;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.adapters.SelectFriendsAdapter;
import com.timappweb.timapp.entities.Friend;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InviteFriendsActivity extends BaseActivity{

    private final String KEY_INSTANCE_STATE_PEOPLE = "statePeople";
    private String TAG = "InviteFriendsActivity";
    private AutoLabelUI mAutoLabel;
    private List<Friend> mPersonList;
    private RecyclerView recyclerView;
    private SelectFriendsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating InviteFriendsActivity");
        setContentView(R.layout.activity_invite_friends);
        this.initToolbar(true);

        findViews();
        setListeners();
        setFriendsList();
    }

    /*@Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        *//*if (savedInstanceState != null) {
            List<Friend> users = savedInstanceState.getParcelableArrayList(KEY_INSTANCE_STATE_PEOPLE);
            if (users != null) {
                mPersonList = users;
                adapter.setPersons(users);
                recyclerView.setAdapter(adapter);
            }
        }*//*
    }*/

    private void itemListClicked(int position) {
        Friend person = mPersonList.get(position);
        boolean isSelected = person.isSelected;
        boolean success;
        if (isSelected) {
            success = mAutoLabel.removeLabel(position);
        } else {
            success = mAutoLabel.addLabel(person.getUsername(), position);
        }
        if (success) {
            adapter.setItemSelected(position, !isSelected);
        }
    }

    private void setListeners() {
        mAutoLabel.setOnLabelsCompletedListener(new AutoLabelUI.OnLabelsCompletedListener() {
            @Override
            public void onLabelsCompleted() {
                Snackbar.make(recyclerView, "Completed!", Snackbar.LENGTH_SHORT).show();
            }
        });

        mAutoLabel.setOnRemoveLabelListener(new AutoLabelUI.OnRemoveLabelListener() {
            @Override
            public void onRemoveLabel(View view, int position) {
                adapter.setItemSelected(position, false);
            }
        });

        mAutoLabel.setOnLabelsEmptyListener(new AutoLabelUI.OnLabelsEmptyListener() {
            @Override
            public void onLabelsEmpty() {
                Snackbar.make(recyclerView, "EMPTY!", Snackbar.LENGTH_SHORT).show();
            }
        });

        mAutoLabel.setOnLabelClickListener(new AutoLabelUI.OnLabelClickListener() {
            @Override
            public void onClickLabel(View v) {

            }
        });

    }

    private void findViews() {
        mAutoLabel = (AutoLabelUI) findViewById(R.id.label_view);
        mAutoLabel.setBackgroundResource(R.drawable.round_corner_background);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }

    private void setFriendsList() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        mPersonList = new ArrayList<>();

        //Populate list
        List<String> names = Arrays.asList(getResources().getStringArray(R.array.names));
        int[] ages = getResources().getIntArray(R.array.ages);
        TypedArray photos = getResources().obtainTypedArray(R.array.photos);

        for (int i = 0; i < names.size(); i++) {
            mPersonList.add(new Friend(names.get(i), ages[i], photos.getResourceId(i, -1), false));
        }

        photos.recycle();

        adapter = new SelectFriendsAdapter(mPersonList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                itemListClicked(position);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        //outState.putParcelableArrayList(KEY_INSTANCE_STATE_PEOPLE,
        //      (ArrayList<? extends Parcelable>) adapter.getPersons());

    }
}
