package com.timappweb.timapp.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 3/29/2016.
 */
public abstract class MultipleSectionAdapter<DataType, ViewHolderType extends RecyclerView.ViewHolder> extends  RecyclerView.Adapter<ViewHolderType> {

    private static final String TAG = "MultipleSectionAdapter";
    private List<SectionItem> sections;

    public MultipleSectionAdapter() {
        this.sections = new LinkedList<>();
    }

    public void clear(){
        for (SectionItem section: this.sections){
            section.clear();
        }
    }

    public void create(Object key, String title){
        this.sections.add(new SectionItem(key, title, new LinkedList<DataType>()));
    }

    public <T extends DataType> void add(Object key, List<T> list){
        if (list.size() > 0){
            Log.v(TAG, "Creating new section " + key + " with " + list.size() + " item(s)");
            SectionItem item = getSection(key);
            item.addAll(list);
        }
    }


    public <T extends DataType> SectionItem<T> getSection(Object id){
        for (SectionItem section: this.sections){
            if (section.is(id)){
                return section;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (SectionItem section: this.sections){
            count += section.size();
        }
        return count;
    }

    public SimpleSectionedRecyclerViewAdapter.Section[] buildSections(){
        List<SimpleSectionedRecyclerViewAdapter.Section> headers = new LinkedList<>();
        int position = 0;
        for (SectionItem section: this.sections){
            if (section.size() > 0){
                headers.add(new SimpleSectionedRecyclerViewAdapter.Section(position, section.getTitle()));
                position += section.size();
            }
        }
        return headers.toArray(new SimpleSectionedRecyclerViewAdapter.Section[headers.size()]);
    }

    public DataType get(int position){
        SectionItem section = this.sections.get(0);
        for (int i = 0; i < this.sections.size(); i++){
            if (section.size() > position){
                return (DataType) section.getItem(position);
            }
            position -= section.size();
            section = sections.get(i);
        }
        return (DataType) section.getItem(position);
    }


    private class SectionItem<T> {
        private List<T> data;
        private String title;
        private Object id;

        public SectionItem(Object id, String title, List<T> data) {
            this.id = id;
            this.data = data;
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public T getItem(int position) {
            return data.get(position);
        }

        public int size() {
            return data.size();
        }

        public void addAll(List<T> item) {
            this.data.addAll(item);
        }

        public boolean is(Object id) {
            return this.id.equals(id);
        }

        public void clear() {
            this.data.clear();
        }
    }
}