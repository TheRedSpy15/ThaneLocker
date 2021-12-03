package com.theredspy15.thanelocker.models;

import android.content.SharedPreferences;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class HillRoute implements Serializable {
    private static final long serialVersionUID = 1234887L;
    public static HashMap<Integer,HillRoute> savedHills = new HashMap<>();
    public static ArrayList<Integer> savedHillIds = new ArrayList<>();

    private String name = "My route";
    private int id = 0;
    private ArrayList<GeoPoint> points = new ArrayList<>();

    public HillRoute() {
        int randId = ThreadLocalRandom.current().nextInt(); // TODO: change to result of hash after finished being created
        if (!savedHillIds.contains(randId)) setId(randId);
        else while (savedHillIds.contains(randId)) ThreadLocalRandom.current().nextInt();
    }

    public static void save() {
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.log("saving hill routes");

        SharedPreferences.Editor prefsEditor = MainActivity.preferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(savedHills);
        prefsEditor.putString("savedHills", json);

        json = gson.toJson(savedHillIds);
        prefsEditor.putString("savedHillIds", json);

        prefsEditor.apply();
    }

    public static void load() {
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.log("loading hill routes");
        Gson gson = new Gson();

        String json = MainActivity.preferences.getString("savedHills", null);
        if (json != null) savedHills = gson.fromJson(json, new TypeToken<HashMap<Integer,HillRoute>>() {}.getType());
        else savedHills = new HashMap<>();

        json = MainActivity.preferences.getString("savedHillIds", null);
        if (json != null) savedHillIds = gson.fromJson(json, new TypeToken<ArrayList<Integer>>() {}.getType());
        else savedHillIds = new ArrayList<>();
    }

    @Deprecated
    public static int RouteNameToId(String name) {
        ArrayList<HillRoute> routes = new ArrayList<>();
        HillRoute foundRoute = new HillRoute();
        for (int id : savedHillIds) {
            routes.add(savedHills.get(id));
        }
        for (HillRoute route : routes) {
            if (route.getName() != null && route.getName().equals(name)) {
                foundRoute = route;
                break;
            }
        }
        return foundRoute.getId();
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
