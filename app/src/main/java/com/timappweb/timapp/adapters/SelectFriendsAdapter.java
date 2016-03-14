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


public class SelectFriendsAdapter extends RecyclerView.Adapter<SelectFriendsAdapter.PersonViewHolder> {

    List<Friend> persons;
    OnItemAdapterClickListener mItemClickListener;
    Context context;
    private HorizontalTagsAdapter horizontalTagsAdapter;

    //Constructor
    public SelectFriendsAdapter(List<Friend> friends) {
        this.persons = friends;
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_select_friend, viewGroup, false);
        context = viewGroup.getContext();

        PersonViewHolder personViewHolder = new PersonViewHolder(v);
        return personViewHolder;
    }

    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int position) {
        Friend friend = persons.get(position);
        personViewHolder.personName.setText(friend.username);
        personViewHolder.personPhoto.setImageResource(friend.photoId); // for the example

        //Listener Horizontal Scroll View
        HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                new HorizontalTagsTouchListener(context, mItemClickListener, position);
        personViewHolder.horizontalTags.setOnTouchListener(mHorizontalTagsTouchListener);

        //code with real friends
        //Picasso.with(context).load(friend.getProfilePictureUrl()).into(personViewHolder.personPhoto);

        setCheckedView(personViewHolder, friend.isSelected);
    }

    public void setItemSelected(int position, boolean isSelected) {
        if (position != -1) {
            persons.get(position).setSelected(isSelected);
            notifyDataSetChanged();
        }
    }

    public List<Friend> getPersons(){
        return persons;
    }

    @Override
    public int getItemCount() {
        return persons.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setOnItemClickListener(final OnItemAdapterClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void setPersons(List<Friend> persons) {
        this.persons = persons;
    }


    public interface OnItemClickListener {

        void onItemClick(View view, int position);
    }

    public void setCheckedView(PersonViewHolder holder ,boolean isChecked) {
        if(isChecked) {
            holder.selectedView.setVisibility(View.VISIBLE);
        } else {
            holder.selectedView.setVisibility(View.GONE);
        }
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
