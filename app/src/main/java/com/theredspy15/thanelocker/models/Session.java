package com.theredspy15.thanelocker.models;

import android.location.Location;

import java.io.Serializable;
import java.util.LinkedList;

public class Session implements Serializable {
    private static final long serialVersionUID = 1234568L;
    private short board_id;
    private String description = "";
    private String time_start = "";
    private String time_end = "";
    private String duration = "";
    private String date = "";
    private String name; // location + date
    private String cityName = "";
    private LinkedList<Location> locations = new LinkedList<>();
    private LinkedList<String> tags = new LinkedList<>();

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

    public LinkedList<Location> getLocations() {
        return locations;
    }

    public void setLocations(LinkedList<Location> locations) {
        this.locations = locations;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
