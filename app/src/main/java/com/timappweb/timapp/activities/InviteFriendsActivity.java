package com.timappweb.timapp.activities;

import android.app.Activity;
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
import com.google.gson.JsonArray;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnFriendsListener;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.SelectFriendsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.PaginationResponse;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class InviteFriendsActivity extends BaseActivity{

    private final String KEY_INSTANCE_STATE_PEOPLE = "statePeople";
    private String TAG = "InviteFriendsActivity";
    private AutoLabelUI mAutoLabel;
    private RecyclerView recyclerView;
    private SelectFriendsAdapter adapter;
    private View inviteButton;
    private TextView textInviteButton;

    private List<User> friendsSelected;
    private List<User> allFbFriends;
    private SimpleFacebook mSimpleFacebook;
    private View noFriendsView;
    private OnFriendsListener onFriendsListener;
    private Place place;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating InviteFriendsActivity");
        setContentView(R.layout.activity_invite_friends);

        place = IntentsUtils.extractPlace(getIntent());
        if (place == null){
            IntentsUtils.home(this);
            finish();
        }

        this.initToolbar(true);
        findViews();
        initSelectedFriends();
        initAdapterListFriends();
        initAutoLabel();
        initInviteButton();

        this.loadFriends();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private void loadFriends(){
        Call<PaginationResponse<User>> call = RestClient.service().friends();
        apiCalls.add(call);
        call.enqueue(new RestCallback<PaginationResponse<User>>(this) {
            @Override
            public void onResponse200(Response<PaginationResponse<User>> response) {
                onUserLoaded(response.body().items);

            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
            }
        });
    }

    private void findViews() {
        inviteButton = findViewById(R.id.invite_button);
        textInviteButton = (TextView) findViewById(R.id.text_invite_button);
        noFriendsView = findViewById(R.id.no_friends_view);
        mAutoLabel = (AutoLabelUI) findViewById(R.id.label_view);
        mAutoLabel.setBackgroundResource(R.drawable.round_corner_background);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }

    private void initSelectedFriends() {
        friendsSelected = new ArrayList<>();
        //TODO : Get invited users from placeActivity and add them to the list

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

    private void initAutoLabel() {
        int maxLabels = MyApplication.getApplicationRules().max_invite_per_request;
        //TODO : Replace 20 by maxLabels;
        mAutoLabel.setMaxLabels(20);
        Log.d(TAG, "Max labels : " + maxLabels);

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
        setSquareTouchListener(inviteButton, textInviteButton);

        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Number of friends invited : " + friendsSelected.size());
                sendInvites();
            }
        });

        updateButtonVisibility();

    }

    private void sendInvites(){
        // Encoding data:
        ArrayList<Integer> ids = new ArrayList<>();
        for (User user: friendsSelected){
            ids.add(user.id);
        }
        Call<JsonArray> call = RestClient.service().sendInvite(place.id, ids);
        call.enqueue(new RestCallback<JsonArray>(this) {
            @Override
            public void onResponse200(Response<JsonArray> response) {
                Toast.makeText(context, "Thanks for inviting your friend!", Toast.LENGTH_LONG).show();
                onSendInviteSuccess();
            }

        });
        this.apiCalls.add(call);
    }

    private void onSendInviteSuccess(){
        NavUtils.navigateUpFromSameTask(this);
    }

    private void itemListClicked(int position) {
        User friend = allFbFriends.get(position);
        boolean success;
        if (friendsSelected.contains(friend)) {
            success = mAutoLabel.removeLabel(position);
        } else {
            success = mAutoLabel.addLabel(friend.getUsername(), position);
        }
        if (success) {
            updateButtonVisibility();
            friendsSelected.add(friend);
            adapter.notifyDataSetChanged();
        }
    }

    private void onUserLoaded(List<User> items){

        allFbFriends = items;
        if(allFbFriends.size()==0) {
            noFriendsView.setVisibility(View.VISIBLE);
        } else {
            noFriendsView.setVisibility(View.GONE);
            adapter.setData(items);
        }
    }

    private void updateButtonVisibility() {
        if(mAutoLabel.getLabelsCounter()>0) {
            inviteButton.setVisibility(View.VISIBLE);
        } else {
            inviteButton.setVisibility(View.GONE);
        }
    }

    public List<User> getFriendsSelected() {
        return friendsSelected;
    }
}
