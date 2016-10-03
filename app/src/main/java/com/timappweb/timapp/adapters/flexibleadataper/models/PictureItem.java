package com.timappweb.timapp.adapters.flexibleadataper.models;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.AbstractModelItem;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.utils.Util;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by Stephane on 16/08/2016.
 */
public class PictureItem  extends AbstractModelItem<PictureItem.PictureViewHolder> {

    private static final String TAG = "PictureItem";
    private Picture picture;
    private FlexibleAdapter.OnItemLongClickListener mLongItemClickListener;

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
    public void bindViewHolder(final FlexibleAdapter adapter, final PictureViewHolder holder, final int position, List payloads) {
        final String fullUrl = picture.getThumbnailUrl(Picture.ThumbnailType.SQUARE);
        if (fullUrl == null){
            Log.e(TAG, "Picture url is null, setting default picture");
            holder.ivPicture.setBackgroundResource(R.drawable.placeholder_picture);
        }
        else{
            Log.d(TAG, "Loading picture in adapter: " + fullUrl);
            holder.ivPicture.setImageURI(Uri.parse(fullUrl));
        }
    }

    public Picture getPicture() {
        return picture;
    }


    public class PictureViewHolder extends FlexibleViewHolder {

        SimpleDraweeView ivPicture;

        PictureViewHolder(View itemView, final FlexibleAdapter adapter) {
            super(itemView, adapter);
            ivPicture = (SimpleDraweeView) itemView.findViewById(R.id.picture);
            Util.setSelectionsBackgroundAdapter(itemView, R.color.black, R.color.colorPrimaryVeryDark, R.color.white);
        }
    }
}
