package com.timappweb.timapp.data.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.timappweb.timapp.exceptions.BadConfigurationError;

/**
 * Created by stephane on 4/4/2016.
 */
@Table(name = "QuotaTypes")
public class QuotaType extends BaseModel{

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
        QuotaType quotaType = new Select()
                .from(QuotaType.class)
                .where("Type = ?", type)
                .executeSingle();
        if (quotaType == null){
            throw new BadConfigurationError();
        }
        return quotaType;
    }

    @Override
    public String toString() {
        return "QuotaType{" +
                "type='" + type + '\'' +
                ", min_delay=" + min_delay +
                ", quota_minute=" + quota_minute +
                ", quota_hour=" + quota_hour +
                //", quota_day=" + quota_day +
                //", quota_month=" + quota_month +
                //", quota_year=" + quota_year +
                //", quota_overall=" + quota_overall +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        QuotaType quotaType = (QuotaType) o;

        if (min_delay != quotaType.min_delay) return false;
        if (quota_minute != quotaType.quota_minute) return false;
        if (quota_hour != quotaType.quota_hour) return false;
        if (quota_day != quotaType.quota_day) return false;
        if (quota_month != quotaType.quota_month) return false;
        if (quota_year != quotaType.quota_year) return false;
        if (quota_overall != quotaType.quota_overall) return false;
        return type.equals(quotaType.type);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
