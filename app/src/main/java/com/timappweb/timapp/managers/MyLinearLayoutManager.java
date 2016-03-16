package com.timappweb.timapp.managers;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

public class MyLinearLayoutManager extends LinearLayoutManager {
    private int mParentWidth;
    private int mItemWidth;

    public MyLinearLayoutManager(Context context, int parentWidth, int itemWidth) {
        super(context);
        mParentWidth = parentWidth;
        mItemWidth = itemWidth;
    }

    @Override
    public int getPaddingLeft() {
        return Math.round(mParentWidth / 2f - mItemWidth / 2f);
    }

    @Override
    public int getPaddingRight() {
        return getPaddingLeft();
    }
}