package com.theredspy15.thanelocker.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Session implements Serializable { // TODO: parcelable in the future
    private static final long serialVersionUID = 1234568L;
    private LinkedList<Short> board_ids = new LinkedList<>();
    private short id = 0;
    String notes = "was very lame hill and too hot..."; // TODO: implement this!
    private String time_start = "";
    private String time_end = "";
    private long start_millis = 0;
    private long end_millis = 0;
    private String date = "";
    private String name = ""; // location + date
    private LinkedList<SessionLocationPoint> locations = new LinkedList<>();
    private LinkedList<String> tags = new LinkedList<>();

    public static HashMap<Short,Session> savedSessions = new HashMap<>();
    public static LinkedList<Short> savedSessionIds = new LinkedList<>();

    public Session() {
        Random random = new Random();
        setId((short) random.nextInt(Short.MAX_VALUE + 1));
    }

    public static void load() {
        Gson gson = new Gson();

        String json = MainActivity.preferences.getString("savedSessions", null);
        if (json != null) savedSessions = gson.fromJson(json, new TypeToken<HashMap<Short,Session>>() {}.getType());
        else savedSessions = new HashMap<>();

        json = MainActivity.preferences.getString("savedSessionIds", null);
        if (json != null) savedSessionIds = gson.fromJson(json, new TypeToken<LinkedList<Short>>() {}.getType());
        else savedSessionIds = new LinkedList<>();
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

        String totalString = String.format("%.1f", total*0.000621371192);
        if (total*0.000621371192 < 10) return "0"+totalString;
        return totalString;
    }

    public String getTopSpeed() { // returns string because I'm using it in only one spot
        double top = 0.0;

        for (SessionLocationPoint point : getLocations()) {
            if (point.getSpeed() > top) top = point.getSpeed();
        }

        String topString = String.format("%.1f", top);
        if (top < 10) return "0"+topString;
        return topString;
    }

    public String getAvgSpeed() { // returns string because I'm using it in only one spot
        double avg = 0.0;

        for (SessionLocationPoint point : getLocations()) {
            avg += point.getSpeed();
        }
        avg = avg / getLocations().size();

        String avgString = String.format("%.1f", avg);
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

    public String getCityName(Context context) throws IOException {
        String cityName;
        SessionLocationPoint point = getLocations().get(0);

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
        cityName = addresses.get(0).getLocality();

        return cityName.trim();
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
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

    public LinkedList<Short> getBoard_ids() {
        return board_ids;
    }

    public void setBoard_ids(LinkedList<Short> board_ids) {
        this.board_ids = board_ids;
    }
}
