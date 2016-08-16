package com.timappweb.timapp.events;

/**
 * Created by Stephane on 16/08/2016.
 */
public class SyncResultMessage {

    public int type;
    public boolean upToDate;

    public SyncResultMessage(int type) {
        this.type = type;
    }

    public void setUpToDate(boolean upToDate) {
        this.upToDate = upToDate;
    }
}
