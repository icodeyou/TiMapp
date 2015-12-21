package com.timappweb.timapp.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddPostActivity;
import com.timappweb.timapp.adapters.FilledTagsAdapter;
import com.timappweb.timapp.views.FilledRecyclerView;

public class AddPostMainFragment extends Fragment {

    private View view;
    private LinearLayout top_layout_if_not_loaded;
    private LinearLayout top_layout_if_no_group;
    private LinearLayout top_layout_with_group;
    private AddPostActivity addPostActivity;
    private Menu mainMenu;

    private FragmentManager fragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_post_main, container, false);
        this.view = view;

        setHasOptionsMenu(true);

        //Initialize variables
        addPostActivity = (AddPostActivity) getActivity();
        fragmentManager =   getFragmentManager();
        top_layout_if_not_loaded = (LinearLayout) view.findViewById(R.id.top_layout_if_not_loaded);
        top_layout_if_no_group = (LinearLayout) view.findViewById(R.id.top_layout_without_group);
        top_layout_with_group = (LinearLayout) view.findViewById(R.id.top_layout_with_group);

        LinearLayout addTagsLayout = (LinearLayout) view.findViewById(R.id.add_tags_layout);
        addTagsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchFragment();
            }
        });


        initAdapters();

        setAloneVisibilities();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_add_spot_main, menu);
        mainMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(addPostActivity);
                return true;
            case R.id.action_add_tags:
                setSearchFragment();
        }
        return super.onOptionsItemSelected(item);
    }


    public void setGroupVisibilities() {
        top_layout_if_not_loaded.setVisibility(View.VISIBLE);
        top_layout_if_no_group.setVisibility(View.GONE);
        top_layout_with_group.setVisibility(View.GONE);
    }

    public void setAloneVisibilities() {
        top_layout_if_not_loaded.setVisibility(View.GONE);
        top_layout_if_no_group.setVisibility(View.GONE);
        top_layout_with_group.setVisibility(View.GONE);
    }

    private void initAdapters() {
        // Selected tags
        FilledRecyclerView rvSelectedTags = (FilledRecyclerView) view.findViewById(R.id.rv_main_selected_tags);
        FilledTagsAdapter filledTagsAdapter = (FilledTagsAdapter) rvSelectedTags.getAdapter();
        rvSelectedTags.setAdapter(filledTagsAdapter);
    }

    public void setSearchFragment() {
/*
        // Save state
        Bundle b = new Bundle();
        b.putString("MainSelectedTags",addPostActivity.getTagsToString());
        fragmentManager.putFragment(b, "MainFragment", this);
*/
        //Change fragment
        Fragment fragmentSearch = new AddPostSearchFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_add_spot, fragmentSearch, "SearchFragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
