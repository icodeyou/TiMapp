package com.timappweb.timapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.utils.EachSecondTimerTask;
import com.timappweb.timapp.utils.TimeTaskCallback;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ExplorePlacesFragment extends Fragment implements OnExploreTabSelectedListener{

    private static final String TAG = "PlaceTagsFragment";
    private PlacesAdapter placesAdapter;
    private ExploreFragment exploreFragment;
    private DrawerActivity drawerActivity;
    private TextView newEventButton;
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

        //ListView and footer
        View v = getLayoutInflater(savedInstanceState).inflate(R.layout.item_listview_footer,null);
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
        //drawerActivity.clearFabPosition();

        if(placesAdapter != null) {
            /*// Updating the list of places
            ArrayList<HorizontalTagsRecyclerView> rvs = placesAdapter.getListRvTags();
            for (HorizontalTagsRecyclerView rv : rvs) {
                rv.getScrollState();
            }*/

            //TODO : Get scroll position for each horizontal RecyclerView
            placesAdapter.clear();
            ExploreMapFragment exploreMapFragment = exploreFragment.getExploreMapFragment();
            List<Place> markers = exploreFragment.getAreaRequestHistory().getInsideBoundsItems(exploreMapFragment.getMapBounds());
            placesAdapter.addAll(markers);
            //TODO : Set scroll position for each horizontal RecyclerView
        }
    }
}
