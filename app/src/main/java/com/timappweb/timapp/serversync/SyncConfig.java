package com.timappweb.timapp.serversync;

/**
 * Created by stephane on 4/22/2016.
 */


public class SyncConfig<DataType>{
    public int version;
    public DataType data;

    public SyncConfig() {
        this.version = 0;
        this.data = null;
    }

    @Override
    public String toString() {
        return "SyncConfig{" +
                "version=" + version +
                ", data=" + data +
                '}';
    }
}