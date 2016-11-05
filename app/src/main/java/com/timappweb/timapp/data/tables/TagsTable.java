package com.timappweb.timapp.data.tables;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventTag;
import com.timappweb.timapp.data.models.EventTag_Table;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.Tag_Table;

/**
 * Created by Stephane on 03/11/2016.
 */

public class TagsTable {


    public static Where<Tag> querySuggestTagForEvent(Event event) {
        return SQLite.select()
                .from(Tag.class)
                .leftOuterJoin(EventTag.class)
                .on(Tag_Table.id.withTable().eq(EventTag_Table.tag_id))
                .where(EventTag_Table.event_id.eq(event.id))
                .orderBy(EventTag_Table.count_ref, false);
    }

}
