package com.timappweb.timapp.sync.callbacks;

import android.content.SyncResult;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserQuota;
import com.timappweb.timapp.sync.callbacks.RemoteMasterSyncCallback;

import java.util.Collection;
import java.util.List;

/**
 * Created by stephane on 5/10/2016.
 */
public class UserQuotaSyncCallback extends RemoteMasterSyncCallback {


    @Override
    public void onMatch(SyncBaseModel remoteModel, SyncBaseModel localModel) {
        UserQuota userQuota = (UserQuota) localModel;
        userQuota.user = MyApplication.getCurrentUser();
        super.onMatch(remoteModel, localModel);
    }

    @Override
    public void onRemoteOnly(Collection<? extends SyncBaseModel> values) {
        User user = MyApplication.getCurrentUser();
        for (SyncBaseModel model: values){
            ((UserQuota)model).user = user;
        }
        super.onRemoteOnly(values);
    }
}
