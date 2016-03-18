package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Friend;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.List;


public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.PersonViewHolder> {

    List<Friend> data;
    OnItemAdapterClickListener mItemClickListener;
    Context context;

    //Constructor
    public FriendsAdapter(Context context) {
        this.context = context;
    }

    public FriendsAdapter(List<Friend> friends) {
        this.data = friends;
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_friend, viewGroup, false);
        context = viewGroup.getContext();

        PersonViewHolder personViewHolder = new PersonViewHolder(v);
        return personViewHolder;
    }

    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int position) {
        Friend friend = data.get(position);
        personViewHolder.personName.setText(friend.username);
        personViewHolder.personPhoto.setImageResource(friend.photoId); // for the example

        //Listener Horizontal Scroll View
        HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                new HorizontalTagsTouchListener(context, mItemClickListener, position);
        personViewHolder.horizontalTags.setOnTouchListener(mHorizontalTagsTouchListener);

        //code with real friends
        //Picasso.with(context).load(friend.getChangerdenooom()).into(personViewHolder.personPhoto);
    }

    public List<Friend> getData(){
        return data;
    }

    public void setData(List<Friend> persons) {
        this.data = persons;
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

    public class PersonViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        CardView cv;
        TextView personName;
        ImageView personPhoto;
        View selectedView;
        HorizontalTagsRecyclerView horizontalTags;

        PersonViewHolder(View itemView) {
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
