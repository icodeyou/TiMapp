package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.MapTag;
import com.timappweb.timapp.entities.MarkerValueInterface;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.exceptions.NoLastLocationException;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.listeners.OnItemViewRendered;
import com.timappweb.timapp.map.PlaceClusterRenderer;
import com.timappweb.timapp.map.RemovableNonHierarchicalDistanceBasedAlgorithm;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestHistory;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestItem;
import com.timappweb.timapp.utils.AreaDataCaching.AreaRequestItemFactory;
import com.timappweb.timapp.utils.MyLocationProvider;
import com.timappweb.timapp.utils.TimeTaskCallback;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.HashMap;
import java.util.List;

public class ExploreMapFragment extends Fragment implements OnExploreTabSelectedListener{
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
    private RecyclerView placesViewer;
    private PlacesAdapter placesAdapter;
    private View progressView;
    private HorizontalTagsRecyclerView filterTagsRv;
    private View filterTagsContainer;

    private static HashMap<Marker, Place> mapMarkers;
    private GoogleMap map;
    private Bundle mapBundle;

    private ExploreFragment exploreFragment;
    private DrawerActivity drawerActivity;
    private FloatingActionButton addSpotFloatingButton;
    //private EachSecondTimerTask eachSecondTimerTask;

    @Override
    public void onTabSelected() {
        Log.d(TAG, "ExploreMapFragment is now selected");
        //drawerActivity.updateFabPosition(placesViewer);
    }

    public AreaRequestHistory getHistory() {
        return history;
    }


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
        Log.d(TAG, "onCreateView");

        root = inflater.inflate(R.layout.fragment_explore_map, container, false);

        exploreFragment = (ExploreFragment) getParentFragment();
        drawerActivity = (DrawerActivity) exploreFragment.getActivity();

        setHasOptionsMenu(true);

        mapView = (MapView) root.findViewById(R.id.map);
        mapView.onCreate(mapBundle);
        progressView = root.findViewById(R.id.progress_view);
        filterTagsRv = (HorizontalTagsRecyclerView) root.findViewById(R.id.search_tags);
        filterTagsContainer = root.findViewById(R.id.search_tags_container);
        placesViewer = (RecyclerView) root.findViewById(R.id.places_viewer);
        addSpotFloatingButton = (FloatingActionButton) root.findViewById(R.id.fab);

        setListeners();
        initPlaceAdapter();


        if (savedInstanceState == null){
            // TODO what happens with instance
        }
        else{
            Log.d(TAG, "Instance saved for map fragment");
        }
        this.loadMapIfNeeded();

        updateFilterView();
        initListeners();

        return root;
    }

    private void setListeners() {
        Log.d(TAG, "Init add_spot_button button");
        addSpotFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.addPostStepLocate(drawerActivity);
            }
        });
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();

        //eachSecondTimerTask.cancel();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
        Log.d(TAG, "ExploreMapFragment.onResume()");
        this.loadMapIfNeeded();

        /*eachSecondTimerTask = EachSecondTimerTask.add(new TimeTaskCallback() {
            @Override
            public void update() {
                placesAdapter.notifyDataSetChanged();
            }
        });*/
    }

    private void initPlaceAdapter() {
        //RV
        placesViewer.setLayoutManager(new LinearLayoutManager(getContext()));

        //Adapter
        placesAdapter = new PlacesAdapter(drawerActivity, false, false);
        placesAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Place place = placesAdapter.getItem(position);
                IntentsUtils.viewPlaceFromMap(getActivity(), place);
            }
        });
        placesViewer.setAdapter(placesAdapter);
    }

    public void setLoaderVisibility(boolean bool) {
        if(progressView!=null) {
            if(bool) {
                progressView.setVisibility(View.VISIBLE);
            }
            else {
                progressView.setVisibility(View.GONE);
            }
        }
    }

    private void displayPlace(Place place) {
        placesAdapter.clear();
        placesAdapter.add(place);
    }


    public void hidePlace() {
        placesAdapter.clear();
        placesAdapter.notifyDataSetChanged();
    }


    public void updateFilterView() {
        if(isFilterActive()) {
            filterTagsContainer.setVisibility(View.VISIBLE);
            filterTagsRv.getAdapter().setData(MyApplication.searchFilter.tags);
            Log.d(TAG,"Number of tags filtered : " + MyApplication.searchFilter.tags.size());
            gMap.setPadding(0,120,0,0);
        } else {
            filterTagsContainer.setVisibility(View.GONE);
            gMap.setPadding(0, 0, 0, 0);
        }
        getActivity().invalidateOptionsMenu();
    }


    private void initListeners() {
        /*placesAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Place place = placesAdapter.getItem(position);
                IntentsUtils.viewPlaceFromMap(getActivity(), place);
            }
        });*/

        filterTagsRv.getAdapter().setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                IntentsUtils.filter(getActivity());
            }
        });

        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hidePlace();
            }
        });
    }

    private void loadMapIfNeeded() {
        try {
            if (gMap == null){
                gMap = ((MapView) root.findViewById(R.id.map)).getMap();
                loadMap();
                mapMarkers = new HashMap<>();
            }
            gMap.setIndoorEnabled(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMap() {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "Map is now ready!");
                gMap = googleMap;
                mapBounds = gMap.getProjection().getVisibleRegion().latLngBounds;
                loadMapData();
            }
        });
    }

    public boolean isFilterActive() {
        return MyApplication.searchFilter.tags!=null && MyApplication.searchFilter.tags.size()!=0;
    }

    private void initAreaRequestHistory(){
        history = new AreaRequestHistory(exploreFragment.getDataLoader());
        history.setAreaRequestItemFactory(new AreaRequestItemFactory() {
            @Override
            public AreaRequestItem build() {
                AreaRequestItem<Place> requestItem = new AreaRequestItem<Place>();
                requestItem.setListener(new AreaRequestItem.OnDataChangeListener() {
                    @Override
                    public void onDataChange() {
                        Log.d(TAG, "AreaRequestItem.onDataChange(): ");
                        // Each time data change we reset everything
                        List<Place> places = history.getInsideBoundsItems(getMapBounds());
                        mClusterManagerPost.clearItems();
                        mClusterManagerPost.addItems(places);
                        mClusterManagerPost.cluster();
                    }
                });
                return requestItem;
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

            this.initAreaRequestHistory();
            exploreFragment.getDataLoader().setAreaRequestHistory(this.history);
            history.resizeArea(getMapBounds());
            this.updateMapData();

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

    public void updateMapData(){
        final LatLngBounds bounds = getMapBounds();
        Log.i(TAG, "Map bounds: " + bounds.southwest + " " + bounds.southwest);

        if (currentZoomMode == ZoomType.OUT){
            // Remove previous cache and all markers
            history.resizeArea(bounds);
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
        if(placesAdapter.getData().size()!=0 && placesAdapter.getItem(0)==place) {
            IntentsUtils.viewPlaceFromMap(getActivity(), place);
        } else {
            displayPlace(place);
        }
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
        return placesAdapter.getItemCount()!=0;
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
                // Steph, je sais pas pourquoi tu fais des modifications sur cet adapter.
                // C'est l'adapter qui concerne le "placeViewer" qui au bottom de la page
                //placesAdapter.clear();
                //placesAdapter.addAll(cluster.getItems());
                ((ExploreFragment)getParentFragment()).getViewPager().setCurrentItem(1);
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

        this.exploreFragment.getDataLoader().setClusterManager(mClusterManagerPost);
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
            updateMapData();
        }
    }

}
