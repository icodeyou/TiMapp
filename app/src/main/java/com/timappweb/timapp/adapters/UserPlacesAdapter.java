package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.PlaceUserInterface;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserPlacesAdapter extends RecyclerView.Adapter<UserPlacesAdapter.PlacePostsViewHolder> {
    private static final String TAG = "UserPlacesAdapter";

    List<PlaceUserInterface> data = new ArrayList<>();
    OnItemAdapterClickListener mItemClickListener;
    Context context;
    private HorizontalTagsAdapter horizontalTagsAdapter;

    //Constructor
    public UserPlacesAdapter(Context context) {
        this.context = context;
    }

    public UserPlacesAdapter(List<PlaceUserInterface> posts) {
        this.data = posts;
    }

    @Override
    public PlacePostsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_post, viewGroup, false);
        context = viewGroup.getContext();

        PlacePostsViewHolder placePostsViewHolder = new PlacePostsViewHolder(v);
        return placePostsViewHolder;
    }

    @Override
    public void onBindViewHolder(PlacePostsViewHolder holder, final int position) {
        PlaceUserInterface placeUserInterface = data.get(position);
        RecyclerView rvPostTags = holder.rvPostTags;

        Log.d(TAG, "User: " + placeUserInterface.getUsername());
        Picasso.with(context).load(placeUserInterface.getProfilePictureUrl()).into(holder.ivProfilePicture);

        // Get the address, name, time, and comment from Post.
        String username = placeUserInterface.getUsername();
        String time = placeUserInterface.getPostTime();
        List<Tag> tags = placeUserInterface.getTags();

        //Set the text
        holder.tvUsername.setText(username);
        holder.tvTime.setText(time);

        //Set the adapter for the Recycler View (which displays tags)
        HorizontalTagsAdapter htAdapter = (HorizontalTagsAdapter) rvPostTags.getAdapter();
        htAdapter.setData(tags);
        rvPostTags.setAdapter(htAdapter);

        //Listener Horizontal Scroll View
        HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                new HorizontalTagsTouchListener(context, mItemClickListener, position);
        rvPostTags.setOnTouchListener(mHorizontalTagsTouchListener);

        //Set LayoutManager
        GridLayoutManager manager_savedTags = new GridLayoutManager(context, 1, LinearLayoutManager.HORIZONTAL, false);
        rvPostTags.setLayoutManager(manager_savedTags);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setOnItemClickListener(final OnItemAdapterClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setData(List<PlaceUserInterface> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public List<PlaceUserInterface> getPosts() {
        return this.data;
    }

    public PlaceUserInterface getPost(int position) {
        return this.data.get(position);
    }

    public class PlacePostsViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        CardView cv;
        TextView tvUsername;
        TextView tvTime;
        RecyclerView rvPostTags;
        CircularImageView ivProfilePicture;

        PlacePostsViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            // Get text views from item_post.xml
            cv = (CardView) itemView.findViewById(R.id.cv);
            tvUsername = (TextView) itemView.findViewById(R.id.tv_username);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            rvPostTags = (RecyclerView) itemView.findViewById(R.id.rv_horizontal_tags);
            ivProfilePicture = (CircularImageView) itemView.findViewById(R.id.profile_picture);

            cv.setPadding(0,5,0,5);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onClick(getAdapterPosition());
            }
        }
    }
}