package com.timappweb.timapp.adapters;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.PlaceHolderItem;
import com.timappweb.timapp.adapters.flexibleadataper.models.PictureItem;
import com.timappweb.timapp.data.models.Picture;

import java.util.LinkedList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;


public class PicturesAdapter extends MyFlexibleAdapter {


    //private String baseUrl = "";

    //Constructor
    public PicturesAdapter(Context context, int pictureGridColumnNb) {
        super(context);
        removeAllOffset = pictureGridColumnNb;

        for (int i = 0; i < removeAllOffset; i++){
            addItem(i, new PlaceHolderItem("PLACEHOLDER" + i));
        }
    }

    public boolean addItem(AbstractFlexibleItem item){
        super.addItem(getItemCount(), item);
        return false;
    }

    @Override
    public boolean hasData() {
        return this.getDataCount() > 0;
    }

    public String[] getPictureUris() {
        LinkedList<String> picturesUris = new LinkedList<>();
        for (int i = 0; i < this.getItemCount(); i++){
            AbstractFlexibleItem item = this.getItem(i);
            if (item instanceof PictureItem){
                PictureItem pictureItem = (PictureItem) item;
                picturesUris.add(pictureItem.getPicture().getUrl());
            }
        }
        String[] tabList = new String[picturesUris.size()];
        return picturesUris.toArray(tabList);
    }

    public int getGridColumnNumber() {
        return removeAllOffset;
    }

    public Picture getPicture(int position) {
        AbstractFlexibleItem item = getItem(position);
        if (item instanceof PictureItem){
            return ((PictureItem) item).getPicture();
        }
        return null;
    }

}
