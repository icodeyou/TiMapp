package com.timappweb.timapp.database.models;

/**
 * Created by stephane on 4/5/2016.
 */
public class UserQuota extends Model{

    @Column(name = "Type", index = true)
    public QuotaType type;

    @Column(name = "User", index = true)
    public User user;

    @Column(name = "MinDelay")
    public int min_delay;

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

    public static QuotaType getByType(String type) {
        return new Select()
                .from(QuotaType.class)
                .where("Type = ?", type)
                .executeSingle();
    }

}
