package com.timappweb.timapp.data.loader;

import com.timappweb.timapp.events.SyncResultMessage;
import com.timappweb.timapp.sync.SyncAdapterOption;

/**
 * Created by Stephane on 02/09/2016.
 */
public class PaginatedSyncResult extends SyncResultMessage{

    private SectionContainer.PaginatedSection section;

    public PaginatedSyncResult(SyncAdapterOption options) {
        super(options);
    }

    public SectionContainer.PaginatedSection getSection() {
        return section;
    }

    public void setSection(SectionContainer.PaginatedSection section) {
        this.section = section;
    }
}
