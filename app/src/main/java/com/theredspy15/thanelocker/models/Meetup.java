package com.theredspy15.thanelocker.models;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class Meetup {
  private int meet_id = 0;
  private String title;
  private String description;
  private List<String> attending_users = new ArrayList<>(); // people attending
  private double latitude;
  private double longitude;
  private String date;

  public static ArrayList<Meetup> meetups = new ArrayList<>();

  public synchronized static void loadFromFirebase() {
    meetups.clear();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.collection("meetups").get().addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        for (QueryDocumentSnapshot document : task.getResult()) {
          meetups.add(document.toObject(Meetup.class));
        }
      } else {
        // failed
      }
    });
  }

  public static void uploadMeetup(Meetup meetup) {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    db.collection("meetups")
        .document(String.valueOf(meetup.getMeet_id()))
        .set(meetup)
        .addOnSuccessListener(aVoid -> {})
        .addOnFailureListener(e -> {});
  }

  public String getTitle() { return title; }

  public void setTitle(String title) { this.title = title; }

  public String getDescription() { return description; }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getAttending_users() { return attending_users; }

  public void setAttending_users(List<String> attending_users) {
    this.attending_users = attending_users;
  }

  public double getLatitude() { return latitude; }

  public void setLatitude(double latitude) { this.latitude = latitude; }

  public double getLongitude() { return longitude; }

  public void setLongitude(double longitude) { this.longitude = longitude; }

  public String getDate() { return date; }

  public void setDate(String date) { this.date = date; }

  public int getMeet_id() { return meet_id; }

  public void setMeet_id(int meet_id) { this.meet_id = meet_id; }
}
