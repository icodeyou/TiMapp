package com.timappweb.timapp.database.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.timappweb.timapp.utils.Util;

/**
 * Created by stephane on 4/4/2016.
 */
@Table(name = "activities")
public class UserActivity extends Model {

    @Column(name = "DateCreated", index = true)
    public int created;


    @Column(name = "ActionType")
    public ActionType type;


    public UserActivity() {
        super();
    }

    public UserActivity(ActionType.ActionTypeName type) {
        super();
        this.type = ActionType.getByName(type);
        this.created = Util.getCurrentTimeSec();
    }
}
