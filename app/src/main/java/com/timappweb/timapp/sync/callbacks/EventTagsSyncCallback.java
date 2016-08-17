package com.timappweb.timapp.sync.callbacks;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventTag;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;

import java.util.Collection;

/**
 * Created by stephane on 5/13/2016.
 */
public class EventTagsSyncCallback extends RemoteMasterSyncCallback {

    Event event;

    public EventTagsSyncCallback(Event event) {
        this.event = event;
        event.deleteTags();
    }

    @Override
    public void onRemoteOnly(Collection<? extends SyncBaseModel> values) {
        for (SyncBaseModel model: values){
            EventTag eventTag = new EventTag(event, (Tag) model, ((Tag)model).count_ref);
            eventTag.deepSave();
        }
    }

}
