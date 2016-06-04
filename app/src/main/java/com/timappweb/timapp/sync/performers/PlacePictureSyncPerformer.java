package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.rest.model.PaginatedResponse;

import java.util.Collection;

/**
 * Created by stephane on 5/14/2016.
 * TODO
 */
public class PlacePictureSyncPerformer extends RemoteMasterSyncPerformer {

    private final Event event;
    private final String baseUrl;

    public PlacePictureSyncPerformer(PaginatedResponse<Picture> body, Event event, SyncResult syncResult) {
        super(body, event.getPictures(), syncResult);
        this.event = event;
        this.baseUrl = body.extra.get("base_url");
    }

    @Override
    public void onRemoteOnly(Collection<? extends SyncBaseModel> values) {
        for (SyncBaseModel model: values){
            Picture picture = (Picture) model;
            picture.base_url = this.baseUrl;
            picture.event = event;
        }
        super.onRemoteOnly(values);
    }

}
