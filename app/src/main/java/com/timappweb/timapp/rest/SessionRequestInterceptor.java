package com.timappweb.timapp.rest;

import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.LocalPersistenceManager;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by stephane on 9/12/2015.
 */
public class SessionRequestInterceptor implements Interceptor
{

    private static final String TAG = "Interceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = LocalPersistenceManager.instance.pref.getString(RestClient.KEY_TOKEN, null);

        // Customize the request
        Request.Builder requestBuilder = original.newBuilder()
                .header("Accept", "application/json")
                        //.header("Authorization", "auth-token")
                .method(original.method(), original.body());
        if (MyApplication.isLoggedIn()) {
            Log.d(TAG, "Request interceptor: User is logged in");
            HttpUrl url = original.url().newBuilder()
                    .addQueryParameter("_token", token)
                    .build();
            requestBuilder
                    .url(url)
                    .header("Authorization", "Bearer " + token);
        }
        Request request = requestBuilder.build();

        return chain.proceed(request);
    }
}

