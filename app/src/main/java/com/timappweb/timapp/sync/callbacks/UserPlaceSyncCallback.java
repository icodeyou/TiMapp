package com.timappweb.timapp.sync.callbacks;

import android.content.SyncResult;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;
import com.timappweb.timapp.sync.callbacks.RemoteMasterSyncCallback;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;

import java.util.Collection;
import java.util.List;

/**
 * Created by stephane on 5/13/2016.
 */
public class UserPlaceSyncCallback extends RemoteMasterSyncCallback<UserEvent> {

    Event event;

    public UserPlaceSyncCallback(Event event) {
        this.event = event;
    }

    @Override
    public void onMatch(UserEvent remoteModel, UserEvent localModel) {
        remoteModel.event = event;
        super.onMatch(remoteModel, localModel);
    }

    @Override
    public void onRemoteOnly(Collection<UserEvent> values) {
        for (UserEvent model: values){
            model.event = event;
        }
        super.onRemoteOnly(values);
    }

}
