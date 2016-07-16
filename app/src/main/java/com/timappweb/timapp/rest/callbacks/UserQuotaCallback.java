package com.timappweb.timapp.rest.callbacks;

import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.config.QuotaType;

/**
 * Created by stephane on 6/6/2016.
 */
public class UserQuotaCallback extends HttpCallback {

    private final int key;

    public UserQuotaCallback(int key) {
        this.key = key;
    }

    @Override
    public void successful(Object feedback) {
        QuotaManager.instance().add(key);
    }

    @Override
    public void forbidden() {
        // TODO set quota..
    }
}
