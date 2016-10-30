package com.timappweb.timapp.adapters;

import android.content.Context;

import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.PlaceHolderItem;
import com.timappweb.timapp.adapters.flexibleadataper.models.PictureItem;
import com.timappweb.timapp.data.models.Picture;

import java.util.LinkedList;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;


public class PicturesAdapter extends MyFlexibleAdapter {


    //private String baseUrl = "";

    //Constructor
    public PicturesAdapter(Context context, int pictureGridColumnNb) {
        super(context);
        beginningOffset = pictureGridColumnNb;

        for (int i = 0; i < beginningOffset; i++){
            addItem(i, new PlaceHolderItem("PLACEHOLDER_PICTURE" + i));
        }
    }

    public boolean addItem(AbstractFlexibleItem item){
        super.addItem(getItemCount(), item);
        return false;
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
        return beginningOffset;
    }

    public Picture getPicture(int position) {
        AbstractFlexibleItem item = getItem(position);
        if (item instanceof PictureItem){
            return ((PictureItem) item).getPicture();
        }
        return null;
    }

}
