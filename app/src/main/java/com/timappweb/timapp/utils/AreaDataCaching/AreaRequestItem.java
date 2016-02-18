package com.timappweb.timapp.utils.AreaDataCaching;

import android.util.Log;

import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.utils.Util;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;

/**
 * Created by stephane on 9/13/2015.
 * Represents a request for a data area
 *
 *
 *
 */
public class AreaRequestItem<T> {
    private static final String TAG = "AreaRequestItem";

    public int dataTimestamp;       // Timestamp on the server (used to filter data when we update the area)
    public int localTimestamp;      // Timestamp on the local machine (used to know when was the last update)
    private int currentRequestId = -1;  // Request id
    public List<T> data;         // LIFO: Last spot in => First spot out
    private Call<List<Place>> pendingCall; // Represents api calls in progress

    public AreaRequestItem(int dataTimestamp, List<T> spots) {
        this.setDataTimestamp(dataTimestamp);
        this.updateLocalTimestamp();
        this.data = spots;
    }

    public AreaRequestItem() {
        this.data = new LinkedList<>();
        this.dataTimestamp = 0;
        this.localTimestamp = 0;
    }


    public void setDataTimestamp(int timesamp) {
        this.dataTimestamp = timesamp;
    }
    public void updateLocalTimestamp() {
        this.localTimestamp = Util.getCurrentTimeSec(); // OK with only 32 bits
    }
    @Override
    public String toString() {
        return "AreaRequestItem{" +
                "dataTimestamp=" + dataTimestamp +
                ", nb spots=" + this.data.size() +
                '}';
    }

    public void update(AreaRequestItem item) {
        data.addAll(item.data);
    }

    /**
     *
     * @return
     */
    public int getLastUpdateDelay() {
        return Util.getCurrentTimeSec() - this.localTimestamp;
    }

    public int setPendingCall(Call<List<Place>> pendingCall) {
        if (this.pendingCall != null){
            Log.d(TAG, "Cancel old call");
            this.pendingCall.cancel();
        }
        this.pendingCall = pendingCall;
        this.currentRequestId++;
        return currentRequestId;
    }


    public void appendData(List<T> places) {
        data.addAll(places);
    }

    public boolean isOutdated(int itemRequestId) {
        Log.d(TAG, "Current request id: " + currentRequestId + ". Request id: " + itemRequestId);
        return currentRequestId == -1 || currentRequestId > itemRequestId;
    }

    public void cancel() {
        Log.d(TAG, "Cancel request");
        this.currentRequestId = -1;
        if (pendingCall != null){
            pendingCall.cancel();
        }
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}