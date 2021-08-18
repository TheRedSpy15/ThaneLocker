package com.theredspy15.thanelocker;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedList;

public class SavedDataManager {

    public static LinkedList<Board> savedBoards = new LinkedList<>();
    public static LinkedList<Session> savedSessions = new LinkedList<>();

    public static void loadSavedData() {
        Gson gson = new Gson();
        String json = MainActivity.preferences.getString("savedBoards", null);
        if (json != null) savedBoards = gson.fromJson(json, new TypeToken<LinkedList<Board>>() {}.getType());
        else savedBoards = new LinkedList<>();

        json = MainActivity.preferences.getString("savedSessions", null);
        if (json != null) savedSessions = gson.fromJson(json, new TypeToken<LinkedList<Session>>() {}.getType());
        else savedSessions = new LinkedList<>();
    }

    public static void saveData() {
        SharedPreferences.Editor prefsEditor = MainActivity.preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(savedBoards);
        prefsEditor.putString("savedBoards", json);
        json = gson.toJson(savedSessions);
        prefsEditor.putString("savedSessions", json);
        prefsEditor.apply();
    }

}
