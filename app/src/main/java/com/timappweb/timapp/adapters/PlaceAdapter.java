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

public class PlaceAdapter extends ArrayAdapter<Tag> {
    private final Context context;
    private final ArrayList<Tag> tagsArrayList;

    public PlaceAdapter(Context context, ArrayList<Tag> tagsArrayList) {
        super(context, R.layout.list_item_tag, tagsArrayList);
        this.context = context;
        this.tagsArrayList = tagsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = (View) inflater.inflate(R.layout.list_item_tag, parent, false);

        // 3. Get icon,title & counter views from the rowView
        TextView tagView = (TextView) rowView.findViewById(R.id.tv_tag);
        TextView counterView = (TextView) rowView.findViewById(R.id.tv_tag_counter);

        // 4. Set the text for textView
        String tagString = String.valueOf(tagsArrayList.get(position).getName());
        String tagCounterString = String.valueOf(tagsArrayList.get(position).getCount_ref());
        tagView.setText(tagString);
        counterView.setText(tagCounterString);

        // 5. return rowView
        return rowView;
    }
}