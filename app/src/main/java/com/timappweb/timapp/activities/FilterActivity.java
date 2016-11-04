package com.timappweb.timapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.listeners.OnSuggestQueryListener;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;
import com.timappweb.timapp.managers.SearchTagDataProvider;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends BaseActivity {
    String TAG = "FilterActivity";
    private View                        progressBarView;
    //private BubbleCategoryAdapter     categoriesAdapter;
    //private RecyclerView              categoriesRv;
    //private List<EventCategory>       categoriesSelected;
    private SearchAndSelectTagManager   searchAndSelectTagManager;

    ////////////////////////////////////////////////////////////////////////////////
    //// onCreate
    ////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        progressBarView = findViewById(R.id.progress_view);
        //categoriesRv = (RecyclerView) findViewById(R.id.rv_categories);

        //initCategories();
        //initCategoriesSelected();

        this.initToolbar(false);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "FilterActivity::onResume()");
        super.onResume();
        if(searchAndSelectTagManager != null) searchAndSelectTagManager.setSelectedTags(MyApplication.searchFilter.tags);
    }

    private void initCategories() {
        //categoriesAdapter = new BubbleCategoryAdapter(this);
        //categoriesRv.setAdapter(categoriesAdapter);
        //TODO : Use the same GridLayout than AddEventActivity for categories selection, and delete class GridLayoutManager.
        //GridLayoutManager manager = new SpanningGridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        //categoriesRv.setLayoutManager(manager);
    }


    ////////////////////////////////////////////////////////////////////////////////
    //// onCreateOptionsMenu
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "FilterActivity::onCreateOptionsMenu()");
        final Activity thatActivity = this;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_filter, menu);

        SearchView searchView = initSearchView(menu);
        searchView.clearFocus();
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        //set hint for searchview
        searchView.setQueryHint(getString(R.string.hint_searchview_filter));
        searchAndSelectTagManager = new SearchAndSelectTagManager(
                this,
                searchView,
                (RecyclerView) findViewById(R.id.rv_suggested_tags_filter),
                (HorizontalTagsRecyclerView) findViewById(R.id.rv_selected_tags),
                new OnSuggestQueryListener(),
                findViewById(R.id.action_validate_search),
                findViewById(R.id.check_layout),
                findViewById(R.id.bottom_line_hrv),
                3 //TODO [important] Get config from server
        )
        .setDataProvider(new SearchTagDataProvider() {
                    @Override
                    public void onLoadEnds() {
                        //Write here things to do when tags are loaded
                    }
                });
        searchAndSelectTagManager.loadTags("");

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //// onOptionsItemSelected
    ////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //IntentsUtils.home(this);
                finish();
                return true;
            case R.id.action_validate_search:
                // MyApplication.searchFilter.eventCategories = categoriesAdapter.getAllCategories();
                List<Tag> data = searchAndSelectTagManager.getSelectedTags();
                MyApplication.searchFilter.tags = new ArrayList<>(data);
                Log.d(TAG, "Selected tags: " + Tag.tagsToString(MyApplication.searchFilter.tags));
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

/*
    private void initCategoriesSelected() {
        //If nothing is saved in the preferences
        categoriesSelected = new ArrayList<>(categoriesAdapter.getAllCategories());
    }*/

    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS
/*
    public void selectCategory(EventCategory category) {
        categoriesSelected.add(category);
    }

    public void unselectCategory(EventCategory category) {
        categoriesSelected.remove(category);
    }

    public List<EventCategory> getCategoriesSelected() {
        return categoriesSelected;
    }*/

}
