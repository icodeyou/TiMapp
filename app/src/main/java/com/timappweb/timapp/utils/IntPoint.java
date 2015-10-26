package com.timappweb.timapp.utils;

/**
 * Created by stephane on 9/16/2015.
 */
public class IntPoint {
    public int x;
    public int y;

    public IntPoint() {
    }

    public int distance(IntPoint p){
        return Math.max(Math.abs(this.x - p.x), Math.abs(this.y - p.y));
    }

    public IntPoint(IntPoint p) {
        this.y = p.y;
        this.x = p.x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntPoint intPoint = (IntPoint) o;

        if (x != intPoint.x) return false;
        return y == intPoint.y;
    }

    @Override
    public int hashCode() {
        return (100000 * x) + y;
    }

    /**
     * As LatLng object take as a first argument a latitude, we tag y as a first parameter
     * @param y
     * @param x
     */
    public IntPoint(int y, int x) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "IntPoint{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
