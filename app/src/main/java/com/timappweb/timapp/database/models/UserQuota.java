package com.timappweb.timapp.database.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.timappweb.timapp.utils.Util;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by stephane on 4/5/2016.
 */
@Table(name = "UserQuotas")
public class UserQuota extends Model {

    private static final String TAG = "UserQuota";
    public String _quota_error_reason;

    @Column(name = "Type", index = true, uniqueGroups = {"uniqueUserQuota"}, onUniqueConflicts = {Column.ConflictAction.REPLACE})
    public QuotaType type;

    @Column(name = "UserId", index = true, uniqueGroups = {"uniqueUserQuota"}, onUniqueConflicts = {Column.ConflictAction.REPLACE})
    public int user_id;

    @Column(name = "LastActivity")
    public int last_activity;

    @Column(name = "QuotaMinute")
    public int quota_minute;

    @Column(name = "QuotaHour")
    public int quota_hour;

    @Column(name = "QuotaDay")
    public int quota_day;

    @Column(name = "QuotaMonth")
    public int quota_month;

    @Column(name = "QuotaYear")
    public int quota_year;

    @Column(name = "QuotaOverall")
    public int quota_overall;

    public UserQuota() {
        super();
    }

    public UserQuota(int user_id, QuotaType type) {
        super();
        this.user_id = user_id;
        this.type = type;
        this.last_activity = 0;
        this.quota_minute = 0;
        this.quota_hour = 0;
        this.quota_day = 0;
        this.quota_month = 0;
        this.quota_year = 0;
        this.quota_overall = 0;
    }

    public static List<UserQuota> all(){
        return new Select().from(UserQuota.class)
                .execute();
    }

    public static UserQuota get(int user_id, QuotaType type){
        UserQuota userQuota = new Select()
                .from(UserQuota.class)
                .where("UserId = ?", user_id)
                .where("Type = ?", type.getId())
                .executeSingle();
        if (userQuota == null){
            Log.i(TAG, "Cannot find user quota for user_id=" + user_id + " and type=" + type.type);
            Log.d(TAG, "Current quotas are: " + UserQuota.all());
            userQuota = new UserQuota(user_id, type);
            userQuota.save();
        }
        return userQuota;
    }

/*
    public static UserQuota get(int user_id, int type_id){
        UserQuota userQuota = new Select()
                .from(UserQuota.class)
                .where("UserId = ?", user_id)
                .where("Type = ?", type_id)
                .executeSingle();
        if (userQuota == null){
            Log.i(TAG, "Cannot find user quota for user_id=" + user_id + " and type=" + type_id);
            Log.d(TAG, "Current quotas are: " + UserQuota.all());
            userQuota = new UserQuota(user_id, type_id);
            userQuota.save();
        }
        return userQuota;
    }*/

    public boolean hasValidQuotas() {
        this.resetQuotas();

        if (this.type.min_delay != 0 && Util.delayFromNow(this.last_activity) < this.type.min_delay){
            setMinDelayReason();
            return false;
        }

        return  _checkQuota(this.quota_minute, this.type.quota_minute, "minute")
                && _checkQuota(this.quota_hour, this.type.quota_hour, "hour")
                && _checkQuota(this.quota_day, this.type.quota_day, "day")
                && _checkQuota(this.quota_month, this.type.quota_month, "month")
                && _checkQuota(this.quota_year, this.type.quota_year, "year")
                && _checkQuota(this.quota_overall, this.type.quota_overall, "life");
    }

    public void setMinDelayReason(){
        int mustWaitSeconds = this.type.min_delay - Util.delayFromNow(this.last_activity);
        this._quota_error_reason = "You must wait " + Util.secondsDurationToPrettyTime(mustWaitSeconds) + " before posting again";
    }


    public boolean _checkQuota(int current, int limit, String period){
        if (limit > 0 && current >= limit){
            this._quota_error_reason = "You are limited at " + limit + " per " + period;
            return false;
        }

        return true;
    }

    public String getQuotaErrorReason(){
        return this._quota_error_reason;
    }


    public static UserQuota increment(int id, QuotaType type) {
        UserQuota userQuota = UserQuota.get(id, type);
        userQuota.increment();
        userQuota.save();
        return userQuota;
    }

    public void increment() {
        this.quota_minute += 1;
        this.quota_hour += 1;
        this.quota_day += 1;
        this.quota_month += 1;
        this.quota_year += 1;
        this.quota_overall += 1;
        this.last_activity = Util.getCurrentTimeSec();
    }

    public void resetQuotas(){
        if (last_activity == 0){
            return;
        }
        Calendar currentDate = Calendar.getInstance(); // locale-specific
        currentDate.setTimeInMillis(Util.getCurrentTimeSec() * 1000);
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
        this.quota_minute = 0;

        if (Util.isSameDate(lastActivity, currentDate, Calendar.MINUTE)){
            return ;
        }
        Log.v(TAG, "    -> new hour");
        this.quota_hour = 0;

        if (Util.isSameDate(lastActivity, currentDate, Calendar.HOUR_OF_DAY)){
            return ;
        }
        Log.v(TAG, "    -> new day");
        this.quota_day = 0;

        if (Util.isSameDate(lastActivity, currentDate, Calendar.DAY_OF_MONTH)){
            return ;
        }
        this.quota_month = 0;
        Log.v(TAG, "    -> new month");

        if (Util.isSameDate(lastActivity, currentDate, Calendar.DAY_OF_YEAR)){
            return ;
        }
        this.quota_year = 0;
        Log.v(TAG, "    -> new year");
    }


    @Override
    public String toString() {
        return "UserQuota{" +
                "name=" + type.type +
                ", user_id=" + user_id +
                ", last_activity=" + last_activity + "("+Util.secondsTimestampToPrettyTime(last_activity)+")" +
                ", quota_minute=" + quota_minute +
                ", quota_hour=" + quota_hour +
                ", quota_day=" + quota_day +
                ", quota_month=" + quota_month +
                ", quota_year=" + quota_year +
                ", quota_overall=" + quota_overall +
                '}';
    }
}
