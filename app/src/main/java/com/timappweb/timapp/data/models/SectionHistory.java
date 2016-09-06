package com.timappweb.timapp.data.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.timappweb.timapp.data.loader.SectionContainer;

/**
 * Created by stephane on 5/8/2016.
 */
@Table(name = "SectionHistory")
public class SectionHistory extends MyModel {
    private static final String TAG = "SyncHistory";

    /**
     * Sync group key
     */
    @Column(name = "GroupKey", notNull = true)
    String group_key;

    /**
     * last update (unix timestamp in millis)
     */
    @Column(name = "LastUpdate", notNull = true)
    long last_update;

    /**
     * Section start
     */
    @Column(name = "Start", notNull = true)
    long start;

    /**
     * Section end
     */
    @Column(name = "End", notNull = true)
    long end;

    public static boolean contains(String key, SectionContainer.PaginatedSection section) {
        From from = new Select().from(SectionHistory.class)
                .where("GroupKey = ?", key);
        if (section.getEnd() > section.getStart()){
            from.where("Start <= ? AND end >= ?", section.getStart(), section.getEnd());
        }
        else{
            from.where("Start >= ? AND end <= ?", section.getStart(), section.getEnd());
        }
        return from.exists();
    }

    public static void add(String groupKey, SectionContainer.PaginatedSection section) {
        SectionHistory history = new SectionHistory();
        history.end = section.end;
        history.start = section.start;
        history.group_key = groupKey;
        history.last_update = section.lastUpdate;
        history.save();
    }
}
