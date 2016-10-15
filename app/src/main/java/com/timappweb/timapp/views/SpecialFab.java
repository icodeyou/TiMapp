package com.timappweb.timapp.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.timappweb.timapp.R;

public class SpecialFab extends FrameLayout {

    private Context context;

    public SpecialFab(Context context) {
        super(context);
        init(context);
    }

    public SpecialFab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SpecialFab(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        inflate(getContext(), R.layout.view_category_selector, this);
    }
}
