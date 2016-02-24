package com.timappweb.timapp.entities;

import java.io.Serializable;

public class Category implements Serializable{

    public Integer id;
    public String name;
    public int resourceTransparent;
    public int resourceHighlight;

    public Category(int id, String name, int resourceTransparent, int resourceHighlight) {
        this.id = id;
        this.name = name;
        this.resourceTransparent = resourceTransparent;
        this.resourceHighlight= resourceHighlight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        return name.equals(category.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }


}
