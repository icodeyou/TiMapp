package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;
import com.timappweb.timapp.views.EventView;

import java.util.ArrayList;
import java.util.List;

public class FooterRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "EventsAdapter";
    private Context context;
    private int colorRes = -1;
    private boolean isTagsVisible;
    private boolean footerActive;

    private List<Place> data;

    private OnItemAdapterClickListener itemAdapterClickListener;

    public FooterRecyclerView(Context context, boolean footerActive, boolean isTagsVisible) {
        data = new ArrayList<>();
        this.context = context;
        this.footerActive = footerActive;
        this.isTagsVisible = isTagsVisible;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder baseHolder, int position) {
        if(baseHolder instanceof PlacesViewHolder) {
            PlacesViewHolder holder = (PlacesViewHolder) baseHolder;
            Log.d(TAG, "Get view for " + (position+1) + "/" + getItemCount());
            final Place place = data.get(position);

            holder.eventView.setEvent(place);

            //OnTagsRvClick : Same event as adapter click.
            HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                    new HorizontalTagsTouchListener(context, itemAdapterClickListener, position);
            holder.horizontalTagsRv.setOnTouchListener(mHorizontalTagsTouchListener);
        }
    }

    @Override
    public int getItemCount() {
        if(footerActive) {
            return data.size()+1;
        } else {
            return data.size();
        }
    }

    //////////////////////////////////////////////////
    //Footer
    ///////////

    private class VIEW_TYPES {
        public static final int NORMAL = 1;
        public static final int FOOTER = 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;

        switch (viewType)
        {
            case VIEW_TYPES.NORMAL:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_event, viewGroup, false);
                return new PlacesViewHolder(v);
            case VIEW_TYPES.FOOTER:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.explore_places_button, viewGroup, false);
                return new FooterPlacesViewHolder(v);
            default:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_event, viewGroup, false);
                return new PlacesViewHolder(v);
        }


    }

    @Override
    public int getItemViewType(int position) {

        if(isPositionFooter(position))
            return VIEW_TYPES.FOOTER;
        else
            return VIEW_TYPES.NORMAL;

    }

    private boolean isPositionFooter(int position) {
        return position == data.size() && footerActive;
    }

    //////////////////////////////////////////////////

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


    public void generateDummyData() {
        Place dummyPlace = Place.createDummy();
        add(dummyPlace);
        Place dummyPlace2 = Place.createDummy();
        add(dummyPlace2);
    }

    public void setItemAdapterClickListener(OnItemAdapterClickListener itemAdapterClickListener) {
        this.itemAdapterClickListener = itemAdapterClickListener;
    }

    public class PlacesViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        EventView eventView;
        HorizontalTagsRecyclerView horizontalTagsRv;

        PlacesViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            eventView = (EventView) itemView.findViewById(R.id.event_view);
            horizontalTagsRv = eventView.getRvEventTags();
        }

        @Override
        public void onClick(View v) {
            if (itemAdapterClickListener != null) {
                itemAdapterClickListener.onClick(getAdapterPosition());
            }
        }
    }

    public class FooterPlacesViewHolder extends RecyclerView.ViewHolder {

        private final Button newEventButton;

        FooterPlacesViewHolder(View itemView) {
            super(itemView);
            newEventButton = (Button) itemView.findViewById(R.id.create_button);
            newEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.addPlace(context);
                }
            });

        }
    }
}