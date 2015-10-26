package com.timappweb.timapp.utils;

import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.entities.Spot;

import java.security.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by stephane on 9/13/2015.
 */

public class AreaRequestItem {
    public int timestamp;       // Request timestamp
    public int localTimestamp;
    public List<Spot> data;    // LIFO: Last spot in => First spot out
    public boolean isDisplayed = false;    // True if it's display on the map

    public AreaRequestItem(int timestamp, List<Spot> spots) {
        this.setTimesamp(timestamp);
        this.data = spots;
    }

    public AreaRequestItem() {
        this.data = new LinkedList<>();
    }


    public void setTimesamp(int timesamp) {
        this.timestamp = timesamp;
        this.localTimestamp = (int)(System.currentTimeMillis() / 1000); // OK with only 32 bits
    }
    @Override
    public String toString() {
        return "AreaRequestItem{" +
                "timestamp=" + timestamp +
                ", nb spots=" + this.data.size() +
                '}';
    }

    public void update(AreaRequestItem item) {
        this.setTimesamp(timestamp);
        data.addAll(item.data);
    }

    public int getLastUpdateDelay() {
        return (int)(System.currentTimeMillis() / 1000) - this.localTimestamp;
    }
}