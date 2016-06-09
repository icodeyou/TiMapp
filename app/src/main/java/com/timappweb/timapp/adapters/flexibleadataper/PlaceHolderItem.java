package com.timappweb.timapp.adapters.flexibleadataper;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.ISectionable;

/**
 * Created by stephane on 6/9/2016.
 */
public class PlaceHolderItem extends AbstractFlexibleItem {

    private final String id;

    public PlaceHolderItem(String id) {
        super();
        this.id = id;
    }
    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof PlaceHolderItem) {
            PlaceHolderItem inItem = (PlaceHolderItem) inObject;
            return this.getId().equals(inItem.getId());
        }
        return false;
    }


    public String getId() {
        return id;
    }

    @Override
    public int getLayoutRes() {
        return com.github.florent37.materialviewpager.R.layout.material_view_pager_placeholder;
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new RecyclerView.ViewHolder(inflater.inflate(getLayoutRes(), parent, false)){

        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bindViewHolder(FlexibleAdapter adapter, RecyclerView.ViewHolder holder, int position, List payloads) {
        return;
    }

}
