package com.timappweb.timapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddEventActivity;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.models.EventCategory;

public class AddEventCategoriesAdapter extends EventCategoriesAdapter {

    protected LayoutInflater inflater;

    private Context context;
    private ImageView currentCategoryIcon;

    public AddEventCategoriesAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void onBindViewHolder(EventCategoriesAdapter.CategoriesViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        final AddEventActivity addEventActivity = (AddEventActivity) context;
        final EventCategory eventCategory = ConfigurationProvider.eventCategories().get(position);
        final ImageView categoryIcon = holder.categoryIcon;

        if(position==0) {
            categoryIcon.setImageResource(eventCategory.getIconWhiteResId());
            categoryIcon.setBackgroundResource(R.drawable.b4);
            currentCategoryIcon = categoryIcon;
        }
        else {
            categoryIcon.setImageResource(eventCategory.getIconBlackResId());
            categoryIcon.setBackground(null);
        }

        holder.getItemView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEventActivity.getViewPager().setCurrentItem(position);
            }
        });
    }

    public void setIconNewCategory(AddEventActivity addEventActivity, EventCategory newEventCategory) {
        //Set image to normal for old selected category
        EventCategory oldEventCategorySelected = addEventActivity.getEventCategorySelected();
        int oldCategoryResource = oldEventCategorySelected.getIconBlackResId();
        currentCategoryIcon.setImageResource(oldCategoryResource);
        currentCategoryIcon.setBackground(null);

        //Set image to highlight for new selected category
        int newCategoryResource = newEventCategory.getIconWhiteResId();
        ImageView iconNewCategory = getIconFromId(newEventCategory.remote_id);
        iconNewCategory.setImageResource(newCategoryResource);
        iconNewCategory.setBackgroundResource(R.drawable.b4);

        currentCategoryIcon = iconNewCategory;

        //set selected category in AddEventActivity
        addEventActivity.setCategory(newEventCategory);
    }


}