package com.timappweb.timapp.utils;

import com.timappweb.timapp.config.server.ServerHeader;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Stephane on 10/09/2016.
 */
public class DisableQuotaRequestInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder requestBuilder = chain.request().newBuilder()
                .header(ServerHeader.XQUOTA_DISABLED, "1");

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}
