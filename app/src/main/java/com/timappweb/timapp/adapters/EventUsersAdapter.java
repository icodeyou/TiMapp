package com.timappweb.timapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.entities.PlaceUserInterface;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.utils.TwoDimArray;

import java.util.LinkedList;
import java.util.List;

public abstract class EventUsersAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "EventUsersAdapter";

    public class VIEW_TYPES {
        public static final int PLACEHOLDER_TOP = 0;
        public static final int HERE = 1;
        public static final int COMING = 2;
        public static final int INVITED = 3;
    }

    private OnItemAdapterClickListener mItemClickListener;
    protected Context context;
    protected TwoDimArray<SectionItem> data;

    //Constructor
    public EventUsersAdapter(Context context) {
        setHasStableIds(true);
        this.context = context;
        this.data = new TwoDimArray<>();
        this.data.create(new SectionItem<PlaceUserInterface>(UserPlaceStatusEnum.HERE, VIEW_TYPES.HERE));
        this.data.create(new SectionItem<PlaceUserInterface>(UserPlaceStatusEnum.COMING, VIEW_TYPES.COMING));
        this.data.create(new SectionItem<PlaceUserInterface>(UserPlaceStatusEnum.INVITED, VIEW_TYPES.INVITED));
    }

    public void addData(UserPlaceStatusEnum type, List<? extends PlaceUserInterface> items) {
        this.data.add(type, items);
    }

    public void addData(List<UserEvent> data) {
        for (UserEvent userEvent : data){
            this.data.addOne(userEvent.status, userEvent);
        }
    }

    public PlaceUserInterface getData(int position) {
        return data.get(position);
    }

    public void clearSection(UserPlaceStatusEnum status) {
        this.data.clear(status);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_userplace, viewGroup, false);
        return new PlacePeopleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (getItemViewType(position)){
            case VIEW_TYPES.COMING:
            case VIEW_TYPES.HERE:
            case VIEW_TYPES.INVITED:
                PlacePeopleViewHolder peopleViewHolder = (PlacePeopleViewHolder) holder;
                fillViewHolder(peopleViewHolder, (PlaceUserInterface) data.get(position));
                break;
        }
    }

    protected void fillViewHolder(PlacePeopleViewHolder peopleViewHolder, PlaceUserInterface placeUserInterface) {
        User user = placeUserInterface.getUser();
        RecyclerView rvPostTags = peopleViewHolder.rvPostTags;

        if (user != null){
            Log.d(TAG, "User: " + user.getUsername());
            final String pic = user.getProfilePictureUrl();
            if(pic !=null && peopleViewHolder.ivProfilePicture!=null) {
                Uri uri = Uri.parse(pic);
                peopleViewHolder.ivProfilePicture.setImageURI(uri);
            }

            String username = user.getUsername();
            peopleViewHolder.tvUsername.setText(username);
        }

        peopleViewHolder.tvTime.setText(placeUserInterface.getTimeCreated());

        if (placeUserInterface.getTags()==null) {
            rvPostTags.setVisibility(View.GONE);
        }
        else {
            rvPostTags.setVisibility(View.VISIBLE);
            List<Tag> tags = placeUserInterface.getTags();

            //Set the adapter for the Recycler View (which displays tags)
            HorizontalTagsAdapter htAdapter = (HorizontalTagsAdapter) rvPostTags.getAdapter();
            htAdapter.setData(tags);
            rvPostTags.setAdapter(htAdapter);

            //Listener Horizontal Scroll View
            /*
            HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                    new HorizontalTagsTouchListener(context, mItemClickListener, position);
            rvPostTags.setOnTouchListener(mHorizontalTagsTouchListener);*/

            //Set LayoutManager
            GridLayoutManager manager_savedTags = new GridLayoutManager(context, 1, LinearLayoutManager.HORIZONTAL, false);
            rvPostTags.setLayoutManager(manager_savedTags);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        SectionItem sectionItem = (SectionItem) data.getSectionFromPosition(position);
        return sectionItem.getViewType();
    }

    public void clear() {
        data.clear();
    }

    public void setOnItemClickListener(final OnItemAdapterClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public class PlacePeopleViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        View cv;
        TextView tvUsername;
        TextView tvTime;
        RecyclerView rvPostTags;
        SimpleDraweeView ivProfilePicture;

        PlacePeopleViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            // Get text views from item_post.xml
            cv = itemView.findViewById(R.id.cv);
            tvUsername = (TextView) itemView.findViewById(R.id.tv_username);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            rvPostTags = (RecyclerView) itemView.findViewById(R.id.rv_horizontal_tags);
            ivProfilePicture = (SimpleDraweeView) itemView.findViewById(R.id.profile_picture);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onClick(getAdapterPosition());
            }
        }
    }




    public class SectionItem<T> implements TwoDimArray.SectionItem{
        private final Object id;
        private List<T> data;
        private int viewType;

        public SectionItem(Object id, int viewType, List<T> data) {
            this.id = id;
            this.viewType = viewType;
            this.data = data;
        }

        public SectionItem(Object id, int viewType) {
            this.id = id;
            this.viewType = viewType;
            this.data = new LinkedList<>();
        }

        public T getItem(int position) {
            return data.get(position);
        }

        public int size() {
            return data.size();
        }

        @Override
        public void addAll(List item) {
            this.data.addAll(item);
        }

        @Override
        public void add(Object item) {
            this.data.add((T)item);
        }


        public boolean is(Object id) {
            return this.id.equals(id);
        }

        public void clear() {
            this.data.clear();
        }


        public int getViewType() {
            return viewType;
        }
    }


}