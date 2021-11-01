package com.theredspy15.thanelocker.models;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class HillRoute {
    private ArrayList<GeoPoint> points = new ArrayList<>();

    public ArrayList<GeoPoint> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<GeoPoint> points) {
        this.points = points;
    }
}
