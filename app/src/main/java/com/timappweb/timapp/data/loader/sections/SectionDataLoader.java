package com.timappweb.timapp.data.loader.sections;

import android.util.Log;

import com.timappweb.timapp.data.loader.DataLoaderInterface;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;
import com.timappweb.timapp.utils.Util;

import java.util.List;

import retrofit2.Response;

import static com.timappweb.timapp.data.loader.sections.SectionContainer.*;

/**
 * Pagination store on app side
 *
 * @param <T>
 */
public class SectionDataLoader<T> implements DataLoaderInterface {


    private long _lastLoad = 0;

    public enum LoadType {MORE, UPDATE, NEWEST}

    private static final String TAG = "PaginatedDataLoader";

    // ---------------------------------------------------------------------------------------------

    private long minDelayForceRefresh;
    protected SectionDataProviderInterface dataProvider;
    protected SectionContainer sectionContainer;
    protected SectionBoundsFormatter formatter;
    protected Callback<T> callback;
    private boolean useCache;
    private CacheEngine cacheEngine;

    protected boolean _isFullyLoaded;
    //protected long _lastUpdate;
    private HttpCallManager currentCallManager;

    // ---------------------------------------------------------------------------------------------

    public SectionDataLoader() {
        this.sectionContainer = new SectionContainer();
    }

    // ---------------------------------------------------------------------------------------------


    public SectionDataLoader<T> useCache(boolean cache) {
        this.useCache = cache;
        return this;
    }

    public PaginateDirection getOrder() {
        return sectionContainer.getOrder();
    }


    public SectionDataLoader<T> setCacheEngine(CacheEngine<T> cacheEngine) {
        this.cacheEngine = cacheEngine;
        this.useCache = true;
        return this;
    }

    public SectionDataLoader<T> setOrder(PaginateDirection order) {
        this.sectionContainer.setOrder(order);
        return this;
    }

    public SectionContainer getSectionContainer() {
        return sectionContainer;
    }

    public SectionDataProviderInterface getDataProvider() {
        return dataProvider;
    }

    public SectionDataLoader setFormatter(SectionBoundsFormatter formatter) {
        this.formatter = formatter;
        return this;
    }

    public boolean isFullyLoaded() {
        return _isFullyLoaded;
    }

    public boolean isLoading() {
        return currentCallManager != null && !currentCallManager.isDone();
    }

    public SectionDataLoader setCallback(Callback<T> callback) {
        this.callback = callback;
        return this;
    }

    public SectionDataLoader setMinDelayRefresh(long minDelayForceRefresh) {
        this.minDelayForceRefresh = minDelayForceRefresh;
        return this;
    }

    public SectionDataLoader setDataProvider(SectionDataProviderInterface<T> dataProvider) {
        this.dataProvider = dataProvider;
        return this;
    }

    public CacheEngine getCacheEngine() {
        return cacheEngine;
    }

    /**
     * Load a new section. Cannot be trigger if there is already a
     * @return
     */
    public boolean loadMore(){
        if (_isFullyLoaded || isLoading()){
            return false;
        }
        PaginatedSection lastSection = sectionContainer.last();
        final PaginatedSection newSection = createOlderSection(lastSection);

        //if (newSection.getEnd() == -1) return false;

        newSection.setLoadType(LoadType.MORE);
        sectionContainer.addSection(newSection);
        this.load(newSection);
        return true;
    }

    public boolean firstLoad() {
        return this.loadNewest();
    }

    private void load(PaginatedSection section) {
        synchronized (this) {
            if (this.useCache && this.cacheEngine.contains(section)) {
                List<T> data = this.cacheEngine.get(section);
                section.setStatus(LoadStatus.DONE);
                if (callback != null) callback.onLoadEnd(section, data);
            } else {
                this.remoteLoad(section);
            }
        }
    }


    public boolean loadNewest(){
        if (isLoading()){
            return false;
        }
        if (!Util.isOlderThan(this._lastLoad, this.minDelayForceRefresh)){
            return false;
        }
        PaginatedSection section = sectionContainer.first();
        // Check if we need to load newest
        if (        section == null
                ||  section.isStatus(LoadStatus.DONE)){
            PaginatedSection newSection = createNewerSection(section);
            if (section == null || newSection.getEnd() > 0){
                newSection.setLoadType(LoadType.NEWEST);
                this.load(newSection);
                return true;
            }
        }
        return false;
    }

