package com.timappweb.timapp.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

import com.timappweb.timapp.adapters.FilledTagsAdapter;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.listeners.MyLinearLayoutManager;

import java.util.LinkedList;

public class FilledRecyclerView extends TagRecyclerView{

    private static final String TAG = "SuggestedTagRecycler";

    public FilledRecyclerView(Context context) {
        super(context);
        this.init();
    }

    public FilledRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public FilledRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }


    protected void init(){
        this.setAdapter(new FilledTagsAdapter(getContext(), new LinkedList<Tag>()));

        MyLinearLayoutManager layoutManager = new MyLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false);
        this.setLayoutManager(layoutManager);
        this.scrollToEnd();


    }

}
