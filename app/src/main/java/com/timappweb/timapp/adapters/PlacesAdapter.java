package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Tag;

import java.util.ArrayList;
import java.util.LinkedList;

public class PlacesAdapter extends ArrayAdapter<Place> {
    private final Context context;

    public PlacesAdapter(Context context) {
        super(context, R.layout.item_place);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Place place = this.getItem(position);

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
        TextView countPosts = (TextView) postBox.findViewById(R.id.people_counter_place);
        RecyclerView rv_lastPostTags = (RecyclerView) postBox.findViewById(R.id.rv_horizontal_tags);

        // Get
        String location = place.location;
        String time = place.getTime();
        ArrayList<Tag> mainTags = getMainTags();

        //Set texts
        tvLocation.setText(location);
        tvTime.setText(time);

        //Set the adapter for RV
        HorizontalTagsAdapter horizontalTagsAdapter = new HorizontalTagsAdapter(getContext(), new LinkedList<Tag>());
        horizontalTagsAdapter.setData(mainTags);
        rv_lastPostTags.setAdapter(horizontalTagsAdapter);

        //Set LayoutManager for RV
        GridLayoutManager manager_savedTags = new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false);
        rv_lastPostTags.setLayoutManager(manager_savedTags);

        //return the view
        return postBox;
    }

    @Override
    public void add(Place place) {
        super.add(place);
        super.notifyDataSetChanged();
    }

    private ArrayList<Tag> getMainTags() {
        ArrayList<Tag> emptyList = new ArrayList<Tag>();
        return emptyList;
    }

    public void generateDummyData() {
        Place dummyPlace = Place.createDummy();
        add(dummyPlace);
        Place dummyPlace2 = Place.createDummy();
        add(dummyPlace2);
    }
}