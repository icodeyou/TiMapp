package com.timappweb.timapp.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.ui.IconGenerator;
import com.timappweb.timapp.R;
import com.timappweb.timapp.entities.Spot;
import com.timappweb.timapp.entities.SpotsTag;
import com.timappweb.timapp.exceptions.NoLastLocationException;
import com.timappweb.timapp.map.RemovableNonHierarchicalDistanceBasedAlgorithm;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.AreaIterator;
import com.timappweb.timapp.utils.AreaRequestHistory;
import com.timappweb.timapp.utils.AreaRequestItem;
import com.timappweb.timapp.utils.IntLatLngBounds;
import com.timappweb.timapp.utils.IntPoint;
import com.timappweb.timapp.utils.MyLocationProvider;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit.client.Response;

public class MapsFragment extends SupportMapFragment {
    private static final String TAG = "GoogleMapFragment";
    private static final long TIME_WAIT_MAP_VIEW = 500;

    // Declare a variable for the cluster manager.
    private ClusterManager<Spot> mClusterManagerSpot;
    private ClusterManager<SpotsTag> mClusterManagerTags;
    private GoogleMap mMap = null;
    private MapView mapView = null;
    private float previousZoomLevel = -1;
    private float currentZoomLevel = -1;

    enum ZoomType {IN, OUT, NONE};
    private ZoomType currentZoomMode = ZoomType.NONE;

    /**
     * When we click on a market we need to now the corresponding spot
     */
    //private HashMap<Marker, Spot> markers = new HashMap<>();
    //private HashMap<Spot, Marker> mapSpotToMarker = new HashMap<>();


    private AreaRequestHistory history;

    /**
     * Clear spots markers according the the spots given
     * @param data
     */
    public void clearSpots(List<Spot> data) {
        for (Spot spot: data){
            mClusterManagerSpot.removeItem(spot);
        }
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "OnViewCreated");

        mapView = (MapView) getActivity().findViewById(R.id.google_map_fragment);

