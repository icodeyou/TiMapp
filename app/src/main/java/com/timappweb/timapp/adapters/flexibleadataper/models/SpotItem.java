package com.timappweb.timapp.adapters.flexibleadataper.models;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.AbstractModelItem;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.databinding.LayoutSpotBinding;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by Stephane on 16/08/2016.
 */
public class SpotItem extends AbstractFlexibleItem<SpotItem.SpotViewHolder>
    implements IFilterable {

    private static final String TAG = "PictureItem";
    private Spot spot;

    public SpotItem(Spot spot) {
        this.spot = spot;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.layout_spot;
    }

    @Override
    public SpotViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        LayoutSpotBinding mBinding  = DataBindingUtil.inflate(inflater, getLayoutRes(), parent, false);
        return new SpotViewHolder(mBinding.getRoot(), mBinding, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, SpotViewHolder holder, final int position, List payloads) {
        holder.mBinding.setSpot(spot);
        //holder.mBinding.notifyChange();
        holder.mBinding.executePendingBindings();
    }

    public Spot getSpot() {
        return spot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpotItem spotItem = (SpotItem) o;
        return spot != null ? spot.equals(spotItem.spot) : spotItem.spot == null;

    }

    @Override
    public int hashCode() {
        return spot != null ? spot.hashCode() : 0;
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

        public final LayoutSpotBinding mBinding;

        SpotViewHolder(View itemView, LayoutSpotBinding binding, FlexibleAdapter adapter) {
            super(itemView, adapter);
            this.mBinding = binding;
        }

    }

    
}
