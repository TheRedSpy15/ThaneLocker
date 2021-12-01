package com.theredspy15.thanelocker.models;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Board implements Serializable {
    private static final long serialVersionUID = 1234567L;
    private int id = 0;
    private boolean advanceMode = false;
    private int user_id = Profile.localProfile.getId();
    @NonNull private String name = "board";
    @Nullable private byte[] image;
    @Nullable private String description = "No Description";
    private String trucks = "Generic Trucks";
    private int truckWidth = 180; // TODO
    private String footStop = "None"; // i.e type of footstop if present
    @Nullable private String deck="Generic Deck";
    private byte rearAngle=50;
    private byte frontAngle=50;
    private String rd_bushing="Stock Bushings"; // example format: Riptide 88a WPS Barrel
    private String bd_bushings="Stock Bushings";
    private String wheels = "Generic Wheels";
    private String bearings="Standard ABEC Bearings";
    private String pivot="Stock Pivot Cup"; // Brand, duro (if applicable)
    private String riserHt="1/16";
    private ArrayList<String> tags = new ArrayList<>();
    private String gripTp="Standard Griptape";
    private ArrayList<Session> sessions;
    private boolean forSale = false;
    private double cost; // always USD

    public static HashMap<Integer,Board> savedBoards = new HashMap<>();
    public static ArrayList<Integer> savedBoardIds = new ArrayList<>();

    public Board() {
        int randId = ThreadLocalRandom.current().nextInt(); // TODO: change to result of hash after finished being created
        if (!savedBoardIds.contains(randId)) setId(randId);
        else while (savedBoardIds.contains(randId)) ThreadLocalRandom.current().nextInt();

        setUser_id(Profile.localProfile.getId());
    }

    public static void load() {
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        Gson gson = new Gson();

        String json = MainActivity.preferences.getString("savedBoards", null);
        if (json != null) savedBoards = gson.fromJson(json, new TypeToken<HashMap<Integer,Board>>() {}.getType());
        else savedBoards = new HashMap<>();

        json = MainActivity.preferences.getString("savedBoardIds", null);
        if (json != null) savedBoardIds = gson.fromJson(json, new TypeToken<ArrayList<Integer>>() {}.getType());
        else savedBoardIds = new ArrayList<>();

        // extra crashlytics keys
        crashlytics.setCustomKey("board_count",savedBoardIds.size());
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

    public float fastestSpeed() {
        float fastest = 0;
        for (Session session : Session.sessionsWithBoard(getId()))
            if (Float.parseFloat(session.getTopSpeed()) > fastest)
                fastest = Float.parseFloat(session.getTopSpeed());
        return fastest;
    }

    public float avgSpeed() {
        float avg = 0;
        ArrayList<Session> sessions = Session.sessionsWithBoard(getId());
        for (Session session : sessions)
            avg += Float.parseFloat(session.getAvgSpeed());
        if (sessions.size() > 0) avg = avg / sessions.size();
        return avg;
    }

    public float furthestDistance() {
        float furthest = 0;
        for (Session session : Session.sessionsWithBoard(getId()))
            if (Float.parseFloat(session.getTotalDistance()) > furthest)
                furthest = Float.parseFloat(session.getTotalDistance());
        return furthest;
    }

    public float totalDistance() {
        float total = 0;
        for (Session session : Session.sessionsWithBoard(getId()))
            total += Float.parseFloat(session.getTotalDistance());
        return total;
    }

    public float avgDistance() {
        float avg = 0;
        for (Session session : Session.sessionsWithBoard(getId()))
            avg += Float.parseFloat(session.getTotalDistance());
        avg = avg / Session.sessionsWithBoard(getId()).size();
        return avg;
    }

    @Deprecated
    public static int BoardNameToId(String name) {
        ArrayList<Board> boards = new ArrayList<>();
        Board foundBoard = new Board();
        for (int id : savedBoardIds) {
            boards.add(savedBoards.get(id));
        }
        for (Board board : boards) {
            if (board.getName() != null && board.getName().equals(name)) {
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

    @Nullable
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

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
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

    public String getRiserHt() {
        return riserHt;
    }

    public void setRiserHt(String riserHt) {
        this.riserHt = riserHt;
    }

    public String getGripTp() {
        return gripTp;
    }

    public void setGripTp(String gripTp) {
        this.gripTp = gripTp;
    }

    public ArrayList<Session> getSessions() {
        return sessions;
    }

    public void setSessions(ArrayList<Session> sessions) {
        this.sessions = sessions;
    }

    public String getPivot() {
        return pivot;
    }

    public void setPivot(String pivot) {
        this.pivot = pivot;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
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

    public boolean isAdvanceMode() {
        return advanceMode;
    }

    public void setAdvanceMode(boolean proMode) {
        this.advanceMode = proMode;
    }

    public boolean isForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}
