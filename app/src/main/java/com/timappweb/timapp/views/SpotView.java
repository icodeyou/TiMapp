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
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Spot;
import com.timappweb.timapp.listeners.OnSpotClickListener;

import me.grantland.widget.AutofitTextView;

public class SpotView extends LinearLayout{
    private final static String TAG = "SpotView";

    //private OnSpotClickListener         onSpotClickListener;

    private HorizontalTagsRecyclerView  rvSpotTags;
    private ImageView                   bigCategoryIcon;
    private ImageView                   smallCategoryIcon;
    private RelativeLayout              parentLayout;
    private View                        gradientBottomView;
    private View                        gradientTopView;
    private LinearLayout                mainHorizontalLayout;
    private View                        editView;
    private ImageView                   editButton;
    private ImageView                        removeButton;

    private boolean                     isTagsVisible;
    private boolean                     isBottomShadow;
    private boolean                     isTopShadow;
    private AutofitTextView             tvName;
    private boolean                     isGravityCentered;
    private int                         colorRes;
    private boolean                     editMode;

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
        colorRes = ta.getColor(R.styleable.SpotView_background_color, -1);
        isGravityCentered = ta.getBoolean(R.styleable.SpotView_gravity_center, false);
        editMode = ta.getBoolean(R.styleable.SpotView_edit_mode, false);
        ta.recycle();

        this.init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_spot, this);

        parentLayout = (RelativeLayout) findViewById(R.id.parent_layout_spot);
        mainHorizontalLayout = (LinearLayout) findViewById(R.id.horizontal_linear_layout);
        bigCategoryIcon = (ImageView) findViewById(R.id.big_image_category_spot);
        smallCategoryIcon = (ImageView) findViewById(R.id.small_image_category_spot);
        gradientBottomView = findViewById(R.id.bottom_gradient);
        gradientTopView = findViewById(R.id.top_gradient);
        rvSpotTags = (HorizontalTagsRecyclerView) findViewById(R.id.rv_horizontal_tags);
        tvName = (AutofitTextView) findViewById(R.id.title_spot);
        editView = findViewById(R.id.edit_view);
        editButton = (ImageView) findViewById(R.id.ic_edit);
        removeButton = (ImageView) findViewById(R.id.ic_remove);

        parentLayout.setBackgroundColor(colorRes);
        setBottomShadow(isBottomShadow);
        setTopShadow(isTopShadow);
        setTagsVisible(isTagsVisible);
        setEditView(editMode);
        if(isGravityCentered) {
            mainHorizontalLayout.setGravity(Gravity.CENTER);
        }
    }

    public void setOnSpotClickListener(final OnSpotClickListener onSpotClickListener) {
        editButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSpotClickListener.onEditClick();
            }
        });
        removeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSpotClickListener.onRemoveClick();
            }
        });
    }

    public HorizontalTagsRecyclerView getRvSpotTags() {
        return rvSpotTags;
    }

    private void setBottomShadow(boolean isVisible) {
        if(isVisible) {
            Log.d(TAG, "bottom shadow is visible");
            gradientBottomView.setVisibility(VISIBLE);
        } else {
            Log.d(TAG, "bottom shadow is not visible");
            gradientBottomView.setVisibility(GONE);
        }
    }

    private void setTopShadow(boolean isVisible) {
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

    private void setEditView(boolean editMode) {
        if(editMode) {
            editView.setVisibility(VISIBLE);
            //prevent design issues after edits
            tvName.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        } else {
            editView.setVisibility(GONE);
        }
    }

    public void setSpot(Spot spot) {
        HorizontalTagsAdapter htAdapter = rvSpotTags.getAdapter();
        htAdapter.setData(spot.tags);
        tvName.setText(spot.name);
    }

    public void setColor(int color) {
        parentLayout.setBackgroundColor(color);
    }
}
