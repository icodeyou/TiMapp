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

import com.squareup.picasso.Picasso;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.PlaceUserInterface;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.ArrayList;
import java.util.List;

public class PlaceUsersAdapter extends MultipleSectionAdapter<PlaceUserInterface, PlaceUsersAdapter.PlacePeopleViewHolder> {
    private static final String TAG = "PlaceUsersAdapter";

    OnItemAdapterClickListener mItemClickListener;
    Context context;

    //Constructor
    public PlaceUsersAdapter(Context context) {
        super();
        this.context = context;
    }

    @Override
    public PlacePeopleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_userplace, viewGroup, false);

        PlacePeopleViewHolder placePeopleViewHolder = new PlacePeopleViewHolder(v);
        return placePeopleViewHolder;
    }

    @Override
    public void onBindViewHolder(PlacePeopleViewHolder holder, final int position) {
        Log.d(TAG, "::onBindViewHolder() -> " + position);
        PlaceUserInterface placeUserInterface = this.get(position);
        User user = placeUserInterface.getUser();
        RecyclerView rvPostTags = holder.rvPostTags;

        Log.d(TAG, "User: " + user.getUsername());
        String pic = user.getProfilePictureUrl();
        if(pic !=null && holder.ivProfilePicture!=null) {
            Picasso.with(context).load(pic).into(holder.ivProfilePicture);
        }

        String username = user.getUsername();
        holder.tvUsername.setText(username);
        holder.tvTime.setText(placeUserInterface.getTimeCreated());

        if(placeUserInterface.getTags()==null) {
            rvPostTags.setVisibility(View.GONE);
        } else {
            rvPostTags.setVisibility(View.VISIBLE);
            List<Tag> tags = placeUserInterface.getTags();

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
    }


    public void setOnItemClickListener(final OnItemAdapterClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public class PlacePeopleViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        CardView cv;
        TextView tvUsername;
        TextView tvTime;
        RecyclerView rvPostTags;
        ImageView ivProfilePicture;

        PlacePeopleViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            // Get text views from item_post.xml
            cv = (CardView) itemView.findViewById(R.id.cv);
            tvUsername = (TextView) itemView.findViewById(R.id.tv_username);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            rvPostTags = (RecyclerView) itemView.findViewById(R.id.rv_horizontal_tags);
            ivProfilePicture = (ImageView) itemView.findViewById(R.id.profile_picture);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onClick(getAdapterPosition());
            }
        }
    }
}