package com.timappweb.timapp.data.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

/**
 * Created by stephane on 5/8/2016.
 */
@Table(name = "PlaceTag")
public class PlaceTag extends MyModel {


    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "Place", uniqueGroups = "unique_tag",
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    public Place place;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "Tag", uniqueGroups = "unique_tag",
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    public Tag tag;

    @Column(name = "CountRef")
    public int count_ref;

    @Override
    public String toString() {
        return "PlaceTag{" +
                "place=" + place +
                ", tag=" + tag +
                '}';
    }

    public PlaceTag(Place place, Tag tag) {
        this.place = place;
        this.tag = tag;
    }
}
