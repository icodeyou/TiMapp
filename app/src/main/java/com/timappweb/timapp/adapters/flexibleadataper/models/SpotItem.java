package com.timappweb.timapp.adapters.flexibleadataper.models;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.AbstractModelItem;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.databinding.LayoutSpotBinding;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by Stephane on 16/08/2016.
 */
public class SpotItem extends AbstractModelItem<SpotItem.SpotViewHolder>
    implements IFilterable {

    private static final String TAG = "PictureItem";
    private Spot spot;

    public SpotItem(Spot spot) {
        super(String.valueOf(spot.getRemoteId()));
        this.spot = spot;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.layout_spot;
    }

    @Override
    public SpotViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        LayoutSpotBinding mBinding  = DataBindingUtil.inflate(inflater, getLayoutRes(), parent, false);
        return new SpotViewHolder(mBinding, inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, SpotViewHolder holder, final int position, List payloads) {
        holder.mBinding.setSpot(spot);
        holder.mBinding.executePendingBindings();
    }

    public Spot getSpot() {
        return spot;
    }

    @Override
    public boolean filter(String constraint) {
        if (spot.getName() == null){
            return false;
        }
        String text = spot.getName().toLowerCase();
        return text.contains(constraint.toLowerCase());
    }


    public class SpotViewHolder extends FlexibleViewHolder {

        private final LayoutSpotBinding mBinding;

        SpotViewHolder(LayoutSpotBinding mBinding, View itemView, FlexibleAdapter adapter) {
            super(itemView, adapter);
            this.mBinding = mBinding;
        }

    }

    
}
