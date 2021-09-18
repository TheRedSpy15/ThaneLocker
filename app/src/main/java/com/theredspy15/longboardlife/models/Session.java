package com.theredspy15.longboardlife.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theredspy15.longboardlife.ui.activitycontrollers.MainActivity;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class Session implements Serializable { // TODO: parcelable in the future
    private static final long serialVersionUID = 1234568L;
    public static final double xpValue = 1.1;

    private ArrayList<Integer> board_ids = new ArrayList<>();
    private int id = 0;
    private int user_id = 0;
    private String notes = "was very lame hill and too hot..."; // TODO: implement this!
    private String time_start = "";
    private String time_end = "";
    private long start_millis = 0;
    private long end_millis = 0;
    private String date = "";
    private String name = ""; // location + date
    private final ArrayList<SessionLocationPoint> locations = new ArrayList<>();
    private ArrayList<String> tags = new ArrayList<>();

    public static HashMap<Integer,Session> savedSessions = new HashMap<>();
    public static ArrayList<Integer> savedSessionIds = new ArrayList<>();

    public Session() {
        int randId = ThreadLocalRandom.current().nextInt(); // TODO: change to result of hash after finished being created
        if (!savedSessionIds.contains(randId)) setId(randId);
        else while (savedSessionIds.contains(randId)) randId = ThreadLocalRandom.current().nextInt();

        setUser_id(Profile.localProfile.getId());
    }

    public static void load() {
        Gson gson = new Gson();

        String json = MainActivity.preferences.getString("savedSessions", null);
        if (json != null) savedSessions = gson.fromJson(json, new TypeToken<HashMap<Integer,Session>>() {}.getType());
        else savedSessions = new HashMap<>();

        json = MainActivity.preferences.getString("savedSessionIds", null);
        if (json != null) savedSessionIds = gson.fromJson(json, new TypeToken<ArrayList<Integer>>() {}.getType());
        else savedSessionIds = new ArrayList<>();
    }

    public static void save() {
        SharedPreferences.Editor prefsEditor = MainActivity.preferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(savedSessions);
        prefsEditor.putString("savedSessions", json);

        json = gson.toJson(savedSessionIds);
        prefsEditor.putString("savedSessionIds", json);

        prefsEditor.apply();
    }

    public static ArrayList<Session> sessionsWithBoard(int board_id) {
        ArrayList<Session> sessionsWithBoard = new ArrayList<>();

        for (int session_id : Session.savedSessionIds) {
            Session session = Session.savedSessions.get(session_id);

            if (session != null && session.getBoard_ids().contains(board_id))
                sessionsWithBoard.add(session);
        }

        return sessionsWithBoard;
    }

    public String getTotalDistance() { // returns string because I'm using it in only one spot
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

        String totalString = String.format("%.1f", total*0.000621371192); // converts to miles
        if (total*0.000621371192 < 10) return "0"+totalString;
        return totalString;
    }

    public String getTopSpeed() { // returns string because I'm using it in only one spot
        double top = 0.0;

        for (SessionLocationPoint point : getLocations())
            if (point.getSpeed() > top)
                top = point.getSpeed();

        String topString = String.format("%.1f", top);
        if (top < 10) return "0"+topString; // keep formatting
        return topString;
    }

    public String getAvgSpeed() { // returns string because I'm using it in only one spot
        double avg = 0.0;

        for (SessionLocationPoint point : getLocations())
            if (point.getSpeed() > 5) avg += point.getSpeed();
        avg = avg / getLocations().size();

        String avgString = String.format("%.1f", avg); // keep formatting
        if (avg < 10) return "0"+avgString;
        return avgString;
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

    public long getDuration() {
        long duration = getEnd_millis() - getStart_millis();
        return duration / (60 * 1000); //returns minutes
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
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

    public ArrayList<SessionLocationPoint> getLocations() {
        return locations;
    }

    public String getCityName(Context context) throws IOException {
        String cityName;
        SessionLocationPoint point = getLocations().get(0);

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
        cityName = addresses.get(0).getLocality();

        return cityName.trim();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getStart_millis() {
        return start_millis;
    }

    public void setStart_millis(long start_millis) {
        this.start_millis = start_millis;
    }

    public long getEnd_millis() {
        return end_millis;
    }

    public void setEnd_millis(long end_millis) {
        this.end_millis = end_millis;
    }

    public ArrayList<Integer> getBoard_ids() {
        return board_ids;
    }

    public void setBoard_ids(ArrayList<Integer> board_ids) {
        this.board_ids = board_ids;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}