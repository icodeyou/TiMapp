package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Friend;

import java.util.List;


public class SelectFriendsAdapter extends RecyclerView.Adapter<SelectFriendsAdapter.PersonViewHolder> {

    List<Friend> persons;
    OnItemClickListener mItemClickListener;
    Context context;

    public SelectFriendsAdapter(List<Friend> friends) {
        this.persons = friends;
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_item, viewGroup, false);
        context = viewGroup.getContext();
        return new PersonViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        Friend friend = persons.get(i);
        personViewHolder.personName.setText(friend.username);
        personViewHolder.personAge.setText(String.valueOf(friend.age));

        personViewHolder.personPhoto.setImageResource(friend.photoId); // for the example

        //code with real friends
        //Picasso.with(context).load(friend.getProfilePictureUrl()).into(personViewHolder.personPhoto);

        personViewHolder.cbSelected.setChecked(friend.isSelected);
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

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void setPersons(List<Friend> persons) {
        this.persons = persons;
    }


    public interface OnItemClickListener {

        void onItemClick(View view, int position);
    }

    public class PersonViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        CardView cv;
        TextView personName;
        TextView personAge;
        ImageView personPhoto;
        CheckBox cbSelected;

        PersonViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            cv = (CardView) itemView.findViewById(R.id.cv);
            personName = (TextView) itemView.findViewById(R.id.person_name);
            personAge = (TextView) itemView.findViewById(R.id.person_age);
            personPhoto = (ImageView) itemView.findViewById(R.id.person_photo);
            cbSelected = (CheckBox) itemView.findViewById(R.id.cbSelected);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getPosition());
            }
        }
    }

}
