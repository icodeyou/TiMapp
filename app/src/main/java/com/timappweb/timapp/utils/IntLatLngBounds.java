package com.timappweb.timapp.utils;

import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by stephane on 9/17/2015.
 */
public class IntLatLngBounds {

    public IntLatLng northeast;
    public IntLatLng southwest;

    public IntLatLngBounds(LatLngBounds bounds) {
        this.northeast = new IntLatLng(bounds.northeast);
        this.southwest = new IntLatLng(bounds.southwest);
    }

    public int getWidth(){
        return Math.abs(this.northeast.longitude - this.southwest.longitude);
    }
    public int getHeight(){
        return Math.abs(this.northeast.latitude - this.southwest.latitude);
    }

    public double  getMeterHeight(){
        return ((double)this.getHeight()  * 111.0 / IntLatLng.precision);
    }

    public double  getMeterWidth(){
        return ((double)this.getHeight() * 100.0 / IntLatLng.precision);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntLatLngBounds that = (IntLatLngBounds) o;

        if (!northeast.equals(that.northeast)) return false;
        return southwest.equals(that.southwest);

    }

    @Override
    public String toString() {
        return "IntLatLngBounds{" +
                "northeast=" + northeast +
                ", southwest=" + southwest +
                '}';
    }

    public IntLatLngBounds(IntLatLng southwest, IntLatLng northeast) {
        this.northeast = northeast;
        this.southwest = southwest;
    }

    public LatLngBounds toDouble() {
        return new LatLngBounds(this.southwest.toDouble(), this.northeast.toDouble());
    }
}
