package com.theredspy15.longboardlife.models;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Meetup {
    private int meet_id = 0;
    private String title;
    private String description;
    private List<Integer> attending_users = new ArrayList<>(); // people attending
    private double latitude;
    private double longitude;
    private Date date;

    public static ArrayList<Meetup> meetups = new ArrayList<>();

    public synchronized static void loadFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("meetups")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            meetups.add(document.toObject(Meetup.class));
                            System.out.println(meetups.size());
                        }
                    } else {
                        // failed
                    }
                });
    }

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

    public void setAttending_users(List<Integer> attending_users) {
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
