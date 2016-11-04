package com.timappweb.timapp.data.loader.paginate;

import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.Where;

/**
 * Created by Stephane on 03/11/2016.
 */

public interface IPaginateFilter {
    String toServerParams();
    void orderBy(Where query);
    SQLCondition strictCondition();
    SQLCondition equalsCondition();
}
