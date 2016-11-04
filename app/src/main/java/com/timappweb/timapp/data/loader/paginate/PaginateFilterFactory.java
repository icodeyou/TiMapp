package com.timappweb.timapp.data.loader.paginate;

import com.raizlabs.android.dbflow.sql.language.property.BaseProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.timappweb.timapp.data.models.SyncBaseModel;

/**
 * Created by Stephane on 03/11/2016.
 */

public class PaginateFilterFactory {

    private static CursorPaginateDataLoader.FilterValueTransformer<SyncBaseModel> syncIdTransformer;
    private static CursorPaginateDataLoader.FilterValueTransformer<SyncBaseModel> createdIdTransformer;

    public static PaginateFilter createSyncIdFilter(Property property) {
        return new PaginateFilter(property, "id", false, getSyncIdTransformer());
    }

    public static CursorPaginateDataLoader.FilterValueTransformer<SyncBaseModel> getSyncIdTransformer() {
        if (syncIdTransformer == null) {
            syncIdTransformer = new CursorPaginateDataLoader.FilterValueTransformer<SyncBaseModel>() {
                @Override
                public Object transform(SyncBaseModel model) {
                    return model.getRemoteId();
                }
            };
        }
        return syncIdTransformer;
    }

    public static PaginateFilter createCreatedFilter(BaseProperty property) {
        return new PaginateFilter(property, "created", false, getCreatedTransformer());
    }

    public static CursorPaginateDataLoader.FilterValueTransformer<SyncBaseModel> getCreatedTransformer() {
        if (createdIdTransformer == null) {
            createdIdTransformer = new CursorPaginateDataLoader.FilterValueTransformer<SyncBaseModel>() {
                @Override
                public Object transform(SyncBaseModel model) {
                    return model.created;
                }
            };
        }
        return createdIdTransformer;
    }
}
