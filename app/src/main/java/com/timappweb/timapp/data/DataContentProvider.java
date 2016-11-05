package com.timappweb.timapp.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.timappweb.timapp.data.models.MyModel;

/**
 * Created by Stephane on 03/11/2016.
 */

public class DataContentProvider extends ContentProvider{

    private static String sAuthority;

    public static Uri createUri(Class<? extends MyModel> aClass, Long id) {
        final StringBuilder uri = new StringBuilder();
        uri.append("content://");
        uri.append(sAuthority);
        uri.append("/");
        uri.append(aClass.getName().toLowerCase());

        if (id != null) {
            uri.append("/");
            uri.append(id.toString());
        }

        return Uri.parse(uri.toString());
    }

    @Override
    public boolean onCreate() {
        sAuthority = getContext().getPackageName();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
