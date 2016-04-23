package com.timappweb.timapp.data.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.utils.Util;

/**
 * Created by stephane on 4/5/2016.
 */
@Table(name = "SpotCategory")
public class SpotCategory extends BaseModel {

    @Column(name = "Created")
    public int created;

    @Column(name = "Name")
    public String name;

    @Column(name = "Position")
    public int position;

    public SpotCategory() {
        super();
    }

}