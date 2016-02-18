package com.timappweb.timapp.adapters;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.entities.User;

import java.util.ArrayList;
import java.util.List;

public class PostsAdapter extends ArrayAdapter<Post> {
    private static final String TAG = "PostsAdapter";
    private final Context context;

    public PostsAdapter(Context context) {
        super(context, R.layout.item_post, 0);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Post post = getItem(position);

        // Get the view from inflater
        View postBox = convertView;
        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            postBox = inflater.inflate(R.layout.item_post, parent, false);
        }

        // Get text views from item_post.xml
        TextView tvUsername = (TextView) postBox.findViewById(R.id.tv_username);
        TextView tvTime = (TextView) postBox.findViewById(R.id.tv_time);
        RecyclerView rv_lastPostTags = (RecyclerView) postBox.findViewById(R.id.rv_horizontal_tags);
        ImageView ivProfilePicture = (ImageView) postBox.findViewById(R.id.profile_picture);

        User user = post.getUser();
        if (user != null && !post.anonymous){
            Log.d(TAG, "User: " + user.toString());
            Picasso.with(context).load(user.getProfilePictureUrl()).into(ivProfilePicture);
        }
        else{
            Log.d(TAG, "User is null ? " + user + ". Anonymous ? " + post.anonymous);
        }

        // Get the address, name, time, and comment from Post.
        String username = post.getUsername();
        String time = post.getPrettyTimeCreated();
        List<Tag> tags = post.getTags();

        //Set the text
        tvUsername.setText(username);
        tvTime.setText(time);

        //Set the adapter for the Recycler View (which displays tags)
        HorizontalTagsAdapter htAdapter = (HorizontalTagsAdapter) rv_lastPostTags.getAdapter();
        htAdapter.setData(tags);
        rv_lastPostTags.setAdapter(htAdapter);

        //Set LayoutManager
        GridLayoutManager manager_savedTags = new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false);
        rv_lastPostTags.setLayoutManager(manager_savedTags);

        //return the view
        return postBox;
    }
}