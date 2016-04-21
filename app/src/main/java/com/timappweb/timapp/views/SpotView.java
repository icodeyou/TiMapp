package com.timappweb.timapp.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Spot;

public class SpotView extends LinearLayout{
    private final static String TAG = "SpotView";

    private HorizontalTagsRecyclerView  rvSpotTags;
    private ImageView                   categoryIcon;
    private ImageView                   parentLayout;
    private SimpleTimerView             tvCountPoints;
    private View                        gradientBottomView;
    private View                        gradientTopView;

    private boolean                     isTagsVisible = false;
    private boolean                     isBottomShadow = false;
    private boolean                     isTopShadow = false;

    public SpotView(Context context) {
        super(context);
        this.init();
    }

    public SpotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Get attributes in XML
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PlaceView, 0, 0);
        isTagsVisible = ta.getBoolean(R.styleable.PlaceView_tags_visible, false);
        isBottomShadow = ta.getBoolean(R.styleable.PlaceView_bottom_shadow, false);
        isTopShadow = ta.getBoolean(R.styleable.PlaceView_top_shadow, false);
        ta.recycle();

        this.init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_spot, this);

        categoryIcon = (ImageView) findViewById(R.id.image_category_spot);
        gradientBottomView = findViewById(R.id.bottom_gradient);
        gradientTopView = findViewById(R.id.top_gradient);
        rvSpotTags = (HorizontalTagsRecyclerView) findViewById(R.id.rv_horizontal_tags);

        setBottomShadow(isBottomShadow);
        setTopShadow(isTopShadow);
        setTagsVisible(isTagsVisible);
    }

    public HorizontalTagsRecyclerView getRvSpotTags() {
        return rvSpotTags;
    }

    /*public void setSpot(Spot spot) {

    }*/

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
        } else {
            rvSpotTags.setVisibility(GONE);
        }
    }

    public void setSpot(Spot spot) {
        //TODO : set spot

        HorizontalTagsAdapter htAdapter = rvSpotTags.getAdapter();
        htAdapter.setData(spot.tags);


    }
}
