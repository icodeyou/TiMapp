package com.timappweb.timapp.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.activities.InviteFriendsActivity;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SelectFriendsAdapter extends FriendsAdapter {

    /**
     * Retain friends that are invited
     */
    HashMap<User, InviteInfo> invites;

    public SelectFriendsAdapter(Context context) {
        super(context);
        invites = new HashMap<>();
    }


    @Override
    public void onBindViewHolder(FriendViewHolder friendViewHolder, int position) {
        super.onBindViewHolder(friendViewHolder, position);
        User friend = data.get(position);

        if(invites.containsKey(friend) && invites.get(friend).selected) {
            friendViewHolder.selectedView.setVisibility(View.VISIBLE);
        } else {
            friendViewHolder.selectedView.setVisibility(View.GONE);
        }
    }

    public InviteInfo getInviteInfo(User friend) {
        return invites.get(friend);
    }

    public void setSelected(User friend, boolean selected) {
        InviteInfo info = this.invites.get(friend);
        if (info == null){
            info = new InviteInfo();
            info.editable = true;
            invites.put(friend, info);
        }
        info.selected = selected;
    }
    public int count(boolean isSelected, boolean isEditable) {
        int i = 0;
        for (Map.Entry<User, InviteInfo> infoEntry: invites.entrySet()){
            if (infoEntry.getValue().selected == isSelected && infoEntry.getValue().editable == isEditable){
                i++;
            }
        }
        return i;
    }


    public void setEditable(User friend, boolean editable) {
        InviteInfo info = this.invites.get(friend);
        if (info == null){
            info = new InviteInfo();
            info.selected = false;
            invites.put(friend, info);
        }
        info.editable = editable;
    }

    public HashMap<User, InviteInfo> getInviteInfo() {
        return invites;
    }


    public class InviteInfo{
        public boolean editable;
        public boolean selected;
    }
}
