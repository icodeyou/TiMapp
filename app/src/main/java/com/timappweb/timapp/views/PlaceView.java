package com.timappweb.timapp.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.exceptions.UnknownCategoryException;

public class PlaceView extends RelativeLayout{
    private final static String TAG = "PlaceView";

    private View                        colorBackground;
    private AutoResizeTextView          tvLocation;
    private TextView                    tvTime;
    private HorizontalTagsRecyclerView  rvPlaceTags;
    private ImageView                   categoryIcon;
    private ImageView                   parentLayout;
    private SimpleTimerView             tvCountPoints;
    private View                        gradientBottomView;
    private View                        gradientTopView;

    private int                         colorRes = -1;
    private boolean                     isTagsVisible = false;
    private boolean                     isBottomShadow = false;
    private boolean                     isTopShadow = false;
    private Place                       place;

    public PlaceView(Context context) {
        super(context);
        this.init();
    }

    public PlaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Get attributes in XML
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PlaceView, 0, 0);
        isTagsVisible = ta.getBoolean(R.styleable.PlaceView_tags_visible, false);
        isBottomShadow = ta.getBoolean(R.styleable.PlaceView_bottom_shadow, false);
        isTopShadow = ta.getBoolean(R.styleable.PlaceView_top_shadow, false);
        colorRes = ta.getColor(R.styleable.PlaceView_background_color, -1);
        ta.recycle();

        this.init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_place, this);

        colorBackground = findViewById(R.id.parent_layout_place);
        tvLocation = (AutoResizeTextView) findViewById(R.id.title_place);
        tvCountPoints = (SimpleTimerView) findViewById(R.id.places_points);
        tvTime = (TextView) findViewById(R.id.time_place);
        categoryIcon = (ImageView) findViewById(R.id.image_category_place);
        parentLayout = (ImageView) findViewById(R.id.background_place);
        gradientBottomView = findViewById(R.id.bottom_gradient);
        gradientTopView = findViewById(R.id.top_gradient);
        rvPlaceTags = (HorizontalTagsRecyclerView) findViewById(R.id.rv_horizontal_tags);

        setBottomShadow(isBottomShadow);
        setTopShadow(isTopShadow);
        setTagsVisible(isTagsVisible);
    }

    public HorizontalTagsRecyclerView getRvPlaceTags() {
        return rvPlaceTags;
    }

    public void setPlace(Place place) {
        this.place = place;

        //Date
        tvTime.setText(place.getTime());

        //Title
        tvLocation.setText(place.name);

        //Category
        Category category = null;
        try {
            //Category Icon
            category = MyApplication.getCategoryById(place.category_id);
            categoryIcon.setImageResource(category.getIconWhiteResId());
            MyApplication.setCategoryBackground(categoryIcon, place.getLevel());

            //Place background
            if (colorRes == -1) {
                parentLayout.setVisibility(View.VISIBLE);
                parentLayout.setImageResource(category.getSmallImageResId());
                colorBackground.setBackgroundResource(R.color.background_place);
            } else {
                parentLayout.setVisibility(View.GONE);
                colorBackground.setBackgroundResource(colorRes);
            }
        } catch (UnknownCategoryException e) {
            Log.e(TAG, "no category found for id : " + place.category_id);
        }

        //Adapter
        HorizontalTagsAdapter htAdapter = rvPlaceTags.getAdapter();
        htAdapter.setData(place.tags);

        //Counter
        int initialTime = place.getPoints();
        tvCountPoints.initTimer(initialTime * 1000);
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
            rvPlaceTags.setVisibility(VISIBLE);
        } else {
            rvPlaceTags.setVisibility(GONE);
        }
    }

    public Place getPlace() {
        return place;
    }
}
