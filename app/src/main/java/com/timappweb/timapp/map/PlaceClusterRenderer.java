package com.timappweb.timapp.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.*;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.timappweb.timapp.entities.MarkerValueInterface;
import com.timappweb.timapp.entities.Place;

import java.util.zip.Inflater;

/**
 * Created by stephane on 1/21/2016.
 */
public class PlaceClusterRenderer extends DefaultClusterRenderer<Place> {

    private final IconGenerator mIconGenerator;
    private final IconGenerator mClusterIconGenerator;
    //private final ImageView c;
    //private final ImageView mClusterImageView;
    //private final int mDimension;

    // TODO
    public PlaceClusterRenderer(Context context, GoogleMap map, com.google.maps.android.clustering.ClusterManager clusterManager) {
        super(context, map, clusterManager);
        mIconGenerator = new IconGenerator(context);
        mClusterIconGenerator = new IconGenerator(context);


        //View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
        //mClusterIconGenerator.setContentView(multiProfile);
        //mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

        //mImageView = new ImageView(context);
        //mDimension = (int) context.getResources().getDimension(R.dimen.custom_profile_image);
        //mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
        //int padding = (int) context.getResources().getDimension(R.dimen.custom_profile_padding);
        //mImageView.setPadding(padding, padding, padding, padding);
        //mIconGenerator.setContentView(mImageView);
    }

    @Override
    protected void onBeforeClusterItemRendered(Place place, MarkerOptions markerOptions) {
        // Draw a single person.
        // Set the info window to show their name.
        markerOptions.icon(BitmapDescriptorFactory.fromResource(place.getResource()));
    }

}
