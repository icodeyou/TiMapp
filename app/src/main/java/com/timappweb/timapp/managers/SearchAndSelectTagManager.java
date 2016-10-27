package com.timappweb.timapp.managers;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.greenfrvr.hashtagview.HashtagView;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.DataTransformTag;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.listeners.OnBasicQueryTagListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.utils.SearchHistory;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;


public class SearchAndSelectTagManager {

    private static final String TAG = "SearchAndSelectTag";

    private Activity activity;
    private SearchView searchView;
    private HorizontalTagsRecyclerView selectedRV;
    private HashtagView suggestedRV;
    private SearchHistory searchHistory;
    private HorizontalTagsAdapter horizontalAdapter;
    private OnBasicQueryTagListener queryTagListener;
    private final View validateButton;
    private View line;
    private final View finishView;
    private int maxTags;

    public SearchAndSelectTagManager(Activity activity,
                                     SearchView searchView,
                                     HashtagView suggestedRV,
                                     HorizontalTagsRecyclerView selectedRecyclerView,
                                     OnBasicQueryTagListener queryTagListener,
                                     View validateButton,
                                     View finishView,
                                     View line,
                                     int maxTags) {
        this.activity = activity;
        this.searchView = searchView;
        this.suggestedRV = suggestedRV;
        this.selectedRV = selectedRecyclerView;
        this.horizontalAdapter = selectedRV.getAdapter();
        this.queryTagListener = queryTagListener;
        this.validateButton = validateButton;
        this.line = line;
        this.maxTags = maxTags;
        this.finishView = finishView;

        this.init();
    }

    public SearchAndSelectTagManager setDataProvider(SearchTagDataProvider provider){
        provider.setManager(this);
        this.searchHistory.setDataProvider(provider);
        return this;
    }

    private void init(){

        final SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);

        this.searchHistory = new SearchHistory<Tag>(
                ConfigurationProvider.rules().tags_min_search_length,
                ConfigurationProvider.rules().tags_suggest_limit);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
        queryTagListener.setSearchAndSelectTagManager(this);
        searchView.setOnQueryTextListener(queryTagListener);

        actionCounter();

        horizontalAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Log.d(TAG, "Clicked on selected item");
                Tag tag = selectedRV.getAdapter().getData(position);
                selectedRV.getAdapter().removeData(position);
                actionCounter();
                if(suggestedRV != null) {
                    suggestedRV.addItem(tag);
                }
                else {
                    searchView.setVisibility(View.VISIBLE);
                    searchView.requestFocus();
                }
            }
        });

        if(suggestedRV != null) {
            suggestedRV.addOnTagClickListener(new HashtagView.TagsClickListener() {
                @Override
                public void onItemClicked(Object item) {
                    Tag tag = (Tag) item;
                    if (addTag(tag.getName())){
                        suggestedRV.removeItem(item);
                        actionCounter();
                    }
                }
            });
            suggestedRV.setData(new LinkedList<Tag>(), new DataTransformTag());
        }
    }
    /**
     * Suggest tag according to user input
     * Cache results according to the term given
     * @param term
     */
    public void suggestTag(final String term){
        if (term.length() < ConfigurationProvider.rules().tags_min_search_length){
            return;
        }
        this.loadTags(term);
    }

    public void loadTags(final String term){
        searchHistory.search(term);
    }

    public void setSuggestedData(List<Tag> tags) {
        if(suggestedRV != null) {
            suggestedRV.setData(tags);
            horizontalAdapter.notifyDataSetChanged();
        }
    }

    public boolean addTag(String tag) {
        if (tryAddData(tag.replaceAll("\\s",""))){ //replaceAll remove eventual spaces
            selectedRV.scrollToEnd();
            searchView.setQuery("",false);
            return true;
        }
        return false;
    }

    private boolean tryAddData(String selectedTag) {
        Tag newTag = new Tag(selectedTag, 0);
        Log.d(TAG,"REGEX is : " + ConfigurationProvider.rules().tags_name_regex);
        if (horizontalAdapter.getData().contains(newTag)) {
            Toast.makeText(activity, R.string.toast_tag_already_chosen, Toast.LENGTH_SHORT).show();
            return false;
        } else if(horizontalAdapter.getData().size() >= maxTags) {
            String message = activity.getResources().
                    getQuantityString(R.plurals.searchview_hint_few_tags, maxTags, maxTags);
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(newTag.getName().isEmpty()) {
            Toast.makeText(activity, R.string.toast_no_tag, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (!newTag.isShortEnough()){
            Toast.makeText(activity, R.string.toast_tiny_text_size, Toast.LENGTH_LONG).show();
            return false;
        }
        else if (!newTag.isLongEnough()) {
            Toast.makeText(activity, R.string.toast_huge_text_size, Toast.LENGTH_LONG).show();
            return false;
        }
        else if (Pattern.compile(ConfigurationProvider.rules().tags_name_regex).matcher(newTag.getName()).matches()) {
            Toast.makeText(activity, R.string.toast_tag_not_valid, Toast.LENGTH_LONG).show();
            return false;
        }
        else {
            newTag.setName(newTag.getName());
            horizontalAdapter.add(newTag);
            return true;
        }
    }

    public SearchHistory getSearchHistory() {
        return searchHistory;
    }

    public boolean hasSelectedTag(Tag tag) {
        return selectedRV != null && horizontalAdapter.getData().contains(tag);
    }

    public boolean hasSuggestedTag(String tag) {
        return true; //TODO Jack
    }

    public void actionCounter() {
        // If we ever want to update the keyboard by calling setImeOptions,
        // we need to clear the focus of the searchview.
        int numberTags = horizontalAdapter.getData().size();
        if(validateButton != null) validateButton.setEnabled(numberTags>= selectedRV.getMinTags());

        int tagsLeft = (maxTags-numberTags);
        Log.d(TAG, "tags left : "+tagsLeft);
        searchView.setQueryHint(activity.getResources().
                getQuantityString(R.plurals.searchview_hint_few_tags, tagsLeft, tagsLeft));

        if(numberTags == 0) {
            if(line != null) line.setVisibility(View.GONE);
            selectedRV.setVisibility(View.GONE);
            searchView.setQueryHint(activity.getResources().getString(R.string.searchview_hint_no_tags));
        } else if(numberTags == maxTags) {
            searchView.clearFocus();
            searchView.setVisibility(View.INVISIBLE);
            finishView.setVisibility(View.VISIBLE);
            if(suggestedRV != null) suggestedRV.setVisibility(View.GONE);
        } else {
            selectedRV.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.VISIBLE);
            finishView.setVisibility(View.GONE);
            if(suggestedRV != null) suggestedRV.setVisibility(View.VISIBLE);
            if(line != null) line.setVisibility(View.VISIBLE);
        }
    }

    public void setQuery(String tag, boolean save) {
        searchView.setEnabled(false);
        searchView.setQuery(tag, save);
        searchView.setEnabled(true);
    }

    public List<Tag> getSelectedTags() {
        return horizontalAdapter.getData();
    }
}