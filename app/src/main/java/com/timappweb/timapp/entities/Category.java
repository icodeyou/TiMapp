package com.timappweb.timapp.entities;

import android.media.Image;
import android.widget.ImageView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;

import java.io.Serializable;
import java.util.HashMap;

public class Category implements Serializable{

    public Integer id;
    public String name;

    public Category(String name) {
        this.name = name;
    }

    public int getIconId(){
        return MyApplication.mapNameToIcon.get(this.name);
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
