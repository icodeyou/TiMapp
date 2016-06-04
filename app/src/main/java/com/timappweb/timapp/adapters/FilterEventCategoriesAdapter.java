package com.timappweb.timapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.activities.FilterActivity;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.models.EventCategory;

public class FilterEventCategoriesAdapter extends EventCategoriesAdapter {

    protected LayoutInflater inflater;

    private Context context;

    private FilterActivity filterActivity;

    public FilterEventCategoriesAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public CategoriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        filterActivity= (FilterActivity) context;
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(EventCategoriesAdapter.CategoriesViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        final EventCategory eventCategory = ConfigurationProvider.eventCategories().get(position);
        final ImageView categoryIcon = holder.categoryIcon;

        //final List<EventCategory> categoriesSelected = filterActivity.getCategoriesSelected();

/*
        if(categoriesSelected.contains(eventCategory)) {
            categoryIcon.setImageResource(eventCategory.resourceWhite);
            categoryIcon.setBackgroundResource(R.drawable.b4);
        } else {
            categoryIcon.setImageResource(eventCategory.resourceBlack);
            categoryIcon.setBackground(null);
        }
        holder.getItemView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(categoriesSelected.contains(eventCategory)) {
                    filterActivity.unselectCategory(eventCategory);
                    notifyDataSetChanged();
                } else {
                    filterActivity.selectCategory(eventCategory);
                    notifyDataSetChanged();
                }
            }
        });*/
    }

}