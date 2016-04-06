package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.PlaceUserInterface;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.entities.UserPlace;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.listeners.HorizontalTagsTouchListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class PlaceUsersAdapter
        extends RecyclerView.Adapter<PlaceUsersAdapter.PlacePeopleViewHolder> {
    private static final String TAG = "PlaceUsersAdapter";

    private OnItemAdapterClickListener mItemClickListener;
    protected Context context;
    protected List<PlaceUserInterface> data;

    //Constructor
    public PlaceUsersAdapter(Context context) {
        setHasStableIds(true);
        this.context = context;
        this.data = new ArrayList<>();
    }

    public class VIEW_TYPES {
        public static final int UNDEFINED = 0;
        public static final int HERE = 1;
        public static final int COMING = 2;
        public static final int INVITED = 3;
    }

    @Override
    public PlacePeopleViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_userplace, viewGroup, false);

        return new PlacePeopleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final PlacePeopleViewHolder holder, final int position) {
        Log.d(TAG, "::onBindViewHolder() -> " + position);
        PlaceUserInterface placeUserInterface = data.get(position);
        User user = placeUserInterface.getUser();
        RecyclerView rvPostTags = holder.rvPostTags;

        Log.d(TAG, "User: " + user.getUsername());
        final String pic = user.getProfilePictureUrl();
        if(pic !=null && holder.ivProfilePicture!=null) {
            /*holder.ivProfilePicture.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        // Wait until layout to call Picasso
                        @Override
                        public void onGlobalLayout() {
                            // Ensure we call this only once
                            holder.ivProfilePicture.getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(this);


                            Picasso.with(context)
                                    .load(pic)
                                    .centerCrop()
                                    .resize(0, holder.ivProfilePicture.getMeasuredHeight())
                                    .error(R.drawable.placeholder_profile_error)
                                    .placeholder(R.drawable.placeholder_profile)
                                    .into(holder.ivProfilePicture);
                        }
                    });*/

            Picasso.with(context)
                    .load(pic)
                    .centerCrop()
                    .resize((int) context.getResources().getDimension(R.dimen.width_pic_card),
                            (int) context.getResources().getDimension(R.dimen.width_pic_card))
                            //.error(R.drawable.placeholder_profile_error)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(holder.ivProfilePicture);
        }

        String username = user.getUsername();
        holder.tvUsername.setText(username);
        holder.tvTime.setText(placeUserInterface.getTimeCreated());

        if(placeUserInterface.getTags()==null) {
            rvPostTags.setVisibility(View.GONE);
        } else {
            rvPostTags.setVisibility(View.VISIBLE);
            List<Tag> tags = placeUserInterface.getTags();

            //Set the adapter for the Recycler View (which displays tags)
            HorizontalTagsAdapter htAdapter = (HorizontalTagsAdapter) rvPostTags.getAdapter();
            htAdapter.setData(tags);
            rvPostTags.setAdapter(htAdapter);

            //Listener Horizontal Scroll View
            HorizontalTagsTouchListener mHorizontalTagsTouchListener =
                    new HorizontalTagsTouchListener(context, mItemClickListener, position);
            rvPostTags.setOnTouchListener(mHorizontalTagsTouchListener);

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
        PlaceUserInterface placeUserInterface = data.get(position);
        return placeUserInterface.getViewType();
    }

    public PlaceUserInterface getData(int position) {
        return data.get(position);
    }

    public void addData(PlaceUserInterface placeUserInterface) {
        data.add(placeUserInterface);
        notifyDataSetChanged();
    }

    public void addData(List<PlaceUserInterface> placeUserInterfaces) {
        data.addAll(placeUserInterfaces);
        notifyDataSetChanged();
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }


    public void setOnItemClickListener(final OnItemAdapterClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


    public class PlacePeopleViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        CardView cv;
        TextView tvUsername;
        TextView tvTime;
        RecyclerView rvPostTags;
        ImageView ivProfilePicture;

        PlacePeopleViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            // Get text views from item_post.xml
            cv = (CardView) itemView.findViewById(R.id.cv);
            tvUsername = (TextView) itemView.findViewById(R.id.tv_username);
            tvTime = (TextView) itemView.findViewById(R.id.tv_time);
            rvPostTags = (RecyclerView) itemView.findViewById(R.id.rv_horizontal_tags);
            ivProfilePicture = (ImageView) itemView.findViewById(R.id.profile_picture);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onClick(getAdapterPosition());
            }
        }
    }


}