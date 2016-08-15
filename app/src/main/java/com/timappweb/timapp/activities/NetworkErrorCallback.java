package com.timappweb.timapp.activities;

import android.content.Context;
import android.widget.Toast;

import com.timappweb.timapp.R;

import java.io.IOException;

/**
 * Created by Stephane on 13/08/2016.
 */
public class NetworkErrorCallback extends com.timappweb.timapp.rest.callbacks.RequestFailureCallback {

    private final Context context;

    public NetworkErrorCallback(Context context) {
        this.context = context;
    }

    @Override
    public void network(IOException error) {
        Toast.makeText(context, R.string.no_internet_connection_message, Toast.LENGTH_LONG).show();
    }
}