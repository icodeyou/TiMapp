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
import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder> {

    protected LayoutInflater inflater;
    protected List<Category> data = Collections.emptyList();

    private Context context;
    private Category selectedCategory = null;
    private ImageView currentCategoryIcon;
    private int currentCategoryId;

    public CategoriesAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.data = MyApplication.categories;
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
        final Category newCategory = data.get(position);
        final ImageView newCategoryIcon = holder.newCategoryIcon;

        newCategoryIcon.setImageResource(newCategory.getIconId());

        holder.getItemView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set image to normal for old selected category
                if (selectedCategory != null) {
                    ImageView oldCategoryIcon = currentCategoryIcon;
                    String oldCategoryName = "ic_category_" + selectedCategory.name;
                    int oldCategoryResId = context.getResources().getIdentifier(oldCategoryName, "drawable", context.getPackageName());
                    oldCategoryIcon.setImageResource(oldCategoryResId);
                }
                //Set image to highlight for new selected category
                String newCategoryName = "ic_category_highlight_" + newCategory.name;
                int newCategoryResId = context.getResources().getIdentifier(newCategoryName, "drawable", context.getPackageName());
                newCategoryIcon.setImageResource(newCategoryResId);

                currentCategoryIcon = newCategoryIcon;
                selectedCategory = data.get(position);

                //set selectedCategory in AddPlaceActivity
                AddPlaceActivity addPlaceActivity = (AddPlaceActivity) context;
                addPlaceActivity.setCategory(selectedCategory);
                addPlaceActivity.setButtonValidation();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    public void setData(List<Category> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }

    public Category getCategory(int position) {
        return this.data.get(position);
    }

    public List<Category> getCategories() {
        return this.data;
    }

    public void removeData(int position) {
        this.data.remove(position);
        this.notifyDataSetChanged();
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
    }

    private void resetBackground() {

    }

}