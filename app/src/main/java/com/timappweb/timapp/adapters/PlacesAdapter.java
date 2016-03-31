package com.timappweb.timapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.listeners.ColorButtonOnTouchListener;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.listeners.OnItemViewRendered;
import com.timappweb.timapp.views.AutoResizeTextView;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.iwgang.countdownview.CountdownView;

public class PlacesAdapter extends ArrayAdapter<Place> {
    private static final String TAG = "PlacesAdapter";
    private static final int COEF_TRANSPARENCY = 30; // opacity equals to 255.
    private final Context context;
    private final HashMap<Place, CountDownTimer> countDownTimers = new HashMap<>();
    private HorizontalTagsRecyclerView rvPlaceTags;
    private boolean isTagsVisible;

    private OnItemAdapterClickListener itemAdapterClickListener;
    private OnItemViewRendered itemViewRendered;

    private static int countDownInterval = 30000;
    private LayoutInflater inflater;

    public PlacesAdapter(Context context) {
        super(context, R.layout.item_place);
        this.context = context;
        this.isTagsVisible = true;
    }

    public PlacesAdapter(Context context, boolean bool) {
        super(context, R.layout.item_place);
        this.context = context;
        this.isTagsVisible = bool;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //Log.d(TAG, "Get view for " + position + "/" + this.getCount());
        final Place place = this.getItem(position);

        // Get the view from inflater
        View view = convertView;
        if(convertView==null) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_place, parent, false);
        }

        // Initialize
        AutoResizeTextView tvLocation = (AutoResizeTextView) view.findViewById(R.id.title_place);
        TextView tvTime = (TextView) view.findViewById(R.id.time_place);
        CountdownView tvCountPoints = (CountdownView ) view.findViewById(R.id.people_counter_place);
        rvPlaceTags = (HorizontalTagsRecyclerView) view.findViewById(R.id.rv_horizontal_tags);
        ImageView categoryIcon = (ImageView) view.findViewById(R.id.image_category_place);

        initTimer(place, tvCountPoints);

        //Set texts
        tvTime.setText(place.getTime());
        tvLocation.setText(place.name);

        Category category = null;
        try {
            //Category Icon
            category = MyApplication.getCategoryById(place.category_id);
            categoryIcon.setImageResource(category.getIconWhiteResId());
            MyApplication.setCategoryBackground(categoryIcon, place.getLevel());

            //Place background
            ImageView imageView = (ImageView) view.findViewById(R.id.background_place);
            imageView.setImageResource(category.getImageResId());
        } catch (UnknownCategoryException e) {
            Log.e(TAG, "no category found for id : " + place.category_id);
        }

        if(isTagsVisible) {
            //Set the adapter for RV
            HorizontalTagsAdapter htAdapter = rvPlaceTags.getAdapter();
            htAdapter.setData(place.tags);
            // rvPlaceTags.setAdapter(htAdapter); Ligne à supprimer si tout marche correctement
            htAdapter.notifyDataSetChanged();

            //Set LayoutManager for RV
            GridLayoutManager manager_savedTags = new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false);
            rvPlaceTags.setLayoutManager(manager_savedTags);
        }
        else {
            rvPlaceTags.setVisibility(View.GONE);
        }

        //Listener entire view
        if (this.itemAdapterClickListener != null){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemAdapterClickListener.onClick(position);
                }
            });
        }

        //Listener Horizontal Scroll View
        HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                new HorizontalTagsTouchListener(getContext(), itemAdapterClickListener, position);
        rvPlaceTags.setOnTouchListener(mHorizontalTagsTouchListener);

        if (itemViewRendered != null){
            itemViewRendered.onItemAdded();
        }

        //return the view
        return view;
    }

    private void initTimer(Place place, final CountdownView tvCountPoints) {
        int initialTime = place.getPoints();
        tvCountPoints.start(1000 * initialTime);

        /*if(countDownTimers.get(place)==null) {
            int initialTime = place.getPoints();
            CountDownTimer countDownTimer = new CountDownTimer(initialTime, countDownInterval) {

                public void onTick(long millisUntilFinished) {
                    tvCountPoints.setText(String.valueOf(millisUntilFinished/1000));
                }

                public void onFinish() {
                    tvCountPoints.setText(R.string.counter_over);
                }
            }.start();
            countDownTimers.put(place, countDownTimer);
        }*/
    }

    @Override
    public void add(Place place) {
        super.add(place);
        super.notifyDataSetChanged();
    }

    public void generateDummyData() {
        Place dummyPlace = Place.createDummy();
        add(dummyPlace);
        Place dummyPlace2 = Place.createDummy();
        add(dummyPlace2);
    }

    public void setItemAdapterClickListener(OnItemAdapterClickListener itemAdapterClickListener) {
        this.itemAdapterClickListener = itemAdapterClickListener;
    }

    public void setItemViewRendered(OnItemViewRendered itemViewRendered) {
        this.itemViewRendered = itemViewRendered;
    }
}