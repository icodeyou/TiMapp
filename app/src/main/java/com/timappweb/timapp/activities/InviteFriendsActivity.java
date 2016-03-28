package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Intent;
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
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnFriendsListener;
import com.sromku.simple.fb.utils.Attributes;
import com.sromku.simple.fb.utils.PictureAttributes;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SelectFriendsAdapter;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.PaginationResponse;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InviteFriendsActivity extends BaseActivity{

    private final String KEY_INSTANCE_STATE_PEOPLE = "statePeople";
    private String TAG = "InviteFriendsActivity";
    private AutoLabelUI mAutoLabel;
    private RecyclerView recyclerView;
    private SelectFriendsAdapter adapter;
    private View inviteButton;
    private TextView textInviteButton;

    private List<Profile> friendsSelected;
    private List<Profile> allFbFriends;
    private SimpleFacebook mSimpleFacebook;
    private View noFriendsView;
    private OnFriendsListener onFriendsListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating InviteFriendsActivity");
        setContentView(R.layout.activity_invite_friends);
        this.initToolbar(true);
        initOnFriendsLoadedListener();
        this.getFriends(onFriendsListener);

        inviteButton = findViewById(R.id.invite_button);
        textInviteButton = (TextView) findViewById(R.id.text_invite_button);
        noFriendsView = findViewById(R.id.no_friends_view);

        findViews();
        initSelectedFriends();
        initAdapterListFriends();
        setRvListeners();
        initInviteButton();

        this.loadFriends();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private void initOnFriendsLoadedListener() {
        onFriendsListener = new OnFriendsListener() {
            @Override
            public void onComplete(List<Profile> friends) {
                allFbFriends = friends;
                Log.i("Simple Facebook", "Number of friends = " + friends.size());
                if(allFbFriends.size()==0) {
                    noFriendsView.setVisibility(View.VISIBLE);
                } else {
                    noFriendsView.setVisibility(View.GONE);
                    adapter.setData(allFbFriends);
                }
            }
        };
    }

    private void loadFriends(){
        Call<PaginationResponse<User>> call = RestClient.service().friends();
        call.enqueue(new RestCallback<PaginationResponse<User>>(this) {
            @Override
            public void onResponse200(Response<PaginationResponse<User>> response) {
                List<User> users = response.body().items;
                Log.v(TAG, users.toString());
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
            }
        });
        apiCalls.add(call);
    }

    private void initSelectedFriends() {
        friendsSelected = new ArrayList<>();
        //TODO : Get invited users from placeActivity and add them to the list

    }

    private void findViews() {
        mAutoLabel = (AutoLabelUI) findViewById(R.id.label_view);
        mAutoLabel.setBackgroundResource(R.drawable.round_corner_background);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }

    private void initAdapterListFriends() {
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        adapter = new SelectFriendsAdapter(this);
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
                friendsSelected.remove(position);
                adapter.notifyDataSetChanged();
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
        Profile friend = allFbFriends.get(position);
        boolean success;
        if (friendsSelected.contains(friend)) {
            success = mAutoLabel.removeLabel(position);
        } else {
            success = mAutoLabel.addLabel(friend.getName(), position);
        }
        if (success) {
            updateButtonVisibility();
            friendsSelected.add(friend);
            adapter.notifyDataSetChanged();
        }
    }

    private void updateButtonVisibility() {
        if(mAutoLabel.getLabelsCounter()>0) {
            inviteButton.setVisibility(View.VISIBLE);
        } else {
            inviteButton.setVisibility(View.GONE);
        }
    }

    public List<Profile> getFriendsSelected() {
        return friendsSelected;
    }
}
