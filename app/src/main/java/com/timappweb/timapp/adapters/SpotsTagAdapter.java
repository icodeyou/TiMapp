package com.timappweb.timapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.MapTag;

import java.util.ArrayList;

/**
 * Created by stephane on 9/5/2015.
 */
public class SpotsTagAdapter extends ArrayAdapter<MapTag> {
    public SpotsTagAdapter(Context context, ArrayList<MapTag> mapTags) {
        super(context, 0, mapTags);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        MapTag mapTag = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tv_username);
        // Populate the data into the template view using the data object
        tvName.setText(mapTag.count_ref);
        // Return the completed view to render on screen
        return convertView;
    }
}