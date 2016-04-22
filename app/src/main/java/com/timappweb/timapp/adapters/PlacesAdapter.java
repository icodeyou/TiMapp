package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;
import com.timappweb.timapp.views.PlaceView;

import java.util.ArrayList;
import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlacesViewHolder> {
    private static final String TAG = "PlacesAdapter";
    private Context context;

    private List<Place> data;

    private OnItemAdapterClickListener itemAdapterClickListener;

    public PlacesAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    @Override
    public PlacesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new PlacesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PlacesViewHolder baseHolder, int position) {
        //if(baseHolder instanceof PlacesViewHolder)
        //PlacesViewHolder holder = (PlacesViewHolder) baseHolder;
        Log.d(TAG, "Get view for " + (position+1) + "/" + getItemCount());
        final Place place = data.get(position);

        baseHolder.placeView.setPlace(place);

        //OnTagsRvClick : Same event as adapter click.
        HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                new HorizontalTagsTouchListener(context, itemAdapterClickListener, position);
        baseHolder.horizontalTagsRv.setOnTouchListener(mHorizontalTagsTouchListener);
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

        PlaceView placeView;
        HorizontalTagsRecyclerView horizontalTagsRv;

        PlacesViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            placeView = (PlaceView) itemView.findViewById(R.id.place_view);
            horizontalTagsRv = placeView.getRvPlaceTags();
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
            newEventButton = (Button) itemView.findViewById(R.id.create_button);
            newEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.addPlace(context);
                }
            });

        }
    }*/
}