package com.timappweb.timapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Picture;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

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
    public void onBindViewHolder(PictureViewHolder holder, final int position) {
        Picture picture = data.get(data.size()-position-1);
        final String fullUrl = this.baseUrl + "/" + picture.getPreviewUrl();
        Log.d(TAG, "Loading picture in adapter: " + fullUrl);

        final String[] uris = new String[data.size()];
        int i = 0;
        for (Picture p: data){
            uris[i++] = this.baseUrl + "/" + p.getUrl();
        }
        final Uri uri = Uri.parse(fullUrl);
        holder.ivPicture.setImageURI(uri);
        holder.ivPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity mActivity = (Activity) context;
                IntentsUtils.viewPicture(mActivity, position, uris);

            }
        });
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
        SimpleDraweeView ivPicture;

        PictureViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            ivPicture = (SimpleDraweeView) itemView.findViewById(R.id.picture);

            //set height picture to prevent padding on view
            Point size = new Point();
            ((Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
            int screenWidth = size.x;
            ivPicture.getLayoutParams().height = screenWidth;
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onClick(getAdapterPosition());
            }
        }
    }
}
