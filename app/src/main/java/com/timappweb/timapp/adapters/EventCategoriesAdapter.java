package com.timappweb.timapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.activities.AddEventActivity;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.utils.Util;

import java.util.HashMap;

public class EventCategoriesAdapter extends CategoriesAdapter {

    protected LayoutInflater inflater;
    //protected List<EventCategory> eventCategories = Collections.emptyList();
    protected HashMap<Integer, ImageView> icons = new HashMap<>();

    private ImageView currentCategoryIcon;

    private OnItemAdapterClickListener mClickListener;

    protected static int NUMBER_MAIN_CATEGORIES;

    private Context context;

    public EventCategoriesAdapter(Context context, boolean isLegend) {
        super(context,isLegend);
        this.context = context;

        AddEventActivity addEventActivity = (AddEventActivity) context;
        NUMBER_MAIN_CATEGORIES = addEventActivity.getNumberOfMainCategories();
    }

    @Override
    public void onBindViewHolder(CategoriesViewHolder holder, final int position) {
        final EventCategory eventCategory;
        eventCategory = ConfigurationProvider.eventCategories().get(position);
        final ImageView categoryIcon = holder.categoryIcon;
        final TextView categoryText = holder.categoryText;
        icons.put(eventCategory.remote_id, categoryIcon);

        categoryIcon.setImageDrawable(eventCategory.getIconDrawable(this.context));
        if(isLegend) {
            String capitalizedName = Util.capitalize(eventCategory.getName());
            categoryText.setText(capitalizedName);
        } else {
            categoryText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if(!isLegend) {
            return NUMBER_MAIN_CATEGORIES;
        } else {
            return ConfigurationProvider.eventCategories().size();
        }
    }

    public EventCategory getCategory(int position) {
        return ConfigurationProvider.eventCategories().get(position);
    }

}