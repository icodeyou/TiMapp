package com.timappweb.timapp.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Spot;

import me.grantland.widget.AutofitTextView;

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
    private LinearLayout                mainHorizontalLayout;

    private boolean                     isTagsVisible = false;
    private boolean                     isBottomShadow = false;
    private boolean                     isTopShadow = false;
    private TextView                    tvName;
    private boolean                     isGravityCentered;

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
        colorRes = ta.getColor(R.styleable.SpotView_background_color, Color.BLACK);
        isGravityCentered = ta.getBoolean(R.styleable.SpotView_gravity_center, false);
        ta.recycle();

        this.init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_spot, this);

        mainHorizontalLayout = (LinearLayout) findViewById(R.id.horizontal_linear_layout);
        bigCategoryIcon = (ImageView) findViewById(R.id.big_image_category_spot);
        smallCategoryIcon = (ImageView) findViewById(R.id.small_image_category_spot);
        gradientBottomView = findViewById(R.id.bottom_gradient);
        gradientTopView = findViewById(R.id.top_gradient);
        rvSpotTags = (HorizontalTagsRecyclerView) findViewById(R.id.rv_horizontal_tags);
        tvName = (AutofitTextView) findViewById(R.id.title_spot);

        mainHorizontalLayout.setBackgroundColor(colorRes);
        setBottomShadow(isBottomShadow);
        setTopShadow(isTopShadow);
        setTagsVisible(isTagsVisible);
        if(isGravityCentered) {
            mainHorizontalLayout.setGravity(Gravity.CENTER);
        }
    }

    public HorizontalTagsRecyclerView getRvSpotTags() {
        return rvSpotTags;
    }

    public void setBottomShadow(boolean isVisible) {
        if(isVisible) {
            Log.d(TAG, "bottom shadow is visible");
            gradientBottomView.setVisibility(VISIBLE);
        } else {
            Log.d(TAG, "bottom shadow is not visible");
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
        HorizontalTagsAdapter htAdapter = rvSpotTags.getAdapter();
        htAdapter.setData(spot.tags);
        tvName.setText(spot.name);
    }
}
