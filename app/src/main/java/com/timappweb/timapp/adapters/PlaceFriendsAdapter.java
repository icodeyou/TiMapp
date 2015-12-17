package com.timappweb.timapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.User;

import java.util.ArrayList;

public class PlaceFriendsAdapter extends ArrayAdapter<User> {
    private final Context context;
    private final ArrayList<User> usersArrayList;

    public PlaceFriendsAdapter(Context context, ArrayList<User> usersArrayList) {
        super(context, R.layout.item_friend, usersArrayList);
        this.context = context;
        this.usersArrayList = usersArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = usersArrayList.get(position);

        // Get the view from inflater
        View rowUser = convertView;
        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowUser = inflater.inflate(R.layout.item_friend, parent, false);
        }

        // Get text view from item_post.xml
        TextView tvUsername = (TextView) rowUser.findViewById(R.id.tv_username);
        if (user.status) {
            tvUsername.setBackgroundColor(Color.parseColor("#123456"));
        }
        // Get the username.
        String username = String.valueOf(user.getUsername());

        //Set the text
        tvUsername.setText(username);

        //return the view
        return rowUser;
    }

    public int addData(User user) {
        usersArrayList.add(user);
        int position = usersArrayList.size();
        user.setHere(true);
        super.notifyDataSetChanged();
        return position;
    }


}