package com.timappweb.timapp.data.tables;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventTag;
import com.timappweb.timapp.data.models.EventTag_Table;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.Tag_Table;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserTag;
import com.timappweb.timapp.data.models.UserTag_Table;
import com.timappweb.timapp.data.models.User_Table;

import java.util.List;

/**
 * Created by Stephane on 03/11/2016.
 */

public class UsersTable {

    public static User load(long id) {
        return SQLite.select()
                .from(User.class)
                .where(User_Table.id.eq(id))
                .querySingle();
    }

    public static List<Tag> loadUserTags(User user) {
        return SQLite.select()
                .from(Tag.class)
                .innerJoin(UserTag.class)
                .on(UserTag_Table.tag_id.eq(Tag_Table.id.withTable()))
                .where(UserTag_Table.user_id.eq(user.id))
                .queryList();
    }
}
