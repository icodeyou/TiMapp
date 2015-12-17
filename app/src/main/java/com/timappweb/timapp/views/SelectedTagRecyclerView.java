package com.timappweb.timapp.views;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;


import com.timappweb.timapp.adapters.DisplayedTagsAdapter;
import com.timappweb.timapp.entities.Tag;

import java.util.LinkedList;

/**
 * Created by stephane on 12/15/2015.
 */
public class SelectedTagRecyclerView extends TagRecyclerView {


    public SelectedTagRecyclerView(Context context) {
        super(context);
        this.init();
    }

    public SelectedTagRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public SelectedTagRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }

    protected void init(){
        this.setAdapter(new DisplayedTagsAdapter(getContext(), new LinkedList<Tag>()));

        GridLayoutManager manager = new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false);
        this.setLayoutManager(manager);
        this.scrollToEnd();
    }
}
