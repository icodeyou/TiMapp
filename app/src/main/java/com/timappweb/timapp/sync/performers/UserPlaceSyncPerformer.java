package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;

import java.util.Collection;
import java.util.List;

/**
 * Created by stephane on 5/13/2016.
 */
public class UserPlaceSyncPerformer extends RemoteMasterSyncPerformer{

    Event event;

    public UserPlaceSyncPerformer(PaginatedResponse<UserEvent> body, List<? extends SyncBaseModel> users, SyncResult syncResult, Event event) {
        super(body, users, syncResult);
        this.event = event;
    }

    @Override
    public void onMatch(SyncBaseModel remoteModel, SyncBaseModel localModel) {
        ((UserEvent)remoteModel).event = event;
        super.onMatch(remoteModel, localModel);
    }

    @Override
    public void onRemoteOnly(Collection<? extends SyncBaseModel> values) {
        for (SyncBaseModel model: values){
            ((UserEvent)model).event = event;
        }
        super.onRemoteOnly(values);
    }
}
