package com.timappweb.timapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.FilterActivity;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.activities.PostActivity;
import com.timappweb.timapp.entities.MapTag;
import com.timappweb.timapp.entities.MarkerValueInterface;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.exceptions.NoLastLocationException;
import com.timappweb.timapp.map.RemovableNonHierarchicalDistanceBasedAlgorithm;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.AreaDataCaching.AreaDataLoaderFromAPI;
import com.timappweb.timapp.utils.AreaDataCaching.AreaDataLoaderInterface;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestHistory;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestItem;
import com.timappweb.timapp.utils.IntLatLngBounds;
import com.timappweb.timapp.utils.IntPoint;
import com.timappweb.timapp.utils.MyLocationProvider;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit.client.Response;

public class ExploreMapFragment extends SupportMapFragment {
    private static final String TAG = "GoogleMapFragment";
    private static final long TIME_WAIT_MAP_VIEW = 500;

    // Declare a variable for the cluster manager.
    private ClusterManager<MarkerValueInterface> mClusterManagerPost;
    private GoogleMap mMap = null;
    private MapView mapView = null;
    private float previousZoomLevel = -1;
    private float currentZoomLevel = -1;

    private static HashMap<Marker, MarkerValueInterface> mapMarkers;
    private AreaDataLoaderInterface dataLoader;

    enum ZoomType {IN, OUT, NONE};
    private ZoomType currentZoomMode = ZoomType.NONE;


    /**
     * When we click on a market we need to now the corresponding spot
     */
    //private HashMap<Marker, Post> markers = new HashMap<>();
    //private HashMap<Post, Marker> mapSpotToMarker = new HashMap<>();


    private AreaRequestHistory history;

    /**
     * Clear spots markers according the the spots given
     * @param data
     */
    public void clearPosts(List<Post> data) {
        for (Post spot: data){
            mClusterManagerPost.removeItem(spot);
        }
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "OnViewCreated");
        setHasOptionsMenu(true);

        mapView = (MapView) getActivity().findViewById(R.id.google_map_fragment);

