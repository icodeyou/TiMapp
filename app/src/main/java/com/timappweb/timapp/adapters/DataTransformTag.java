package com.timappweb.timapp.adapters;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.SuperscriptSpan;

import com.greenfrvr.hashtagview.HashtagView;
import com.timappweb.timapp.entities.Tag;

public class DataTransformTag implements HashtagView.DataTransform<Tag> {

    @Override
    public CharSequence prepare(Tag item) {
        String label = item.name;
        //SpannableString spannableString = new SpannableString(label);
        //spannableString.setSpan(new SuperscriptSpan(), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //return spannableString;
        return label;
    }
}
