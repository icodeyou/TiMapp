package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

public class EventUsersHeaderAdapter extends EventUsersAdapter
        implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {

    private static final String TAG = "EventUsersAdapter";

    private OnItemAdapterClickListener mItemClickListener;

    public EventUsersHeaderAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public long getHeaderId(int position) {
        return ((EventUsersAdapter.SectionItem) data.getSectionFromPosition(position)).getViewType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.header_place_people, parent, false);
        return new RecyclerView.ViewHolder(view) {};
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        int type = ((EventUsersAdapter.SectionItem) data.getSectionFromPosition(position)).getViewType();
        switch (type) {
            case VIEW_TYPES.HERE:
                textView.setText(context.getResources().getString(R.string.header_posts));
                break;
            case VIEW_TYPES.COMING:
                textView.setText(context.getResources().getString(R.string.header_coming));
                break;
            case VIEW_TYPES.INVITED:
                textView.setText(context.getResources().getString(R.string.header_invited));
                break;
        }
    }

}
