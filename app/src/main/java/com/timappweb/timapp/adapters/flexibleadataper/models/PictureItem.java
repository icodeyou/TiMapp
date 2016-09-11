package com.timappweb.timapp.adapters.flexibleadataper.models;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.AbstractModelItem;
import com.timappweb.timapp.data.models.Picture;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.viewholders.FlexibleViewHolder;

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
    public void bindViewHolder(final FlexibleAdapter adapter, PictureViewHolder holder, final int position, List payloads) {
        final String fullUrl = picture.getThumbnailUrl(Picture.ThumbnailType.SQUARE);
        if (fullUrl == null){
            Log.e(TAG, "Picture url is null, setting default picture");
            holder.ivPicture.setBackgroundResource(R.drawable.placeholder_picture);
        }
        else{
            Log.d(TAG, "Loading picture in adapter: " + fullUrl);
            holder.ivPicture.setImageURI(Uri.parse(fullUrl));
        }
        holder.ivPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.mItemClickListener != null){
                    adapter.mItemClickListener.onItemClick(position);
                }
            }
        });
    }

    public Picture getPicture() {
        return picture;
    }


    public class PictureViewHolder extends FlexibleViewHolder {

        SimpleDraweeView ivPicture;

        PictureViewHolder(View itemView, FlexibleAdapter adapter) {
            super(itemView, adapter);
            ivPicture = (SimpleDraweeView) itemView.findViewById(R.id.picture);
        }

    }

    
}
