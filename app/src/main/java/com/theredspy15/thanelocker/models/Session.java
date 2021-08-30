package com.theredspy15.thanelocker.models;

import android.location.Location;
import android.location.LocationManager;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Random;

public class Session implements Serializable { // TODO: parcelable in the future
    private static final long serialVersionUID = 1234568L;
    private short board_id;
    private short id = 0;
    private String description = "";
    private String time_start = "";
    private String time_end = "";
    private String duration = "";
    private String date = "";
    private String name; // location + date
    private String cityName = "";
    private LinkedList<SessionLocationPoint> locations = new LinkedList<>(); // TODO: save lat/long/speed/etc in seperate lists
    private LinkedList<String> tags = new LinkedList<>();

    public Session() {
        Random random = new Random();
        setId((short) random.nextInt(Short.MAX_VALUE + 1));
    }

    public double getTotalDistance() {
        double total = 0.0;

        for (int i = 0; i<getLocations().size(); i++) {
            if (i != getLocations().size()-1) {
                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLongitude(getLocations().get(i).getLongitude());
                location.setLatitude(getLocations().get(i).getLatitude());

                Location location2 = new Location(LocationManager.GPS_PROVIDER);
                location2.setLongitude(getLocations().get(i+1).getLongitude());
                location2.setLatitude(getLocations().get(i+1).getLatitude());

                total += location.distanceTo(location2);
            }
        }

        return total*0.000621371192;
    }

    public double getTopSpeed() {
        double top = 0.0;

        for (SessionLocationPoint point : getLocations()) {
            if (point.getSpeed() > top) top = point.getSpeed();
        }

        return top;
    }

    public double getAvgSpeed() {
        double avg = 0.0;

        for (SessionLocationPoint point : getLocations()) {
            avg += point.getSpeed();
        }
        avg = avg / getLocations().size();

        return avg;
    }

    public short getBoard_id() {
        return board_id;
    }

    public void setBoard_id(short board_id) {
        this.board_id = board_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime_start() {
        return time_start;
    }

    public void setTime_start(String time_start) {
        this.time_start = time_start;
    }

    public String getTime_end() {
        return time_end;
    }

    public void setTime_end(String time_end) {
        this.time_end = time_end;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public LinkedList<String> getTags() {
        return tags;
    }

    public void setTags(LinkedList<String> tags) {
        this.tags = tags;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<SessionLocationPoint> getLocations() {
        return locations;
    }

    public void setLocations(LinkedList<SessionLocationPoint> locations) {
        this.locations = locations;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }
}
