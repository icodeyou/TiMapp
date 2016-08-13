package com.timappweb.timapp.utils;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.timappweb.timapp.data.models.Event;

/**
 * Created by stephane on 6/1/2016.
 */
public class BindingHelper {

    /*
    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView imageView, String imageUrl) {
        freF.with(imageView.getContext()).load(imageUrl).into(imageView);
    }*/

    @BindingAdapter("app:imageResource")
    public static void setImageResource(ImageView imageView, int resource){
        imageView.setImageResource(resource);
    }

    @BindingAdapter({"event:backgroundImage"})
    public static void setBackgroundImage(ImageView imageView, Event event) {
        if (event!= null){
            imageView.setImageDrawable(event.getBackgroundImage(imageView.getContext()));
        }
    }

    @BindingAdapter("app:errorText")
    public static void setErrorMessage(MaterialEditText editText, CharSequence errorMessage) {
        editText.setError(errorMessage);
    }

}

