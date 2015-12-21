package com.timappweb.timapp.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.timappweb.timapp.adapters.TagsAdapter;

public abstract class TagRecyclerView extends RecyclerView {

    public TagRecyclerView(Context context) {
        super(context);
    }

    public TagRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TagRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void scrollToEnd(){
        this.scrollToPosition(getAdapter().getItemCount() - 1);
    }

    @Override
    public TagsAdapter getAdapter() {
        return (TagsAdapter) super.getAdapter();
    }

}
