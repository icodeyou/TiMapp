package com.timappweb.timapp.data.loader.sections;

import android.util.Log;

import com.timappweb.timapp.data.loader.DataLoaderInterface;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;
import com.timappweb.timapp.utils.KeyValueStorage;
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
    private PaginatedSection currentLoadSection;

    private long minDelayForceRefresh;
    private long minDelayAutoRefresh;

    public enum LoadType {MORE, UPDATE, NEWEST}

    private static final String TAG = "SectionDataLoader";

    // ---------------------------------------------------------------------------------------------

    protected SectionDataProviderInterface dataProvider;
    protected SectionContainer sectionContainer;
    protected SectionBoundsFormatter formatter;
    protected Callback<T> callback;
    private boolean useCache;
    private CacheEngine<T> cacheEngine;

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

    public synchronized boolean isLoading() {
        return currentLoadSection != null;
    }
    public synchronized boolean isLoading(LoadType type) {
        return currentLoadSection != null && currentLoadSection.getLoadType() == type;
    }

    public SectionDataLoader setCallback(Callback<T> callback) {
        this.callback = callback;
        return this;
    }

    public SectionDataLoader setMinDelayAutoRefresh(long minDelayAutoRefresh) {
        this.minDelayAutoRefresh = minDelayAutoRefresh;
        return this;
    }

    public SectionDataLoader setMinDelayForceRefresh(long minDelayForceRefresh) {
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
        Log.d(TAG, "Trigger a @loadMore()");
        if (_isFullyLoaded){
            Log.d(TAG, "    - Abort, already  fully loaded");
            return false;
        }
        PaginatedSection lastSection = sectionContainer.last();
        final PaginatedSection newSection = createOlderSection(lastSection);

        //if (newSection.getEnd() == -1) return false;

        newSection.setLoadType(LoadType.MORE);
        return this.load(newSection);
    }


    public boolean loadNewest() {
        Log.d(TAG, "Trigger a @loadNewest()");

        if (this.requireLoadNewest()) {
            Log.d(TAG, "Min delay to force refresh is not respected. Cancel load newest.");
            return false;
        }

        PaginatedSection section = sectionContainer.first();
        // Check if we need to load newest
        if (section == null
                || section.isStatus(LoadStatus.DONE)) {
            PaginatedSection newSection = createNewerSection(section);
            if (section == null || newSection.getEnd() > 0) {
                newSection.setLoadType(LoadType.NEWEST);
                return this.load(newSection);
            }
        }
        return false;
    }


    public long getLastLoadNewest() {
        return 0; // TODO
    }

    private boolean requireLoadNewest() {
        return this.minDelayForceRefresh > 0 &&
                (System.currentTimeMillis() - this.getLastLoadNewest()) <= this.minDelayForceRefresh;
    }

    /**
     * Only one load at a time garantee!
     * @param section
     * @return true if load start, false otherwise
     */
    private boolean load(PaginatedSection section) {
        synchronized (this) {
            if (isLoading()) {
                Log.d(TAG, "    - Already loading another section. Abort: " + section);
                return false;
            }
            Log.d(TAG, "    - Init loading new section: " + section);
            currentLoadSection = section;
        }
        if (this.useCache && this.cacheEngine.contains(section)) {
            Log.d(TAG, "    - Using cache for section" + section);
            List<T> data = this.cacheEngine.get(section);
            section.setStatus(LoadStatus.DONE);
            onLoadSectionEnd();
            if (callback != null) callback.onLoadEnd(section, data);
        } else {
            this.remoteLoad(section);
        }
        return true;
    }


    private PaginatedSection createNewerSection(PaginatedSection section) {
        return new PaginatedSection(-1, section != null ? section.start + (this.getOrder() == PaginateDirection.ASC ? 1 : -1) : -1);
    }

    private PaginatedSection createOlderSection(PaginatedSection section) {
        long start = section == null ? -1 : section.end + (this.getOrder() == PaginateDirection.ASC ? -1 : 1);
        return new PaginatedSection(start, -1);
    }

    private void remoteLoad(final PaginatedSection newSection) {
        Log.d(TAG, "    - Start remote load for section: " + newSection);
        currentCallManager = dataProvider.remoteLoad(newSection)
                .onResponse(new HttpCallback<ResponseSyncWrapper<T>>() {
                    @Override
                    public void successful(ResponseSyncWrapper<T> feedback) {
                        // Update min and max ids
                        if (feedback.getCount() > 0){
                            if (newSection.getEnd() == -1) {
                                newSection.setEnd(formatter.format(feedback.getLastItem()));
                            }
                            if (newSection.getStart() == -1) {
                                newSection.setStart(formatter.format(feedback.getFirstItem()));
                            }
                        }
                        // if no more data => set loadmore done
                        if (newSection.getLoadType() == LoadType.MORE){
                            _isFullyLoaded = feedback.getCount() <= 0 || feedback.up_to_date;
                                Log.d(TAG, "    - Has more data = " + !_isFullyLoaded + " (count = " + feedback.getCount() + ", up to date = " + feedback.up_to_date + ")");
                        }
                        // If data does not meet, remove older ones...
                        else if (newSection.getLoadType() == LoadType.NEWEST){
                            if (feedback.getCount() == feedback.getLimit()){
                                PaginatedSection section = sectionContainer.findOlderSection(newSection);
                                if (section == null){
                                    Log.d(TAG, "    - Sections does not follow, clearing older sections...");
                                    sectionContainer.clear();
                                }
                            }
                        }

                        Log.d(TAG, "    - Section loaded with success: " + newSection);
                        newSection.setStatus(LoadStatus.DONE);
                        if (useCache && cacheEngine != null){
                            cacheEngine.add(newSection, feedback.items);
                        }
                        if (callback != null) callback.onLoadEnd(newSection, feedback.items);
                    }

                    @Override
                    public void notSuccessful() {
                        Log.e(TAG, "    - Cannot load section: " + newSection + ". HTTP code: " + this.response.code());
                        newSection.setStatus(LoadStatus.ERROR);
                        if (callback != null) callback.onLoadError(new CannotSyncException("Server response is not successfull", 0), newSection);

                    }
                })
                .onError(new RequestFailureCallback(){
                    @Override
                    public void onError(Throwable error) {
                        Log.e(TAG, "     - Cannot load section: " + newSection + ". Error: " + error.getMessage());
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
                        onLoadSectionEnd();
                    }
                })
                .perform();
    }


    private synchronized void onLoadSectionEnd(){
        if (currentLoadSection == null){
            return; // Should not happen...
        }
        if (currentLoadSection.getEnd() == -1 || currentLoadSection.getStart() == -1){
            // If no data we don't add the section
            Log.d(TAG, "    - [END] No data for this section. " + currentLoadSection);
        }
        else{
            Log.i(TAG, "    - [END] Adding section: " + currentLoadSection);
            sectionContainer.addSection(currentLoadSection);
        }
        currentLoadSection = null;
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

