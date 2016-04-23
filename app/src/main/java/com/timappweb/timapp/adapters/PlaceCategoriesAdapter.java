package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Category;

import java.util.HashMap;

public class PlaceCategoriesAdapter extends RecyclerView.Adapter<PlaceCategoriesAdapter.CategoriesViewHolder> {

    protected LayoutInflater inflater;
    //protected List<Category> eventCategories = Collections.emptyList();
    protected HashMap<Integer, ImageView> icons = new HashMap<>();

    private Context context;

    public PlaceCategoriesAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        //this.eventCategories = MyApplication.getEventCategories();
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
        final Category category = MyApplication.getEventCategories().get(position);
        final ImageView categoryIcon = holder.categoryIcon;
        icons.put(category.id, categoryIcon);
    }

    public ImageView getIconFromId(int categoryId) {
        return icons.get(categoryId);
    }

    @Override
    public int getItemCount() {
        return MyApplication.getEventCategories().size();
    }

    public Category getCategory(int position) {
        return MyApplication.getEventCategories().get(position);
    }

    class CategoriesViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIcon;

        public CategoriesViewHolder(View view) {
            super(view);
            categoryIcon = (ImageView) view.findViewById(R.id.category_icon);
        }

        public View getItemView() {
            return itemView;
        }
    }

}