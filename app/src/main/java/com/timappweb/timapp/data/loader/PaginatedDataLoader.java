package com.timappweb.timapp.data.loader;

import android.util.Log;

import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;

import java.util.List;

import static com.timappweb.timapp.data.loader.SectionContainer.*;

public class PaginatedDataLoader<T> {


    public enum LoadType {MORE, UPDATE, NEWEST}

    private static final String TAG = "PaginatedDataLoader";

    // ---------------------------------------------------------------------------------------------

    private long minDelayForceRefresh;
    protected PaginatedDataProviderInterface dataProvider;
    protected SectionContainer sectionContainer;
    protected SectionBoundsFormatter formatter;
    protected Callback<T> callback;
    private boolean useCache;
    private CacheEngine cacheEngine;

    protected boolean _isFullyLoaded;
    //protected long _lastUpdate;

    // ---------------------------------------------------------------------------------------------

    public PaginatedDataLoader() {
        this.sectionContainer = new SectionContainer();
    }

    // ---------------------------------------------------------------------------------------------


    public PaginatedDataLoader<T> useCache(boolean cache) {
        this.useCache = cache;
        return this;
    }

    public PaginateDirection getOrder() {
        return sectionContainer.getOrder();
    }


    public PaginatedDataLoader<T> setCacheEngine(CacheEngine<EventsInvitation> cacheEngine) {
        this.cacheEngine = cacheEngine;
        this.useCache = true;
        return this;
    }

    public PaginatedDataLoader<T> setOrder(PaginateDirection order) {
        this.sectionContainer.setOrder(order);
        return this;
    }

    public SectionContainer getSectionContainer() {
        return sectionContainer;
    }

    public PaginatedDataProviderInterface getDataProvider() {
        return dataProvider;
    }

    public PaginatedDataLoader<T> setFormatter(SectionBoundsFormatter<T> formatter) {
        this.formatter = formatter;
        return this;
    }

    public boolean isFullyLoaded() {
        return _isFullyLoaded;
    }
    public boolean isLoading() {
        return sectionContainer.isLoading();
    }

    public PaginatedDataLoader setCallback(Callback<T> callback) {
        this.callback = callback;
        return this;
    }

    public PaginatedDataLoader setMinDelayForceRefresh(long minDelayForceRefresh) {
        this.minDelayForceRefresh = minDelayForceRefresh;
        return this;
    }

    public PaginatedDataLoader setDataProvider(PaginatedDataProviderInterface<T> dataProvider) {
        this.dataProvider = dataProvider;
        return this;
    }

    /**
     * Load a new section. Cannot be trigger if there is already a
     * @return
     */
    public boolean loadMore(){
        if (_isFullyLoaded || isLoading()){
            return false;
        }
        SectionContainer.Section lastSection = sectionContainer.last();
        final SectionContainer.Section newSection = createOlderSection(lastSection);

        //if (newSection.getEnd() == -1) return false;

        newSection.setLoadType(LoadType.MORE);
        sectionContainer.addSection(newSection);
        this.load(newSection);
        return true;
    }

    private void load(Section section) {
        if (this.useCache && this.cacheEngine.contains(section)){
            List<T> data = this.cacheEngine.get(section);
            if (callback != null) callback.onLoadEnd(section, data);
        }
        else{
            this.remoteLoad(section);
        }
    }

    public boolean loadNewest(){
        if (isLoading()){
            return false;
        }
        SectionContainer.Section section = sectionContainer.first();
        // Check if we need to load newest
        if (        section == null
                ||  section.isStatus(LoadStatus.DONE)){
            SectionContainer.Section newSection = createNewerSection(section);
            if (section == null || newSection.getEnd() > 0){
                newSection.setLoadType(LoadType.NEWEST);
                this.load(newSection);
                return true;
            }
        }
        return false;
    }

    public Section createNewerSection(Section section) {
        return new Section(-1, section != null ? section.start + (this.getOrder() == PaginateDirection.ASC ? 1 : -1) : -1);
    }

    public Section createOlderSection(Section section) {
        long start = section == null ? -1 : section.end + (this.getOrder() == PaginateDirection.ASC ? -1 : 1);
        return new Section(start, -1);
    }

    private void remoteLoad(final Section newSection) {
        dataProvider.remoteLoad(newSection)
                .onResponse(new HttpCallback<ResponseSyncWrapper<T>>() {
                    @Override
                    public void successful(ResponseSyncWrapper<T> feedback) {
                        // Update min and max ids
                        if (feedback.getCount() > 0){
                            if (newSection.getLoadType() == LoadType.NEWEST){
                                newSection.setEnd(feedback.limit() == feedback.getCount() ? formatter.format(feedback.getLastItem()) : newSection.getEnd());
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
                            if (feedback.getCount() == feedback.limit()){
                                Section section = sectionContainer.findOlderSection(newSection);
                                if (section == null){
                                    Log.d(TAG, "Clearing sections...");
                                    sectionContainer.clear();
                                    sectionContainer.addSection(newSection);
                                }
                            }
                        }

                        Log.d(TAG, "Section loaded with success: " + newSection);
                        newSection.setStatus(LoadStatus.DONE);
                        if (callback != null) callback.onLoadEnd(newSection, feedback.items);
                    }

                    @Override
                    public void notSuccessful() {
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
                .perform();
    }

    public interface Callback<T>{

        void onLoadEnd(SectionContainer.Section section, List<T> data);

        void onLoadError(Throwable error, Section section);

    }

    public interface SectionBoundsFormatter<T>{

        long format(T data);

    }

    public interface CacheEngine<T>{

        void add(Section<T> section, List<T> data);

        boolean contains(Section<T> section);

        List<T> get(Section<T> section);

    }
}

