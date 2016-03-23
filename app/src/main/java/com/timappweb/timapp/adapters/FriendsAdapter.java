package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.sromku.simple.fb.entities.Profile;
import com.timappweb.timapp.R;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.List;


public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    List<Profile> data;
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
        context = viewGroup.getContext();

        FriendViewHolder friendViewHolder = new FriendViewHolder(v);
        return friendViewHolder;
    }

    @Override
    public void onBindViewHolder(FriendViewHolder friendViewHolder, int position) {
        Profile friend = data.get(position);
        friendViewHolder.personName.setText(friend.getName());

        //Listener Horizontal Scroll View
        HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                new HorizontalTagsTouchListener(context, mItemClickListener, position);
        friendViewHolder.horizontalTags.setOnTouchListener(mHorizontalTagsTouchListener);

        //code with real friends
        Picasso.with(context).load(friend.getPicture()).into(friendViewHolder.personPhoto);
    }

    public List<Profile> getData(){
        return data;
    }

    public void setData(List<Profile> friends) {
        this.data = friends;
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

    public class FriendViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        CardView cv;
        TextView personName;
        ImageView personPhoto;
        View selectedView;
        HorizontalTagsRecyclerView horizontalTags;

        FriendViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            cv = (CardView) itemView.findViewById(R.id.cv);
            personName = (TextView) itemView.findViewById(R.id.person_name);
            personPhoto = (ImageView) itemView.findViewById(R.id.person_photo);
            selectedView = itemView.findViewById(R.id.selectedView);
            horizontalTags = (HorizontalTagsRecyclerView) itemView.findViewById(R.id.rv_horizontal_tags);

            HorizontalTagsAdapter horizontalTagsAdapter = horizontalTags.getAdapter();
            horizontalTagsAdapter.setDummyData();

        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onClick(getAdapterPosition());
            }
        }
    }
}
