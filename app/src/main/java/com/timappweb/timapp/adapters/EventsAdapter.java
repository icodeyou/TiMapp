package com.timappweb.timapp.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.databinding.LayoutEventBinding;
import com.timappweb.timapp.databinding.LayoutSpotBinding;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.utils.Util;
import com.timappweb.timapp.views.SimpleTimerView;

import java.util.ArrayList;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> {

    private static final String TAG = "EventsAdapter";

    // =============================================================================================

    private Context context;
    private List<Event> data;
    private OnItemAdapterClickListener itemAdapterClickListener;

    // =============================================================================================

    public EventsAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    @Override
    public EventsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       LayoutEventBinding mBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.layout_event, parent, false);
        return new EventsViewHolder(mBinding.getRoot(), mBinding);
    }

    @Override
    public void onBindViewHolder(EventsViewHolder viewHolder, int position) {
        final Event event = data.get(position);
        Log.d(TAG, "Get view for " + (position + 1) + "/" + getItemCount());
        viewHolder.setEventInHolder(event);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(Event event) {
        this.data.add(event);
        notifyDataSetChanged();
    }

    public void setData(List<Event> events) {
        this.data = events;
        notifyDataSetChanged();
    }

    public List<Event> getData() {
        return data;
    }

    public Event getItem(int position) {
        return data.get(position);
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public void setItemAdapterClickListener(OnItemAdapterClickListener itemAdapterClickListener) {
        this.itemAdapterClickListener = itemAdapterClickListener;
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        SimpleTimerView tvCountPoints;
        TextView titleTv;
        LayoutEventBinding mBinding;

        EventsViewHolder(View itemView, LayoutEventBinding binding) {
            super(itemView);
            this.mBinding = binding;
            tvCountPoints = (SimpleTimerView) itemView.findViewById(R.id.points_text);
            titleTv = (TextView) itemView.findViewById(R.id.title_category);
            titleTv.setVisibility(View.VISIBLE);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemAdapterClickListener != null) {
                itemAdapterClickListener.onClick(getAdapterPosition());
            }
        }

        public void setEventInHolder(Event event) {
            mBinding.setEvent(event);
            int initialTime = event.getPoints();
            tvCountPoints.initTimer(initialTime);

            try {
                titleTv.setText(Util.capitalize(event.getCategory().name));
            } catch (UnknownCategoryException e) {
                e.printStackTrace();
            }

            // Following line is important, it will force to load the variable in a custom view
            mBinding.executePendingBindings();
        }
    }

}