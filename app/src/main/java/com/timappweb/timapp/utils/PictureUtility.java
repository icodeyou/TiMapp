package com.timappweb.timapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.ConfigurationProvider;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import id.zelory.compressor.Compressor;

/**
 * Created by stephane on 3/31/2016.
 */
public class PictureUtility {

    private static final String TAG = "PictureUtility";
    private static final int MAX_TIMES_COMPRESS = 10;
    private static final int COMPRESSION_QUALITY = 80;

    public static File resize(@NotNull File f, int imageMaxWidth, int imageMaxHeight) throws IOException {
        Log.d(TAG, "BEFORE COMPRESSION: " +
                "Photo '"+ f.getAbsolutePath() + "'" +
                " Size: " + Util.byteToKB(f.length()));
        Log.v(TAG, "    - Width max : " + imageMaxWidth);
        Log.v(TAG, "    - Height max : " + imageMaxHeight);

        File newFile = new Compressor.Builder(MyApplication.getApplicationBaseContext())
                .setMaxWidth(imageMaxWidth)
                .setMaxHeight(imageMaxHeight)
                .setQuality(COMPRESSION_QUALITY)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getAbsolutePath())
                .build()
                .compressToFile(f);

        Log.i(TAG, "After picture compression of '"+ f.getAbsolutePath() + "'" +
                " Size: " + Util.byteToKB(f.length()));


        return newFile;

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

    public static Bitmap rotateBitmapIfNeeded(Bitmap bitmap, File file) throws IOException {
        //From http://stackoverflow.com/questions/14066038/why-image-captured-using-camera-intent-gets-rotated-on-some-devices-in-android
        ExifInterface ei = new ExifInterface(file.getAbsolutePath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap newBitmap;
        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                newBitmap= rotateImage(bitmap, 90);
                return newBitmap;
            case ExifInterface.ORIENTATION_ROTATE_180:
                newBitmap= rotateImage(bitmap, 180);
                return newBitmap;
            case ExifInterface.ORIENTATION_ROTATE_270:
                newBitmap= rotateImage(bitmap, 270);
                return newBitmap;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                return bitmap;
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }

    private static void persistImage(Bitmap bitmap, File file) throws IOException {
        OutputStream os;
        os = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        os.flush();
        os.close();
    }

    public static Bitmap bitmapFromUrl(String url) {
        try {
            Log.d(TAG, "Get drawable from url: " + url);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap x = BitmapFactory.decodeStream(input);
            if (x == null){
                throw new Exception("Cannot decode input stream from url: " + url);
            }
            return x;
        } catch (Exception e) {
            Log.e(TAG, "Cannot convert to drawable: " + e.getMessage());
            e.printStackTrace();
            return null;
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
