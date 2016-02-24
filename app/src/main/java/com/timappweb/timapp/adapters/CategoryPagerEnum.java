package com.timappweb.timapp.adapters;

import com.timappweb.timapp.R;

/**
 * Created by Jack on 24/02/2016.
 */
public enum CategoryPagerEnum {
    MUSIC(R.string.category_music, R.layout.category_music),
    STRIKE(R.string.category_strike, R.layout.category_strike),
    STREETSHOW(R.string.category_streetshow, R.layout.category_streetshow),
    SPORT(R.string.category_sport, R.layout.category_sport),
    BAR(R.string.category_bar, R.layout.category_bar),
    NIGHTCLUB(R.string.category_nightclub, R.layout.category_nightclub),
    SHOW(R.string.category_show, R.layout.category_show),
    PARTY(R.string.category_party, R.layout.category_party),
    UNKNOWN(R.string.category_unknown, R.layout.category_unknown);

    private int mTitleResId;
    private int mLayoutResId;

    CategoryPagerEnum(int titleResId, int layoutResId) {
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }
}
