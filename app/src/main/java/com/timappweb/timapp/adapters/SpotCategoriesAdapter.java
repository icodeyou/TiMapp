package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddSpotActivity;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.ArrayList;
import java.util.List;

public class SpotCategoriesAdapter extends RecyclerView.Adapter<SpotCategoriesAdapter.SpotCategoriesViewHolder> {
    private static final String TAG = "SpotCategoriesAdapter";
    private final AddSpotActivity addSpotActivity;
    private Context context;

    private List<SpotCategory> data;
    private ImageView currentCategoryIcon;
    private OnItemAdapterClickListener itemAdapterClickListener;

    public SpotCategoriesAdapter(Context context) {
        this.data = new ArrayList<>();
        this.context = context;
        this.addSpotActivity = (AddSpotActivity) context;
    }

    @Override
    public SpotCategoriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spot_category, parent, false);
        return new SpotCategoriesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SpotCategoriesViewHolder holder, int position) {
        Log.d(TAG, "Get view for " + (position + 1) + "/" + getItemCount());
        final SpotCategory spotCategory = ConfigurationProvider.spotCategories().get(position);
        holder.tvCategory.setText(spotCategory.name);

        if (spotCategory.equals(addSpotActivity.getCategorySelected())) {
            holder.itemView.setBackgroundResource(R.color.silver);
        } else {
            holder.itemView.setBackground(null);
        }
    }

    @Override
    public int getItemCount() {
        return ConfigurationProvider.spotCategories().size();
    }

    public List<SpotCategory> getData() {
        return ConfigurationProvider.spotCategories();
    }

    public SpotCategory getCategory(int position) {
        return ConfigurationProvider.spotCategories().get(position);
    }

    public void add(SpotCategory spotCategory) {
        this.data.add(spotCategory);
        notifyDataSetChanged();
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public void setItemAdapterClickListener(OnItemAdapterClickListener itemAdapterClickListener) {
        this.itemAdapterClickListener = itemAdapterClickListener;
    }

    public void addAll(List<SpotCategory> spotCategories) {
        this.data.addAll(spotCategories);
    }

    public class SpotCategoriesViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        TextView tvCategory;
        ImageView icCategory;

        SpotCategoriesViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvCategory = (TextView) itemView.findViewById(R.id.text);
            icCategory = (ImageView) itemView.findViewById(R.id.icon);
        }

        @Override
        public void onClick(View v) {
            if (itemAdapterClickListener != null) {
                itemAdapterClickListener.onClick(getAdapterPosition());
            }
        }
    }
}