package com.timappweb.timapp.data.entities;

import com.google.gson.annotations.Expose;
import com.timappweb.timapp.utils.SearchHistory;

import java.io.Serializable;
import java.util.List;

public class Tag implements Serializable, SearchHistory.SearchableItem{

    public static final int MINLENGTH = 2;
    public static final int MAXLENGTH = 30;

    // =============================================================================================

    @Expose
    public Integer id;

    @Expose
    public String name;

    @Expose(serialize = true, deserialize = false)
    public int count_ref;

    // =============================================================================================

    public Tag(String name, int count_ref)  {
        this.name = name;
        this.count_ref = count_ref;
    }

    public Tag(String name) {
        this.count_ref = 0;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCountRef() {
        return count_ref;
    }

    public void setCountRef(int count_ref) {
        this.count_ref = count_ref;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        return name.equals(tag.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static Tag createDummy() {
        return new Tag("DummyTag", 34);
    }

    public boolean isShortEnough() {
        boolean bool =  getName().length()>=MINLENGTH;
        return  bool;
    }

    public boolean isLongEnough() {
        boolean bool =  getName().length()<= MAXLENGTH;
        return  bool;
    }

    @Override
    public boolean matchSearch(String term) {
        return this.name.startsWith(term);
    }

    public static String tagsToString(List<Tag> tags){
        if (tags == null || tags.size() == 0){
            return "";
        }
        String res = tags.get(0).getName();

        for (int i = 1; i < tags.size(); i++){
            res += "," + tags.get(i).name;
        }
        return res;
    }
}