package com.timappweb.timapp.data.models;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.utils.Util;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by stephane on 4/5/2016.
 */
@Table(database = AppDatabase.class, uniqueColumnGroups = {
        @UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.REPLACE)
})
public class UserQuota extends SyncBaseModel {

    public enum QuotaPeriod {MINUTE, HOUR, DAY, MONTH, YEAR, OVERALL}

    private static final String TAG = "UserQuota";

    // =============================================================================================
    // DATABASE

    @Expose
    @NotNull
    @Column
    @Unique(unique = false, uniqueGroups = 1)
    @SerializedName("type_id")
    public int type_id;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @NotNull
    @ForeignKey(tableClass = User.class, onDelete = ForeignKeyAction.CASCADE, onUpdate = ForeignKeyAction.CASCADE)
    @Unique(unique = false, uniqueGroups = 1)
    @Expose
    public User user;

    @Expose
    @Column(name = "MinDelay")
    public long min_delay;

    @Expose
    @Column(name = "QuotaMinute")
    public int quota_minute;

    @Expose
    @Column(name = "QuotaHour")
    public int quota_hour;

    @Expose
    @Column(name = "QuotaDay")
    public int quota_day;

    @Expose
    @Column(name = "QuotaMonth")
    public int quota_month;

    @Expose
    @Column(name = "QuotaYear")
    public int quota_year;

    @Expose
    @Column(name = "QuotaOverall")
    public int quota_overall;

    @Expose
    @Column(name = "LastActivity")
    public long last_activity;

    @Expose
    @Column(name = "CountMinute")
    public int count_minute;

    @Expose
    @Column(name = "CountHour")
    public int count_hour;

    @Expose
    @Column(name = "CountDay")
    public int count_day;

    @Expose
    @Column(name = "CountMonth")
    public int count_month;

    @Expose
    @Column(name = "CountYear")
    public int count_year;

    @Expose
    @Column(name = "CountOverall")
    public int count_overall;

    // =============================================================================================

    public UserQuota() {
        super();
    }

    public static UserQuota get(long user_id, int type){
        UserQuota userQuota = SQLite.select()
                .from(UserQuota.class)
                .where(UserQuota_Table.user_id.eq(user_id))
                .and(UserQuota_Table.type_id.eq(type))
                .querySingle();
        return userQuota;
    }

    public boolean hasValidQuotas(){
        try {
            this.assertValidQuotas();
            return true;
        }
        catch (QuotaManager.QuotaError ex){
            return false;
        }
    }

    public boolean assertValidQuotas() throws QuotaManager.QuotaError {
        this.updateQuotas();

        if (this.min_delay != 0 && Util.delayFromNow((int)this.last_activity) < this.min_delay){
            throw new QuotaManager.QuotaMinDelayError(this.last_activity, this.min_delay);
        }

        return  _checkQuota(this.count_minute, this.quota_minute, QuotaPeriod.MINUTE)
                && _checkQuota(this.quota_hour, this.quota_hour, QuotaPeriod.HOUR)
                && _checkQuota(this.quota_day, this.quota_day, QuotaPeriod.DAY)
                && _checkQuota(this.quota_month, this.quota_month, QuotaPeriod.MONTH)
                && _checkQuota(this.count_year, this.quota_year, QuotaPeriod.YEAR)
                && _checkQuota(this.count_overall, this.quota_overall, QuotaPeriod.OVERALL);
    }

    protected boolean _checkQuota(int current, int limit, QuotaPeriod period) throws QuotaManager.QuotaCountError {
        if (limit > 0 && current >= limit){
            throw new QuotaManager.QuotaCountError(period, limit, current);
        }

        return true;
    }


    public static void increment(long userId, int type) {
        UserQuota userQuota = UserQuota.get(userId, type);
        if (userQuota == null){
            Log.i(TAG, "Quota type: " + type + " does not exists");
            return;
        }
        userQuota.increment();
        userQuota.mySaveSafeCall();
        Log.d(TAG, "Updated user quota: " + userQuota);
    }

    public void increment() {
        this.count_minute += 1;
        this.count_hour += 1;
        this.count_day += 1;
        this.count_month += 1;
        this.count_year += 1;
        this.count_overall += 1;
        this.last_activity = Util.getCurrentTimeSec();
    }

