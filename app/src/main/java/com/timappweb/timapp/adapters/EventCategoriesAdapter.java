package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddEventActivity;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.HashMap;

public class EventCategoriesAdapter extends RecyclerView.Adapter<EventCategoriesAdapter.CategoriesViewHolder> {

    protected LayoutInflater inflater;
    //protected List<EventCategory> eventCategories = Collections.emptyList();
    protected HashMap<Integer, ImageView> icons = new HashMap<>();

    private ImageView currentCategoryIcon;

    private OnItemAdapterClickListener mClickListener;

    protected static int NUMBER_MAIN_CATEGORIES;

    private Context context;

    public EventCategoriesAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        //this.eventCategories = MyApplication.getEventCategories();
        this.context = context;

        AddEventActivity addEventActivity = (AddEventActivity) context;
        NUMBER_MAIN_CATEGORIES = addEventActivity.getNumberOfMainCategories();
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
        final EventCategory eventCategory;
        eventCategory = ConfigurationProvider.eventCategories().get(position);
        final ImageView categoryIcon = holder.categoryIcon;
        final TextView categoryText = holder.categoryText;
        icons.put(eventCategory.remote_id, categoryIcon);

        categoryIcon.setImageResource(eventCategory.getIconWhiteResId());
        categoryText.setText(eventCategory.getName());

        /*if (spotCategory.equals(addSpotActivity.getCategorySelected())) {
            holder.itemView.setBackgroundResource(R.color.silver);
        } else {
            holder.itemView.setBackground(null);
        }*/
    }

    private ImageView getIconFromId(int categoryId) {
        return icons.get(categoryId);
    }

    @Override
    public int getItemCount() {
        return ConfigurationProvider.eventCategories().size();
    }

    public EventCategory getCategory(int position) {
        return ConfigurationProvider.eventCategories().get(position);
    }

    public void setOnItemClickListener(OnItemAdapterClickListener onClickListener) {
        this.mClickListener = onClickListener;
    }

    public void setIconNewCategory(AddEventActivity addEventActivity, EventCategory newEventCategory) {
        //Set image to normal for old selected category
        EventCategory oldEventCategorySelected = addEventActivity.getEventCategorySelected();
        int oldCategoryResource = oldEventCategorySelected.getIconBlackResId();
        currentCategoryIcon.setImageResource(oldCategoryResource);
        currentCategoryIcon.setBackground(null);

        //Set image to highlight for new selected category
        int newCategoryResource = newEventCategory.getIconWhiteResId();
        ImageView iconNewCategory = getIconFromId(newEventCategory.remote_id);
        iconNewCategory.setImageResource(newCategoryResource);
        iconNewCategory.setBackgroundResource(R.drawable.b4);

        currentCategoryIcon = iconNewCategory;

        //set selected category in AddEventActivity
        addEventActivity.setCategory(newEventCategory);
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