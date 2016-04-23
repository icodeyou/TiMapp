package com.timappweb.timapp.data.contentproviders;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.timappweb.timapp.data.models.QuotaType;

/**
 * Created by stephane on 4/23/2016.
 */
public class QuotaTypeContentProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String resultRecords = new Select().from(QuotaType.class).toSql();
        return Cache.openDatabase().rawQuery(resultRecords, null);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        new Delete().from(QuotaType.class)
                .where(selection)
                .execute();
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        new Delete().from(QuotaType.class)
                .execute();
        return 0;
    }
}
