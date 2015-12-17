package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedList;

public class PostsAdapter extends ArrayAdapter<Post> {
    private final Context context;
    private final ArrayList<Post> postsArrayList;

    public PostsAdapter(Context context, ArrayList<Post> postsArrayList) {
        super(context, R.layout.item_post, postsArrayList);
        this.context = context;
        this.postsArrayList = postsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Post post = postsArrayList.get(position);

        // Get the view from inflater
        View postBox = convertView;
        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            postBox = inflater.inflate(R.layout.item_post, parent, false);
        }

        // Get text views from item_post.xml
        TextView tvAddress = (TextView) postBox.findViewById(R.id.tv_address);
        TextView tvUsername = (TextView) postBox.findViewById(R.id.tv_username);
        TextView tvTime = (TextView) postBox.findViewById(R.id.tv_time);
        TextView tvComment = (TextView) postBox.findViewById(R.id.tv_comment);
        RecyclerView rv_lastPostTags = (RecyclerView) postBox.findViewById(R.id.rv_displayed_tags);


        // Get the address, name, time, and comment from Post.
        String address = String.valueOf(post.getAdress());
        String username = String.valueOf(post.getUsername());
        String time = String.valueOf(post.getPrettyTimeCreated());
        String comment = String.valueOf(post.getComment());
        ArrayList<Tag> tags = post.getTags();

        //Set the text
        tvAddress.setText(address);
        tvUsername.setText(username);
        tvTime.setText(time);
        tvComment.setText(comment);

        //Set the adapter for the Recycler View (which displays tags)
        DisplayedTagsAdapter displayedTagsAdapter = new DisplayedTagsAdapter(getContext(), new LinkedList<Tag>());
        displayedTagsAdapter.setData(tags);
        rv_lastPostTags.setAdapter(displayedTagsAdapter);

        //Set LayoutManager
        GridLayoutManager manager_savedTags = new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false);
        rv_lastPostTags.setLayoutManager(manager_savedTags);

        //return the view
        return postBox;
    }
}