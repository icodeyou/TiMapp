package com.timappweb.timapp.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Spot;

public class SpotView extends LinearLayout{
    private final static String TAG = "SpotView";
    private int colorRes = -1;

    private HorizontalTagsRecyclerView  rvSpotTags;
    private ImageView                   bigCategoryIcon;
    private ImageView                   smallCategoryIcon;
    private ImageView                   parentLayout;
    private SimpleTimerView             tvCountPoints;
    private View                        gradientBottomView;
    private View                        gradientTopView;
    private View                        backgroundView;

    private boolean                     isTagsVisible = false;
    private boolean                     isBottomShadow = false;
    private boolean                     isTopShadow = false;
    private float                       alphaBackground;

    public SpotView(Context context) {
        super(context);
        this.init();
    }

    public SpotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Get attributes in XML
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SpotView, 0, 0);
        isTagsVisible = ta.getBoolean(R.styleable.SpotView_tags_visible, false);
        isBottomShadow = ta.getBoolean(R.styleable.SpotView_bottom_shadow, false);
        isTopShadow = ta.getBoolean(R.styleable.SpotView_top_shadow, false);
        colorRes = ta.getColor(R.styleable.PlaceView_background_color, -1);
        alphaBackground = ta.getFloat(R.styleable.SpotView_background_alpha, 1f);
        ta.recycle();

        this.init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_spot, this);

        bigCategoryIcon = (ImageView) findViewById(R.id.big_image_category_spot);
        smallCategoryIcon = (ImageView) findViewById(R.id.small_image_category_spot);
        gradientBottomView = findViewById(R.id.bottom_gradient);
        gradientTopView = findViewById(R.id.top_gradient);
        rvSpotTags = (HorizontalTagsRecyclerView) findViewById(R.id.rv_horizontal_tags);
        backgroundView = findViewById(R.id.background_view);

        backgroundView.setAlpha(alphaBackground);
        setBottomShadow(isBottomShadow);
        setTopShadow(isTopShadow);
        setTagsVisible(isTagsVisible);
    }

    public HorizontalTagsRecyclerView getRvSpotTags() {
        return rvSpotTags;
    }

    public void setBottomShadow(boolean isVisible) {
        if(isVisible) {
            gradientBottomView.setVisibility(VISIBLE);
        } else {
            gradientBottomView.setVisibility(GONE);
        }
    }

    public void setTopShadow(boolean isVisible) {
        if(isVisible) {
            gradientTopView.setVisibility(VISIBLE);
        } else {
            gradientTopView.setVisibility(GONE);
        }
    }

    public void setTagsVisible(boolean tagsVisibility) {
        if(tagsVisibility) {
            rvSpotTags.setVisibility(VISIBLE);
            bigCategoryIcon.setVisibility(VISIBLE);
            smallCategoryIcon.setVisibility(GONE);
        } else {
            rvSpotTags.setVisibility(GONE);
            bigCategoryIcon.setVisibility(GONE);
            smallCategoryIcon.setVisibility(VISIBLE);
        }
    }

    public void setSpot(Spot spot) {
        //TODO : set spot

        HorizontalTagsAdapter htAdapter = rvSpotTags.getAdapter();
        htAdapter.setData(spot.tags);
    }
}
