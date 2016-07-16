package com.timappweb.timapp.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.activities.AddEventActivity;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.models.EventCategory;

public class MainEventCategoriesAdapter extends EventCategoriesAdapter {

    public MainEventCategoriesAdapter(Context context) {
        super(context);
    }

    @Override
    public int getItemCount() {
        return NUMBER_MAIN_CATEGORIES;
    }

    @Override
    public void onBindViewHolder(CategoriesViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        final TextView categoryText = holder.categoryText;
        categoryText.setVisibility(View.GONE);
    }


}
