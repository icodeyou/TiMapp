package com.timappweb.timapp.sync.callbacks;

import android.util.Log;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;

import java.util.Collection;

/**
 * Created by Stephane on 19/08/2016.
 */
public class PictureSyncCallback extends RemoteMasterSyncCallback<Picture> {

    private static final String TAG = "PictureSyncCallback";
    private final Event event;

    public PictureSyncCallback(Event event) {
        this.event = event;
    }

    @Override
    public void onMatch(Picture remoteModel, Picture localModel) {

    }

    @Override
    public void onRemoteOnly(Collection<Picture> entries) {
        for (Picture entry: entries){
            entry.event = event;
        }
    }

    @Override
    public void onLocalOnly(Picture localModel) {
        Log.i(TAG, "Deleting: " + localModel.toString());
        localModel.delete();
    }
}
