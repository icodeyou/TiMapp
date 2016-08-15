package com.timappweb.timapp.config;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.Model;
import com.activeandroid.query.Select;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.UserActivity;
import com.timappweb.timapp.data.models.UserQuota;
import com.timappweb.timapp.sync.UserSyncAdapter;
import com.timappweb.timapp.utils.Util;


/**
 * Created by stephane on 4/4/2016.
 */
public class QuotaManager {

    private static final String TAG = "QuotaManager";
    private static QuotaManager inst = null;
    private final Context context;

    private QuotaManager(Context context){
        this.context = context;
    };

    public static void init(Context context){
            inst = new QuotaManager(context);
            inst.init();
    }

    public static QuotaManager instance(){
        return inst;
    }

    public UserActivity add(int quotaTypeId){
        Log.d(TAG, "Adding user activity: " + quotaTypeId);
        UserActivity userActivity = new UserActivity(quotaTypeId);
        userActivity.save();

        UserQuota.increment(MyApplication.getCurrentUser().getId(), quotaTypeId);

        return userActivity;
    }

    public UserActivity getLastActivity(QuotaType type){
        Model res = new Select()
                .from(UserActivity.class)
                .where("QuotaType = ?", type)
                //.orderBy("date_created DESC")
                .executeSingle();
        return (UserActivity) res;
    }

    public boolean checkQuota(int quotaTypeId){
        return checkQuota(quotaTypeId, false);
    }

    public boolean checkQuota(int quotaTypeId, boolean showMessage){
        if (!MyApplication.isLoggedIn()){
            return false;
        }
        UserQuota userQuota = MyApplication.getCurrentUser().getQuota(quotaTypeId);

        if (userQuota == null){
            Log.e(TAG, "There is no quota with id: " + quotaTypeId);
            UserSyncAdapter.syncImmediately(MyApplication.getApplicationBaseContext());
            return true;
        }

        Log.d(TAG, "CHECKING QUOTA : " + userQuota);
        boolean validQuota = userQuota.hasValidQuotas();
        if (showMessage && !validQuota){
            Toast.makeText(context, userQuota.getQuotaErrorReason(), Toast.LENGTH_LONG).show();
        }
        return validQuota;
    }

    private void init(){

    }

}
