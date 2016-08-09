package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder> {

    protected final boolean isLegend;
    private final LayoutInflater inflater;
    private OnItemAdapterClickListener mClickListener;

    public CategoriesAdapter(Context context, boolean isLegend) {
        this.isLegend = isLegend;
        inflater = LayoutInflater.from(context);

        //this.eventCategories = MyApplication.getEventCategories();
    }

    @Override
    public EventCategoriesAdapter.CategoriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View categories_view = inflater.inflate(R.layout.item_category, parent, false);

        return new CategoriesViewHolder(categories_view);
    }

    @Override
    public void onBindViewHolder(EventCategoriesAdapter.CategoriesViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public void setOnItemClickListener(OnItemAdapterClickListener onClickListener) {
        this.mClickListener = onClickListener;
    }

    class CategoriesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView categoryIcon;
        TextView categoryText;

        public CategoriesViewHolder(View view) {
            super(view);
            categoryIcon = (ImageView) view.findViewById(R.id.category_icon);
            categoryText = (TextView) view.findViewById(R.id.category_name);
            view.setOnClickListener(this);
        }

        public View getItemView() {
            return itemView;
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null) {
                mClickListener.onClick(getAdapterPosition());
            }
        }
    }
}
