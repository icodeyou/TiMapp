package com.timappweb.timapp.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Event;

import java.util.ArrayList;
import java.util.HashMap;

public class EventClusterRenderer extends DefaultClusterRenderer<Event> {

    private static final String TAG = "EventClusterRenderer";
    private final IconGenerator mIconGenerator;
    private final IconGenerator mClusterIconGenerator;

    //Big coef = small iconUrl
    private static int ICON_SIZE_COEF = 12;
    private static int PADDING_ICON = 70;

    private final int mDimension;
    private final ImageView mImageView;
    private final ImageView mClusterImageView;
    private final Context context;

    // TODO
    public EventClusterRenderer(Activity activity, GoogleMap map, com.google.maps.android.clustering.ClusterManager clusterManager) {
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
    protected void onBeforeClusterItemRendered(Event event, MarkerOptions markerOptions) {
        ImageView categoryImage= new ImageView(context);
        categoryImage.setImageDrawable(event.getCategoryWithDefault().getIconDrawable(this.context));
        //categoryImage.setImageDrawable(context.getResources().getDrawable(R.drawable.category_unknown));
        categoryImage.setPadding(PADDING_ICON,PADDING_ICON,PADDING_ICON,PADDING_ICON);
        categoryImage.setBackgroundResource(event.getLevelBackground());
        categoryImage.setDrawingCacheEnabled(true);

        // Without this code, the view will have a dimension of 0,0 and the bitmap will be null
        categoryImage.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        categoryImage.layout(0, 0, categoryImage.getMeasuredWidth(), categoryImage.getMeasuredHeight());
        categoryImage.buildDrawingCache(true);

        //set EventCategory Icon Diameter
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

        bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);;

        //add marker to Map
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bmp));
        markerOptions.anchor(0.5f,0.5f); // set marker centered on its location
    }


    @Override
    protected void onBeforeClusterRendered(Cluster<Event> cluster, MarkerOptions markerOptions) {
        // Draw multiple people.
        // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
        HashMap<Long, Drawable> profilePhotos = new HashMap<>();
        int width = mDimension;
        int height = mDimension;


        for (Event p : cluster.getItems()) {
            // Draw 4 at most.
            if (profilePhotos.size() == 4) break;
            if (!p.hasCategory()){
                continue;
            }
            if (!profilePhotos.containsKey(p.getCategory().getRemoteId())){
                Drawable drawable = p.getCategory().getIconDrawable(this.context);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.put(p.getCategory().getRemoteId(), drawable);
            }
        }
        // When there is no data because categories are not loaded correctly...
        if (profilePhotos.values().size() == 0){
            Log.e(TAG, "No event with a category for this cluster: " + cluster+ ". Aborting rendering");
            return;
        }
        MultiDrawable multiDrawable = new MultiDrawable(new ArrayList<Drawable>(profilePhotos.values()));
        multiDrawable.setBounds(0, 0, width, height);
        mClusterImageView.setImageDrawable(multiDrawable);
        //int padding = (int) context.getResources().getDimension(R.dimen.map_icon_padding);
        //mClusterImageView.setPadding(padding, padding, padding, padding);

        //mClusterIconGenerator.setTextAppearance(R.style.map_cluster_text);
        mClusterIconGenerator.setColor(ContextCompat.getColor(context, R.color.background_cluster));
        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }
}