        if (savedInstanceState == null){
            // TODO what happens with instance
        }
        else{
            Log.d(TAG, "Instance saved for map fragment");
        }
        this.loadMapIfNeeded();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //Clear the menu, to avoid inflating several times the same menu
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        Log.d(TAG, "FILTER CALLED");
        if (id == R.id.action_filter) {
            Log.d(TAG, "FILTER CALLED");
            Intent intent = new Intent(getActivity(),FilterActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Resuming map fragment");
        this.loadMapIfNeeded();
    }

    private void loadMapIfNeeded() {
        if (mMap == null){
            this.loadMap();
            mapMarkers = new HashMap<>();
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
            //setUpMapEvents();
            centerMap();
            //loadDummyData();
        } catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void showMarkerDetail(final Post postIncomplete){
        Toast.makeText(getActivity(), "Loading post: " + postIncomplete.getId(), Toast.LENGTH_LONG);
        RestClient.service().viewSpot(postIncomplete.getId(), new RestCallback<Post>(this.getContext()) {
            @Override
            public void success(Post post, Response response) {
                if (post != null) {
                    Log.i(TAG, "Loaded details for spot: " + post.toString());

                    Intent intent = new Intent(getActivity(), PostActivity.class);
                    intent.putExtra("post", post);
                    getActivity().startActivity(intent);
                } else {
                    Log.e(TAG, "Invalid spot id: " + postIncomplete.getId());
                }
            }
        });

    }

    private void showMarkerDetail(Place place){
        Intent intent = new Intent(getActivity(), PlaceActivity.class);
        getActivity().startActivity(intent);
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


    private void addMarker(Place place){
        Marker marker = mMap.addMarker(new MarkerOptions()
                .title(place.name)
                .position(place.getPosition())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mapMarkers.put(marker, place);
    }
    /**
     * Create dummy data on the map
     */
    private void loadDummyData(){
        mClusterManagerPost.addItem(Post.createDummy());
        mClusterManagerPost.cluster();

        Place place = Place.createDummy();
        addMarker(place);
       // mClusterManagerSpot.addItem(Place.createDummy());
        //mClusterManagerSpot.addItem(Place.createDummy());
        //mClusterManagerSpot.cluster();
    }

    private void loadData(){
        final LatLngBounds bounds = getMapBounds();
        Log.i(TAG, "Map bounds: " + bounds.southwest + " " + bounds.southwest);

        if (currentZoomMode == ZoomType.OUT || history == null){
            // Remove previous cache and all spots
            mClusterManagerPost.clearItems();
            IntLatLngBounds intBounds = new IntLatLngBounds(bounds);
            Log.i(TAG, "New zoom level: " + intBounds.getMeterWidth() + "m x " + intBounds.getMeterHeight()+"m");
            this.history = new AreaRequestHistory(intBounds.getWidth(), intBounds.getHeight(),
                    intBounds.southwest, this.dataLoader);
        }

        history.updateData(bounds);
    }



    private void addTagMarkers(List<MapTag> mapTags){
        IconGenerator item = new IconGenerator(getActivity());
        item.setTextAppearance(R.style.iconMapTagText);
        /*
        for (MapTag spotTag: mapTags){
            //LatLng ll = spotTag.getLatLng();
            //Bitmap iconBitmap = item.makeIcon(spotTag.name);
            //mMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)).anchor(0.5f, 0.6f));
            mClusterManagerTags.addItem(spotTag);
        }
        mClusterManagerTags.addItems(mapTags);
        mClusterManagerTags.cluster();
        */
    }


    private void showMarkerDetail(MarkerValueInterface markerValue){
        if (markerValue instanceof Place){
            showMarkerDetail((Place) markerValue);
        }
        else if (markerValue instanceof Post){
            showMarkerDetail((Post) markerValue);
        }
    }

    private void setUpMapEvents(){
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                MarkerValueInterface markerValue = mapMarkers.get(marker);
                if (markerValue != null){
                    Log.i(TAG, "You clicked on a marker with place: " + markerValue.getId());
                    showMarkerDetail(markerValue);
                }
                else{
                    Toast.makeText(getActivity(), "Cannot load this marker", Toast.LENGTH_SHORT);
                }

                return true;
            }
        });
    }

    private void setUpClusterer(){
        Log.i(TAG, "Setting up cluster!");

        // Initialize the manager with the context and the map.
        mClusterManagerPost = new ClusterManager<MarkerValueInterface>(getActivity(), mMap);
        mClusterManagerPost.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MarkerValueInterface>() {
            @Override
            public boolean onClusterClick(Cluster<MarkerValueInterface> cluster) {
                Log.d(TAG, "You clicked on a post cluster");
                return true;
            }
        });
        mClusterManagerPost.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerValueInterface>() {
            @Override
            public boolean onClusterItemClick(MarkerValueInterface item) {
                Log.d(TAG, "You clicked on a post cluster item: " + item);
                showMarkerDetail(item);
                return true;
            }

        });
        mMap.setOnMarkerClickListener(mClusterManagerPost);
        mMap.setOnCameraChangeListener(new OnCameraChangeListener());

        mClusterManagerPost.setAlgorithm(new RemovableNonHierarchicalDistanceBasedAlgorithm<MarkerValueInterface>());

        this.dataLoader = new AreaDataLoaderFromAPI(this.getContext(), mClusterManagerPost);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        //RestClient.stopPendingRequest();
    }

    private class OnCameraChangeListener implements GoogleMap.OnCameraChangeListener{
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
                    mClusterManagerPost.clearItems();
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
                            Log.i(TAG, "Post caching too far from origin. Clearing spot history and markers");
                            clearPosts(entry.getValue().data);
                            it.remove();
                            removeNb++;
                        }
                    }
                    if (removeNb > 0)
                        mClusterManagerPost.cluster();
                }
            }
            previousZoomLevel = cameraPosition.zoom;
            mClusterManagerPost.onCameraChange(cameraPosition);
            loadData();
        }

    }
}

/**
 * Google Maps v2 for Android: the Pop-up window with high-grade copying and a support of events of feeding into
 * http://sysmagazine.com/posts/213415/
 *
 */
