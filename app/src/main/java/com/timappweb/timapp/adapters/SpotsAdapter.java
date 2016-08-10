package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.List;

// TODO must be reimplemented
public class SpotsAdapter extends RecyclerView.Adapter<SpotsAdapter.SpotViewHolder> {
    private static final String TAG = "SpotsAdapter";
    private Context context;

    private List<Spot> data;

    private OnItemAdapterClickListener itemAdapterClickListener;

    public SpotsAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    @Override
    public SpotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_spot, parent, false);
        return new SpotViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SpotViewHolder holder, int position) {
            Log.d(TAG, "Get view for " + (position+1) + "/" + getItemCount());
            final Spot spot = data.get(position);

            //TODO : NullPointerException !
            /*//OnTagsRvClick : Same event as adapter click.
            HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                    new HorizontalTagsTouchListener(context, itemAdapterClickListener, position);
            holder.horizontalTagsRv.setOnTouchListener(mHorizontalTagsTouchListener);*/
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(Spot spot) {
        this.data.add(spot);
        notifyDataSetChanged();
    }

    public void setData(List<Spot> spots) {
        this.data = spots;
        notifyDataSetChanged();
    }

    public List<Spot> getData() {
        return data;
    }

    public Spot getItem(int position) {
        return data.get(position);
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public void setItemAdapterClickListener(OnItemAdapterClickListener itemAdapterClickListener) {
        this.itemAdapterClickListener = itemAdapterClickListener;
    }

    public class SpotViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        HorizontalTagsRecyclerView horizontalTagsRv;

        SpotViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            //horizontalTagsRv = spotView.getRvSpotTags();
        }

        @Override
        public void onClick(View v) {
            if (itemAdapterClickListener != null) {
                itemAdapterClickListener.onClick(getAdapterPosition());
            }
        }
    }
}