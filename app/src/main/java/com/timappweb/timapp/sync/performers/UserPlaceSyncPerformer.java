package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;

import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.UserPlace;
import com.timappweb.timapp.rest.model.PaginationResponse;

import java.util.Collection;
import java.util.List;

/**
 * Created by stephane on 5/13/2016.
 */
public class UserPlaceSyncPerformer extends RemoteMasterSyncPerformer{

    Place place;

    public UserPlaceSyncPerformer(PaginationResponse<UserPlace> body, List<? extends SyncBaseModel> users, SyncResult syncResult, Place place) {
        super(body, users, syncResult);
        this.place = place;
    }

    @Override
    public void onMatch(SyncBaseModel remoteModel, SyncBaseModel localModel) {
        ((UserPlace)remoteModel).place = place;
        super.onMatch(remoteModel, localModel);
    }

    @Override
    public void onRemoteOnly(Collection<? extends SyncBaseModel> values) {
        for (SyncBaseModel model: values){
            ((UserPlace)model).place = place;
        }
        super.onRemoteOnly(values);
    }
}
