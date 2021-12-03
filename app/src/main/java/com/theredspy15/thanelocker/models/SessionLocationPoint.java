package com.theredspy15.thanelocker.models;

import java.io.Serializable;

public class SessionLocationPoint implements Serializable {
    private static final long serialVersionUID = 1234569L;
    private long timeStamp = 0;
    private float speed = 0;
    private double latitude = 0.0;
    private double longitude = 0.0;

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = (float) (speed * 2.2369362920544); // mph
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

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
