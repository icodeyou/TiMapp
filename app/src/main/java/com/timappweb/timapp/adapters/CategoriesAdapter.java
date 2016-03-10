package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddPlaceActivity;
import com.timappweb.timapp.entities.Category;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder> {

    protected LayoutInflater inflater;
    protected List<Category> categories = Collections.emptyList();
    protected HashMap<Integer, ImageView> icons = new HashMap<>();

    private Context context;
    private ImageView currentCategoryIcon;

    public CategoriesAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.categories = MyApplication.categories;
        this.context = context;
    }

    @Override
    public CategoriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View categories_view = inflater.inflate(R.layout.item_category, parent, false);

        //set holder
        CategoriesViewHolder holder = new CategoriesViewHolder(categories_view);
        return holder;
    }

    @Override
    public void onBindViewHolder(CategoriesViewHolder holder, final int position) {
        final AddPlaceActivity addPlaceActivity = (AddPlaceActivity) context;
        final Category category = categories.get(position);
        final ImageView categoryIcon = holder.newCategoryIcon;
        icons.put(category.id, categoryIcon);

        if(position==0) {
            categoryIcon.setImageResource(category.resourceWhite);
            categoryIcon.setBackgroundResource(R.drawable.b4);
            currentCategoryIcon = categoryIcon;
        }
        else {
            categoryIcon.setImageResource(category.resourceBlack);
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
        int oldCategoryResource = oldCategorySelected.resourceBlack;
        currentCategoryIcon.setImageResource(oldCategoryResource);
        currentCategoryIcon.setBackground(null);

        //Set image to highlight for new selected category
        int newCategoryResource = newCategory.resourceWhite;
        ImageView iconNewCategory = getIconFromId(newCategory.id);
        iconNewCategory.setImageResource(newCategoryResource);
        iconNewCategory.setBackgroundResource(R.drawable.b4);

        currentCategoryIcon = iconNewCategory;

        //set selected category in AddPlaceActivity
        addPlaceActivity.setCategory(newCategory);
    }

    public ImageView getIconFromId(int categoryId) {
        return icons.get(categoryId);
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        this.notifyDataSetChanged();
    }


    public Category getCategory(int position) {
        return this.categories.get(position);
    }

    class CategoriesViewHolder extends RecyclerView.ViewHolder {
        ImageView newCategoryIcon;

        public CategoriesViewHolder(View view) {
            super(view);
            newCategoryIcon = (ImageView) view.findViewById(R.id.category_icon);
        }

        public View getItemView() {
            return itemView;
        }

        public View getCategoryIcon() {
            return newCategoryIcon;
        }
    }

}