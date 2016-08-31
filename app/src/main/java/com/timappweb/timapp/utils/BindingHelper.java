package com.timappweb.timapp.utils;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.timappweb.timapp.data.models.Event;

/**
 * Created by stephane on 6/1/2016.
 */
public class BindingHelper {
    private static final String TAG = "BindingHelper";

    /*
    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView imageView, String imageUrl) {
        freF.with(imageView.getContext()).load(imageUrl).into(imageView);
    }*/

    @BindingAdapter("app:imageResource")
    public static void setImageResource(ImageView imageView, int resource){
        imageView.setImageResource(resource);
    }

    @BindingAdapter("app:imageDrawable")
    public static void setImageDrawable(ImageView imageView, Drawable resource){
        imageView.setImageDrawable(resource);
    }


    // TODO REMOVE PICASSO
    @BindingAdapter({"event:backgroundImage"})
    public static void setBackgroundImage(ImageView imageView, Event event) {
        if (event!= null){
            if (event.hasPicture()){
                final String fullUrl = event.getBackgroundUrl();
                // Temp solution
                Log.d(TAG, "Loading picture in adapter: " + fullUrl);
                Picasso.with(imageView.getContext()).load(fullUrl).centerCrop().fit().into(imageView);
                // @warning cannot use the following line because:
                //      E/BitmapFactory: Unable to decode stream: java.io.FileNotFoundException: ...: open failed: ENOENT (No such file or directory)
                //      I/System.out: resolveUri failed on bad bitmap uri:
                //imageView.setImageURI(Uri.parse(fullUrl));
            }
            else{
                imageView.setImageDrawable(event.getBackgroundImage(imageView.getContext()));
            }
        }
    }

    @BindingAdapter("app:errorText")
    public static void setErrorMessage(MaterialEditText editText, CharSequence errorMessage) {
        editText.setError(errorMessage);
    }

}

