package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserFriend;

import java.util.Collection;
import java.util.List;

/**
 * Created by stephane on 5/5/2016.
 *
 */
public class FriendsSyncPerformer extends MultipleEntriesSyncPerformer {

    private static final String TAG = "FriendsSyncPerformer";

    public FriendsSyncPerformer(List<? extends SyncBaseModel> remoteEntries,
                                List<? extends SyncBaseModel> localEntries, SyncResult syncResult) {
        super(remoteEntries, localEntries, syncResult);
    }

    @Override
    public void onMatch(SyncBaseModel remoteModel, SyncBaseModel localModel) {
        if (!remoteModel.isSync(localModel)){
            localModel.sync(remoteModel);
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
            m.saveWithRemoteKey();
            addAssociation((User) m);
            syncResult.stats.numInserts++;
        }
    }

    @Override
    public void onLocalOnly(SyncBaseModel localModel) {
        Log.i(TAG, "Deleting: " + localModel.toString());
        new Delete().from(UserFriend.class).where("UserTarget = ?", localModel.getId()).execute();
        syncResult.stats.numDeletes++;
    }

    private void addAssociation(User model){
        UserFriend friend = new UserFriend();
        friend.userTarget = (User) model;
        friend.userSource = MyApplication.getCurrentUser();
        long id = friend.save();
        Log.v(TAG, "Inserting new friend association with id: " + id);
    }
}
