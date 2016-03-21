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
import com.timappweb.timapp.adapters.SelectFriendsAdapter;
import com.timappweb.timapp.entities.Friend;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

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
    private View inviteButton;
    private TextView textInviteButton;

    private List<Friend> friendsSelected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating InviteFriendsActivity");
        setContentView(R.layout.activity_invite_friends);
        this.initToolbar(true);

        inviteButton = findViewById(R.id.invite_button);
        textInviteButton = (TextView) findViewById(R.id.text_invite_button);

        findViews();
        initSelectedFriends();
        initRv();
        setRvListeners();
        initInviteButton();
    }

    private void initSelectedFriends() {
        friendsSelected = new ArrayList<>();
        //TODO : Get invited users from placeActivity and add them to the list

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private void findViews() {
        mAutoLabel = (AutoLabelUI) findViewById(R.id.label_view);
        mAutoLabel.setBackgroundResource(R.drawable.round_corner_background);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }

    private void initRv() {
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

        adapter = new SelectFriendsAdapter(this);
        adapter.setData(mPersonList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                itemListClicked(position);
            }
        });
    }

    private void setRvListeners() {
        mAutoLabel.setOnLabelsCompletedListener(new AutoLabelUI.OnLabelsCompletedListener() {
            @Override
            public void onLabelsCompleted() {
                Toast.makeText(InviteFriendsActivity.this, R.string.cannot_add_more_friends,
                        Toast.LENGTH_SHORT).show();
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
                updateButtonVisibility();
            }
        });

        mAutoLabel.setOnLabelClickListener(new AutoLabelUI.OnLabelClickListener() {
            @Override
            public void onClickLabel(View v) {

            }
        });
    }

    private void initInviteButton() {
        final Activity thatActivity = this;
        setSquareTouchListener(inviteButton, textInviteButton);

        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(thatActivity);
            }
        });

        updateButtonVisibility();

    }

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
            updateButtonVisibility();
            adapter.setItemSelected(position, !isSelected);
        }
    }

    private void updateButtonVisibility() {
        if(mAutoLabel.getLabelsCounter()>0) {
            inviteButton.setVisibility(View.VISIBLE);
        } else {
            inviteButton.setVisibility(View.GONE);
        }
    }
}
