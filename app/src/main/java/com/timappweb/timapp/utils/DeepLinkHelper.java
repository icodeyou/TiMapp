package com.timappweb.timapp.utils;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stephane on 08/10/2016.
 *
 * Deep link format:
 * https://domain/?link=your_deep_link&apn=package_name[&amv=minimum_version][&ad=1][&al=android_link][&afl=fallback_link]
 */
public class DeepLinkHelper {

    private static final String TAG = "DeepLinkHelper";
    private boolean _isAdd;
    private int _minVersion;
    private String _appCode;
    private String _packageName;
    private Map<String, String> _customParameters;
    private String _playStoreAppLink;
    private String _androidLink;
    private String _deepLink;

    public DeepLinkHelper() {
        this._customParameters = new HashMap<>();
    }

    public String build() {
        // Get the unique appcode for this app.

        // Get this app's package name.
        String queryParamters = "";
        try {
            queryParamters = generateQueryParameters();
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(queryParamters)) {
            this._deepLink = this._deepLink + queryParamters;
        }
        // Build the link with all required parameters
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(this._appCode + ".app.goo.gl")
                .path("/")
                .appendQueryParameter("link", this._deepLink)
                .appendQueryParameter("apn", this._packageName);

        // If the deep link is used in an advertisement, this value must be set to 1.
        if (_isAdd) {
            builder.appendQueryParameter("ad", "1");
        }

        // Minimum version is optional.
        if (this._minVersion > 0) {
            builder.appendQueryParameter("amv", Integer.toString(this._minVersion));
        }

        if (!TextUtils.isEmpty(this._androidLink)) {
            builder.appendQueryParameter("al", this._androidLink);
        }

        if (!TextUtils.isEmpty(this._playStoreAppLink)) {
            builder.appendQueryParameter("afl", this._playStoreAppLink);
        }

        // Return the completed deep link.
        return builder.build().toString();
    }

    public DeepLinkHelper isAdd(boolean add){
        this._isAdd = add;
        return this;
    }
    public DeepLinkHelper minVersion(int version){
        this._minVersion = version;
        return this;
    }
    public DeepLinkHelper appCode(String _appCode) {
        this._appCode = _appCode;
        return this;
    }

    public DeepLinkHelper packageName(String _packageName) {
        this._packageName = _packageName;
        return this;
    }

    public DeepLinkHelper playStoreAppLink(String _playStoreAppLink) {
        this._playStoreAppLink = _playStoreAppLink;
        return this;
    }

    public DeepLinkHelper androidLink(String _androidLink) {
        this._androidLink = _androidLink;
        return this;
    }

    public DeepLinkHelper deepLink(String _deepLink) {
        this._deepLink = _deepLink;
        return this;
    }

    public DeepLinkHelper addQueryParameter(String key, String value){
        this._customParameters.put(key, value);
        return this;
    }

    private String generateQueryParameters() throws UnsupportedEncodingException {
        StringBuilder queryParameters = new StringBuilder();
        //server purposes
        queryParameters.append("?");

        if (!_customParameters.isEmpty()) {
            for (Map.Entry<String, String> parameter : _customParameters.entrySet()) {
                queryParameters.append(String.format("&%1s=%2s", parameter.getKey(), parameter.getValue()));
            }
        }
        return URLEncoder.encode(queryParameters.toString(), "UTF-8");
    }

}
