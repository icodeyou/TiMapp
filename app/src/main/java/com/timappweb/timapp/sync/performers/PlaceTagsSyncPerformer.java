package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;

import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.PlaceTag;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.Tag;

import java.util.Collection;
import java.util.List;

/**
 * Created by stephane on 5/13/2016.
 */
public class PlaceTagsSyncPerformer extends RemoteMasterSyncPerformer{

    Place place;


    public PlaceTagsSyncPerformer(List<? extends SyncBaseModel> data, SyncResult syncResult, Place place) {
        super(data, null, syncResult);
        this.place = place;

        place.deleteTags();
    }


    @Override
    public void onRemoteOnly(Collection<? extends SyncBaseModel> values) {
        for (SyncBaseModel model: values){
            PlaceTag placeTag = new PlaceTag(place, (Tag) model, ((Tag)model).count_ref);
            placeTag.deepSave();
        }
    }
}
