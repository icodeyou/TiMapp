package com.timappweb.timapp.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.utils.Util;

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

        float dimens[] = new float[4];
        //Convert dp into pixels
        float scale = rowView.getResources().getDisplayMetrics().density;
        dimens[0] = rowView.getResources().getDimension(R.dimen.text_lv_base);
        dimens[1] = rowView.getResources().getDimension(R.dimen.text_lv1);
        dimens[2] = rowView.getResources().getDimension(R.dimen.text_lv2);
        dimens[3] = rowView.getResources().getDimension(R.dimen.text_lv3);

        if(position<3) {
            int tagNumber = position + 1;
            tvTag.setTextSize(Util.convertPixelsToDp(dimens[tagNumber], getContext()));
            tvTag.setTypeface(Typeface.DEFAULT_BOLD);
            tvCounter.setTextSize(Util.convertPixelsToDp(dimens[tagNumber], getContext()));
            tvCounter.setTypeface(Typeface.DEFAULT_BOLD);
            //rowView.setBackgroundResource(R.color.background_list_main_tags);
        }
        else  {
            tvTag.setTextSize(Util.convertPixelsToDp(dimens[0], getContext()));
            tvTag.setTypeface(Typeface.DEFAULT);
            tvCounter.setTextSize(Util.convertPixelsToDp(dimens[0], getContext()));
            tvCounter.setTypeface(Typeface.DEFAULT);
            /*if(position%2 == 0) {
                rowView.setBackgroundResource(R.color.background_list_main_tags);
            } else {
                rowView.setBackgroundResource(R.color.background_list_tags);
            }*/
        }

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