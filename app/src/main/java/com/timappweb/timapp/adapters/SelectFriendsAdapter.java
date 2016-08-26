package com.timappweb.timapp.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.activities.InviteFriendsActivity;
import com.timappweb.timapp.adapters.flexibleadataper.models.InvitationItem;
import com.timappweb.timapp.adapters.flexibleadataper.models.UserItem;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;


public class SelectFriendsAdapter extends FriendsAdapter {

    public SelectFriendsAdapter(Context context) {
        super(context);
    }

    public int countNewSelectedUser(){
        return getNewSelectedUserIds().size();
    }


    public List<Long> getNewSelectedUserIds() {
        List<Long> res = new LinkedList<>();
        for (int position: this.getSelectedPositions()){
            AbstractFlexibleItem item = this.getItem(position);
            if (item instanceof UserItem){
                UserItem userItem = (UserItem) item;
                if (userItem.isSelectable()){
                    res.add(userItem.getUser().getRemoteId());
                }
            }
        }
        return res;
    }
}
