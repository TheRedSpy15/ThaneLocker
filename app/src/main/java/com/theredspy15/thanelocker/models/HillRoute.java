package com.theredspy15.thanelocker.models;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;

public class HillRoute implements Serializable {
    private static final long serialVersionUID = 1234887L;
    public static ArrayList<HillRoute> savedHills = new ArrayList<>();

    private String name = "My route";
    private ArrayList<GeoPoint> points = new ArrayList<>();

    public static void save() {
        SharedPreferences.Editor prefsEditor = MainActivity.preferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(savedHills);
        prefsEditor.putString("savedHills", json);

        prefsEditor.apply();
    }

    public static void load() {
        Gson gson = new Gson();

        String json = MainActivity.preferences.getString("savedHills", null);
        if (json != null) savedHills = gson.fromJson(json, new TypeToken<ArrayList<HillRoute>>() {}.getType());
        else savedHills = new ArrayList<>();
    }

    public ArrayList<GeoPoint> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<GeoPoint> points) {
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
