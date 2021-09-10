package com.theredspy15.thanelocker.models;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class Board implements Serializable { // TODO: parcelable in the future
    private static final long serialVersionUID = 1234567L;
    private int id;
    private int user_id = 0;
    private String name;
    private byte[] image;
    private String description;
    private String trucks;
    private int truckWidth = 180; // TODO
    private String footStop = "None"; // i.e type of footstop if present
    private String deck="Zenit Mini Marble";
    private byte rearAngle=50;
    private byte frontAngle=50;
    private String rd_bushing="Stock Bushings"; // example format: Riptide 88a WPS Barrel
    private String bd_bushings="Stock Bushings";
    private String wheels;
    private String bearings="Standard ABEC Bearings";
    private String pivot="Stock Pivot Cup"; // Brand, duro (if applicable)
    private double riserHt=0.0;
    private LinkedList<String> tags = new LinkedList<>();
    private String gripTp="Standard Griptape";
    private LinkedList<Session> sessions;

    public static HashMap<Integer,Board> savedBoards = new HashMap<>();
    public static LinkedList<Integer> savedBoardIds = new LinkedList<>();

    public Board() {
        int randId = ThreadLocalRandom.current().nextInt(); // TODO: change to result of hash after finished being created
        if (!savedBoardIds.contains(randId)) setId(randId);
        else while (savedBoardIds.contains(randId)) ThreadLocalRandom.current().nextInt();

        setUser_id(Profile.localProfile.getId());
    }

    public static void load() {
        Gson gson = new Gson();

        String json = MainActivity.preferences.getString("savedBoards", null);
        if (json != null) savedBoards = gson.fromJson(json, new TypeToken<HashMap<Integer,Board>>() {}.getType());
        else savedBoards = new HashMap<>();

        json = MainActivity.preferences.getString("savedBoardIds", null);
        if (json != null) savedBoardIds = gson.fromJson(json, new TypeToken<LinkedList<Integer>>() {}.getType());
        else savedBoardIds = new LinkedList<>();
    }

    public static void save() {
        SharedPreferences.Editor prefsEditor = MainActivity.preferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(savedBoards);
        prefsEditor.putString("savedBoards", json);

        json = gson.toJson(savedBoardIds);
        prefsEditor.putString("savedBoardIds", json);

        prefsEditor.apply();
    }

    public static int BoardNameToId(String name) {
        LinkedList<Board> boards = new LinkedList<>();
        Board foundBoard = new Board();
        for (int id : savedBoardIds) {
            boards.add(savedBoards.get(id));
        }
        for (Board board : boards) {
            if (board.getName().equals(name)) {
                foundBoard = board;
                break;
            }
        }
        return foundBoard.getId();
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

    public String getTrucks() {
        return trucks;
    }

    public void setTrucks(String trucks) {
        this.trucks = trucks;
    }

    public byte getRearAngle() {
        return rearAngle;
    }

    public void setRearAngle(byte rearAngle) {
        this.rearAngle = rearAngle;
    }

    public byte getFrontAngle() {
        return frontAngle;
    }

    public void setFrontAngle(byte frontAngle) {
        this.frontAngle = frontAngle;
    }

    public String getRd_bushing() {
        return rd_bushing;
    }

    public void setRd_bushing(String rd_bushing) {
        this.rd_bushing = rd_bushing;
    }

    public String getBd_bushings() {
        return bd_bushings;
    }

    public void setBd_bushings(String bd_bushings) {
        this.bd_bushings = bd_bushings;
    }

    public String getWheels() {
        return wheels;
    }

    public void setWheels(String wheels) {
        this.wheels = wheels;
    }

    public String getBearings() {
        return bearings;
    }

    public void setBearings(String bearings) {
        this.bearings = bearings;
    }

    public double getRiserHt() {
        return riserHt;
    }

    public void setRiserHt(double riserHt) {
        this.riserHt = riserHt;
    }

    public String getGripTp() {
        return gripTp;
    }

    public void setGripTp(String gripTp) {
        this.gripTp = gripTp;
    }

    public LinkedList<Session> getSessions() {
        return sessions;
    }

    public void setSessions(LinkedList<Session> sessions) {
        this.sessions = sessions;
    }

    public String getPivot() {
        return pivot;
    }

    public void setPivot(String pivot) {
        this.pivot = pivot;
    }

    public LinkedList<String> getTags() {
        return tags;
    }

    public void setTags(LinkedList<String> tags) {
        this.tags = tags;
    }

    public String getDeck() {
        return deck;
    }

    public void setDeck(String deck) {
        this.deck = deck;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getTruckWidth() {
        return truckWidth;
    }

    public void setTruckWidth(int truckWidth) {
        this.truckWidth = truckWidth;
    }

    public String getFootStop() {
        return footStop;
    }

    public void setFootStop(String footStop) {
        this.footStop = footStop;
    }
}
