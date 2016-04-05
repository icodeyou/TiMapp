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

    @Column(name = "Type", index = true)
    public String type;
    
    @Column(name = "MinDelay")
    public int min_delay;

    @Column(name = "TotalMinute")
    public int total_minute;

    @Column(name = "TotalHour")
    public int total_hour;

    @Column(name = "TotalDay")
    public int total_day;

    @Column(name = "TotalMonth")
    public int total_month;

    @Column(name = "TotalYear")
    public int total_year;

    @Column(name = "TotalOverall")
    public int total_overall;

    public QuotaType() {
        super();
    }

    public static QuotaType getByType(String type) {
        return new Select()
                .from(QuotaType.class)
                .where("Type = ?", type)
                .executeSingle();
    }
}
