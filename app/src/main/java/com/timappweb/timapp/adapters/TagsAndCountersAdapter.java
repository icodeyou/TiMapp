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
        super(context, R.layout.item_tag_with_counter);
        this.context = context;
        this.tagsArrayList = new ArrayList<Tag>();
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
        String tagString = String.valueOf(tagsArrayList.get(position).getName());
        String tagCounterString = String.valueOf(tagsArrayList.get(position).getCountRef());
        tagView.setText(tagString);
        counterView.setText(tagCounterString);

        // return rowView
        return rowView;
    }

    //Generate Data for ListView
    //////////////////////////////////////////////////////////////////////////////
    public ArrayList<Tag> generateDummyData(){
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(new Tag("#friteschezjojo",1587));
        tags.add(new Tag("#boeing",747));
        tags.add(new Tag("#airbus",380));
        tags.add(new Tag("#lolilol",185));
        tags.add(new Tag("#whatever",184));
        tags.add(new Tag("#salt",154));
        tags.add(new Tag("#beer",146));
        tags.add(new Tag("#idontknowwhattosay",130));
        tags.add(new Tag("#nowords",114));
        tags.add(new Tag("#amazing",104));
        tags.add(new Tag("#wtf",85));
        tags.add(new Tag("#youhavetoseeittobelieveit",55));
        tags.add(new Tag("#ohmygod",30));
        tags.add(new Tag("#thisissofunny", 21));
        tags.add(new Tag("#beach", 14));
        notifyDataSetChanged();
        return tags;
    }
}