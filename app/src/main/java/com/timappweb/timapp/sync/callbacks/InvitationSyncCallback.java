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

/**
 * Created by stephane on 5/5/2016.
 *
 */
public class InvitationSyncCallback implements MultipleEntriesSyncPerformer.Callback {

    private static final String TAG = "FriendsSyncCallback";

    @Override
    public void onMatch(SyncBaseModel remoteModel, SyncBaseModel localModel) {
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
    public void onRemoteOnly(Collection<? extends SyncBaseModel> values){
        // Add new items
        for (SyncBaseModel m : values) {
            try {
                Log.i(TAG, "Scheduling insert: " + m.toString());
                EventsInvitation invitation = (EventsInvitation) m;
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
    public void onLocalOnly(SyncBaseModel localModel) {
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
