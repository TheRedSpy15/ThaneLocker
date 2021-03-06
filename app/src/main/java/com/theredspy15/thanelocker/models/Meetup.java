package com.theredspy15.thanelocker.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Meetup {
    private int meet_id = 0;
    private String title;
    private String description;
    private List<Integer> attending_users = new ArrayList<>(); // owner is always the first user, index 0
    private double latitude;
    private double longitude;
    private Date date;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Integer> getAttending_users() {
        return attending_users;
    }

    public void getAttending_users(List<Integer> attending_users) {
        this.attending_users = attending_users;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getMeet_id() {
        return meet_id;
    }

    public void setMeet_id(int meet_id) {
        this.meet_id = meet_id;
    }
}
