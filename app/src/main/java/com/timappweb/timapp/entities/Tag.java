package com.timappweb.timapp.entities;

import java.io.Serializable;
import java.util.ArrayList;

public class Tag implements Serializable{

    public Integer id;
    public String name;
    public int count_ref;
    public ArrayList<Tag> tags = null;

    public Tag(String name, int count_ref) {
        this.count_ref = count_ref;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount_ref() {
        return count_ref;
    }

    public void setCount_ref(int count_ref) {
        this.count_ref = count_ref;
    }

}
