package com.timappweb.timapp.fragments;

import android.location.Location;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.MapTag;
import com.timappweb.timapp.entities.MarkerValueInterface;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.exceptions.NoLastLocationException;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.map.PlaceClusterRenderer;
import com.timappweb.timapp.map.RemovableNonHierarchicalDistanceBasedAlgorithm;
import com.timappweb.timapp.utils.AreaDataCaching.AreaDataLoaderFromAPI;
import com.timappweb.timapp.utils.AreaDataCaching.AreaDataLoaderInterface;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestHistory;
import com.timappweb.timapp.utils.MyLocationProvider;

import java.util.HashMap;
import java.util.List;

public class ExploreMapFragment extends Fragment{
    private Context context;
    private static final String TAG = "GoogleMapFragment";
    private static final long TIME_WAIT_MAP_VIEW = 500;
    private static LatLngBounds mapBounds;

    // Declare a variable for the cluster manager.
    private ClusterManager<Place> mClusterManagerPost;
    private GoogleMap gMap = null;
    private MapView mapView = null;
    private float previousZoomLevel = -1;
    private float currentZoomLevel = -1;

    //Views
    private View root;
    private ListView placesViewer;
    private PlacesAdapter placesAdapter;

    private static HashMap<Marker, Place> mapMarkers;
    private AreaDataLoaderInterface dataLoader;
    private GoogleMap map;
    private Bundle mapBundle;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context= getActivity().getApplicationContext();
        root = inflater.inflate(R.layout.fragment_explore_map, container, false);

        Log.i(TAG, "OnViewCreated");
        setHasOptionsMenu(true);

        //initialize
        mapView = (MapView) root.findViewById(R.id.map);
        mapView.onCreate(mapBundle);
        placesViewer = (ListView) root.findViewById(R.id.places_viewer);
        placesAdapter = new PlacesAdapter(getActivity());

        if (savedInstanceState == null){
            // TODO what happens with instance
        }
        else{
            Log.d(TAG, "Instance saved for map fragment");
        }
        this.loadMapIfNeeded();

        initListeners();

        return root;
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
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
        Log.d(TAG, "ExploreMapFragment.onResume()");
        this.loadMapIfNeeded();
    }




    private void initListeners() {
        placesAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Place place = placesAdapter.getItem(position);
                IntentsUtils.viewPlaceFromMap(getActivity(), place);
            }
        });

        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hidePlace();
            }
        });
    }

    private void displayPlace(Place place) {
        placesViewer.setVisibility(View.VISIBLE);
        placesAdapter.clear();
        placesAdapter.add(place);
        placesViewer.setAdapter(placesAdapter);
    }

    public void hidePlace() {
        placesViewer.setVisibility(View.GONE);
    }

    private void loadMapIfNeeded() {
        if (gMap == null){
            gMap = ((MapView) root.findViewById(R.id.map)).getMap();
            loadMap();
            mapMarkers = new HashMap<>();
        }
    }

    public void loadMap() {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "Map is now ready!");
                gMap = googleMap;
                loadMapData();
            }
        });
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #gMap} is not null.
     */
    private void loadMapData() {
        Log.i(TAG, "Setting up the map... Please wait.");

        // For showing a move to my loction button
        gMap.setMyLocationEnabled(true);

        try{
            setUpClusterer();
            //setUpMapEvents();
            centerMap();
            //loadDummyData();
        } catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
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
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MyLocationProvider.convert(locationProvider.getLastGPSLocation()), 12.0f));
        }
        catch (NoLastLocationException ex){
            Log.d(TAG, "Cannot center: no last name");
        }
    }

    public static LatLngBounds getMapBounds(){
        return mapBounds;
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
            //gMap.addMarker(new MarkerOptions().position(ll).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)).anchor(0.5f, 0.6f));
            mClusterManagerTags.addItem(spotTag);
        }
        mClusterManagerTags.addItems(mapTags);
        mClusterManagerTags.cluster();
        */
    }


    private void showMarkerDetail(MarkerValueInterface markerValue){
        Place place = (Place) markerValue;
        displayPlace(place);
    }

    private void setUpMapEvents(){
        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                MarkerValueInterface markerValue = mapMarkers.get(marker);
                if (markerValue != null) {
                    Log.i(TAG, "You clicked on a marker with viewPlaceFromPublish: " + markerValue.getId());
                    showMarkerDetail(markerValue);
                } else {
                    Toast.makeText(getActivity(), "Cannot load this marker", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });
    }

    public boolean isPlacesViewerVisible() {
        return placesViewer.getVisibility()==View.VISIBLE;
    }

    private void setUpClusterer(){
        Log.i(TAG, "Setting up cluster!");

        // Initialize the manager with the context and the map.
        mClusterManagerPost = new ClusterManager<Place>(getActivity(), gMap);
        mClusterManagerPost.setRenderer(new PlaceClusterRenderer(getActivity(), gMap, mClusterManagerPost));
        mClusterManagerPost.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Place>() {
            @Override
            public boolean onClusterClick(Cluster<Place> cluster) {
                Log.d(TAG, "You clicked on a post cluster");
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), currentZoomLevel + 1));
                return true;
            }
        });
        mClusterManagerPost.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Place>() {
            @Override
            public boolean onClusterItemClick(Place item) {
                Log.d(TAG, "You clicked on a cluster item: " + item);
                showMarkerDetail(item);
                return true;
            }

        });
        gMap.setOnMarkerClickListener(mClusterManagerPost);
        gMap.setOnCameraChangeListener(new OnCameraChangeListener());

        mClusterManagerPost.setAlgorithm(new RemovableNonHierarchicalDistanceBasedAlgorithm<Place>());

        this.dataLoader = new AreaDataLoaderFromAPI(this.getContext(), mClusterManagerPost);
    }

    @Override
    public void onDestroyView(){
        mapView.onDestroy();
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
            mapBounds = gMap.getProjection().getVisibleRegion().latLngBounds;

            // Updating datas
            loadData();
        }
    }

}
