package com.timappweb.timapp.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by stephane on 5/14/2016.
 */
public class SwipeRefreshLayout extends android.support.v4.widget.SwipeRefreshLayout{

    public SwipeRefreshLayout(Context context) {
        super(context);
        this._init();
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this._init();

    }

    private void _init(){
        this.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

}
