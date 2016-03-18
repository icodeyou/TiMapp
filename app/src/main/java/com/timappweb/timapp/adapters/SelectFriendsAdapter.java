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


public class SelectFriendsAdapter extends FriendsAdapter {

    OnItemAdapterClickListener mItemClickListener;
    Context context;

    //Constructor
    public SelectFriendsAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void onBindViewHolder(FriendsAdapter.PersonViewHolder personViewHolder, int position) {
        super.onBindViewHolder(personViewHolder, position);
        Friend friend = data.get(position);
        setCheckedView(personViewHolder, friend.isSelected);
    }

    public void setItemSelected(int position, boolean isSelected) {
        if (position != -1) {
            data.get(position).setSelected(isSelected);
            notifyDataSetChanged();
        }
    }

    public void setCheckedView(PersonViewHolder holder ,boolean isChecked) {
        if(isChecked) {
            holder.selectedView.setVisibility(View.VISIBLE);
        } else {
            holder.selectedView.setVisibility(View.GONE);
        }
    }
}
