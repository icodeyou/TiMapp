package com.timappweb.timapp.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.*;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.Place;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaceClusterRenderer extends DefaultClusterRenderer<Place> {

    private final IconGenerator mIconGenerator;
    private final IconGenerator mClusterIconGenerator;

    //Big coef = small icon
    private static int ICON_SIZE_COEF = 12;

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
        categoryImage.setImageResource(place.getIconResource());
        categoryImage = MyApplication.setCategoryBackground(categoryImage,place.getLevel());

        categoryImage.setDrawingCacheEnabled(true);

        // Without this code, the view will have a dimension of 0,0 and the bitmap will be null
        categoryImage.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        categoryImage.layout(0, 0, categoryImage.getMeasuredWidth(), categoryImage.getMeasuredHeight());
        categoryImage.buildDrawingCache(true);

        //set Category Icon Diameter
        Point size = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
        int iconDiameter = size.x/ICON_SIZE_COEF;

        /*float resourceSize = context.getResources().getDimension(R.dimen.marker);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int pixelsSize = Math.round(resourceSize/(metrics.xdpi*(1.0f/25.4f)));
        */

        Bitmap bmp = Bitmap.createScaledBitmap(categoryImage.getDrawingCache(),
                iconDiameter,iconDiameter,true);
        categoryImage.setDrawingCacheEnabled(false); // clear drawing cache

        bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);

        //add marker to Map
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bmp));
        markerOptions.anchor(0.5f,0.5f); // set marker centered on its location
    }

    /*private Bitmap getResizedBitmap(Bitmap bmp, int newWidth, int newHeight) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE NewActivity MATRIX FOR THE MANIPULATION
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
        HashMap<Integer, Drawable> profilePhotos = new HashMap<>();
        int width = mDimension;
        int height = mDimension;


        for (Place p : cluster.getItems()) {
            // Draw 4 at most.
            if (profilePhotos.size() == 4) break;
            if (!profilePhotos.containsKey(p.category_id)){
                Drawable drawable = ContextCompat.getDrawable(context, p.getIconResource());
                drawable.setBounds(0, 0, width, height);
                profilePhotos.put(p.category_id, drawable);
            }
        }
        MultiDrawable multiDrawable = new MultiDrawable(new ArrayList<Drawable>(profilePhotos.values()));
        multiDrawable.setBounds(0, 0, width, height);
        mClusterImageView.setImageDrawable(multiDrawable);
        //int padding = (int) context.getResources().getDimension(R.dimen.map_icon_padding);
        //mClusterImageView.setPadding(padding, padding, padding, padding);

        mClusterIconGenerator.setTextAppearance(R.style.map_cluster_text);
        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }
}
