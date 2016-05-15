package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;

import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.rest.model.PaginationResponse;

import java.util.Collection;

/**
 * Created by stephane on 5/14/2016.
 * TODO
 */
public class PlacePictureSyncPerformer extends RemoteMasterSyncPerformer {

    private final Place place;
    private final String baseUrl;

    public PlacePictureSyncPerformer(PaginationResponse<Picture> body, Place place, SyncResult syncResult) {
        super(body, place.getPictures(), syncResult);
        this.place = place;
        this.baseUrl = body.extra.get("base_url");
    }

    @Override
    public void onRemoteOnly(Collection<? extends SyncBaseModel> values) {
        for (SyncBaseModel model: values){
            ((Picture)model).base_url = this.baseUrl;
        }
        super.onRemoteOnly(values);
    }

}
