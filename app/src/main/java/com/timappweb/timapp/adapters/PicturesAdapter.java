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

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;


public class PicturesAdapter extends MyFlexibleAdapter {

    private List<String> picturesUris;
    private int gridColumnNumber;

    //private String baseUrl = "";

    //Constructor
    public PicturesAdapter(Context context, int pictureGridColumnNb) {
        super(context);
        gridColumnNumber = pictureGridColumnNb;

        for (int i = 0; i < gridColumnNumber; i++){
            addItem(i, new PlaceHolderItem("PLACEHOLDER" + i));
        }
    }


    public void setData(List<Picture> pictures) {
        this.removeData();
        this.addData(pictures);
    }

    public void appendData(List<Picture> pictures){
        if (picturesUris == null){
            picturesUris = new LinkedList<>();
        }
        for (Picture p: pictures){
            picturesUris.add(p.getUrl());
            addItem(new PictureItem(p));
        }
    }

    public boolean addItem(AbstractFlexibleItem item){
        super.addItem(getItemCount(), item);
        return false;
    }

    @Override
    public boolean addBeginning(List<AbstractFlexibleItem> items) {
        return this.addItems(gridColumnNumber, items);
    }

    public int getDataCount(){
        return this.getItemCountOfTypes(R.layout.item_picture);
    }

    @Override
    public boolean hasData() {
        return this.getDataCount() > 0;
    }

    public void removeData() {
        this.picturesUris = null;
        this.removeItemsOfType(R.layout.item_picture);
    }

    public void addData(List<Picture> pictures) {
        if (pictures == null) return;
        if (picturesUris == null){
            picturesUris = new LinkedList<>();
        }
        for (Picture p: pictures){
            picturesUris.add(p.getUrl());
            this.addItem(new PictureItem(p));
        }
    }

    public String[] getPictureUris() {
        return (String[]) picturesUris.toArray();
    }

    public int getGridColumnNumber() {
        return gridColumnNumber;
    }
}
