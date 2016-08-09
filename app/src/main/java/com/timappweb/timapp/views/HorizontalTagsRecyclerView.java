package com.timappweb.timapp.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.data.models.Tag;

import java.util.ArrayList;
import java.util.List;


public class HorizontalTagsRecyclerView extends RecyclerView {
    private int backgroundColor;
    private int textColor;
    private boolean isBold;
    private Float textSize;
    private HorizontalTagsAdapter horizontalTagsAdapter;

    //Constructors
    public HorizontalTagsRecyclerView(Context context) {
        super(context);
        this.initAttributes();
        this.initAdapter();
    }

    public HorizontalTagsRecyclerView(Context context, List<Tag> tags) {
        super(context);

        this.initAttributes();

        this.initAdapter();
        horizontalTagsAdapter.setData(tags);
    }

    public HorizontalTagsRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initAttributes();

        //Get attributes in XML
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HorizontalTagsRecyclerView, 0, 0);
        backgroundColor = ta.getColor(R.styleable.HorizontalTagsRecyclerView_horizontal_tags_background_color, backgroundColor);
        textColor = ta.getColor(R.styleable.HorizontalTagsRecyclerView_horizontal_tags_text_color, textColor);
        isBold = ta.getBoolean(R.styleable.HorizontalTagsRecyclerView_horizontal_tags_is_bold, true);
        textSize = ta.getDimension(R.styleable.HorizontalTagsRecyclerView_horizontal_tags_text_size, textSize);
        ta.recycle();

        this.initAdapter();
    }

    public HorizontalTagsRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.initAttributes();
        this.initAdapter();
    }

    private void initAttributes() {
        backgroundColor = 0;
        textColor = Color.WHITE;
        isBold = true;
        textSize = getResources().getDimension(R.dimen.text_normal);
    }

    // Override
    @Override
    public HorizontalTagsAdapter getAdapter() {
        return horizontalTagsAdapter;
    }

    public int getMaxTags() {
        return horizontalTagsAdapter.getMaxTags();
    }


    //Methods

    private void initAdapter() {
        this.setHasFixedSize(true);

        horizontalTagsAdapter = new HorizontalTagsAdapter(getContext());
        horizontalTagsAdapter.setColors(textColor, backgroundColor); // Set colors from attributes
        horizontalTagsAdapter.settextStyle(isBold);
        horizontalTagsAdapter.settextSize(textSize);
        this.setAdapter(horizontalTagsAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        this.setLayoutManager(manager);
        this.scrollToEnd();

        //return horizontalTagsAdapter;
    }

    public void setDummyData() {
        Tag dummy = new Tag("dummy");

        List<Tag> dummyTags = new ArrayList<>();
        dummyTags.add(dummy);
        dummyTags.add(dummy);
        dummyTags.add(dummy);
        //adapter.setData(dummyTags);

        //this.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        this.setLayoutManager(manager);
        this.scrollToEnd();
    }

    public void scrollToEnd(){
        this.scrollToPosition(getAdapter().getItemCount() - 1);
    }

}