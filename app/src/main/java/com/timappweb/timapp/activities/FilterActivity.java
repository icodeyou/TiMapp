package com.timappweb.timapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.greenfrvr.hashtagview.HashtagView;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.adapters.DataTransformTag;
import com.timappweb.timapp.adapters.FilterCategoriesAdapter;
import com.timappweb.timapp.listeners.ColorSquareOnTouchListener;
import com.timappweb.timapp.listeners.OnFilterQueryTagListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;
import com.timappweb.timapp.R;
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
    //private RecyclerView categoriesRv;
    private View saveButton;
    private FilterCategoriesAdapter categoriesAdapter;
    //private List<Category> categoriesSelected;
    private TextView textSaveButton;
    private HorizontalTagsRecyclerView selectedTagsRecyclerView;
    private HashtagView hashtagView;
    private View tagScrollView;

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
        saveButton = findViewById(R.id.save_button);
        textSaveButton = (TextView) findViewById(R.id.text_save_button);
        selectedTagsRecyclerView = (HorizontalTagsRecyclerView) findViewById(R.id.rv_selected_tags);
        hashtagView = (HashtagView) findViewById(R.id.rv_suggested_tags_filter);
        tagScrollView = findViewById(R.id.tags_scrollview);

        initAdapterAndManager();
        //initCategoriesSelected();
        setListeners();
        setTopRvVisibility();

        this.initToolbar(false);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "FilterActivity::onResume()");
        super.onResume();
        selectedTagsRecyclerView.getAdapter().setData(MyApplication.searchFilter.tags);
        setTopRvVisibility();
    }

    private void initAdapterAndManager() {
        //categoriesAdapter = new FilterCategoriesAdapter(this);
        //categoriesRv.setAdapter(categoriesAdapter);
        GridLayoutManager manager = new SpanningGridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        //categoriesRv.setLayoutManager(manager);
    }

    private void setListeners() {
        saveButton.setOnTouchListener(new ColorSquareOnTouchListener(this,textSaveButton));

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        hashtagView.addOnTagClickListener(new HashtagView.TagsClickListener() {
            @Override
            public void onItemClicked(Object item) {
                Tag tag = (Tag) item;
                searchView.setQuery(tag.name, true);
                searchView.clearFocus();
            }
        });

        selectedTagsRecyclerView.getAdapter().setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Log.d(TAG, "Clicked on selected item");
                selectedTagsRecyclerView.getAdapter().removeData(position);
                //setTextButton();
            }
        });
    }

    public void submit() {
        // MyApplication.searchFilter.categories = categoriesAdapter.getAllCategories();
        List<Tag> data = selectedTagsRecyclerView.getAdapter().getData();

        /*MyApplication.searchFilter.tags = new ArrayList<>();
        for(int i=0; i<data.size();i++) {
            MyApplication.searchFilter.tags.add(new Tag(""));
        }
        Collections.copy(MyApplication.searchFilter.tags, data);*/

        MyApplication.searchFilter.tags = new ArrayList<>(data);
        Log.d(TAG, "Selected tags: " + Tag.tagsToString(MyApplication.searchFilter.tags));
        NavUtils.navigateUpFromSameTask(this);
    }


    ////////////////////////////////////////////////////////////////////////////////
    //// onCreateOptionsMenu
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "FilterActivity::onCreateOptionsMenu()");
        final Activity thatActivity = this;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_tags, menu);

        setSearchview(menu);
        searchView.clearFocus();
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        //set hint for searchview
        searchView.setQueryHint(getString(R.string.hint_searchview_filter));
        OnFilterQueryTagListener onFilterQueryTagListener = new OnFilterQueryTagListener(this);
        hashtagView.setTransformer(new DataTransformTag());
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
                        tagScrollView.setVisibility(View.VISIBLE);
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

    public void setTopRvVisibility() {
        if(selectedTagsRecyclerView.getAdapter().getData().size()==0) {
            selectedTagsRecyclerView.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
        } else {
            selectedTagsRecyclerView.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
        }
    }

    public void setTextButton() {
        if(selectedTagsRecyclerView.getAdapter().getData().size()==0) {
            textSaveButton.setText(R.string.search_button_empty);
        } else {
            textSaveButton.setText(R.string.search_button);
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
    public void selectCategory(Category category) {
        categoriesSelected.add(category);
    }

    public void unselectCategory(Category category) {
        categoriesSelected.remove(category);
    }

    public List<Category> getCategoriesSelected() {
        return categoriesSelected;
    }*/

    public View getProgressBarView() {
        return progressBarView;
    }

}
