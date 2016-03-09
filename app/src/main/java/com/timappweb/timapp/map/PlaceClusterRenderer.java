package com.timappweb.timapp.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.*;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.MarkerValueInterface;
import com.timappweb.timapp.entities.Place;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class PlaceClusterRenderer extends DefaultClusterRenderer<Place> {

    private final IconGenerator mIconGenerator;
    private final IconGenerator mClusterIconGenerator;

    private final int mDimension;
    private final ImageView mImageView;
    private final ImageView mClusterImageView;
    private final Context context;

    // TODO
    public PlaceClusterRenderer(Activity activity, GoogleMap map, com.google.maps.android.clustering.ClusterManager clusterManager) {
        super(activity, map, clusterManager);
        mIconGenerator = new IconGenerator(activity);
        mClusterIconGenerator = new IconGenerator(activity);
        this.context = activity;

        View multiProfile = activity.getLayoutInflater().inflate(R.layout.multi_map_icons, null);
        mClusterIconGenerator.setContentView(multiProfile);
        mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

        mImageView = new ImageView(context);
        mDimension = (int) context.getResources().getDimension(R.dimen.map_icon);
        mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
        int padding = (int) context.getResources().getDimension(R.dimen.map_icon_padding);
        mImageView.setPadding(padding, padding, padding, padding);
        mIconGenerator.setContentView(mImageView);


    }

    @Override
    protected void onBeforeClusterItemRendered(Place place, MarkerOptions markerOptions) {
        ImageView categoryImage= new ImageView(context);
        categoryImage.setImageResource(place.getResource());
        categoryImage = MyApplication.setCategoryBackground(categoryImage,place.getLevel());

        categoryImage.setDrawingCacheEnabled(true);

        // Without this code, the view will have a dimension of 0,0 and the bitmap will be null
        categoryImage.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        categoryImage.layout(0, 0, categoryImage.getMeasuredWidth(), categoryImage.getMeasuredHeight());
        categoryImage.buildDrawingCache(true);
        Bitmap bmp = Bitmap.createScaledBitmap(categoryImage.getDrawingCache(),100,100,true);
        categoryImage.setDrawingCacheEnabled(false); // clear drawing cache

        bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);

        //add marker to Map
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bmp));
    }

    /*private Bitmap getResizedBitmap(Bitmap bmp, int newWidth, int newHeight) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bmp, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }*/


    @Override
    protected void onBeforeClusterRendered(Cluster<Place> cluster, MarkerOptions markerOptions) {
        // Draw multiple people.
        // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
        List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
        int width = mDimension;
        int height = mDimension;

        for (Place p : cluster.getItems()) {
            // Draw 4 at most.
            if (profilePhotos.size() == 4) break;
            Drawable drawable = ContextCompat.getDrawable(context, p.getResource());
            drawable.setBounds(0, 0, width, height);
            profilePhotos.add(drawable);
        }
        MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
        multiDrawable.setBounds(0, 0, width, height);

        mClusterImageView.setImageDrawable(multiDrawable);
        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }
}
