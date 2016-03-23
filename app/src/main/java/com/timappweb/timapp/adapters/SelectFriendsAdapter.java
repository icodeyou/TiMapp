package com.timappweb.timapp.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.sromku.simple.fb.entities.Profile;
import com.timappweb.timapp.activities.InviteFriendsActivity;
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
        Profile friend = data.get(position);
        List<Profile> friendsSelected = inviteFriendsActivity.getFriendsSelected();
        setCheckedView(friendViewHolder, friendsSelected.contains(friend));
    }

    public void setCheckedView(FriendViewHolder holder ,boolean isChecked) {
        if(isChecked) {
            holder.selectedView.setVisibility(View.VISIBLE);
        } else {
            holder.selectedView.setVisibility(View.GONE);
        }
    }
}
