package com.timappweb.timapp.adapters;

import android.content.Context;
import android.util.TypedValue;
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

    public TagsAndCountersAdapter(Context context) {
        super(context, 0);
        this.context = context;
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
        TextView tvTag = (TextView) rowView.findViewById(R.id.tv_tag);
        TextView tvCounter = (TextView) rowView.findViewById(R.id.tv_tag_counter);

        // Set the text for textView
        //TODO: The if loop should not be necessary
        if (getData().size() > 0){
            String tagString = String.valueOf(getItem(position).getName());
            String tagCounterString = String.valueOf(getItem(position).getCountRef());
            tvTag.setText(tagString);
            tvCounter.setText(tagCounterString);
        }

        /*float dimens[] = new float[3];
        //Convert dp into pixels
        float pixels2 = rowView.getResources().getDimension(R.dimen.text_lv2);
        dimens[2] = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels2,
                rowView.getResources().getDisplayMetrics());
        float pixels1 = rowView.getResources().getDimension(R.dimen.text_lv1);
        dimens[1] = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels1,
                rowView.getResources().getDisplayMetrics());
        float pixels0 = rowView.getResources().getDimension(R.dimen.text_lv0);
        dimens[0] = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels0,
                rowView.getResources().getDisplayMetrics());

        if(position<3) {
            tvTag.setTextSize(dimens[position]);
            tvCounter.setTextSize(dimens[position]);
        }*/

        // return rowView
        return rowView;
    }

    public ArrayList<Tag> getData() {
        ArrayList<Tag> tags = new ArrayList<Tag>();

        for (int i=0; i<this.getCount(); i++) {
            tags.add(getItem(i));
        }

        return tags;
    }
}