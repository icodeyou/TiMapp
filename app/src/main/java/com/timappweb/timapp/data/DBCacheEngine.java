package com.timappweb.timapp.data;

import android.util.Log;

import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.timappweb.timapp.data.loader.sections.SectionDataLoader;
import com.timappweb.timapp.data.loader.sections.SectionContainer;
import com.timappweb.timapp.data.models.SectionHistory;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.utils.Util;

import java.util.List;

/**
 * Created by Stephane on 06/09/2016.
 */
public abstract class DBCacheEngine<T extends SyncBaseModel> implements SectionDataLoader.CacheEngine<T> {

    private static final String TAG = "DBCacheEngine";

    // ---------------------------------------------------------------------------------------------

    private final Class<T>      clazz;
    private int                 limit       = 10;

    // ---------------------------------------------------------------------------------------------

    public DBCacheEngine(Class<T> clazz) {
        this.clazz = clazz;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
    protected String getHashKey(){
        return clazz.getCanonicalName();
    }

    protected abstract void persist(List<T> data) throws Exception;

    @Override
    public void add(SectionContainer.PaginatedSection<T> section, List<T> data) {
        try {
            this.persist(data);
            SectionHistory.add(this.getHashKey(), section);
        } catch (Exception e) {
            Log.e(TAG, "Cannot persist data: " + e.getMessage());
        }
    }

    @Override
    public boolean contains(SectionContainer.PaginatedSection<T> section) {
        if (section.getEnd() == -1 && section.getStart() == -1){
            return SectionHistory.getFirst(this.getHashKey()) != null;
        }
        return SectionHistory.contains(this.getHashKey(), section);
    }

    @Override
    public List<T> get(SectionContainer.PaginatedSection<T> section) {
        if (section.getEnd() == -1 && section.getStart() == -1){
            SectionHistory firstCachedSection = SectionHistory.getFirst(this.getHashKey());
            Util.appAssert(firstCachedSection != null, TAG, "Should not be null.");
            section.setStart(firstCachedSection.start);
            section.setEnd(firstCachedSection.end);
        }

        From query = new Select().from(clazz)
                .orderBy("SyncId DESC")
                .limit(this.limit);

        if (section.getEnd() == -1){
           query.where("SyncId <= ? ", section.getStart());
        }
        else if (section.getStart() == -1){
            query.where("SyncId >= ? ", section.getEnd());
        }
        else{
            query.where("SyncId >= ? AND SyncId <= ?", section.getStart(), section.getEnd());
        }

        return query.execute();
    }

}
