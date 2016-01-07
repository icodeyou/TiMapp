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

import java.util.Collections;
import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.MyViewHolder> {

    protected LayoutInflater inflater;
    protected List<Category> data = Collections.emptyList();

    public CategoriesAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.data = MyApplication.mapNameToCategory;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View saved_tags_view = inflater.inflate(R.layout.item_category, parent, false);
        MyViewHolder holder = new MyViewHolder(saved_tags_view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Category current = data.get(position);
        holder.categoryIcon.setImageResource(current.getIconId());
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    public void setData(List<Category> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }

    public Category getData(int position) {
        return this.data.get(position);
    }

    public List<Category> getData() {
        return this.data;
    }


    public void removeData(int position) {
        this.data.remove(position);
        this.notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIcon;

        public MyViewHolder(View view) {
            super(view);
            categoryIcon = (ImageView) view.findViewById(R.id.category_icon);
        }
    }
}