package com.timappweb.timapp.utils;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Created by stephane on 12/9/2015.
 *
 * Search history.
 *
 * @param <T> data type contained
 */
public class SearchHistory<T>{

    private static final String TAG = "SearchHistory";

    // ---------------------------------------------------------------------------------------------

    /**
     * Minimum search length. If search term is smaller than this value, no search will be triggered
     */
    private int         minimumSearchLength;
    /**
     * Maximum result number to store for a search result
     */
    private int         maximumResults;
    /**
     * Last search term
     */
    private String      lastSearch              = ""; // Last search to update view accordingly...
    /**
     * match data to a search term
     * Item wraps data corresponding to the search term to provides useful info
     */
    private HashMap<String, Item> data;

    /**
     * Data provider. Use to get entries according to the seach term
     */
    private DataProvider provider;

    // ---------------------------------------------------------------------------------------------

    public SearchHistory(DataProvider provider, int minimumSearchLength, int maximumResults) {
        this.data = new HashMap<>();
        this.minimumSearchLength = minimumSearchLength;
        this.maximumResults = maximumResults;
        this.provider = provider;
    }

    public SearchHistory(int minimumSearchLength, int maximumResults) {
        this(null, minimumSearchLength, maximumResults);
    }

    // ---------------------------------------------------------------------------------------------

    public void search(String term){
        if (term.length() < this.minimumSearchLength){
            return ;
        }
        Log.d(TAG, "Search for term " + term);

        // add data to adapter
        this.setLastSearch(term);

        // Data are in cache and fetch is done
        if (this.hasTerm(term) && !this.data.get(term).pending){
            this.provider.onSearchComplete(term, this.data.get(term).getData());
        }
        else if (!this.hasTerm(term)){
            // Data are not in cache, try searching for a sub term
            SearchHistory.Item subHistory = this.getTermOrSubTerm(term);
            if (subHistory != null){
                this.provider.onSearchComplete(term, subHistory.getFilteredData(term));
                if (subHistory.isComplete()){
                    return ;
                }
            }

            this.create(term);
            provider.load(term);
        }
    }

    /**
     * Get the biggest search term that is not pending
     * @param term
     * @return
     */
    public Item getTermOrSubTerm(String term){
        if (!this.data.containsKey(term) || this.data.get(term).pending){
            if (term.length() > this.minimumSearchLength){
                if (term.length() <= 1){
                    return this.hasTerm("") ? this.data.get("") : null;
                }
                else{
                    return this.getTermOrSubTerm(term.substring(0, term.length() - 2));
                }
            }
            else{
                return null;
            }
        }
        else{
            return this.data.get(term);
        }
    }


    /**
     *
     * @param term
     * @param data
     */
    public void onSearchResponse(String term, List<T> data, boolean append) {
        Item node = this.addInCache(term, data, append);
        if (node != null){
            node.setComplete(node.size() >= this.maximumResults);
        }
        this.provider.onSearchComplete(term, data);
    }

    /**
     * TODO bugs when searching fast
     * 01-31 16:33:05.019 26505-26505/com.timappweb.timapp E/AndroidRuntime: java.lang.NullPointerException: Attempt to invoke interface method 'java.util.Iterator java.util.List.iterator()' on a null object reference
     01-31 16:33:05.019 26505-26505/com.timappweb.timapp E/AndroidRuntime:     at com.timappweb.timapp.utils.SearchHistory.filterData(SearchHistory.java:135)
     01-31 16:33:05.019 26505-26505/com.timappweb.timapp E/AndroidRuntime:     at com.timappweb.timapp.utils.SearchHistory$Item.getFilteredData(SearchHistory.java:179)
     01-31 16:33:05.019 26505-26505/com.timappweb.timapp E/AndroidRuntime:     at com.timappweb.timapp.utils.SearchHistory.search(SearchHistory.java:54)
     01-31 16:33:05.019 26505-26505/com.timappweb.timapp E/AndroidRuntime:     at com.timappweb.timapp.managers.SearchAndSelectTagManager.loadData(SearchAndSelectTagManager.java:113)
     01-31 16:33:05.019 26505-26505/com.timappweb.timapp E/AndroidRuntime:     at com.timappweb.timapp.managers.SearchAndSelectTagManager.suggestTag(SearchAndSelectTagManager.java:109)
     01-31 16:33:05.019 26505-26505/com.timappweb.timapp E/AndroidRuntime:     at com.timappweb.timapp.listeners.OnBasicQueryTagListener.onQueryTextChange(OnBasicQueryTagListener.java:37)
     01-31 16:33:05.019 26505-26505/com.timappweb.timapp E/AndroidRuntime:     at android.support.v7.widget.SearchView.onTextChanged(SearchView.java:1148)
     01-31 16:33:05.019 26505-26505/com.timappweb.timapp E/AndroidRuntime:     at android.support.v7.widget.SearchView.access$2000(SearchView.java:101)
     01-31 16:33:05.019 26505-26505/com.timappweb.timapp E/AndroidRuntime:     at android.support.v7.widget.SearchView$12.onTextChanged(SearchView.java:1622)
     * @param data
     * @param term
     * @param <T>
     * @return
     */
    public static <T> List<T> filterData(List<T> data, String term){
        List<T> results = new LinkedList<>();
        if (data != null){
            for (T d: data){
                if (((SearchableItem)d).matchSearch(term)){
                    results.add(d);
                }
            }
        }
        return results;
    }