    public PaginatedSection createNewerSection(PaginatedSection section) {
        return new PaginatedSection(-1, section != null ? section.start + (this.getOrder() == PaginateDirection.ASC ? 1 : -1) : -1);
    }

    public PaginatedSection createOlderSection(PaginatedSection section) {
        long start = section == null ? -1 : section.end + (this.getOrder() == PaginateDirection.ASC ? -1 : 1);
        return new PaginatedSection(start, -1);
    }

    private void remoteLoad(final PaginatedSection newSection) {
        currentCallManager = dataProvider.remoteLoad(newSection)
                .onResponse(new HttpCallback<ResponseSyncWrapper<T>>() {
                    @Override
                    public void successful(ResponseSyncWrapper<T> feedback) {
                        // Update min and max ids
                        if (feedback.getCount() > 0){
                            if (newSection.getLoadType() == LoadType.NEWEST){
                                newSection.setEnd(feedback.getLimit() == feedback.getCount() ? formatter.format(feedback.getLastItem()) : newSection.getEnd());
                                newSection.setStart(formatter.format(feedback.getFirstItem()));
                            }
                            else {
                                if (newSection.getEnd() == -1) {
                                    newSection.setEnd(formatter.format(feedback.getLastItem()));
                                }
                                if (newSection.getStart() == -1) {
                                    newSection.setStart(formatter.format(feedback.getFirstItem()));
                                }
                            }
                        }
                        // if no more data => set loadmore done
                        if (newSection.getLoadType() == LoadType.MORE){
                            _isFullyLoaded = feedback.getCount() <= 0 || feedback.up_to_date;
                        }
                        // If data does not meet, remove older ones...
                        else if (newSection.getLoadType() == LoadType.NEWEST){
                            if (feedback.getCount() == feedback.getLimit()){
                                PaginatedSection section = sectionContainer.findOlderSection(newSection);
                                if (section == null){
                                    Log.d(TAG, "Clearing sections...");
                                    sectionContainer.clear();
                                    sectionContainer.addSection(newSection);
                                }
                            }
                        }

                        Log.d(TAG, "Section loaded with success: " + newSection);
                        newSection.setStatus(LoadStatus.DONE);
                        if (useCache && cacheEngine != null){
                            cacheEngine.add(newSection, feedback.items);
                        }
                        if (callback != null) callback.onLoadEnd(newSection, feedback.items);
                    }

                    @Override
                    public void notSuccessful() {
                        Log.e(TAG, "Cannot load section: " + newSection + ". HTTP code: " + this.response.code());
                        newSection.setStatus(LoadStatus.ERROR);
                        if (callback != null) callback.onLoadError(new CannotSyncException("Server response is not successfull", 0), newSection);

                    }
                })
                .onError(new RequestFailureCallback(){
                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG, "Cannot load section: " + newSection + ". Error: " + error.getMessage());
                        newSection.setStatus(LoadStatus.ERROR);
                        if (callback != null) callback.onLoadError(error, newSection);
                    }
                })
                .onFinally(new HttpCallManager.FinallyCallback() {
                    @Override
                    public void onFinally(Response response, Throwable error) {
                        switch (newSection.getLoadType()){
                            case NEWEST:
                                SectionDataLoader.this._lastLoad = System.currentTimeMillis();
                                break;
                        }
                    }
                })
                .perform();
    }

    /**
     *
     * @param <T>
     */
    public interface Callback<T>{

        void onLoadEnd(PaginatedSection section, List<T> data);

        void onLoadError(Throwable error, PaginatedSection section);

    }

    /**
     *
     * @param <T>
     */
    public interface SectionBoundsFormatter<T>{

        long format(T data);

    }

    /**
     *
     * @param <T>
     */
    public interface CacheEngine<T>{

        void add(PaginatedSection<T> section, List<T> data);

        boolean contains(PaginatedSection<T> section);

        List<T> get(PaginatedSection<T> section);

    }
}

