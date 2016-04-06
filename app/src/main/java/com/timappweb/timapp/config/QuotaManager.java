package com.timappweb.timapp.config;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.database.models.QuotaType;
import com.timappweb.timapp.database.models.UserActivity;
import com.timappweb.timapp.database.models.UserQuota;
import com.timappweb.timapp.rest.RestClient;

import java.util.List;


/**
 * Created by stephane on 4/4/2016.
 */
public class QuotaManager {

    private static final String TAG = "QuotaManager";
    private static QuotaManager inst = null;

    private QuotaManager(){};

    public static QuotaManager instance(){
        if (inst == null){
            inst = new QuotaManager();
            inst.init();
        }
        return inst;
    }

    public UserActivity add(String type){
        Log.d(TAG, "Adding user activity: " + type);
        UserActivity userActivity = new UserActivity(type);
        userActivity.save();

        UserQuota userQuota = UserQuota.increment(MyApplication.getCurrentUser().id, QuotaType.getByType(type));
        Log.d(TAG, "Updated user quota: " + userQuota);

        return userActivity;
    }

    public void incrementQuota(){

    }

    public UserActivity getLastActivity(QuotaType type){
        Model res = new Select()
                .from(UserActivity.class)
                .where("QuotaType = ?", type)
                //.orderBy("date_created DESC")
                .executeSingle();
        return (UserActivity) res;
    }

    public boolean checkQuota(String typeString){
        QuotaType type = QuotaType.getByType(typeString);
        UserQuota userQuota = UserQuota.get(MyApplication.getCurrentUser().id, type);

        Log.d(TAG, "CHECKING QUOTA : " + userQuota);

        return userQuota.hasValidQuotas();
    }

    public void clearOldActivities(){
        //new Delete().from(UserActivity.class).where("date_created = ?", 1).execute();
    }

    private void init(){
        List<QuotaType> quotas = new Select().from(QuotaType.class).execute();
        if (quotas.size() == 0){
            initDummyQuota();
        }
        Log.d(TAG, "Loaded quotas: " + quotas.toString());
    }

    private void initDummyQuota(){
        new Delete().from(QuotaType.class).execute();
        (new QuotaType(QuotaType.PICTURE, 600, 0, 0, 0, 0, 0, 0)).save();
        (new QuotaType(QuotaType.POST, 600, 0, 0 , 0, 0, 0, 0)).save();
        (new QuotaType(QuotaType.FRIENDS, 600, 0, 0 , 0, 0, 0, 0)).save();
        (new QuotaType(QuotaType.NOTIFY_COMING, 600, 0, 0 , 0, 0, 0, 0)).save();
        (new QuotaType(QuotaType.PLACE, 600, 0, 0 , 0, 0, 0, 0)).save();
    }
}
