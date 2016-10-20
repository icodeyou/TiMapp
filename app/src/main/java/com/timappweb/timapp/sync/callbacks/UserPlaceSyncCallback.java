package com.timappweb.timapp.sync.callbacks;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.UserEvent;

import java.util.Collection;

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
