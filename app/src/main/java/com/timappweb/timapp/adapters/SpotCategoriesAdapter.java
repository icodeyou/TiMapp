package com.timappweb.timapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.activities.AddEventActivity;
import com.timappweb.timapp.activities.AddSpotActivity;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.utils.Util;

import java.util.HashMap;

public class SpotCategoriesAdapter extends CategoriesAdapter {

    protected LayoutInflater inflater;
    //protected List<EventCategory> eventCategories = Collections.emptyList();
    protected HashMap<Integer, ImageView> icons = new HashMap<>();

    private ImageView currentCategoryIcon;

    private OnItemAdapterClickListener mClickListener;

    protected static int NUMBER_MAIN_CATEGORIES;

    private Context context;
    private static final String TAG = "SpotCategoriesAdapter";

    public SpotCategoriesAdapter(Context context, boolean isLegend) {
        super(context,isLegend);
        this.context = context;

        AddSpotActivity addSpotActivity = (AddSpotActivity) context;
        NUMBER_MAIN_CATEGORIES = addSpotActivity.getNumberOfMainCategories();
    }

    @Override
    public void onBindViewHolder(CategoriesViewHolder holder, final int position) {
        Log.d(TAG, "Get view for " + (position + 1) + "/" + getItemCount());
        Log.d(TAG,String.valueOf(isLegend));
        final SpotCategory spotCategory = ConfigurationProvider.spotCategories().get(position);
        final ImageView categoryIcon = holder.categoryIcon;
        final TextView categoryText = holder.categoryText;
        icons.put(spotCategory.remote_id, categoryIcon);

        categoryIcon.setImageResource(spotCategory.getIconWhiteResId());
        if(isLegend) {
            String nameCapitalized = Util.capitalize(spotCategory.name);
            categoryText.setText(nameCapitalized);
        } else {
            categoryText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if(!isLegend) {
            return NUMBER_MAIN_CATEGORIES;
        } else {
            return ConfigurationProvider.spotCategories().size();
        }
    }

    public SpotCategory getCategory(int position) {
        return ConfigurationProvider.spotCategories().get(position);
    }
}