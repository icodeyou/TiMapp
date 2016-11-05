package com.timappweb.timapp.data.loader.paginate;

import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.sql.language.property.BaseProperty;
import com.raizlabs.android.dbflow.sql.language.property.IntProperty;
import com.raizlabs.android.dbflow.sql.language.property.LongProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.timappweb.timapp.data.models.MyModel;
import com.timappweb.timapp.data.models.SyncBaseModel;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Created by Stephane on 03/11/2016.
 */
public class PaginateFilter {

    public final boolean asc;
    public final BaseProperty localField;
    public final String remoteField;
    public final CursorPaginateDataLoader.FilterValueTransformer transformer;
    public Object value;

    public PaginateFilter(BaseProperty localField, String remoteField, boolean asc, CursorPaginateDataLoader.FilterValueTransformer<? extends MyModel> transformer) {
        this.asc = asc;
        this.localField = localField;
        this.remoteField = remoteField;
        this.transformer = transformer;
    }

    public String toServerParams() {
        String res = this.remoteField + ":" + (this.asc ? "asc" : "desc");
        if (value != null) {
            res += ":" + value;
        }
        return res;
    }

    public void orderBy(Where query) {
        query.orderBy(localField, asc);
    }

    public void updateValue(MyModel model) {
        this.value = this.transformer.transform(model);
    }

    public SQLCondition strictCondition() {
        if (asc) {
            return column(this.localField.getNameAlias()).greaterThan(value);
        } else {
            return column(this.localField.getNameAlias()).lessThan(value);
        }
    }

    public SQLCondition equalsCondition() {
        return column(this.localField.getNameAlias()).eq(value);
    }


}
