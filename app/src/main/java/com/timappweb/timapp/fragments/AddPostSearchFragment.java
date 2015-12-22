package com.timappweb.timapp.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.Managers.SearchAndSelectTagManager;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddPostActivity;
import com.timappweb.timapp.adapters.TagsAdapter;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.views.FilledRecyclerView;
import com.timappweb.timapp.views.HorizontalRecyclerView;

import java.util.List;

public class AddPostSearchFragment extends Fragment {

    private AddPostActivity addPostActivity;
    private SearchView searchView;
    private HorizontalRecyclerView selectedTagsRV;
    private FilledRecyclerView suggestedTagsRV;
    private Menu searchMenu;
    private MenuItem searchItem;
    private FragmentManager fragmentManager;
    private SearchAndSelectTagManager searchAndSelectTagManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_post_search, container, false);

        setHasOptionsMenu(true);

        //Initialize variables
        fragmentManager = getFragmentManager();
        addPostActivity = (AddPostActivity) getActivity();
        selectedTagsRV = (HorizontalRecyclerView) view.findViewById(R.id.rv_search_selected_tags);
        suggestedTagsRV = (FilledRecyclerView) view.findViewById(R.id.rv_search_suggested_tags);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_add_spot_search, menu);
        searchMenu = menu;

        setSearchview();

        //Set the manager for inputs and suggestions of tags
        searchAndSelectTagManager = new SearchAndSelectTagManager(getActivity(),
                searchView, suggestedTagsRV, selectedTagsRV);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(addPostActivity);
                return true;
            case R.id.action_validate:
                validateAndChangeFragment();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSearchview() {
        //Set search item
        searchItem = searchMenu.findItem(R.id.action_search);

        if(addPostActivity.isSearchFragmentDisplayed()) {
            //Always display the searchview expanded in the action bar
            searchItem.expandActionView();
        }

        //Manage events when there are expand/collapse actions on SearchView
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                addPostActivity.displayMainFragment();
                return false;
            }
        });

        //set searchView
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPostActivity.displaySearchFragment();
            }
        });

        //set hint for searchview
        searchView.setQueryHint(addPostActivity.getString(R.string.hint_searchview_add_post));
    }

    private void validateAndChangeFragment() {

        addPostActivity.displayMainFragment();

        //add new tags to the adapter
        TagsAdapter tagsAdapterSearch = selectedTagsRV.getAdapter();
        List<Tag> newData = tagsAdapterSearch.getData();
        addPostActivity.getFilledTagsAdapter().setData(newData);

        if(newData.size()!=0) {
            addPostActivity.getFragmentMain().hideAddTagsLayout();
            addPostActivity.getFragmentMain().displaySelectedTagsRV();
        } else {
            addPostActivity.getFragmentMain().displayAddTagsLayout();
            addPostActivity.getFragmentMain().hideSelectedTagsRV();
        }
    }

    public MenuItem getSearchItem() {
        return searchItem;
    }

    public SearchAndSelectTagManager getSearchAndSelectTagManager() {
        return searchAndSelectTagManager;
    }

    public RecyclerView getSelectedTagsRV() {
        return selectedTagsRV;
    }
}