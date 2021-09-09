package com.theredspy15.thanelocker.models;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;
import com.theredspy15.thanelocker.utils.App;

import java.util.HashMap;
import java.util.LinkedList;

public class Profile {
    public static Profile localProfile;
    private int id = 0;
    private int level = 1;
    private int level_xp = 0; // 0-100 for all levels
    private String name = "John Doe";
    private byte age = 18;
    private String description = "just another awesome skater";
    private String state = "California";
    private String country = "United States";
    private byte[] image;

    public Profile() {
        //int randId = ThreadLocalRandom.current().nextInt();
        //setId(randId);


        //if (!savedSessionIds.contains(randId)) setId(randId); TODO: check with database when thats created
        //else while (savedSessionIds.contains(randId)) randId = ThreadLocalRandom.current().nextInt();
    }

    public static void load() {
        Gson gson = new Gson();

        String json = MainActivity.preferences.getString("localProfile", null);
        if (json != null) localProfile = gson.fromJson(json, new TypeToken<HashMap<Integer,Board>>() {}.getType());
        else localProfile = new Profile();
    }

    public static void save() {
        SharedPreferences.Editor prefsEditor = MainActivity.preferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(localProfile);
        prefsEditor.putString("localProfile", json);

        prefsEditor.apply();
    }

    public static LinkedList<Session> sessionsWithProfile() { // TODO: overload later on to loading from database when thats created
        LinkedList<Session> sessionsWithBoard = new LinkedList<>();

        for (int session_id : Session.savedSessionIds) {
            Session session = Session.savedSessions.get(session_id);

            if (session != null && session.getUser_id() == localProfile.getId())
                sessionsWithBoard.add(session);
        }

        return sessionsWithBoard;
    }

    public static LinkedList<Board> boardsWithProfile() { // TODO: overload later on to loading from database when thats created
        LinkedList<Board> sessionsWithBoard = new LinkedList<>();

        for (int board_id : Board.savedBoardIds) {
            Board board = Board.savedBoards.get(board_id);

            if (board != null && board.getUser_id() == localProfile.getId())
                sessionsWithBoard.add(board);
        }

        return sessionsWithBoard;
    }

    public static Board favoriteBoard() {
        LinkedList<Integer> boardsFromSessions = new LinkedList<>();
        for (Session session : sessionsWithProfile())
            for (int board_id : session.getBoard_ids()) boardsFromSessions.add(board_id);

        return Board.savedBoards.get(App.mostCommon(boardsFromSessions));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getAge() {
        return age;
    }

    public void setAge(byte age) {
        this.age = age;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel_xp() {
        return level_xp;
    }

    public void setLevel_xp(int level_xp) {
        this.level_xp = level_xp;
    }
}
