package com.timappweb.timapp.adapters;

import android.content.Context;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.SubUserItem;
import com.timappweb.timapp.adapters.flexibleadataper.models.UserItem;
import com.timappweb.timapp.data.models.UserFriend;

import java.util.List;


public class FriendsAdapter extends MyFlexibleAdapter {

    //Constructor
    public FriendsAdapter(Context context) {
        super(context);
    }

    public void setData(List<UserFriend> friends) {
        removeItemsOfType(R.layout.item_userplace);
        removeItemsOfType(R.layout.item_usertags);
        removeItemsOfType(R.layout.item_friend);
        addData(friends);
    }

    public void addData(List<UserFriend> data) {
        for (UserFriend friend: data){
            this.addItem(new UserItem(friend.userTarget));
        }
        notifyDataSetChanged();
    }


}
