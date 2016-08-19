package com.timappweb.timapp.sync.performers;

import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;
import com.timappweb.timapp.sync.exceptions.HttpResponseSyncException;

import java.io.IOException;

/**
 * Created by stephane on 5/5/2016.
 */
public interface SyncPerformer {

    void perform() throws IOException, CannotSyncException;

}
