package com.timappweb.timapp.data.tables;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.EventsInvitation_Table;
import com.timappweb.timapp.data.models.User;

import java.util.List;

/**
 * Created by Stephane on 03/11/2016.
 */

public class EventInvitationsTable extends BaseTable{

    public static List<EventsInvitation> getSentInvitationsByUser(User user, Event event) {
        return new SQLite().select()
                .from(EventsInvitation.class)
                .where(EventsInvitation_Table.event_id.eq(event.id))
                .and(EventsInvitation_Table.user_source_id.eq(user.id))
                .queryList();
    }

    public static Where<EventsInvitation> inviteReceived(User currentUser) {
        return SQLite.select()
                .from(EventsInvitation.class)
                .where(EventsInvitation_Table.user_target_id.eq(currentUser.id));
    }
}
