package com.timappweb.timapp.data.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.ImageSaver;

/**
 * Created by Stephane on 04/09/2016.
 */
public abstract class Category extends SyncBaseModel{

    public static final String ICON_DIRECTORY_NAME = "icons";
    private static final String TAG = "Category";

    protected Drawable _iconDrawable;

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
}
