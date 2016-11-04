package com.timappweb.timapp.data.models.dummy;

import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SpotCategory;

import java.util.UUID;

/**
 * Created by stephane on 5/25/2016.
 */
public class DummySpotFactory {

    public static Spot create(){
        Spot spot = new Spot();
        spot.id = 1L;
        spot.name = "Le phenomen";
        spot.description = "Very bad bar, the owner is not very sympathic... He does not beleive in our app!!!";
        spot.setCategory(new SpotCategory("bar"));
        return spot;
    }

    public static String uniqName(){
        return "Spot " + UUID.randomUUID();
    }
}
