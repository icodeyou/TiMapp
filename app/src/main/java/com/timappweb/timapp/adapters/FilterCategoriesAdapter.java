package com.timappweb.timapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.FilterActivity;
import com.timappweb.timapp.entities.Category;

import java.util.List;

public class FilterCategoriesAdapter extends CategoriesAdapter {

    protected LayoutInflater inflater;

    private Context context;

    private FilterActivity filterActivity;

    public FilterCategoriesAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public CategoriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        filterActivity= (FilterActivity) context;
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(CategoriesAdapter.CategoriesViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        final Category category = categories.get(position);
        final ImageView categoryIcon = holder.categoryIcon;

        final List<Category> categoriesSelected = filterActivity.getCategoriesSelected();

        if(categoriesSelected.contains(category)) {
            categoryIcon.setImageResource(category.resourceWhite);
            categoryIcon.setBackgroundResource(R.drawable.b4);
        } else {
            categoryIcon.setImageResource(category.resourceBlack);
            categoryIcon.setBackground(null);
        }

        holder.getItemView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(categoriesSelected.contains(category)) {
                    filterActivity.unselectCategory(category);
                    notifyDataSetChanged();
                } else {
                    filterActivity.selectCategory(category);
                    notifyDataSetChanged();
                }
            }
        });
    }

}