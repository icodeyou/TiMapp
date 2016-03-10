package com.timappweb.timapp.entities;


public class Friend extends User{
    public boolean isSelected;

    public Friend() {

    }

    public Friend(String name, int age, int photoId, boolean selected) {
        super.username = name;
        super.age = age;
        super.photoId = photoId;
        this.isSelected = selected;
    }

    public void setSelected(boolean bool) {
        this.isSelected = bool;
    }
}
