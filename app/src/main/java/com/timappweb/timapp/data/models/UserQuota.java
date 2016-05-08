package com.timappweb.timapp.data.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.utils.Util;

import java.util.Calendar;
import java.util.List;

/**
 * Created by stephane on 4/5/2016.
 */
@Table(name = "UserQuotas")
public class UserQuota extends SyncBaseModel {

    private static final String TAG = "UserQuota";
    public String _quota_error_reason;

    @Expose
    @Column(name = "QuotaTypeId", index = true, uniqueGroups = {"uniqueUserQuota"}, onUniqueConflicts = {Column.ConflictAction.REPLACE})
    public int remote_id;

    @Expose
    @Column(name = "UserId", index = true, uniqueGroups = {"uniqueUserQuota"}, onUniqueConflicts = {Column.ConflictAction.REPLACE})
    public int user_id;

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


    public UserQuota() {
        super();
    }

    public static List<UserQuota> all(){
        return new Select().from(UserQuota.class)
                .execute();
    }

    public static UserQuota get(int user_id, int type){
        UserQuota userQuota = new Select()
                .from(UserQuota.class)
                .where("UserId = ?", user_id)
                .where("QuotaTypeId = ?", type)
                .executeSingle();
        return userQuota;
    }

    public boolean hasValidQuotas() {
        this.resetQuotas();

        if (this.min_delay != 0 && Util.delayFromNow((int)this.last_activity) < this.min_delay){
            setMinDelayReason();
            return false;
        }

        return  _checkQuota(this.count_minute, this.quota_minute, "minute")
                && _checkQuota(this.quota_hour, this.quota_hour, "hour")
                && _checkQuota(this.quota_day, this.quota_day, "day")
                && _checkQuota(this.quota_month, this.quota_month, "month")
                && _checkQuota(this.count_year, this.quota_year, "year")
                && _checkQuota(this.count_overall, this.quota_overall, "life");
    }

    public void setMinDelayReason(){
        int mustWaitSeconds = (int)this.min_delay - Util.delayFromNow((int)this.last_activity);
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


    public static void increment(int userId, int type) {
        UserQuota userQuota = UserQuota.get(userId, type);
        if (userQuota == null){
            Log.i(TAG, "Quota type: " + type + " does not exists");
            return;
        }
        Log.d(TAG, "Updated user quota: " + userQuota);
        userQuota.increment();
        userQuota.save();
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

    public void resetQuotas(){
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
                " type=" + remote_id +
                ", user=" + user_id +
                ", last_activity=" + last_activity + "/" + min_delay + " ("+ Util.delayFromNow((int)last_activity)+" sec)" +
                ", minute=" + count_minute + "/" + quota_minute +
                ", hour=" + count_hour + "/" + quota_hour +
                ", day=" + count_day + "/" + quota_day +
                ", month=" + count_month + "/" + quota_month +
                ", year=" + count_year + "/" + quota_year +
                ", overall=" + count_overall + "/" + quota_overall +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UserQuota userQuota = (UserQuota) o;

        if (remote_id != userQuota.remote_id) return false;
        if (user_id != userQuota.user_id) return false;
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

        if (remote_id != userQuota.remote_id) return false;
        if (user_id != userQuota.user_id) return false;
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
        result = 31 * result + remote_id;
        result = 31 * result + user_id;
        return result;
    }


}
