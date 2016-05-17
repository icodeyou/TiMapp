package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;
import com.timappweb.timapp.views.EventView;

import java.util.ArrayList;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> {
    private static final String TAG = "EventsAdapter";
    private Context context;

    private List<Place> data;

    private OnItemAdapterClickListener itemAdapterClickListener;

    public EventsAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    @Override
    public EventsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EventsViewHolder viewHolder, int position) {
        //if(baseHolder instanceof EventsViewHolder)
        //EventsViewHolder holder = (EventsViewHolder) baseHolder;
        Log.d(TAG, "Get view for " + (position + 1) + "/" + getItemCount());
        final Place place = data.get(position);

        //viewHolder.eventView.setEvent(place);
        viewHolder.eventView.setEvent(place);
        HorizontalTagsRecyclerView htrv = viewHolder.eventView.getRvEventTags();
        if (htrv != null){
            //OnTagsRvClick : Same event as adapter click.
            HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                    new HorizontalTagsTouchListener(context, itemAdapterClickListener, position);
            htrv.setOnTouchListener(mHorizontalTagsTouchListener);
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(Place place) {
        this.data.add(place);
        notifyDataSetChanged();
    }

    public void setData(List<Place> places) {
        this.data = places;
        notifyDataSetChanged();
    }

    public List<Place> getData() {
        return data;
    }

    public Place getItem(int position) {
        return data.get(position);
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public void setItemAdapterClickListener(OnItemAdapterClickListener itemAdapterClickListener) {
        this.itemAdapterClickListener = itemAdapterClickListener;
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        EventView eventView;

        EventsViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            eventView = (EventView) itemView.findViewById(R.id.event_view);
        }

        @Override
        public void onClick(View v) {
            if (itemAdapterClickListener != null) {
                itemAdapterClickListener.onClick(getAdapterPosition());
            }
        }
    }

    /*public class FooterPlacesViewHolder extends RecyclerView.ViewHolder {

        private final Button newEventButton;

        FooterPlacesViewHolder(View itemView) {
            super(itemView);
            newEventButton = (Button) itemView.findViewById(R.remote_id.create_button);
            newEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.addPlace(context);
                }
            });

        }
    }*/
}