package com.timappweb.timapp.data.loader;

import com.timappweb.timapp.events.SyncResultMessage;
import com.timappweb.timapp.sync.SyncAdapterOption;

/**
 * Created by Stephane on 02/09/2016.
 */
public class PaginatedSyncResult extends SyncResultMessage{

    private SectionContainer.Section section;

    public PaginatedSyncResult(SyncAdapterOption options) {
        super(options);
    }

    public SectionContainer.Section getSection() {
        return section;
    }

    public void setSection(SectionContainer.Section section) {
        this.section = section;
    }
}
