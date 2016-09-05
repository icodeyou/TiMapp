package com.timappweb.timapp.events;

import com.timappweb.timapp.sync.SyncAdapterOption;

/**
 * Created by Stephane on 16/08/2016.
 */
public class SyncResultMessage {

    private SyncAdapterOption options;
    private boolean upToDate;
    private int count = 0;
    private Exception error;
    private long minId;
    private long maxId;

    public SyncResultMessage(SyncAdapterOption options) {
        this.options = options;
    }

    public void setUpToDate(boolean upToDate) {
        this.upToDate = upToDate;
    }

    public int countItems() {
        return count;
    }

    public void setError(Exception error) {
        this.error = error;
    }

    public boolean hasError(){
        return error != null;
    }

    public Exception getError() {
        return error;
    }

    public SyncAdapterOption getOptions() {
        return options;
    }

    public long getMinId() {
        return minId;
    }

    public long getMaxId() {
        return maxId;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
