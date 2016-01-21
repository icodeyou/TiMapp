package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

public class PlacesAdapter extends ArrayAdapter<Place> {
    private static final String TAG = "PlacesAdapter";
    private final Context context;

    public void setItemAdapterClickListener(OnItemAdapterClickListener itemAdapterClickListener) {
        this.itemAdapterClickListener = itemAdapterClickListener;
    }

    private OnItemAdapterClickListener itemAdapterClickListener;

    public PlacesAdapter(Context context) {
        super(context, R.layout.item_place);
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //Log.d(TAG, "Get view for " + position + "/" + this.getCount());
        final Place place = this.getItem(position);

        // Get the view from inflater
        View postBox = convertView;
        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            postBox = inflater.inflate(R.layout.item_place, parent, false);
        }

        // Initialize
        TextView tvLocation = (TextView) postBox.findViewById(R.id.title_place);
        TextView tvTime = (TextView) postBox.findViewById(R.id.time_place);
        TextView tvCountPosts = (TextView) postBox.findViewById(R.id.people_counter_place);
        RecyclerView rv_lastPostTags = (RecyclerView) postBox.findViewById(R.id.rv_horizontal_tags);
        ImageView categoryIcon = (ImageView) postBox.findViewById(R.id.image_category_place);

        //Set texts
        tvLocation.setText(place.name);
        tvTime.setText(place.getTime());
        tvCountPosts.setText(String.valueOf(place.countUsers()));

        Category category = MyApplication.getCategory(place.category_id);
        if (category != null){
            categoryIcon.setImageResource(MyApplication.getCategory(place.category_id).resource);
        }
        else{
            // TODO if no category thats weird man
        }

        //Set the adapter for RV
        HorizontalTagsAdapter horizontalTagsAdapter = new HorizontalTagsAdapter(getContext());
        horizontalTagsAdapter.setData(place.getMainTags());
        rv_lastPostTags.setAdapter(horizontalTagsAdapter);

        //Set LayoutManager for RV
        GridLayoutManager manager_savedTags = new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false);
        rv_lastPostTags.setLayoutManager(manager_savedTags);

        //Set OnClickListener for the entire view !

        if (this.itemAdapterClickListener != null){
            postBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemAdapterClickListener.onClick(position);
                }
            });
        }

        //return the view
        return postBox;
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
}