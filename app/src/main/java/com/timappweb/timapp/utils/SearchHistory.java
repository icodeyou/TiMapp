package com.timappweb.timapp.utils;

import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 12/9/2015.
 */
public class SearchHistory<T>{
    private static final String TAG = "SearchHistory";
    private int minimumSearchLength;
    private HashMap<String, Item> data;
    private int maximumResults;
    private String lastSearch = ""; // Last search to update view accordingly...

    private DataProvider provider;
    public void setDataProvider(DataProvider provider){
        this.provider = provider;
    }

    public SearchHistory(DataProvider provider, int minimumSearchLength, int maximumResults) {
        this.data = new HashMap<>();
        this.minimumSearchLength = minimumSearchLength;
        this.maximumResults = maximumResults;
        this.provider = provider;
    }
    public SearchHistory(DataProvider provider) {
        this(provider, 2, 20);
    }
    public SearchHistory(int minimumSearchLength, int maximumResults) {
        this(null, minimumSearchLength, maximumResults);
    }

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

    public void addInCache(String term, List<T> items) {
        Log.d(TAG, "Adding in cache " + items.size() + " element(s) for search " + term);
       // assert (term.length() >= this.minimumSearchLength);
        Item node = this.data.get(term);
        if (node == null) return;
        node.setPending(false);
        node.setData(items);
        //node.setComplete(this.maximumResults > 0 && items.size() >= this.maximumResults);
        node.setComplete(true);
        this.provider.onSearchComplete(term, items);
    }

    public boolean hasTerm(String term){
        return this.data.containsKey(term);
    }

    public boolean hasTermOrSubTerm(String term){
        return this.getTermOrSubTerm(term) != null;
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

    public void create(String term) {
        this.data.put(term, new Item(null, false));
    }

    public void setLastSearch(String lastSearch) {
        this.lastSearch = lastSearch;
    }

    public boolean isLastSearch(String search){
        return this.lastSearch == search;
    }

    public int getMinimumSearchLength() {
        return minimumSearchLength;
    }

    public int getMaximumResults() {
        return maximumResults;
    }

    public String getLastSearch() {
        return lastSearch;
    }

    public static <T> List<T> filterData(List<T> data, String term){
        List<T> results = new LinkedList<>();
        for (T d: data){
            if (((SearchableItem)d).matchSearch(term)){
                results.add(d);
            }
        }
        return results;
    }

    public class Item<T>{
        boolean complete = false;
        boolean pending = true;
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


    };

    public interface SearchableItem{
        boolean matchSearch(String term);
    }

    public interface DataProvider<T>{

        void load(final String term) ;

        void onSearchComplete(String term, List<T> data);
    }

}
