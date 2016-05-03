package com.timappweb.timapp.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.activities.InviteFriendsActivity;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.List;


public class SelectFriendsAdapter extends FriendsAdapter {

    OnItemAdapterClickListener mItemClickListener;
    Context context;
    InviteFriendsActivity inviteFriendsActivity;

    //Constructor
    public SelectFriendsAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        inviteFriendsActivity= (InviteFriendsActivity) context;
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder friendViewHolder, int position) {
        super.onBindViewHolder(friendViewHolder, position);
        User friend = data.get(position);
        List<User> friendsSelected = inviteFriendsActivity.getFriendsSelected();
        if(friendsSelected.contains(friend)) {
            friendViewHolder.selectedView.setVisibility(View.VISIBLE);
        } else {
            friendViewHolder.selectedView.setVisibility(View.GONE);
        }
    }
}
