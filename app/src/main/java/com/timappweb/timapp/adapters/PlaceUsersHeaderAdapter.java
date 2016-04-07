package com.timappweb.timapp.adapters;

import android.content.Context;
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
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.List;

public class PlaceUsersHeaderAdapter extends PlaceUsersAdapter
        implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {

    private static final String TAG = "PlaceUsersAdapter";

    private OnItemAdapterClickListener mItemClickListener;

    public PlaceUsersHeaderAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public long getHeaderId(int position) {
        PlaceUserInterface placeUserInterface = data.get(position);
        return placeUserInterface.getViewType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_place_people, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        PlaceUserInterface placeUserInterface = data.get(position);
        switch (placeUserInterface.getViewType()) {
            case VIEW_TYPES.HERE:
                textView.setText("Posts");
                break;
            case VIEW_TYPES.COMING:
                textView.setText("Coming");
                break;
            case VIEW_TYPES.INVITED:
                textView.setText("Invited");
        }
    }
}
