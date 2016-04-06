package com.timappweb.timapp.database.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;

/**
 * Created by stephane on 4/4/2016.
 */
public class QuotaType extends Model{

    public static final String PLACE = "places";
    public static final String PICTURE = "pictures";
    public static final String POST = "posts";
    public static final String FRIENDS = "places_invitations";
    public static final String NOTIFY_COMING = "places_users";

    @Column(name = "Type", index = true)
    public String type;
    
    @Column(name = "MinDelay")
    public int min_delay;

    @Column(name = "TotalMinute")
    public int quota_minute;

    @Column(name = "TotalHour")
    public int quota_hour;

    @Column(name = "TotalDay")
    public int quota_day;

    @Column(name = "TotalMonth")
    public int quota_month;

    @Column(name = "TotalYear")
    public int quota_year;

    @Column(name = "TotalOverall")
    public int quota_overall;

    public QuotaType() {
        super();
    }

    public QuotaType(String type, int min_delay, int quota_minute, int quota_hour, int quota_day, int quota_month, int quota_year, int quota_overall) {
        this.type = type;
        this.min_delay = min_delay;
        this.quota_minute = quota_minute;
        this.quota_hour = quota_hour;
        this.quota_day = quota_day;
        this.quota_month = quota_month;
        this.quota_year = quota_year;
        this.quota_overall = quota_overall;
    }

    public static QuotaType getByType(String type) {
        return new Select()
                .from(QuotaType.class)
                .where("Type = ?", type)
                .executeSingle();
    }
}
