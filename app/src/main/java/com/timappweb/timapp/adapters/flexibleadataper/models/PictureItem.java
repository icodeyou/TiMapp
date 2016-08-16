package com.timappweb.timapp.adapters.flexibleadataper.models;

import android.app.Activity;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.AbstractModelItem;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Picture;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * Created by Stephane on 16/08/2016.
 */
public class PictureItem  extends AbstractModelItem<PictureItem.PictureViewHolder> {

    private static final String TAG = "PictureItem";
    private Picture picture;

    public PictureItem(Picture picture) {
        super(String.valueOf(picture.getRemoteId()));
        this.picture = picture;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_picture;
    }

    @Override
    public PictureViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new PictureViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, PictureViewHolder holder, int position, List payloads) {
        final String fullUrl = picture.getPreviewUrl();
        Log.d(TAG, "Loading picture in adapter: " + fullUrl);

        final Uri uri = Uri.parse(fullUrl);
        holder.ivPicture.setImageURI(uri);
        holder.ivPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Activity mActivity = (Activity) context;
                //IntentsUtils.viewPicture(mActivity, position, picturesUris);
            }
        });
    }

    public Picture getPicture() {
        return picture;
    }


    public class PictureViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView ivPicture;

        PictureViewHolder(View itemView, FlexibleAdapter adapter) {
            super(itemView);
            ivPicture = (SimpleDraweeView) itemView.findViewById(R.id.picture);
        }

    }

    
}
