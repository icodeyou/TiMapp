package com.timappweb.timapp.views;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;


import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Tag;

import java.util.LinkedList;


public class HorizontalTagsRecyclerView extends RecyclerView {

    //Constructor
    public HorizontalTagsRecyclerView(Context context) {
        super(context);
        this.init();
    }

    public HorizontalTagsRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public HorizontalTagsRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }

    // Override
    @Override
    public HorizontalTagsAdapter getAdapter() {
        return (HorizontalTagsAdapter) super.getAdapter();
    }


    //Methods

    private void init(){
        this.setHasFixedSize(true);

        this.setAdapter(new HorizontalTagsAdapter(getContext()));

        GridLayoutManager manager = new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false);
        this.setLayoutManager(manager);
        this.scrollToEnd();
    }

    public void scrollToEnd(){
        this.scrollToPosition(getAdapter().getItemCount() - 1);
    }

}
