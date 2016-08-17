package com.timappweb.timapp.sync.callbacks;

import android.content.SyncResult;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.UserFriend;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;

import java.util.Collection;
import java.util.List;

/**
 * Created by stephane on 5/5/2016.
 *
 */
public class FriendsSyncCallback implements MultipleEntriesSyncPerformer.Callback {

    private static final String TAG = "FriendsSyncCallback";


    @Override
    public void onMatch(SyncBaseModel remoteModel, SyncBaseModel localModel) {
        try {
            if (!remoteModel.isSync(localModel)){
                    localModel.merge(remoteModel);
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
        try {
            MyApplication.getCurrentUser().saveBelongsToMany(values, UserFriend.class);
        } catch (CannotSaveModelException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocalOnly(SyncBaseModel localModel) {
        Log.i(TAG, "Deleting: " + localModel.toString());
        new Delete().from(UserFriend.class).where("UserTarget = ?", localModel.getId()).execute();
    }
}
