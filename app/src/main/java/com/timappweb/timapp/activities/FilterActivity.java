package com.timappweb.timapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.greenfrvr.hashtagview.HashtagView;
import com.timappweb.timapp.adapters.DataTransformTag;
import com.timappweb.timapp.adapters.FilterCategoriesAdapter;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.listeners.OnBasicQueryTagListener;
import com.timappweb.timapp.listeners.OnFilterQueryTagListener;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.managers.SearchTagDataProvider;
import com.timappweb.timapp.managers.SpanningGridLayoutManager;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends BaseActivity {
    String TAG = "FilterActivity";
    private SearchAndSelectTagManager searchAndSelectTagManager;
    private Activity activity=this;
    private View progressBarView;
    private RecyclerView categoriesRv;
    private View searchButton;
    private FilterCategoriesAdapter categoriesAdapter;
    private List<Category> categoriesSelected;
    private TextView textSearchButton;

    ////////////////////////////////////////////////////////////////////////////////
    //// onCreate
    ////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        progressBarView = findViewById(R.id.progress_view);
        categoriesRv = (RecyclerView) findViewById(R.id.rv_categories);
        searchButton = findViewById(R.id.search_button);
        textSearchButton = (TextView) findViewById(R.id.text_search_button);

        initAdapterAndManager();
        initCategoriesSelected();
        setListeners();

        this.initToolbar(false);
    }

    private void initAdapterAndManager() {
        categoriesAdapter = new FilterCategoriesAdapter(this);
        categoriesRv.setAdapter(categoriesAdapter);
        GridLayoutManager manager = new SpanningGridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        categoriesRv.setLayoutManager(manager);
    }

    private void setListeners() {
        setSquareTouchListener(searchButton, textSearchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    public void submit() {
        //TODO : Enregistrer categoriesSelected + les tags dans les preférences
        NavUtils.navigateUpFromSameTask(this);
        finish();
    }
////////////////////////////////////////////////////////////////////////////////
    //// onCreateOptionsMenu
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final Activity thatActivity = this;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_tags, menu);
        setSearchview(menu);
        searchView.clearFocus();
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        //set hint for searchview
        searchView.setQueryHint(getString(R.string.hint_searchview_add_post));
        OnFilterQueryTagListener onFilterQueryTagListener = new OnFilterQueryTagListener(this);
        HashtagView hashtagView = (HashtagView) findViewById(R.id.rv_suggested_tags_filter);
        hashtagView.setTransformer(new DataTransformTag());
        HorizontalTagsRecyclerView selectedTagsRecyclerView = (HorizontalTagsRecyclerView) findViewById(R.id.rv_selected_tags);
        searchAndSelectTagManager = new SearchAndSelectTagManager(
                this,
                searchView,
                hashtagView,
                selectedTagsRecyclerView,
                onFilterQueryTagListener,
                new SearchTagDataProvider() {
                    @Override
                    public void onLoadEnds() {
                        getProgressBarView().setVisibility(View.GONE);
                    }
                }
        );

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
                IntentsUtils.home(this);
                return true;
            case R.id.action_search:
                /////Handle search actions here
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    public void onUpdateClick(View view) {
        IntentsUtils.home(this);
    }


    private void initCategoriesSelected() {
        //If nothing is saved in the preferences
        categoriesSelected = new ArrayList<Category>(categoriesAdapter.getAllCategories());
        //TODO Steph : Récupérer les catégories selectionnées à partir des préférences
    }

    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    public void selectCategory(Category category) {
        categoriesSelected.add(category);
    }

    public void unselectCategory(Category category) {
        categoriesSelected.remove(category);
    }

    public List<Category> getCategoriesSelected() {
        return categoriesSelected;
    }

    public View getProgressBarView() {
        return progressBarView;
    }

}
