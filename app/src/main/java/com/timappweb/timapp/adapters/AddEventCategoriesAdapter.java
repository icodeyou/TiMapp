package com.timappweb.timapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddPlaceActivity;
import com.timappweb.timapp.entities.Category;

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
        final AddPlaceActivity addPlaceActivity = (AddPlaceActivity) context;
        final Category category = MyApplication.getEventCategories().get(position);
        final ImageView categoryIcon = holder.categoryIcon;

        if(position==0) {
            categoryIcon.setImageResource(category.getIconWhiteResId());
            categoryIcon.setBackgroundResource(R.drawable.b4);
            currentCategoryIcon = categoryIcon;
        }
        else {
            categoryIcon.setImageResource(category.getIconBlackResId());
            categoryIcon.setBackground(null);
        }

        holder.getItemView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlaceActivity.getViewPager().setCurrentItem(position);
            }
        });
    }

    public void setIconNewCategory(AddPlaceActivity addPlaceActivity, Category newCategory) {
        //Set image to normal for old selected category
        Category oldCategorySelected = addPlaceActivity.getCategorySelected();
        int oldCategoryResource = oldCategorySelected.getIconBlackResId();
        currentCategoryIcon.setImageResource(oldCategoryResource);
        currentCategoryIcon.setBackground(null);

        //Set image to highlight for new selected category
        int newCategoryResource = newCategory.getIconWhiteResId();
        ImageView iconNewCategory = getIconFromId(newCategory.id);
        iconNewCategory.setImageResource(newCategoryResource);
        iconNewCategory.setBackgroundResource(R.drawable.b4);

        currentCategoryIcon = iconNewCategory;

        //set selected category in AddPlaceActivity
        addPlaceActivity.setCategory(newCategory);
    }


}