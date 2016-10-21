package com.timappweb.timapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Tag;

public class TagsProfileAdapter extends ArrayAdapter<Tag> {

    private Context context;

    public TagsProfileAdapter(Context context) {
        super(context, R.layout.item_usertags);
        this.context = context;
    }

    @Override
    public View getView(final int position, View root, ViewGroup parent) {
        // Get the view from inflater
        View view = root;
        if(root==null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_usertags, parent, false);
        }

        TextView textView = (TextView) view.findViewById(R.id.usertag);
        String name = this.getItem(position).getName();
        textView.setText("#" + name);

        //return the view
        return view;
    }

}