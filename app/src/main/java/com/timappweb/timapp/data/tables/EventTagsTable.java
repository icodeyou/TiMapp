package com.timappweb.timapp.data.tables;

import android.util.Log;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventTag;
import com.timappweb.timapp.data.models.EventTag_Table;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;

import java.util.List;

/**
 * Created by Stephane on 03/11/2016.
 */

public class EventTagsTable {

    private static final String TAG = "EventTagsTable";

    /**
     * Increment the tag counter by one for each given tag in the list
     * @param tags
     */
    public static void incrementCountRef(final Event event, final List<Tag> tags) {
        DatabaseDefinition database = FlowManager.getDatabase(AppDatabase.class);
        database.executeTransaction(new ITransaction() {
            @Override
            public void execute(DatabaseWrapper databaseWrapper) {
                for (Tag tag: tags){
                    try{
                        tag.mySave();
                        EventTag existingEventTag = SQLite.select().from(EventTag.class)
                                .where(EventTag_Table.tag_id.eq(tag.id))
                                .and(EventTag_Table.event_id.eq(event.id))
                                .querySingle();
                        // Insert if does not exists
                        if (existingEventTag != null){
                            existingEventTag.count_ref += 1;
                            existingEventTag.mySave();
                            //new Update(EventTag.class)
                            //        .set("CountRef = CountRef + 1")
                            //        .where("EventTag.Tag = ? AND EventTag.Event = ?", tag.getId(), event.getId())
                            //        .execute();
                        }
                        // Update existing record
                        else{
                            new EventTag(event, tag, 1).mySave();
                        }
                    } catch (CannotSaveModelException e) {
                        Log.e(TAG, "Cannot save model: " + e.getMessage());
                    }
                }
            }
        });
    }

}
