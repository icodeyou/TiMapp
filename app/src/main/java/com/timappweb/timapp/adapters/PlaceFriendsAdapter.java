package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.User;

import java.util.ArrayList;
import java.util.Objects;

public class PlaceFriendsAdapter extends ArrayAdapter<User> {
    private final Context context;
    private int idLayout;
    private int resource = R.layout.item_friend;

    public PlaceFriendsAdapter(Context context) {
        super(context, 0);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = this.getItem(position);

        // Get the view from inflater
        View rowUser = convertView;
        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowUser = inflater.inflate(resource, parent, false);
        }

        // Get text view from item_post.xml
        TextView tvUsername = (TextView) rowUser.findViewById(R.id.tv_username);
        TextView tvStatus = (TextView) rowUser.findViewById(R.id.tv_status);
        if (user.getStatus()) {
            tvUsername.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.item_friend_invited));
            tvStatus.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.item_friend_invited));
            tvStatus.setText("invited");
        }
        else{
            tvUsername.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.item_friend_here));
            tvStatus.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.item_friend_here));
            tvStatus.setText("here");
        }
        // Get the username.
        String username = String.valueOf(user.getUsername());

        //Set the text
        tvUsername.setText(username);

        //return the view
        return rowUser;
    }

    @Override
    public void add(User user) {
        super.add(user);
        super.notifyDataSetChanged();
    }

    //Generate Data for ListView
    public void initializeDummyData(){
        User dummyUser = User.createDummy();
        dummyUser.setStatus(false);
        this.add(dummyUser);

        User dummyUser2 = User.createDummy();
        dummyUser2.setStatus(false);
        this.add(dummyUser2);
    }
}