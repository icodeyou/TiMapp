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
import com.timappweb.timapp.activities.AddPlaceActivity;
import com.timappweb.timapp.activities.AddSpotActivity;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.Spot;
import com.timappweb.timapp.entities.SpotCategory;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.ArrayList;
import java.util.List;

public class SpotCategoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spot_category, parent, false);
        return new SpotCategoriesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        SpotCategoriesViewHolder holder = (SpotCategoriesViewHolder) viewHolder;
        Log.d(TAG, "Get view for " + (position+1) + "/" + getItemCount());

        final SpotCategory spotCategory = MyApplication.getSpotCategories().get(position);

        holder.tvCategory.setText(spotCategory.name);
        //holder.icCategory.setImageResource(spotCategory.resource);

        if(addSpotActivity.getCategorySelected() != null && addSpotActivity.getCategorySelected()==spotCategory) {
            holder.itemView.setBackgroundResource(R.color.colorSecondary);
        } else {
            holder.itemView.setBackground(null);
        }
    }

    @Override
    public int getItemCount() {
        return MyApplication.getSpotCategories().size();
    }

    public List<SpotCategory> getData() {
        return MyApplication.getSpotCategories();
    }

    public SpotCategory getCategory(int position) {
        return MyApplication.getSpotCategories().get(position);
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