        if (savedInstanceState == null){

        }
        else{
            Log.d(TAG, "Instance saved for map fragment");
        }
        this.loadMapIfNeeded();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Resuming map fragment");
        super.onResume();
        this.loadMapIfNeeded();
    }

    private void loadMapIfNeeded() {
        if (mMap == null){
            this.loadMap();
        }
    }

    public void loadMap() {
        this.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "Map is now ready!");
                mMap = googleMap;
                loadMapData();
            }
        });
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void loadMapData() {
        Log.i(TAG, "Setting up the map... Please wait.");

        // For showing a move to my loction button
        mMap.setMyLocationEnabled(true);

        try{
            setUpClusterer();
            centerMap();
        } catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void showSpotInfo(Spot spot){
        SpotItemFragment spotItemFragment = SpotItemFragment.newInstance(spot);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.container_spots_tag, spotItemFragment)
                .commit();
    }

    private void centerMap(){
        // Comme les unités sont en microdegrés, il faut multiplier par 1E6
        MyLocationProvider locationProvider = new MyLocationProvider(getActivity());
        try{
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationProvider.getLastPosition(), 12.0f));
        }
        catch (NoLastLocationException ex){
            Log.d(TAG, "Cannot center: no last location");
        }
    }

    private LatLngBounds getMapBounds(){
        return mMap.getProjection().getVisibleRegion().latLngBounds;
    }

    private void loadData(){
        final LatLngBounds bounds = getMapBounds();
        Log.i(TAG, "Map bounds: " + bounds.southwest + " " + bounds.southwest);

        if (currentZoomMode == ZoomType.OUT || history == null){
            // We setBounds a nez caching squer size
            IntLatLngBounds intBounds = new IntLatLngBounds(bounds);
            Log.i(TAG, "New zoom level: " + intBounds.getMeterWidth() + " x " + intBounds.getMeterHeight());
            this.history = new AreaRequestHistory(intBounds.getWidth(), intBounds.getHeight(), intBounds.southwest);

        }

        IntPoint northeast = history.getIntPoint(bounds.northeast);
        IntPoint southwest = history.getIntPoint(bounds.southwest);
        Log.i(TAG, "Southwest is " + southwest.toString() + "Northeast point is " + northeast.toString() + " (in cache: " + history.areas.size() + ")");

        AreaIterator areaIterator = new AreaIterator(southwest, northeast);
        IntPoint p = null;
        while (areaIterator.hasNext()){
            p = areaIterator.next();
            IntPoint pCpy = new IntPoint(p);
            Log.i(TAG, "For point: " + p.toString());
            AreaRequestItem request = history.areas.get(p);
            if (request != null){
                Log.i(TAG, "----- " + p + " Some data are in cache ");

                if (request.getLastUpdateDelay() > history.DELAY_BEFORE_UPDATE_REQUEST){
                    QueryCondition conditions = new QueryCondition();
                    Log.i(TAG,"----- " + p + " Data are too old; updating with new data from timestamp: " + request.timestamp);
                    conditions.setBounds(history.getBoundFromPoint(p).toDouble());
                    conditions.setTimestampMin(request.timestamp);
                    loadSpotFromAPI(pCpy, request, conditions);
                }
            }
            else{
                request = new AreaRequestItem();
                // We need to create a new point: todo iterator create pôint
                history.update(pCpy, request);
                // We need to build a new condition object because multi threading
                QueryCondition conditions = new QueryCondition();
                conditions.setBounds(history.getBoundFromPoint(p).toDouble());
                Log.i(TAG, "----- " + p + "  No data in cache we need a server request");
                loadSpotFromAPI(pCpy, request, conditions);
            }
        }

    }

    private void loadSpotFromAPI(final IntPoint p, final AreaRequestItem request, QueryCondition conditions) {
        Log.i(TAG, "Request loading of area: " + conditions.toString());
        RestClient.service().listSpots(conditions.toMap(), new RestCallback<List<Spot>>() {
            @Override
            public void success(List<com.timappweb.timapp.entities.Spot> spots, Response response) {

                Log.i(TAG, "WS loaded tags done. Loaded " + spots.size() + " result(s). " + " for point " + p);
                Toast.makeText(getActivity(), spots.size()  + " tags loaded", Toast.LENGTH_LONG).show();
                request.data.addAll(spots);

                // TODO the server has to send back the timestamp
                if (spots.size() > 1)
                    request.setTimesamp(spots.get(spots.size() - 1).getCreated());
                else {
                    request.setTimesamp(0);
                }

                // TODO request.timestamp;
                //mClusterManagerSpot.clearItems();
                addSpotMarkers(spots);
            }
        });
    }

    private void addSpotMarkers(List<com.timappweb.timapp.entities.Spot> spots){
        Log.i(TAG, "Loading " + spots.size() + " spot(s) on the map");
        //MarkerOptions markerOptions = new MarkerOptions()
                //.title("Marker")
        //        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        mClusterManagerSpot.addItems(spots);
        mClusterManagerSpot.cluster();
    }

    private void addTagMarkers(List<SpotsTag> spotsTags){
        IconGenerator item = new IconGenerator(getActivity());
        item.setTextAppearance(R.style.iconMapTagText);
        /*
        for (SpotsTag spotTag: spotsTags){
            //LatLng ll = spotTag.getLatLng();
            //Bitmap iconBitmap = item.makeIcon(spotTag.name);
            //mMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)).anchor(0.5f, 0.6f));
            mClusterManagerTags.addItem(spotTag);
        }
        */
        mClusterManagerTags.addItems(spotsTags);
        mClusterManagerTags.cluster();
    }



    private void setUpClusterer(){
        Log.i(TAG, "Setting up cluster!");

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManagerSpot = new ClusterManager<Spot>(getActivity(), mMap);
        mClusterManagerSpot.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<Spot>() {
            @Override
            public void onClusterItemInfoWindowClick(Spot spot) {
                Log.d(TAG, "You clicked on a cluster item info windo");
            }
        });
        mClusterManagerSpot.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Spot>() {
            @Override
            public boolean onClusterClick(Cluster<Spot> cluster) {
                Log.d(TAG, "You clicked on a cluster");
                Spot spot = (Spot) cluster.getItems().toArray()[0];
                showSpotInfo(spot);
                return false;
            }
        });
        mClusterManagerSpot.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Spot>() {
            @Override
            public boolean onClusterItemClick(Spot spot) {
                Log.d(TAG, "You clicked on a cluster item");

                RestClient.instance().getService().viewSpot(spot.id, new RestCallback<Spot>() {
                    @Override
                    public void success(Spot spot, Response response) {
                        Log.i(TAG, "Found spot: " + spot.toString());
                        Toast.makeText(getActivity(), "Found spot: " + spot.toString(), Toast.LENGTH_LONG);
                        //                MapsActivity.spotItemFragment.setSpot(spot);
                        showSpotInfo(spot);
                    }
                });
                return true;
            }

        });
        mMap.setOnMarkerClickListener(mClusterManagerSpot);
        // mMap.setOnCameraChangeListener(mClusterManagerTags);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d(TAG, "Zoom level is now: " + cameraPosition.zoom);

                previousZoomLevel = currentZoomLevel;
                currentZoomLevel = cameraPosition.zoom;
                // Register zoom updates
                if (previousZoomLevel == currentZoomLevel) {
                    currentZoomMode = ZoomType.NONE;
                } else if (previousZoomLevel > currentZoomLevel) {
                    currentZoomMode = ZoomType.OUT;
                } else {
                    currentZoomMode = ZoomType.IN;
                }

                if (history != null) {
                    // Check if the zoom changed too much
                    // To do so we check that the number of case displayed in the history grid is not too big
                    // TODO can be optimized by only calculation with latitude and longitude
                    IntPoint southeastPoint = history.getIntPoint(getMapBounds().southwest);
                    IntPoint northeastPoint = history.getIntPoint(getMapBounds().northeast);
                    if (southeastPoint.distance(northeastPoint) > history.MAXIMUM_GRID_SIZE_ON_VIEW) {
                        Log.i(TAG, "Maximum grid size reach. Removing the history...");
                        mClusterManagerSpot.clearItems();
                        history = null;
                    }
                    // TODO also need to check if we are loading too much data (if we zoom in) ???
                    else if (false) {

                    }
                    // Remove history case that are too far from the current view position
                    // TODO optimize to only clear if the last loaded is too far from where we are ?
                    // TODO or create function to clean history by removing too far spots
                    else {
                        Iterator it = history.areas.entrySet().iterator();
                        int removeNb = 0;
                        while (it.hasNext()) {
                            Map.Entry<IntPoint, AreaRequestItem> entry = (Map.Entry) it.next();
                            if (entry.getKey().distance(southeastPoint) > history.MAXIMUM_ORIGIN_DISTANCE) {
                                Log.i(TAG, "Spot caching too far from origin. Clearing spot history and markers");
                                clearSpots(entry.getValue().data);
                                it.remove();
                                removeNb++;
                            }
                        }
                        if (removeNb > 0)
                            mClusterManagerSpot.cluster();
                    }
                }
                previousZoomLevel = cameraPosition.zoom;
                mClusterManagerSpot.onCameraChange(cameraPosition);
                loadData();
            }
        });

        mClusterManagerSpot.setAlgorithm(new RemovableNonHierarchicalDistanceBasedAlgorithm<Spot>());
    }

}

/**
 * Google Maps v2 for Android: the Pop-up window with high-grade copying and a support of events of feeding into
 * http://sysmagazine.com/posts/213415/
 *
 */
