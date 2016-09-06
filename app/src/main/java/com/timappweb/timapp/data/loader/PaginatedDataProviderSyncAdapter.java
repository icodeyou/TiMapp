package com.timappweb.timapp.data.loader;

import android.content.Context;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.events.SyncResultMessage;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.sync.data.DataSyncAdapter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stephane on 02/09/2016.
 */
public class PaginatedDataProviderSyncAdapter {

    private static final String TAG = "PaginatedDataProviderSA";
    private List<Callback> observers = new ArrayList<>();
    private SyncAdapterOption syncAdapterOptions;

    public void registerObserver(Callback observer) {
        observers.add(observer);
    }

    protected void notifyObservers(final SectionContainer.PaginatedSection section) {
        for (final Callback observer : observers) {
            observer.onLoadEnd(section);
        }
    }
    protected void notifyObservers(Exception error, final SectionContainer.PaginatedSection section) {
        for (final Callback observer : observers) {
            observer.onLoadError(error, section);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncEnds(PaginatedSyncResult syncResult){
        SectionContainer.PaginatedSection section = syncResult.getSection();
        if (section != null){
            dispatchLoadResult(syncResult, section);
        }
    }

    public void dispatchLoadResult(SyncResultMessage syncResult, SectionContainer.PaginatedSection section){
        if (syncResult.hasError()){
            notifyObservers(syncResult.getError(), section);
            section.setStatus(SectionContainer.LoadStatus.ERROR);
            return;
        }

        section.setStart(syncResult.getMinId());
        section.setEnd(syncResult.getMaxId());
        section.setStatus(SectionContainer.LoadStatus.DONE);
        notifyObservers(section);
    }

    public void load(SectionContainer.PaginatedSection section) {
        Context context = MyApplication.getApplicationBaseContext();
        this.syncAdapterOptions
                .setMaxId(section.start)
                .setMinId(section.end)
                .setDirection(RestQueryParams.SyncDirection.DOWN)
                .setLastSyncTime();
        DataSyncAdapter.syncImmediately(context,
                context.getString(R.string.content_authority_data), this.syncAdapterOptions.getBundle());
    }

    public PaginatedDataProviderSyncAdapter setSyncAdapterOptions(SyncAdapterOption syncAdapterOptions) {
        this.syncAdapterOptions = syncAdapterOptions;
        return this;
    }

    public interface Callback{

        void onLoadEnd(SectionContainer.PaginatedSection section);

        void onLoadError(Exception error, SectionContainer.PaginatedSection section);

    }

}