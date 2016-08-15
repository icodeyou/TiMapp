package com.timappweb.timapp.data.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.utils.Util;

/**
 * Created by stephane on 4/4/2016.
 */
@Table(name = "activities")
public class UserActivity extends MyModel{

    @Expose
    @Column(name = "DateCreated", index = true)
    public int created;

    @Expose
    @Column(name = "QuotaTypeId")
    public int type;


    public UserActivity() {
        super();
    }

    public UserActivity(int quotaTypeId) {
        super();
        this.type = quotaTypeId;
        this.created = Util.getCurrentTimeSec();
    }
}
