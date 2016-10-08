package com.timappweb.timapp.utils.deeplinks;

import android.content.res.Resources;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Event;

/**
 * Created by Stephane on 08/10/2016.
 *
 * Deep link format:
 * https://domain/?link=your_deep_link&apn=package_name[&amv=minimum_version][&ad=1][&al=android_link][&afl=fallback_link]
 */
public class DeepLinkFactory {

    public static DeepLinkHelper shareEvent(Event event){
        String viewEventLink = MyApplication
                .getApplicationBaseContext()
                .getString(R.string.gcm_view_event, event.getRemoteId());
        return DeepLinkFactory.build()
                .deepLink(viewEventLink);
    }

    public static DeepLinkHelper build(){
        Resources resources = MyApplication.getApplicationBaseContext().getResources();
        int targetSdkVersion = resources.getInteger(R.integer.gcm_min_sdk_version);
        String appCode = resources.getString(R.string.gcm_app_code);

        return new DeepLinkHelper()
                .appCode(appCode)
                .minVersion(targetSdkVersion)
                .packageName(MyApplication.getApplicationBaseContext().getPackageName());
    }
}
