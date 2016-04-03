package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.AutoResizeTextView;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;
import com.timappweb.timapp.views.SimpleTimerView;

import java.util.ArrayList;
import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "PlacesAdapter";
    private Context context;
    private int colorRes = -1;
    private boolean isTagsVisible;
    private boolean footerActive;

    private List<Place> data;

    private OnItemAdapterClickListener itemAdapterClickListener;

    private class VIEW_TYPES {
        public static final int NORMAL = 1;
        public static final int FOOTER = 2;
    }

    @Override
    public int getItemViewType(int position) {

        if(isPositionFooter(position))
            return VIEW_TYPES.FOOTER;
        else
            return VIEW_TYPES.NORMAL;

    }

    private boolean isPositionFooter(int position) {
        return position == data.size() && footerActive;
    }

    public PlacesAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
        this.footerActive = false;
        this.isTagsVisible = true;
    }

    public PlacesAdapter(Context context, boolean footerActive) {
        data = new ArrayList<>();
        this.context = context;
        this.footerActive = footerActive;
        this.isTagsVisible = true;
    }

    public PlacesAdapter(Context context, boolean footerActive, boolean isTagsVisible) {
        data = new ArrayList<>();
        this.context = context;
        this.footerActive = footerActive;
        this.isTagsVisible = isTagsVisible;
    }

    public PlacesAdapter(Context context, boolean isTagsVisible, int colorRes) {
        data = new ArrayList<>();
        this.context = context;
        this.footerActive = false;
        this.isTagsVisible = isTagsVisible;
        this.colorRes = colorRes;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;

        switch (viewType)
        {
            case VIEW_TYPES.NORMAL:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_place, viewGroup, false);
                return new PlacesViewHolder(v);
            case VIEW_TYPES.FOOTER:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.explore_places_button, viewGroup, false);
                return new FooterPlacesViewHolder(v);
            default:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_place, viewGroup, false);
                return new PlacesViewHolder(v);
        }


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder baseHolder, int position) {
        if(baseHolder instanceof PlacesViewHolder) {
            PlacesViewHolder holder = (PlacesViewHolder) baseHolder;
            Log.d(TAG, "Get view for " + (position+1) + "/" + getItemCount());
            final Place place = data.get(position);

            //Set texts
            holder.tvTime.setText(place.getTime());
            holder.tvLocation.setText(place.name);
            initTimer(place, holder.tvCountPoints);

            Category category = null;
            try {
                //Category Icon
                category = MyApplication.getCategoryById(place.category_id);
                holder.categoryIcon.setImageResource(category.getIconWhiteResId());
                MyApplication.setCategoryBackground(holder.categoryIcon, place.getLevel());

                //Place background
                if (colorRes == -1) {
                    holder.parentLayout.setVisibility(View.VISIBLE);
                    holder.parentLayout.setImageResource(category.getSmallImageResId());
                    holder.colorBackground.setBackgroundResource(R.color.background_place);
                } else {
                    holder.parentLayout.setVisibility(View.GONE);
                    holder.colorBackground.setBackgroundResource(colorRes);
                }
            } catch (UnknownCategoryException e) {
                Log.e(TAG, "no category found for id : " + place.category_id);
            }

            if (isTagsVisible) {
                //Set the adapter for RV
                HorizontalTagsAdapter htAdapter = holder.rvPlaceTags.getAdapter();
                htAdapter.setData(place.tags);
            } else {
                holder.rvPlaceTags.setVisibility(View.GONE);
            }

            //Listener Horizontal Scroll View
            HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                    new HorizontalTagsTouchListener(context, itemAdapterClickListener, position);
            holder.rvPlaceTags.setOnTouchListener(mHorizontalTagsTouchListener);
        }
    }

    @Override
    public int getItemCount() {
        if(footerActive) {
            return data.size()+1;
        } else {
            return data.size();
        }
    }

    private void initTimer(Place place, final SimpleTimerView tvCountPoints) {
        int initialTime = place.getPoints();
        tvCountPoints.initTimer(initialTime*1000);
    }

    public void add(Place place) {
        this.data.add(place);
        notifyDataSetChanged();
    }

    public void setData(List<Place> places) {
        this.data = places;
        notifyDataSetChanged();
    }

    public List<Place> getData() {
        return data;
    }

    public Place getItem(int position) {
        return data.get(position);
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }


    public void generateDummyData() {
        Place dummyPlace = Place.createDummy();
        add(dummyPlace);
        Place dummyPlace2 = Place.createDummy();
        add(dummyPlace2);
    }

    public void setItemAdapterClickListener(OnItemAdapterClickListener itemAdapterClickListener) {
        this.itemAdapterClickListener = itemAdapterClickListener;
    }

    public class PlacesViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        private final View colorBackground;
        private final AutoResizeTextView tvLocation;
        private final TextView tvTime;
        private final HorizontalTagsRecyclerView rvPlaceTags;
        private final ImageView categoryIcon;
        private final ImageView parentLayout;
        private final SimpleTimerView tvCountPoints;

        PlacesViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            colorBackground = itemView.findViewById(R.id.parent_layout_place);
            tvLocation = (AutoResizeTextView) itemView.findViewById(R.id.title_place);
            tvCountPoints = (SimpleTimerView) itemView.findViewById(R.id.places_points);
            tvTime = (TextView) itemView.findViewById(R.id.time_place);
            rvPlaceTags = (HorizontalTagsRecyclerView) itemView.findViewById(R.id.rv_horizontal_tags);
            categoryIcon = (ImageView) itemView.findViewById(R.id.image_category_place);
            parentLayout = (ImageView) itemView.findViewById(R.id.background_place);

        }

        @Override
        public void onClick(View v) {
            if (itemAdapterClickListener != null) {
                itemAdapterClickListener.onClick(getAdapterPosition());
            }
        }
    }

    public class FooterPlacesViewHolder extends RecyclerView.ViewHolder {

        private final Button newEventButton;

        FooterPlacesViewHolder(View itemView) {
            super(itemView);
            newEventButton = (Button) itemView.findViewById(R.id.create_button);
            newEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IntentsUtils.addPlace(context);
                }
            });

        }
    }
}