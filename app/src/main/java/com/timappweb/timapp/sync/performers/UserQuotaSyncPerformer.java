package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.MyModel;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserQuota;

import java.util.Collection;
import java.util.List;

/**
 * Created by stephane on 5/10/2016.
 */
public class UserQuotaSyncPerformer extends RemoteMasterSyncPerformer{


    public UserQuotaSyncPerformer(List<? extends SyncBaseModel> remoteEntries, List<? extends SyncBaseModel> localEntries, SyncResult syncResult) {
        super(remoteEntries, localEntries, syncResult);
    }

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