    // ---------------------------------------------------------------------------------------------
    // Gettes/Setters

    public void setDataProvider(DataProvider provider){
        this.provider = provider;
    }

    public boolean hasTerm(String term){
        return this.data.containsKey(term);
    }

    public boolean hasTermOrSubTerm(String term){
        return this.getTermOrSubTerm(term) != null;
    }


    public void setLastSearch(String lastSearch) {
        this.lastSearch = lastSearch;
    }

    public boolean isLastSearch(String search){
        return this.lastSearch == search;
    }

    // ---------------------------------------------------------------------------------------------

    protected Item<T> create(String term) {
        Item item = new Item(null, false);
        this.data.put(term, item);
        return item;
    }

    /**
     * Add the term in cache. If
     * @param term
     * @param items
     * @param append
     * @return
     */
    protected Item<T> addInCache(String term, List<T> items, boolean append) {
        Log.d(TAG, "Adding in cache " + items.size() + " element(s) for search " + term);
        // assert (term.length() >= this.minimumSearchLength);
        Item node = this.data.get(term);
        if (node == null) {
            node = this.create(term);
        }
        node.setPending(false);
        if (append){
            node.addData(items);
        }
        else{
            node.setData(items);
        }
        node.setComplete(items.size() > this.maximumResults);
        return node;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Represents a search history item. It can contain any type of data.
     *
     * @param <T> data type that it contains
     */
    public static class Item<T>{
        /**
         * true if this search if complete. Meaning that there is no more data to fetch for this search
         */
        boolean complete = false;
        /**
         * true if data are currently being loaded in an asynchronous thread. It usefull to know
         * if we need to start a new request or if we must wait because there is a pending request
         * going on
         */
        boolean pending = true;

        /**
         * Data.
         * @warning It can be null
         */
        List<T> data;

        public Item(List<T> data, boolean complete) {
            this.data = data;
            this.complete = complete;
            this.pending = true;
        }

        public void setPending(boolean value){
            this.pending = value;
        }

        public void setData(List<T> data) {
            this.data = data;
        }
        public void addData(List<T> data) {
            if (this.data == null)
                setData(data);
            else
                this.data.addAll(data);
        }

        public void setComplete(boolean complete) {
            this.complete = complete;
        }

        public List<T> getData() {
            return data;
        }

        public boolean isComplete() {
            return complete;
        }

        /**
         * @param term
         * @return data filtered with term
         */
        public List<T> getFilteredData(String term) {
            return SearchHistory.filterData(this.data, term);
        }


        public int size() {
            return this.data != null ? this.data.size() : 0;
        }
    };

    // ---------------------------------------------------------------------------------------------

    /**
     * Searchable item.
     */
    public interface SearchableItem{
        boolean matchSearch(String term);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Data provider
     * @param <T>
     */
    public interface DataProvider<T>{

        /**
         * Data loader. For the search term it loads the corresponding data
         * @param term
         */
        void load(final String term) ;

        /**
         * Called when load ends
         */
        void onLoadEnds();

        /**
         * Triggered when the search with term is done
         * @param term
         * @param data
         */
        void onSearchComplete(String term, List<T> data);
    }

}
