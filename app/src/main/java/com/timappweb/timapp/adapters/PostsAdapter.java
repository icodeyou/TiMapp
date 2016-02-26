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
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.ArrayList;
import java.util.List;

public class PostsAdapter extends ArrayAdapter<Post> {
    private static final String TAG = "PostsAdapter";
    private final Context context;

    private OnItemAdapterClickListener itemAdapterClickListener;

    public PostsAdapter(Context context) {
        super(context, R.layout.item_post, 0);
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Post post = getItem(position);

        // Get the view from inflater
        View view = convertView;
        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_post, parent, false);
        }

        // Get text views from item_post.xml
        TextView tvUsername = (TextView) view.findViewById(R.id.tv_username);
        TextView tvTime = (TextView) view.findViewById(R.id.tv_time);
        RecyclerView rvPostTags = (RecyclerView) view.findViewById(R.id.rv_horizontal_tags);
        ImageView ivProfilePicture = (ImageView) view.findViewById(R.id.profile_picture);

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
        HorizontalTagsAdapter htAdapter = (HorizontalTagsAdapter) rvPostTags.getAdapter();
        htAdapter.setData(tags);
        rvPostTags.setAdapter(htAdapter);

        //Listener entire view
        if (this.itemAdapterClickListener != null){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemAdapterClickListener.onClick(position);
                }
            });
        }

        //Listener Horizontal Scroll View
        HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                new HorizontalTagsTouchListener(getContext(), itemAdapterClickListener, position);
        rvPostTags.setOnTouchListener(mHorizontalTagsTouchListener);

        //Set LayoutManager
        GridLayoutManager manager_savedTags = new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false);
        rvPostTags.setLayoutManager(manager_savedTags);

        //return the view
        return view;
    }

    public void setItemAdapterClickListener(OnItemAdapterClickListener itemAdapterClickListener) {
        this.itemAdapterClickListener = itemAdapterClickListener;
    }

    public String getUsername(int position) {
        Post post = getItem(position);
        User user = post.getUser();
        return user.getUsername();
    }
}