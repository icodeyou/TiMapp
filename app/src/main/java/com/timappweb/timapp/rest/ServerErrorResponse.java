package com.timappweb.timapp.rest;

import retrofit2.Response;

/**
 * Created by Stephane on 24/09/2016.
 */
public class ServerErrorResponse extends Throwable {
    private final Response response;

    public ServerErrorResponse(Response response) {
        super("ServerErrorResponse: " + response.message());
        this.response = response;
    }
}
