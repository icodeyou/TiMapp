package com.timappweb.timapp.utils.location;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.ResultReceiver;
import android.util.Log;

import com.timappweb.timapp.config.Constants;
import com.timappweb.timapp.services.FetchAddressIntentService;

/**
 * Created by stephane on 6/3/2016.
 */
public class ReverseGeocodingHelper {

    private static final String TAG = "ReverseGeocoding";

    public static void request(Context context, Location location, ResultReceiver receiver) {
        Log.d(TAG, "Starting IntentService to get use address from name");
        Intent intent = new Intent(context, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, receiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        context.startService(intent);
    }

}
