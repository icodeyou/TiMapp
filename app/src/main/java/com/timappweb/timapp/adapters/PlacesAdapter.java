package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.AutoResizeTextView;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PlacesAdapter extends ArrayAdapter<Place> {
    private static final String TAG = "PlacesAdapter";
    private final Context context;
    private HorizontalTagsRecyclerView rvPlaceTags;
    private ArrayList<HorizontalTagsRecyclerView> listRvPlacetags;
    private boolean isTagsVisible;

    private OnItemAdapterClickListener itemAdapterClickListener;

    public PlacesAdapter(Context context) {
        super(context, R.layout.item_place);
        this.context = context;
        this.isTagsVisible = true;
        this.listRvPlacetags = new ArrayList<HorizontalTagsRecyclerView>();
    }

    public PlacesAdapter(Context context, boolean bool) {
        super(context, R.layout.item_place);
        this.context = context;
        this.isTagsVisible = bool;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //Log.d(TAG, "Get view for " + position + "/" + this.getCount());
        final Place place = this.getItem(position);

        // Get the view from inflater
        View view = convertView;
        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_place, parent, false);
        }


        // Initialize
        AutoResizeTextView tvLocation = (AutoResizeTextView) view.findViewById(R.id.title_place);
        TextView tvTime = (TextView) view.findViewById(R.id.time_place);
        TextView tvCountPoints = (TextView) view.findViewById(R.id.people_counter_place);
        rvPlaceTags = (HorizontalTagsRecyclerView) view.findViewById(R.id.rv_horizontal_tags);
        ImageView categoryIcon = (ImageView) view.findViewById(R.id.image_category_place);

        //Set texts
        tvLocation.setText(place.name);
        tvTime.setText(place.getTime());
        tvCountPoints.setText(String.valueOf(place.getPoints()));

        Category category = MyApplication.getCategory(place.category_id);
        if (category != null){
            categoryIcon.setImageResource(MyApplication.getCategory(place.category_id).resourceTransparent);
        }
        else{
            Log.i(TAG,"no category found for id : " + place.category_id );
        }

        if(isTagsVisible) {
            //Set the adapter for RV
            HorizontalTagsAdapter htAdapter = rvPlaceTags.getAdapter();
            htAdapter.setData(place.tags);
            rvPlaceTags.setAdapter(htAdapter);

            //Set LayoutManager for RV
            GridLayoutManager manager_savedTags = new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false);
            rvPlaceTags.setLayoutManager(manager_savedTags);
        }
        else {
            rvPlaceTags.setVisibility(View.GONE);
        }

        //listRvPlacetags.add(rvPlaceTags);

        //Listener entire view
        if (this.itemAdapterClickListener != null){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemAdapterClickListener.onClick(position);
                }
            });
        }

        //Listener Horizontal Scroll View
        HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                new HorizontalTagsTouchListener(getContext(), itemAdapterClickListener, position);
        rvPlaceTags.setOnTouchListener(mHorizontalTagsTouchListener);

        //return the view
        return view;
    }

    @Override
    public void add(Place place) {
        super.add(place);
        super.notifyDataSetChanged();
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

    public ArrayList<HorizontalTagsRecyclerView> getListRvTags() {
        return listRvPlacetags;
    }
}