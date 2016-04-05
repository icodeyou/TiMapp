package com.timappweb.timapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Picture;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.utils.Util;

import java.util.ArrayList;
import java.util.List;


public class PicturesAdapter extends RecyclerView.Adapter<PicturesAdapter.PictureViewHolder> {
    private static final String TAG = "PicturesAdapter";

    List<Picture> data = new ArrayList<>();
    OnItemAdapterClickListener mItemClickListener;
    Context context;
    private String baseUrl = "";

    //Constructor
    public PicturesAdapter(Context context) {
        this.context = context;
    }

    @Override
    public PictureViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_picture, viewGroup, false);

        return new PictureViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PictureViewHolder pictureViewHolder, int position) {
        Picture picture = data.get(data.size()-position-1);
        String fullUrl = this.baseUrl + "/" + picture.getPreviewUrl();
        Log.d(TAG, "Loading picture in adapter: " + fullUrl);
        // TODO update picasso if new release > 2.5.2 to fix this bug
        // https://github.com/square/picasso/issues/881
        //Picasso.with(context).load(fullUrl).fit().centerCrop().into(pictureViewHolder.ivPicture);
        Picasso.with(context)
                .load(fullUrl)
                .centerCrop()
                .resize(pictureViewHolder.ivPicture.getMeasuredWidth(), pictureViewHolder.ivPicture.getMeasuredHeight())
                .error(R.drawable.placeholder_profile_error)
                .placeholder(R.drawable.placeholder_profile)
                .into(pictureViewHolder.ivPicture);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setData(List<Picture> pictures) {
        this.data = pictures;
        notifyDataSetChanged();
    }

    public void addData(Picture picture) {
        this.data.add(picture);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(final OnItemAdapterClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;

    }

    public class PictureViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        ImageView ivPicture;

        PictureViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            ivPicture = (ImageView) itemView.findViewById(R.id.picture);

            //set height picture to prevent padding on view
            Point size = new Point();
            ((Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
            int halfScreenWidth = size.x/2;
            ivPicture.getLayoutParams().height = halfScreenWidth;
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onClick(getAdapterPosition());
            }
        }
    }
}
