package com.timappweb.timapp.utils;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.Event;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by Stephane on 08/10/2016.
 *
 * Deep link format:
 * https://domain/?link=your_deep_link&apn=package_name[&amv=minimum_version][&ad=1][&al=android_link][&afl=fallback_link]
 */
public class DeepLinkFactory {

    public static DeepLinkHelper shareEvent(Event event){
        return DeepLinkFactory.build()
                .deepLink("http://www.timapp.fr/events")
                .addQueryParameter("event_id", String.valueOf(event.getRemoteId()));
    }

    public static DeepLinkHelper build(){
        return new DeepLinkHelper()
                .appCode("qgnj7")
                .packageName(MyApplication.getApplicationBaseContext().getPackageName());
    }
}
