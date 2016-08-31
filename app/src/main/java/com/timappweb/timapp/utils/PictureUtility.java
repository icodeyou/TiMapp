package com.timappweb.timapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by stephane on 3/31/2016.
 */
public class PictureUtility {

    private static final String TAG = "PictureUtility";

    public static File resize(@NotNull File f, int imageMaxWidth, int imageMaxHeight) throws IOException {
        /*Picasso.with(context).load(f)
                .resize(imageMaxWidth, imageMaxHeight)
                .onlyScaleDown()
                .into(getTarget(f.getAbsolutePath()));*/

        FileInputStream fis = new FileInputStream(f);
        Bitmap b = BitmapFactory.decodeStream(fis);
        if (b == null){
            fis.close();
            throw new IOException("Cannot decode input stream: " + f);
        }
        b = PictureUtility.resize(b, imageMaxWidth, imageMaxHeight);
        fis.close();
        PictureUtility.persistImage(b, f);
        return f;
    }
    /**
     * reduces the size of the image
     * @param image
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static Bitmap resize(@NotNull Bitmap image, int maxWidth, int maxHeight) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= maxWidth && height <= maxHeight){
            return image;
        }
        float bitmapRatio = (float)width / (float) height;

        if (width > maxWidth) {
            width = maxWidth;
            height = (int) (width / bitmapRatio);
        }

        if (height > maxHeight) {
            height = maxHeight;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private static void persistImage(Bitmap bitmap, File file) throws IOException {
        OutputStream os;
        os = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        os.flush();
        os.close();
    }

    public static Drawable drawableFromUrl(String url, Drawable defaultDrawable) {
        try {
            Log.d(TAG, "Get drawable from url: " + url);
            Bitmap x;
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            InputStream input = null;
            input = connection.getInputStream();
            x = BitmapFactory.decodeStream(input);
            if (x == null){
                throw new Exception("Cannot decode input stream from url: " + url);
            }
            return new BitmapDrawable(x);
        } catch (Exception e) {
            Log.e(TAG, "Cannot convert to drawable: " + e.getMessage());
            e.printStackTrace();
            return defaultDrawable;
        }
    }

    //target to save
   /* private static Target getTarget(final String url){
        Target target = new Target(){

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + url);
                        try {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                            ostream.flush();
                            ostream.close();
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return target;
    }*/
}
