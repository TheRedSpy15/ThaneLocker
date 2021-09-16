package com.theredspy15.thanelocker.models;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;
import com.theredspy15.thanelocker.utils.App;

import java.io.Serializable;
import java.util.LinkedList;

public class Profile implements Serializable {
    public static Profile localProfile;
    private int id = 0;
    private int level = 1;
    private double level_xp = 0; // 0-100 for all levels
    private String name = "John Doe";
    private int age = 18;
    private String description = "just another awesome skater";
    private String state = "California";
    private String country = "United States";
    private LinkedList<Integer> friend_ids = new LinkedList<>();
    private LinkedList<Achievement> achievements = new LinkedList<>();
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
        if (json != null) localProfile = gson.fromJson(json, new TypeToken<Profile>() {}.getType());
        else localProfile = new Profile();
    }

    public static void save() {
        SharedPreferences.Editor prefsEditor = MainActivity.preferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(localProfile);
        prefsEditor.putString("localProfile", json);

        prefsEditor.apply();
    }

    public static LinkedList<Session> sessionsWithLocalProfile() { // TODO: overload later on to loading from database when thats created
        LinkedList<Session> sessionsWithLocalProfile = new LinkedList<>();

        for (int session_id : Session.savedSessionIds) {
            Session session = Session.savedSessions.get(session_id);

            if (session != null && session.getUser_id() == localProfile.getId())
                sessionsWithLocalProfile.add(session);
        }

        return sessionsWithLocalProfile;
    }

    public static LinkedList<Board> boardsWithLocalProfile() { // TODO: overload later on to loading from database when thats created
        LinkedList<Board> boardsWithLocalProfile = new LinkedList<>();

        for (int board_id : Board.savedBoardIds) {
            Board board = Board.savedBoards.get(board_id);

            if (board != null && board.getUser_id() == localProfile.getId())
                boardsWithLocalProfile.add(board);
        }

        return boardsWithLocalProfile;
    }

    public static Board favoriteBoard() {
        LinkedList<Integer> boardsFromSessions = new LinkedList<>();
        for (Session session : sessionsWithLocalProfile())
            boardsFromSessions.addAll(session.getBoard_ids());

        if (!boardsFromSessions.isEmpty())
            return Board.savedBoards.get(App.mostCommon(boardsFromSessions));
        return null;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
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

    public double getLevel_xp() {
        return level_xp;
    }

    public void addXp(double level_xp) {
        this.level_xp += level_xp;
        if (getLevel_xp() >= 100) {
            this.level_xp = 1;
            setLevel(getLevel()+1);
        }
        if (getLevel_xp() < 0) this.level_xp = 1;
    }

    public LinkedList<Integer> getFriend_ids() {
        return friend_ids;
    }

    public void setFriend_ids(LinkedList<Integer> friend_ids) {
        this.friend_ids = friend_ids;
    }
}
