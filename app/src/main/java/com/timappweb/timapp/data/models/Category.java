package com.timappweb.timapp.data.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.ImageSaver;
import com.timappweb.timapp.utils.PictureUtility;

/**
 * Created by Stephane on 04/09/2016.
 */
public abstract class Category extends SyncBaseModel{

    public static final String ICON_DIRECTORY_NAME = "icons";
    private static final String TAG = "Category";

    protected Drawable _iconDrawable;
    private AsyncTask<String, Void, Bitmap> _loadingTask;

    public void loadIconFromLocalStorage(final Context context) {
        if (this._iconDrawable != null){
            return;
        }
        Log.d(TAG, "Trying to load icon from local storage: " + this.getIconLocalFilename());
        Bitmap bitmap = new ImageSaver(context).
                setFileName(this.getIconLocalFilename()).
                setDirectoryName(ICON_DIRECTORY_NAME).
                load();
        if (bitmap != null){
            this._iconDrawable = new BitmapDrawable(context.getResources(), bitmap);
        }
        else{
            Log.e(TAG, "Cannot load icon from local storage: " + this.getIconLocalFilename());
            this.loadIconFromAPI(context);
        }
    }

    public abstract String getIconLocalFilename();

    public abstract String getIconUrl();

    public void setIconDrawable(Drawable iconDrawable) {
        this._iconDrawable = iconDrawable;
    }

    public Drawable getIconDrawable() {
        return getIconDrawable(MyApplication.getApplicationBaseContext());
    }

    public Drawable getIconDrawable(Context context) {
        return this._iconDrawable != null ? this._iconDrawable:
                context.getResources().getDrawable(R.drawable.ic_category_unknown);
    }

    public void loadIconFromAPI(final Context context) {
        if (this._iconDrawable != null || _loadingTask != null){
            return;
        }
        _loadingTask = new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {
                return PictureUtility.bitmapFromUrl(params[0]);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null){
                    Log.i(TAG, "Saving category icon: " + Category.this.getIconLocalFilename());
                    new ImageSaver(context).
                            setFileName(Category.this.getIconLocalFilename()).
                            setDirectoryName(Category.ICON_DIRECTORY_NAME).
                            save(bitmap);
                    Category.this.setIconDrawable(new BitmapDrawable(bitmap));
                }
            }
        }.execute(this.getIconUrl());
    }
}
