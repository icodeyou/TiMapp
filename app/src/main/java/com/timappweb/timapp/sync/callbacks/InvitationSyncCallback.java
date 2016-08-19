package com.timappweb.timapp.sync.callbacks;

import android.content.SyncResult;
import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;

import java.util.Collection;
import java.util.List;

import retrofit2.Response;

/**
 * Created by stephane on 5/5/2016.
 *
 */
public class InvitationSyncCallback implements MultipleEntriesSyncPerformer.Callback<EventsInvitation> {

    private static final String TAG = "FriendsSyncCallback";

    @Override
    public void onMatch(EventsInvitation remoteModel, EventsInvitation localModel) {
        try {
            if (!remoteModel.isSync(localModel)){
                    localModel.merge(remoteModel);

                EventsInvitation invitation = (EventsInvitation) localModel;
                this.completeUser(invitation);
                invitation.deepSave();

                Log.i(TAG, "Updating: " + localModel.toString());
            }
            else{
                Log.i(TAG, "No action: " + localModel.toString());
            }
        } catch (CannotSaveModelException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRemoteOnly(Collection<EventsInvitation> values){
        // Add new items
        for (EventsInvitation invitation : values) {
            try {
                Log.i(TAG, "Scheduling insert: " + invitation.toString());
                if (invitation.user_source == null && invitation.user_target == null) {
                    Log.e(TAG, "Received invitation from unknown counter part... Skipping...");
                    continue;
                }
                this.completeUser(invitation);
                invitation.deepSave();
            } catch (CannotSaveModelException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocalOnly(EventsInvitation localModel) {
        Log.i(TAG, "Deleting: " + localModel.toString());
        localModel.delete();
    }


    private void completeUser(EventsInvitation invitation){
        if (invitation.user_target == null && invitation.user_source != null){
            invitation.user_target = MyApplication.getCurrentUser();
        }
        else if (invitation.user_target != null && invitation.user_source == null){
            invitation.user_source = MyApplication.getCurrentUser();
        }
    }
}
