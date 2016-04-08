package com.timappweb.timapp.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.timappweb.timapp.R;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ArchiveViewPagerFragment extends Fragment {

    private static final String BUNDLE_ASSET = "asset";

    private String asset;
    private PhotoViewAttacher mAttacher;

    public ArchiveViewPagerFragment() {
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.view_pager_page, container, false);

        /*if (savedInstanceState != null) {
            if (asset == null && savedInstanceState.containsKey(BUNDLE_ASSET)) {
                asset = savedInstanceState.getString(BUNDLE_ASSET);
            }
        }*/
        if (asset != null) {
            PhotoView imageView = (PhotoView)rootView.findViewById(R.id.imageView);
            mAttacher = new PhotoViewAttacher(imageView);
            final Uri uri = Uri.parse(asset);
            imageView.setImageURI(uri);
            mAttacher.update();
        }

        return rootView;
    }

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        View rootView = getView();
        if (rootView != null) {
            outState.putString(BUNDLE_ASSET, asset);
        }
    }*/

}