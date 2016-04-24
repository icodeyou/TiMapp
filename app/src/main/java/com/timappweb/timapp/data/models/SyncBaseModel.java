package com.timappweb.timapp.data.models;

import com.activeandroid.Model;

/**
 * Created by stephane on 4/23/2016.
 */
public abstract class SyncBaseModel extends Model {

    public abstract long getSyncKey();
    public abstract boolean isSync(SyncBaseModel model);

}
