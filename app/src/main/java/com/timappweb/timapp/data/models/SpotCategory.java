package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

/**
 * Created by stephane on 4/5/2016.
 */
@Table(name = "SpotCategory")
public class SpotCategory extends SyncBaseModel implements Serializable {

    @Column(name = "SyncId")
    public long id;

    @Column(name = "Name")
    public String name;

    @Column(name = "Position")
    public int position;

    public SpotCategory() {
        super();
    }

    public SpotCategory(String name) {
        super();
        this.name = name;
    }

    public long getSyncKey(){
        return this.id;
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        if (!(model instanceof SpotCategory)) return false;
        SpotCategory that = (SpotCategory) model;

        if (position != that.position) return false;
        return name.equals(that.name);
    }

    public static SpotCategory createDummy() {
        return new SpotCategory("DummyCategory");
    }

    @Override
    public String toString() {
        return "SpotCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", position=" + position +
                '}';
    }
}
