package com.timappweb.timapp.data.models;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.property.Property;

/**
 * Created by Stephane on 04/11/2016.
 */

public class LocalModel extends MyModel{

    @PrimaryKey(autoincrement = true)
    public int id;

    @Override
    public void deleteAssociation(Class<? extends MyModel> associationModel, Property property) {
        SQLite.delete(associationModel)
                .where(property.eq(this.id))
                .execute();
    }
}
