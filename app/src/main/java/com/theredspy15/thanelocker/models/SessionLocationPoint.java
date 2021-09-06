package com.theredspy15.thanelocker.models;

import java.io.Serializable;

public class SessionLocationPoint implements Serializable { // TODO: parcelable in the future
    private static final long serialVersionUID = 1234569L;
    private float speed;
    private double latitude;
    private double longitude;
    private double altitude;

    public float getSpeed() {
        return (float) (speed * 2.2369362920544);
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
}
