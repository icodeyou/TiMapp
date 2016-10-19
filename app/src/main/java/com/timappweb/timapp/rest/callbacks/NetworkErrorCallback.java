package com.timappweb.timapp.rest.callbacks;

import android.content.Context;
import android.util.Log;
import android.util.MalformedJsonException;
import android.widget.Toast;

import com.timappweb.timapp.R;

import java.io.IOException;
import java.net.SocketException;

/**
 * Created by Stephane on 13/08/2016.
 */
public class NetworkErrorCallback extends com.timappweb.timapp.rest.callbacks.RequestFailureCallback {

    private static final String TAG = "NetworkErrorCallback";

    protected final Context context;

    public NetworkErrorCallback(Context context) {
        this.context = context;
    }

    @Override
    public void network(IOException error) {
        Log.e(TAG, "Received server error message: " + error.getMessage());
        //else if (error instanceof SocketException){
        //    Log.e(TAG, "SocketException: " + error.getMessage());
        //}
        Toast.makeText(context, R.string.no_network_access, Toast.LENGTH_LONG).show();

    }

    @Override
    public void unexpectedFormat(Throwable error) {
        Toast.makeText(context, R.string.error_message_service_not_available, Toast.LENGTH_SHORT).show();
    }
}


