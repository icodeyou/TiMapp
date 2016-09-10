package com.timappweb.timapp.data.loader.sections;

import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;

/**
 * Created by Stephane on 04/09/2016.
 */
public interface SectionDataProviderInterface<T> {

    HttpCallManager<ResponseSyncWrapper<T>> remoteLoad(SectionContainer.PaginatedSection section);

}
