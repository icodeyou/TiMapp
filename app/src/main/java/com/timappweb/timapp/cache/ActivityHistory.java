package com.timappweb.timapp.cache;

import com.activeandroid.Model;
import com.activeandroid.query.Select;
import com.timappweb.timapp.database.models.QuotaType;
import com.timappweb.timapp.database.models.UserActivity;


/**
 * Created by stephane on 4/4/2016.
 */
public class ActivityHistory {

    private static ActivityHistory inst = null;

    private ActivityHistory(){};

    public static ActivityHistory instance(){
        if (inst == null){
            inst = new ActivityHistory();
        }
        return inst;
    }

    public UserActivity add(String type){
        UserActivity userActivity = new UserActivity(type);
        userActivity.save();
        return userActivity;
    }

    public UserActivity getLast(QuotaType type){
        Model res = new Select()
                .from(UserActivity.class)
                .where("QuotaType = ?", type)
                .orderBy("date_created DESC")
                .executeSingle();
        return (UserActivity) res;
    }

    public UserActivity getCurrentQuota(QuotaType type){
        Model res = new Select()
                .from(UserActivity.class)
                .where("QuotaType = ?", type)
                .orderBy("date_created DESC")
                .executeSingle();
        return (UserActivity) res;
    }

    public boolean checkQuota(QuotaType type){
        Model res = new Select()
                .from(UserActivity.class)
                .where("QuotaType = ?", type)
                .orderBy("date_created DESC")
                .executeSingle();
        return false;
    }

    public void clearOld(){
        //new Delete().from(UserActivity.class).where("date_created = ?", 1).execute();

    }
}
