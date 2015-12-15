package com.timappweb.timapp.views;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.timappweb.timapp.adapters.SuggestedTagsAdapter;
import com.timappweb.timapp.adapters.TagsAdapter;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.listeners.MyLinearLayoutManager;
import com.timappweb.timapp.listeners.RecyclerItemTouchListener;

import java.util.LinkedList;

/**
 * Created by stephane on 12/15/2015.
 */
public class SuggestedTagRecyclerView extends TagRecyclerView{

    private static final String TAG = "SuggestedTagRecycler";

    public SuggestedTagRecyclerView(Context context) {
        super(context);
        this.init();
    }

    public SuggestedTagRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public SuggestedTagRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }


    protected void init(){
        this.setAdapter(new SuggestedTagsAdapter(getContext(), new LinkedList<Tag>()));

        MyLinearLayoutManager layoutManager = new MyLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false);
        this.setLayoutManager(layoutManager);
        this.scrollToEnd();


    }

}
