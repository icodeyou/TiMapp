package com.timappweb.timapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserFriend;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    List<User> data;
    OnItemAdapterClickListener mItemClickListener;
    Context context;

    //Constructor
    public FriendsAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_friend, viewGroup, false);

        FriendViewHolder friendViewHolder = new FriendViewHolder(v);
        return friendViewHolder;
    }

    @Override
    public void onBindViewHolder(FriendViewHolder friendViewHolder, int position) {
        friendViewHolder.selectedView.setVisibility(View.GONE);
        User friend = data.get(position);
        friendViewHolder.personName.setText(friend.getUsername());

        //Listener Horizontal Scroll View
        HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                new HorizontalTagsTouchListener(context, mItemClickListener, position);
        friendViewHolder.horizontalTags.setOnTouchListener(mHorizontalTagsTouchListener);

        //Horizontal tags
        HorizontalTagsAdapter horizontalTagsAdapter = friendViewHolder.horizontalTags.getAdapter();
        if(!friend.hasTags()) {
            List<Tag> newbieList = new ArrayList<>();
            newbieList.add(new Tag(context.getString(R.string.newbie_tag)));
            horizontalTagsAdapter.setData(newbieList);
        } else {
            horizontalTagsAdapter.setData(friend.getTags());
        }

        //User pic
        Uri uri = Uri.parse(friend.getProfilePictureUrl());
        friendViewHolder.personPhoto.setImageURI(uri);
    }

    public List<User> getData(){
        return data;
    }

    public void setData(List<UserFriend> friends) {
        this.data = new LinkedList<>();
        addData(friends);
        notifyDataSetChanged();
    }

    public void addData(List<UserFriend> data) {
        if (this.data == null) this.data = new LinkedList<>();
        for (UserFriend friend: data){
            this.data.add(friend.userTarget);
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setOnItemClickListener(final OnItemAdapterClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void clear() {
        this.data.clear();
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        View cv;
        TextView personName;
        SimpleDraweeView personPhoto;
        View selectedView;
        HorizontalTagsRecyclerView horizontalTags;

        FriendViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            cv = itemView.findViewById(R.id.cv);
            personName = (TextView) itemView.findViewById(R.id.person_name);
            personPhoto = (SimpleDraweeView) itemView.findViewById(R.id.person_photo);
            selectedView = itemView.findViewById(R.id.selectedView);
            horizontalTags = (HorizontalTagsRecyclerView) itemView.findViewById(R.id.rv_horizontal_tags);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onClick(getAdapterPosition());
            }
        }
    }
}
