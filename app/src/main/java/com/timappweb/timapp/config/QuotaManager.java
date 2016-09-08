package com.timappweb.timapp.config;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.Model;
import com.activeandroid.query.Select;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.UserActivity;
import com.timappweb.timapp.data.models.UserQuota;
import com.timappweb.timapp.sync.user.UserSyncAdapter;
import com.timappweb.timapp.utils.Util;


/**
 * Created by stephane on 4/4/2016.
 */
public class QuotaManager {

    private static final String TAG = "QuotaManager";

    // ---------------------------------------------------------------------------------------------

    private static QuotaManager inst = null;
    private final Context context;

    // ---------------------------------------------------------------------------------------------

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
        UserQuota userQuota = getQuota(quotaTypeId);

        if (userQuota == null){
            Log.e(TAG, "There is no quota with id: " + quotaTypeId);
            this.sync();
            return true;
        }

        try {
            Log.d(TAG, "CHECKING QUOTA : " + userQuota);
            userQuota.assertValidQuotas();
            return true;
        }
        catch (QuotaError error){
            if (showMessage){
                Toast.makeText(context, error.getQuotaErrorReason(), Toast.LENGTH_LONG).show();
            }
            return false;
        }
    }

    private void init(){

    }

    public static void sync() {
        UserSyncAdapter.syncImmediately(MyApplication.getApplicationBaseContext());
    }

    public UserQuota getQuota(int quotaTypeId) {
        return MyApplication.getCurrentUser().getQuota(quotaTypeId);
    }

    // ---------------------------------------------------------------------------------------------


    public static abstract class QuotaError extends Exception {
        public abstract String getQuotaErrorReason();
    }
    public static class QuotaCountError extends QuotaError{

        public final UserQuota.QuotaPeriod quotaPeriod;
        private final int limit;
        private final int currentCount;


        public QuotaCountError(UserQuota.QuotaPeriod quotaPeriod, int limit, int currentCount) {
            this.quotaPeriod = quotaPeriod;
            this.limit = limit;
            this.currentCount = currentCount;
        }

        public String getQuotaErrorReason(){
            Context context = MyApplication.getApplicationBaseContext();
            return context.getString(R.string.quota_wait_not_defined);
        }


    }
    public static class QuotaMinDelayError extends QuotaError{

        private final long lastActivity;
        private final long minDelay;

        public QuotaMinDelayError(long lastActivity, long minDelay) {
            this.lastActivity = lastActivity;
            this.minDelay = minDelay;
        }

        @Override
        public String getQuotaErrorReason() {
            int timeToWait = (int)this.minDelay - Util.delayFromNow((int)this.lastActivity);
            return MyApplication.getApplicationBaseContext().getString(R.string.quota_wait_string,  Util.secondsDurationToPrettyTime(timeToWait));
        }

    }
}
