package com.timappweb.timapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.ArrayList;
import java.util.List;


public class PicturesAdapter extends RecyclerView.Adapter<PicturesAdapter.PictureViewHolder> {
    private static final String TAG = "PicturesAdapter";

    List<Picture> data = new ArrayList<>();
    OnItemAdapterClickListener mItemClickListener;
    Context context;
    private String[] picturesUris;
    //private String baseUrl = "";

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
    public void onBindViewHolder(PictureViewHolder holder, final int position) {
        holder.setPicture(position, data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onViewRecycled(PictureViewHolder holder) {
        if (holder.ivPicture.getController() != null) {
            holder.ivPicture.getController().onDetach();
        }
        if (holder.ivPicture.getTopLevelDrawable() != null) {
            holder.ivPicture.getTopLevelDrawable().setCallback(null);
//                ((BitmapDrawable) holder.mImageView.getTopLevelDrawable()).getBitmap().recycle();
        }
    }

    public void setData(List<Picture> pictures) {
        this.data = pictures;
        notifyDataSetChanged();

        picturesUris = new String[data.size()];
        int i = 0;
        for (Picture p: data){
            picturesUris[i++] = p.getUrl();
        }
    }

    public void setOnItemClickListener(final OnItemAdapterClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    /*
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;

    }*/

    public class PictureViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        SimpleDraweeView ivPicture;

        PictureViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            ivPicture = (SimpleDraweeView) itemView.findViewById(R.id.picture);
            //set height picture to prevent padding on view
            ivPicture.getLayoutParams().height = ivPicture.getLayoutParams().width;
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onClick(getAdapterPosition());
            }
        }

        public void setPicture(final int position, Picture picture) {
            final String fullUrl = picture.getPreviewUrl();
            Log.d(TAG, "Loading picture in adapter: " + fullUrl);

            final Uri uri = Uri.parse(fullUrl);
            ivPicture.setImageURI(uri);
            ivPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Activity mActivity = (Activity) context;
                    IntentsUtils.viewPicture(mActivity, position, picturesUris);
                }
            });
        }
    }
}
