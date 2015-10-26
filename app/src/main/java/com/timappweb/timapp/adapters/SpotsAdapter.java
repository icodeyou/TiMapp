package com.timappweb.timapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Spot;
import com.timappweb.timapp.entities.SpotsTag;

import java.util.ArrayList;

/**
 * Created by stephane on 9/5/2015.
 */
public class SpotsAdapter extends ArrayAdapter<Spot> {
    public SpotsAdapter(Context context, ArrayList<Spot> spots) {
        super(context, 0, spots);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Spot spot = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spot, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tv_username);
        tvName.setText(spot.tag_string);

        TextView tvTags = (TextView) convertView.findViewById(R.id.tv_tags);
        tvTags.setText(spot.tag_string);

        TextView tvCreated = (TextView) convertView.findViewById(R.id.tv_created);
        tvCreated.setText(spot.getCreatedDate());

        // Return the completed view to render on screen
        return convertView;
    }
}