package com.timappweb.timapp.fragments;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
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
import com.timappweb.timapp.entities.MapTag;
import com.timappweb.timapp.entities.MarkerValueInterface;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.exceptions.NoLastLocationException;
import com.timappweb.timapp.map.RemovableNonHierarchicalDistanceBasedAlgorithm;
import com.timappweb.timapp.utils.AreaDataCaching.AreaDataLoaderFromAPI;
import com.timappweb.timapp.utils.AreaDataCaching.AreaDataLoaderInterface;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestHistory;
import com.timappweb.timapp.utils.IntentsUtils;
import com.timappweb.timapp.utils.MyLocationProvider;

import java.util.HashMap;
import java.util.List;

public class ExploreMapFragment extends SupportMapFragment {
    private static final String TAG = "GoogleMapFragment";
    private static final long TIME_WAIT_MAP_VIEW = 500;
    private static LatLngBounds mapBounds;

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

    public static int getDataTimeRange(){
        return 7200;        // TODO dynamic value
    }

    /**
     * When we click on a market we need to now the corresponding spot
     */
    //private HashMap<Marker, Post> markers = new HashMap<>();
    //private HashMap<Post, Marker> mapSpotToMarker = new HashMap<>();


    private AreaRequestHistory history;

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

        if (id == R.id.action_filter) {
            IntentsUtils.filter(getActivity());
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "ExploreMapFragment.onResume()");
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
        IntentsUtils.post(getActivity(), postIncomplete.getId());
    }

    private void showMarkerDetail(Place place){
        IntentsUtils.viewPlace(getActivity());
    }

    private void centerMap(){
        // Comme les unités sont en microdegrés, il faut multiplier par 1E6
        MyLocationProvider locationProvider = new MyLocationProvider(getActivity(), new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "LOCATION CHANGED ! name");
            }
        });

        try{
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MyLocationProvider.convert(locationProvider.getLastGPSLocation()), 12.0f));
        }
        catch (NoLastLocationException ex){
            Log.d(TAG, "Cannot center: no last name");
        }
    }

    public static LatLngBounds getMapBounds(){
        return mapBounds;
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
            this.history = new AreaRequestHistory(bounds, this.dataLoader);
        }

        history.update(bounds);
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
                    Log.i(TAG, "You clicked on a marker with viewPlace: " + markerValue.getId());
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
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), currentZoomLevel + 1));
                return true;
            }
        });
        mClusterManagerPost.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerValueInterface>() {
            @Override
            public boolean onClusterItemClick(MarkerValueInterface item) {
                Log.d(TAG, "You clicked on a cluster item: " + item);
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

            previousZoomLevel = cameraPosition.zoom;
            mClusterManagerPost.onCameraChange(cameraPosition);
            // Update bounds
            mapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

            // Updating datas
            loadData();
        }
    }
}

/**
 * Google Maps v2 for Android: the Pop-up window with high-grade copying and a support of events of feeding into
 * http://sysmagazine.com/posts/213415/
 *
 */
