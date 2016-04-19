package com.timappweb.timapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by stephane on 3/31/2016.
 */
public class PictureUtility {
    
    public static File resize(Context context, File f, int imageMaxWidth, int imageMaxHeight) throws IOException {

        int width = 50, height = 50;

        // http://frescolib.org/docs/resizing-rotating.html

        /*Picasso.with(context).load(f)
                .resize(imageMaxWidth, imageMaxHeight)
                .onlyScaleDown()
                .into(getTarget(f.getAbsolutePath()));*/


        return f;
        /*
        FileInputStream fis = new FileInputStream(f);
        Bitmap b = BitmapFactory.decodeStream(fis);
        b = PictureUtility.getResizedBitmap(b, imageMaxSize);
        fis.close();

        PictureUtility.persistImage(b, f);
        return f;
        /*
        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = new FileInputStream(f);
        BitmapFactory.decodeStream(fis, null, o);
        fis.close();

        int scale = 1;
        if (o.outHeight > imageMaxSize || o.outWidth > imageMaxSize) {
            scale = (int)Math.pow(2, (int) Math.ceil(Math.log(imageMaxSize /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        fis = new FileInputStream(f);
        b = BitmapFactory.decodeStream(fis, null, o2);
        fis.close();

        FileOutputStream fOut = new FileOutputStream(f);
        b.compress(Bitmap.CompressFormat.PNG, 0, fOut);
        fOut.flush();
        fOut.close();
        return  f;
        */
    }
    /**
     * reduces the size of the image
     * @param image
     * @param maxSize
     * @return
     */
    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
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
