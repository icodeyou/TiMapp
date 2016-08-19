package com.timappweb.timapp.sync.exceptions;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;

/**
 * Created by Stephane on 17/08/2016.
 */
public class MissingSyncParameterException extends CannotSyncException {

    public String params;

    public MissingSyncParameterException(String params) {
        super("Sync parameter is missing: " + params, 0);
        this.params = params;
    }

    public String getUserFeedback() {
        return MyApplication.getApplicationBaseContext().getString(R.string.please_update_app);
    }

}
