package com.timappweb.timapp.configsync;

/**
 * Created by stephane on 4/21/2016.
 */
public interface OnConfigChangeListener {

    public void onSyncChanged(int configId, SyncConfig dataWrapper);
}
