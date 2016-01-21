package com.timappweb.timapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Tag;

import java.util.ArrayList;

//Source :
// http://hmkcode.com/android-custom-listview-titles-icons-counter/

public class TagsAndCountersAdapter extends ArrayAdapter<Tag> {
    private final Context context;
    private final ArrayList<Tag> tagsArrayList;

    public TagsAndCountersAdapter(Context context) {
        super(context, 0);
        this.context = context;
        this.tagsArrayList = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the view from inflater
        View rowView = convertView;
        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.item_tag_with_counter, parent, false);
        }

        // Get icon,title & counter views from the rowView
        TextView tagView = (TextView) rowView.findViewById(R.id.tv_tag);
        TextView counterView = (TextView) rowView.findViewById(R.id.tv_tag_counter);

        // Set the text for textView
        //TODO: The if loop should not be necessary
        if (tagsArrayList.size() > 0){
            String tagString = String.valueOf(tagsArrayList.get(position).getName());
            String tagCounterString = String.valueOf(tagsArrayList.get(position).getCountRef());
            tagView.setText(tagString);
            counterView.setText(tagCounterString);
        }

        // return rowView
        return rowView;
    }
}