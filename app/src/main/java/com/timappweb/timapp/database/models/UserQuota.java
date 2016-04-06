package com.timappweb.timapp.database.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.timappweb.timapp.utils.Util;

/**
 * Created by stephane on 4/5/2016.
 */
public class UserQuota extends Model {

    @Column(name = "Type", index = true)
    public QuotaType type;

    @Column(name = "UserId", index = true)
    public int user_id;

    @Column(name = "LastActivity")
    public int last_activity;

    @Column(name = "MaxPerMinute")
    public int quota_minute;

    @Column(name = "MaxPerHour")
    public int quota_hour;

    @Column(name = "MaxPerDay")
    public int quota_day;

    @Column(name = "MaxPerMonth")
    public int quota_month;

    @Column(name = "MaxPerYear")
    public int quota_year;

    @Column(name = "MaxOverall")
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

    public static UserQuota get(int user_id, QuotaType type){
        UserQuota userQuota = (UserQuota) new Select()
                .from(UserQuota.class)
                .where("UserId = ?", user_id)
                .where("Type = ?", type)
                .execute();
        if (userQuota == null){
            userQuota = new UserQuota(user_id, type);
            userQuota.save();
        }
        return userQuota;
    }

    public boolean hasValidQuotas() {
        return Util.isOlderThan(this.last_activity, this.type.min_delay)
                && this.quota_minute < this.type.quota_minute
                && this.quota_hour < this.type.quota_hour
                && this.quota_month < this.type.quota_month
                && this.quota_year < this.type.quota_year
                && this.quota_overall < this.type.quota_overall;
    }

    public static void increment(int id, QuotaType type) {
        Update stmt = new Update(UserQuota.class);
        stmt.set("quota_minute = quota_minute + 1" +
                " AND quota_hour = quota_hour + 1" +
                " AND quota_month = quota_month + 1" +
                " AND quota_year = quota_year + 1" +
                " AND quota_overall = quota_overall + 1" +
                " AND last_activity = ? ", Util.getCurrentTimeSec())
                .where("UserId = ? " , id)
                .where("Type = ? " , id)
                .execute();
    }
}
