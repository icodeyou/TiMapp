package com.timappweb.timapp.database.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;
import com.google.gson.annotations.SerializedName;

/**
 * Created by stephane on 4/4/2016.
 */
public class ActionType extends Model{

    @Column(name = "Name", index = true)
    public ActionTypeName name;

    @Column(name = "MinDelay")
    public int min_delay;

    @Column(name = "MaxPerHour")
    public int max_per_hour;

    @Column(name = "MaxPerDay")
    public int max_per_day;

    @Column(name = "MaxPerWeek")
    public int max_per_week;

    @Column(name = "MaxPerMonth")
    public int max_per_month;

    public ActionType() {
        super();
    }

    public static ActionType getByName(ActionTypeName type) {
        return new Select()
                .from(ActionType.class)
                .where("Name = ?", type)
                .executeSingle();
    }

    public enum ActionTypeName {
        @SerializedName("create_place")
        CREATE_PLACE(),
        @SerializedName("add_picture")
        ADD_PICTURE,
        @SerializedName("add_post")
        ADD_POST,
        @SerializedName("invite_people")
        INVITE_PEOPLE
    }
}
