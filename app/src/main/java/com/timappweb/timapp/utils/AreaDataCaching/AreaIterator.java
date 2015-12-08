package com.timappweb.timapp.utils.AreaDataCaching;

import com.timappweb.timapp.utils.IntPoint;

import java.util.Iterator;

/**
 * Created by stephane on 9/19/2015.
 */
public class AreaIterator implements Iterator<IntPoint> {

    IntPoint southwest = null;
    IntPoint northeast = null;
    IntPoint currentItem = new IntPoint();

    public AreaIterator(IntPoint southwest, IntPoint northeast) {
        this.southwest = southwest;
        this.northeast = northeast;
        this.currentItem = null;
    }

    public int size(){
        return this.sizeWidth() * this.sizeHeight();
    }
    public int sizeWidth(){
        return Math.abs(northeast.x - southwest.x  + 1);
    }
    public int sizeHeight(){
        return Math.abs(northeast.y - southwest.y  + 1);
    }

    @Override
    public boolean hasNext() {
        return !northeast.equals(currentItem);
    }

    @Override
    public IntPoint next() {
        if (currentItem == null){
            currentItem = new IntPoint(southwest);
        }
        else if (currentItem.x < northeast.x){
            currentItem.x++;
        }
        else if (currentItem.y < northeast.y){
            currentItem.y++;
            currentItem.x = southwest.x;
        }
        else{
            currentItem = null;
        }
        return currentItem;
    }

    @Override
    public void remove() {
        currentItem = southwest;
    }
}
