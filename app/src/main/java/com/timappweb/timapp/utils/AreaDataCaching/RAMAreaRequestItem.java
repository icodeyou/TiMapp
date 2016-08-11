package com.timappweb.timapp.utils.AreaDataCaching;

import android.util.Log;

import com.timappweb.timapp.data.models.Event;
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
public class RAMAreaRequestItem<T> implements AreaRequestItemInterface<T>{

    private static final String TAG = "RAMAreaRequestItem";

    // ---------------------------------------------------------------------------------------------

    public int              dataTimestamp;          // Timestamp on the server (used to filter data when we update the area)
    public int              localTimestamp;         // Timestamp on the local machine (used to know when was the last update)
    public int              currentRequestId = -1;  // Request remote_id
    public List<T>          data;                   // LIFO: Last spot in => First spot out
    private Call<List<T>>   pendingCall;            // Represents api calls in progress

    // ---------------------------------------------------------------------------------------------

    public void setListener(OnDataChangeListener listener) {
        this.listener = listener;
    }

    private OnDataChangeListener listener = null;

    public int getDataTimestamp() {
        return dataTimestamp;
    }

    @Override
    public List<T> getData() {
        return data;
    }

    @Override
    public int getRequestId() {
        return currentRequestId;
    }

    public RAMAreaRequestItem(int dataTimestamp, List<T> spots) {
        this.setDataTimestamp(dataTimestamp);
        this.updateLocalTimestamp();
        this.data = spots;
    }

    public RAMAreaRequestItem() {
        this.data = new LinkedList<>();
        this.dataTimestamp = 0;
        this.localTimestamp = 0;
    }

    public void setDataTimestamp(int timestamp) {
        this.dataTimestamp = timestamp;
    }

    public void updateLocalTimestamp() {
        this.localTimestamp = Util.getCurrentTimeSec(); // OK with only 32 bits
    }

    @Override
    public void update(AreaRequestItemInterface<T> item) {
        data.addAll(item.getData());
        if (listener != null) listener.onDataChange();
    }

    @Override
    public String toString() {
        return "RAMAreaRequestItem{" +
                "dataTimestamp=" + dataTimestamp +
                ", nb spots=" + this.data.size() +
                '}';
    }

    /**
     *
     * @return
     */
    public int getLastUpdateDelay() {
        return Util.getCurrentTimeSec() - this.localTimestamp;
    }

    public int setPendingCall(Call<List<T>> pendingCall) {
        if (this.pendingCall != null){
            Log.d(TAG, "Cancel old call");
            this.pendingCall.cancel();
        }
        this.pendingCall = pendingCall;
        this.currentRequestId++;
        return currentRequestId;
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
        if (listener != null) listener.onDataChange();
    }

    public void clear() {
        Log.d(TAG, "Clearing this item with " + this.data.size() + " elems");
        this.data.clear();
        if (listener != null) listener.onDataChange();
    }

}