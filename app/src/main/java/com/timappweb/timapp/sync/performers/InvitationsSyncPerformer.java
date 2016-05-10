package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.PlacesInvitation;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserFriend;

import java.util.Collection;
import java.util.List;

/**
 * Created by stephane on 5/5/2016.
 *
 */
public class InvitationsSyncPerformer extends MultipleEntriesSyncPerformer {

    private static final String TAG = "FriendsSyncPerformer";

    public InvitationsSyncPerformer(List<? extends SyncBaseModel> remoteEntries,
                                    List<? extends SyncBaseModel> localEntries, SyncResult syncResult) {
        super(remoteEntries, localEntries, syncResult);
    }

    @Override
    public void onMatch(SyncBaseModel remoteModel, SyncBaseModel localModel) {
        if (!remoteModel.isSync(localModel)){
            localModel.sync(remoteModel);

            PlacesInvitation invitation = (PlacesInvitation) localModel;
            this.completeUser(invitation);
            invitation.save();

            syncResult.stats.numUpdates++;
            Log.i(TAG, "Updating: " + localModel.toString());
        }
        else{
            Log.i(TAG, "No action: " + localModel.toString());
        }
    }

    @Override
    public void onRemoteOnly(Collection<? extends SyncBaseModel> values){
        // Add new items
        for (SyncBaseModel m : values) {
            Log.i(TAG, "Scheduling insert: " + m.toString());
            PlacesInvitation invitation = (PlacesInvitation) m;
            if (invitation.user_source == null && invitation.user_target == null) {
                Log.e(TAG, "Receivied invitation from unknown counter part... Skipping...");
                continue;
            }
            this.completeUser(invitation);
            invitation.saveWithRemoteKey();
            syncResult.stats.numInserts++;
        }
    }

    @Override
    public void onLocalOnly(SyncBaseModel localModel) {
        Log.i(TAG, "Deleting: " + localModel.toString());
        localModel.delete();
        syncResult.stats.numDeletes++;
    }


    private void completeUser(PlacesInvitation invitation){
        if (invitation.user_target == null && invitation.user_source != null)
            invitation.user_target = MyApplication.getCurrentUser();
        else if (invitation.user_target != null && invitation.user_source == null){
            invitation.user_source = MyApplication.getCurrentUser();
        }
    }
}
