package com.timappweb.timapp.rest;

import android.util.Log;

import java.io.IOException;


import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * Created by Jack on 28/01/2016.
 */
public class LogRequestInterceptor  implements Interceptor {
    private static final String TAG = "LogRequestInter";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        Log.i(TAG, "Sending request " + request.url() + " on " + chain.connection() + " " + request.headers());
        Log.d(TAG, "REQUEST BODY BEGIN\n" +bodyToString(request)+ "\nREQUEST BODY END");

        Response response = chain.proceed(request);

        ResponseBody responseBody = response.body();
        String responseBodyString = response.body().string();

        // now we have extracted the response body but in the process
        // we have consumed the original reponse and can't read it again
        // so we need to build a new one to return from this method

        Response newResponse = response.newBuilder().body(ResponseBody.create(responseBody.contentType(), responseBodyString.getBytes())).build();

        long t2 = System.nanoTime();
        Log.d(TAG, "Received response for " + response.request().url() + " in " + (t2 - t1) + " / " + response.headers());
        Log.d(TAG, "RESPONSE BODY BEGIN:\n"+responseBodyString+"\nRESPONSE BODY END");

        return newResponse;
    }

    private static String bodyToString(final Request request){

        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            if (copy.body() != null)
                copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }
}