package com.timappweb.timapp.utils;

import java.util.HashMap;
import java.util.List;

/**
 * Created by stephane on 12/9/2015.
 */
public class SearchHistory<T>{
    private int minimumSearchLength;
    private HashMap<String, Item<T>> data;
    private int maximumResults;
    private String lastSearch = ""; // Last search to update view accordingly...

    public SearchHistory(int minimumSearchLength, int maximumResults) {
        this.data = new HashMap<>();
        this.minimumSearchLength = minimumSearchLength;
        this.maximumResults = maximumResults;
    }
    public SearchHistory() {
        this(2, 20);
    }

    public void set(String term, List<T> items) {
        assert (term.length() >= this.minimumSearchLength);
        Item<T> node = this.data.get(term);
        if (node == null) return;
        node.setPending(false);
        node.setData(items);
        node.setComplete(this.maximumResults > 0 && items.size() > this.maximumResults);
    }

    public boolean hasTerm(String term){
        return this.data.containsKey(term);
    }

    public boolean hasTermOrSubTerm(String term){
        return this.get(term) != null;
    }

    public Item<T> get(String term){
        if (!this.data.containsKey(term)){
            if (term.length() > this.minimumSearchLength){
                return this.get(term.substring(0, term.length() - 2));
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
        this.data.put(term, new Item<T>(null, false));
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


    };
}
