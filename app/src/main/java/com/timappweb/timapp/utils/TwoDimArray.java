package com.timappweb.timapp.utils;

import android.util.Log;

import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 5/9/2016.
 */
public class TwoDimArray<SectionType extends TwoDimArray.SectionItem> {

    private static final String TAG = "TwoDimArray";

    private List<SectionType> sections;

    public TwoDimArray() {
        this.sections = new LinkedList<>();
    }

    public void clear(){
        for (SectionItem section: this.sections){
            section.clear();
        }
    }

    public void create(SectionType item){
        this.sections.add(item);
    }

    public <T extends SectionType> void add(Object key, List list){
        if (list.size() > 0){
            Log.v(TAG, "Creating new section " + key + " with " + list.size() + " item(s)");
            SectionItem item = getSection(key);
            item.addAll(list);
        }
    }

    public <T> void addOne(Object key, T data){
        SectionItem item = getSection(key);
        if (item != null) item.add(data);
    }


    public <T extends SectionType> SectionItem<T> getSection(Object id){
        for (SectionItem section: this.sections){
            if (section.is(id)){
                return section;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    public int size() {
        int count = 0;
        for (SectionItem section: this.sections){
            count += section.size();
        }
        return count;
    }

    public <T> T get(int position){
        SectionItem section = this.sections.get(0);
        for (int i = 1; i < this.sections.size(); i++){
            if (section.size() > position){
                return (T) section.getItem(position);
            }
            position -= section.size();
            section = sections.get(i);
        }
        return (T) section.getItem(position);
    }

    public SectionItem getSectionFromPosition(int position) {
        for (int i = 0; i < this.sections.size(); i++){
            SectionItem section = this.sections.get(i);
            if (section.size() > position){
                return section;
            }
            position -= section.size();
        }
        throw new IndexOutOfBoundsException();
    }

    public void clear(Object id) {
        this.getSection(id).clear();
    }

    public boolean hasSection() {
        return sections != null && sections.size() != 0;
    }

    public interface SectionItem<T>{
        T getItem(int position);
        int size();
        void addAll(List<T> item);
        void add(T item);
        void clear();
        boolean is(Object id);
    }
}
