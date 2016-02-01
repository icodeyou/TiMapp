package com.timappweb.timapp.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;


import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Tag;

import java.util.LinkedList;


public class HorizontalTagsRecyclerView extends RecyclerView {
    private int backgroundColor;
    private int textColor;

    //Constructor
    public HorizontalTagsRecyclerView(Context context) {
        super(context);
        this.init();
    }

    public HorizontalTagsRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Get colors from attributes in XML
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HorizontalTagsRecyclerView, 0, 0);
        backgroundColor = ta.getColor(R.styleable.HorizontalTagsRecyclerView_background_color, 0);
        textColor = ta.getColor(R.styleable.HorizontalTagsRecyclerView_text_color,0);
        ta.recycle();

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

    private void init() {
        this.setHasFixedSize(true);

        HorizontalTagsAdapter horizontalTagsAdapter = new HorizontalTagsAdapter(getContext());
        horizontalTagsAdapter.setColors(textColor, backgroundColor); // Set colors from attributes
        this.setAdapter(horizontalTagsAdapter);

        GridLayoutManager manager = new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false);
        this.setLayoutManager(manager);
        this.scrollToEnd();
    }

    public void scrollToEnd(){
        this.scrollToPosition(getAdapter().getItemCount() - 1);
    }

}