    public void updateQuotas(){
        if (last_activity == 0){
            return;
        }
        Calendar currentDate = Calendar.getInstance(); // locale-specific
        currentDate.setTimeInMillis(System.currentTimeMillis());
        currentDate.set(Calendar.MILLISECOND, 0);

        Calendar lastActivity =  Calendar.getInstance();
        lastActivity.setTimeInMillis(this.last_activity * 1000);
        lastActivity.set(Calendar.MILLISECOND, 0);

        Log.v(TAG, "Resetting quotas: ");
        if (Util.isSameDate(lastActivity, currentDate, Calendar.SECOND)){
            Log.v(TAG, "    -> Nothing to do");
            return ;
        }
        Log.v(TAG, "    -> new minute");
        this.count_minute = 0;

        if (Util.isSameDate(lastActivity, currentDate, Calendar.MINUTE)){
            return ;
        }
        Log.v(TAG, "    -> new hour");
        this.count_hour = 0;

        if (Util.isSameDate(lastActivity, currentDate, Calendar.HOUR_OF_DAY)){
            return ;
        }
        Log.v(TAG, "    -> new day");
        this.count_day = 0;

        if (Util.isSameDate(lastActivity, currentDate, Calendar.DAY_OF_MONTH)){
            return ;
        }
        this.count_month = 0;
        Log.v(TAG, "    -> new month");

        if (Util.isSameDate(lastActivity, currentDate, Calendar.DAY_OF_YEAR)){
            return ;
        }
        this.count_year = 0;
        Log.v(TAG, "    -> new year");
    }

    @Override
    public String toString() {
        return "UserQuota{" +
                " type=" + type_id +
                ", user=" + (user != null ? user.getUsername() : "!!!NO USER!!!") +
                ", last_activity=" + getLastActivityPretty() + " (min delay: " + min_delay + " sec)" +
                ", minute=" + count_minute + "/" + quota_minute +
                ", hour=" + count_hour + "/" + quota_hour +
                ", day=" + count_day + "/" + quota_day +
                ", month=" + count_month + "/" + quota_month +
                ", year=" + count_year + "/" + quota_year +
                ", overall=" + count_overall + "/" + quota_overall +
                '}';
    }

    public String getLastActivityPretty(){
        PrettyTime p = new PrettyTime();
        return p.format(new Date(this.last_activity * 1000));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UserQuota userQuota = (UserQuota) o;

        if (type_id != userQuota.type_id) return false;
        if (user != userQuota.user) return false;
        if (min_delay != userQuota.min_delay) return false;
        if (quota_minute != userQuota.quota_minute) return false;
        if (quota_hour != userQuota.quota_hour) return false;
        if (quota_day != userQuota.quota_day) return false;
        if (quota_month != userQuota.quota_month) return false;
        if (quota_year != userQuota.quota_year) return false;
        if (quota_overall != userQuota.quota_overall) return false;
        if (last_activity != userQuota.last_activity) return false;
        if (count_minute != userQuota.count_minute) return false;
        if (count_hour != userQuota.count_hour) return false;
        if (count_day != userQuota.count_day) return false;
        if (count_month != userQuota.count_month) return false;
        if (count_year != userQuota.count_year) return false;
        return count_overall == userQuota.count_overall;

    }


    @Override
    public boolean isSync(SyncBaseModel o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UserQuota userQuota = (UserQuota) o;

        if (type_id != userQuota.type_id) return false;
        if (user != userQuota.user) return false;
        if (min_delay != userQuota.min_delay) return false;
        if (quota_minute != userQuota.quota_minute) return false;
        if (quota_hour != userQuota.quota_hour) return false;
        if (quota_day != userQuota.quota_day) return false;
        if (quota_month != userQuota.quota_month) return false;
        if (quota_year != userQuota.quota_year) return false;
        if (quota_overall != userQuota.quota_overall) return false;
        if (last_activity != userQuota.last_activity) return false;
        if (count_minute != userQuota.count_minute) return false;
        if (count_hour != userQuota.count_hour) return false;
        if (count_day != userQuota.count_day) return false;
        if (count_month != userQuota.count_month) return false;
        if (count_year != userQuota.count_year) return false;
        return count_overall == userQuota.count_overall;
    }


    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + type_id;
        result = 31 * result + 2;
        return result;
    }

    @Override
    public int getSyncType() {
        throw new InternalError("Not syncable");
    }


    public void resetCounts() {
        this.count_minute = 0;
        this.count_hour = 0;
        this.count_day = 0;
        this.count_month = 0;
        this.count_overall = 0;
        this.count_year = 0;
        this.resetLastActivity();
    }

    public void resetLastActivity() {
        this.last_activity = 0;
    }

    public void setCountMinute(int countMinute) {
        this.count_minute = countMinute;
    }

    public void setQuotaMinute(int quotaMinute) {
        this.quota_minute = quotaMinute;
    }

    public void setQuotaHour(int quotaHour) {
        this.quota_hour = quotaHour;
    }

}
