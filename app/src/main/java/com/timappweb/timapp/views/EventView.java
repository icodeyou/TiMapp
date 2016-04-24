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
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.exceptions.UnknownCategoryException;

import me.grantland.widget.AutofitTextView;

public class EventView extends RelativeLayout{
    private final static String TAG = "EventView";

    private AutofitTextView             tvName;
    private TextView                    tvTime;
    private HorizontalTagsRecyclerView  rvPlaceTags;
    private ImageView                   categoryIcon;
    private ImageView                   backgroundImage;
    private SimpleTimerView             tvCountPoints;
    private View                        gradientBottomView;
    private View                        gradientTopView;
    private RelativeLayout              mainBox;

    private int                         colorRes = -1;
    private boolean                     isTagsVisible = false;
    private boolean                     isBottomShadow = false;
    private boolean                     isTopShadow = false;
    private boolean                     isPadding;
    private Place                       place;

    public EventView(Context context) {
        super(context);
        this.init();
    }

    public EventView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Get attributes in XML
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EventView, 0, 0);
        isTagsVisible = ta.getBoolean(R.styleable.EventView_tags_visible, false);
        isBottomShadow = ta.getBoolean(R.styleable.EventView_bottom_shadow, false);
        isTopShadow = ta.getBoolean(R.styleable.EventView_top_shadow, false);
        colorRes = ta.getColor(R.styleable.EventView_background_color, -1);
        isPadding = ta.getBoolean(R.styleable.EventView_is_padding, false);
        ta.recycle();

        this.init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_event, this);

        mainBox = (RelativeLayout) findViewById(R.id.main_box_relative);
        tvName = (AutofitTextView) findViewById(R.id.title_event);
        tvCountPoints = (SimpleTimerView) findViewById(R.id.places_points);
        tvTime = (TextView) findViewById(R.id.time_place);
        categoryIcon = (ImageView) findViewById(R.id.image_category_place);
        backgroundImage = (ImageView) findViewById(R.id.background_image_event);
        gradientBottomView = findViewById(R.id.bottom_gradient);
        gradientTopView = findViewById(R.id.top_gradient);
        rvPlaceTags = (HorizontalTagsRecyclerView) findViewById(R.id.rv_horizontal_tags);

        setBottomShadow(isBottomShadow);
        setTopShadow(isTopShadow);
        setTagsVisible(isTagsVisible);
        initPadding(isPadding);
    }

    public HorizontalTagsRecyclerView getRvPlaceTags() {
        return rvPlaceTags;
    }

    public void setPlace(Place place) {
        this.place = place;

        //Date
        tvTime.setText(place.getTime());

        //Title
        tvName.setText(place.name);

        //EventCategory
        EventCategory eventCategory = null;
        if(colorRes != -1) {
            Log.d(TAG,"Setting custom color");
            backgroundImage.setBackgroundResource(colorRes);
        } else {
            try {
                Log.d(TAG,"Setting event Background");
                //EventCategory Icon
                eventCategory = MyApplication.getCategoryById(place.category_id);
                categoryIcon.setImageResource(eventCategory.getIconWhiteResId());
                MyApplication.setCategoryBackground(categoryIcon, place.getLevel());

                //Place background
                backgroundImage.setImageResource(eventCategory.getBigImageResId());
            } catch (UnknownCategoryException e) {
                Log.e(TAG, "no eventCategory found for id : " + place.category_id);
            }
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

    public void initPadding(boolean isPadding) {
        if(!isPadding) {
            Log.d(TAG,"Removing padding");
            mainBox.setPadding(0,0,0,0);
        }
    }

    public Place getPlace() {
        return place;
    }
}
