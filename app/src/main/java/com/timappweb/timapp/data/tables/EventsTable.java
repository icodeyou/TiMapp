package com.timappweb.timapp.data.tables;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Event_Table;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.User_Table;

/**
 * Created by Stephane on 03/11/2016.
 */

public class EventsTable {

    public static Event load(long id) {
        return SQLite.select()
                .from(Event.class)
                .where(Event_Table.id.eq(id))
                .querySingle();
    }

}
