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
    private final MultipleEntriesSyncPerformer performer;
    private final Event event;
    private String baseUrl = null;

    public PictureSyncCallback(MultipleEntriesSyncPerformer performer, Event event) {
        this.performer = performer;
        this.event = event;
    }

    /*
    private String getBaseUrl() {

        if (baseUrl == null){
            try {
                ResponseSyncWrapper<Picture> wrapper = (ResponseSyncWrapper<Picture>) performer
                        .getRemoteLoader()
                        .getResponse()
                        .body();
                baseUrl = wrapper.extra.getAsJsonObject().get("base_url").getAsString();
            }
            catch (Exception ex){
                Log.e(TAG, "Cannot get base url: " + ex.getMessage());
                baseUrl = "";
            }
        }
        return baseUrl;
    }*/


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
