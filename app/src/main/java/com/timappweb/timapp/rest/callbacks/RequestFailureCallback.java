package com.timappweb.timapp.rest.callbacks;

import java.io.IOException;

/**
 * Created by stephane on 6/6/2016.
 */
public  class RequestFailureCallback {

    public  void onError(Throwable error) {};

    public  void network(IOException error) {};
}
