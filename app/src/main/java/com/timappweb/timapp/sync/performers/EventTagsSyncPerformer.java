package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventTag;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.Tag;

import java.util.Collection;
import java.util.List;

/**
 * Created by stephane on 5/13/2016.
 */
public class EventTagsSyncPerformer extends RemoteMasterSyncPerformer{

    Event event;


    public EventTagsSyncPerformer(List<? extends SyncBaseModel> data, SyncResult syncResult, Event event) {
        super(data, null, syncResult);
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
