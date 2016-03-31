package com.timappweb.timapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.List;

public class ExplorePlacesFragment extends Fragment implements OnExploreTabSelectedListener{

    private static final String TAG = "PlaceTagsFragment";
    private PlacesAdapter placesAdapter;
    private ExploreFragment exploreFragment;
    private DrawerActivity drawerActivity;
    private TextView newEventButton;
    private View progressView;
    private View noEventsView;
    //private EachSecondTimerTask eachSecondTimerTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View root = inflater.inflate(R.layout.fragment_explore_places, container, false);

        exploreFragment = (ExploreFragment) getParentFragment();
        drawerActivity = (DrawerActivity) exploreFragment.getActivity();

        //Views
        ListView lvTags = (ListView) root.findViewById(R.id.list_places);
        progressView = root.findViewById(R.id.loading_view);
        noEventsView = root.findViewById(R.id.no_events_view);

        //ListView and footer
        View v = getLayoutInflater(savedInstanceState).inflate(R.layout.explore_places_button,null);
        lvTags.addFooterView(v);
        newEventButton = (TextView) v.findViewById(R.id.create_button);
        newEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.addPlace(drawerActivity);
            }
        });

        //Adapter
        placesAdapter = new PlacesAdapter(getContext());
        lvTags.setAdapter(placesAdapter);
        placesAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                IntentsUtils.viewPlaceFromMap(getContext(), placesAdapter.getItem(position));
            }
        });
        /*lvTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Place place = placesAdapter.getItem(position);
                IntentsUtils.viewPlaceFromMap(getActivity(), place);
            }
        });*/

        return root;
    }

    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");

        /*eachSecondTimerTask = EachSecondTimerTask.add(new TimeTaskCallback() {
            @Override
            public void update() {
                placesAdapter.notifyDataSetChanged();
            }
        });
*/
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        //eachSecondTimerTask.cancel();
    }

    @Override
    public void onTabSelected() {
        Log.d(TAG, "Explore places fragment is now selected");

        if(placesAdapter != null) {
            placesAdapter.clear();
            ExploreMapFragment exploreMapFragment = exploreFragment.getExploreMapFragment();
            List<Place> markers = exploreFragment.getAreaRequestHistory().getInsideBoundsItems(exploreMapFragment.getMapBounds());
            placesAdapter.addAll(markers);
            if(placesAdapter.isEmpty()) {
                noEventsView.setVisibility(View.VISIBLE);
            } else {
                noEventsView.setVisibility(View.GONE);
            }
        }
    }
}
