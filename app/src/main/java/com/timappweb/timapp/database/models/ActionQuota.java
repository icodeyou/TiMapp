package com.timappweb.timapp.database.models;

/**
 * Created by stephane on 4/4/2016.
 */
public class ActionQuota {

    public User user;
    public ActionType actionType;

    public int last_action;
    public int min_delay;

    public int total_day;
    public int quota_day;

    public int total_week;
    public int quota_week;

    public int total_month;
    public int quota_month;

    public int total_year;
    public int quota_year;

    public int total_overall;
    public int quota_overall;
}
