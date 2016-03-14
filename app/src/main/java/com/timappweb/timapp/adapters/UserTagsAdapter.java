package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.AutoResizeTextView;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;

public class UserTagsAdapter extends ArrayAdapter<Tag> {
    private static final String TAG = "UserTagsAdapter";
    private Context context;
    private ArrayList<Tag> data;

    public UserTagsAdapter(Context context) {
        super(context, R.layout.item_usertags);
        this.context = context;
        this.data = new ArrayList<>();
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

    public void setDummyData() {
        add(new Tag("ski"));
        add(new Tag("hightech"));
        add(new Tag("ledzeppelin"));
    }
}