package com.timappweb.timapp.entities;

import com.timappweb.timapp.utils.SearchHistory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Tag implements Serializable, SearchHistory.SearchableItem{

    public Integer id;
    public String name;
    public int count_ref;
    public static final int MINLENGTH = 2;
    public static final int MAXLENGTH = 30;

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
}
