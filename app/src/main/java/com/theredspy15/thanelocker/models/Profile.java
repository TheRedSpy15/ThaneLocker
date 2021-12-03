package com.theredspy15.thanelocker.models;

import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;
import com.theredspy15.thanelocker.utils.App;

import java.io.Serializable;
import java.util.ArrayList;

public class Profile implements Serializable {
    private static final long serialVersionUID = 1234570L;
    public static Profile localProfile;
    @SerializedName("id") private int id = 0;
    @SerializedName("level") private int level = 1;
    @SerializedName("xp") private int level_xp = 0; // 0-100 for all levels
    @SerializedName("name") @Nullable private String name;
    @SerializedName("age") private int age = 0;
    @SerializedName("description") @Nullable private String description;
    @SerializedName("state") @Nullable private String state;
    @SerializedName("country") @Nullable private String country;
    @SerializedName("friends") private ArrayList<Integer> friend_ids = new ArrayList<>();
    // @SerializedName("achievements") private ArrayList<Achievement> achievements = new ArrayList<>(); TODO should load separately
    @SerializedName("image") @Nullable private byte[] image;
    @SerializedName("password") private String passwordHash;
    @SerializedName("email") private String email;

    public Profile() {
        //int randId = ThreadLocalRandom.current().nextInt();
        //setId(randId);


        //if (!savedSessionIds.contains(randId)) setId(randId); TODO: check with database when thats created
        //else while (savedSessionIds.contains(randId)) randId = ThreadLocalRandom.current().nextInt();
    }

    public static void load() {
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.log("Loading profile");
        Gson gson = new Gson();

        String json = MainActivity.preferences.getString("localProfile", null);
        if (json != null) {
            crashlytics.setCustomKey("profile_json", json); // TODO ignore image
            localProfile = gson.fromJson(json, new TypeToken<Profile>() {}.getType());
        } else localProfile = new Profile();

        // extra crashlytics keys
        if (localProfile.name != null) {
            crashlytics.setCustomKey("user_name:", localProfile.name);
        }
        crashlytics.setCustomKey("premium_user", MainActivity.preferences.getBoolean("subscribe",false));
        crashlytics.setCustomKey("user_id", localProfile.getId());
        crashlytics.setCustomKey("user_level",localProfile.level);
        crashlytics.setCustomKey("user_xp", localProfile.getLevel_xp());
    }

    public static void save() {
        SharedPreferences.Editor prefsEditor = MainActivity.preferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(localProfile);
        prefsEditor.putString("localProfile", json);

        prefsEditor.apply();
    }

    public static ArrayList<Session> sessionsWithLocalProfile() { // TODO: overload later on to loading from database when thats created
        ArrayList<Session> sessionsWithLocalProfile = new ArrayList<>();

        for (int session_id : Session.savedSessionIds) {
            Session session = Session.savedSessions.get(session_id);

            if (session != null && session.getUser_id() == localProfile.getId())
                sessionsWithLocalProfile.add(session);
        }

        return sessionsWithLocalProfile;
    }

    public static ArrayList<Board> boardsWithLocalProfile() { // TODO: overload later on to loading from database when thats created
        ArrayList<Board> boardsWithLocalProfile = new ArrayList<>();

        for (int board_id : Board.savedBoardIds) {
            Board board = Board.savedBoards.get(board_id);

            if (board != null && board.getUser_id() == localProfile.getId())
                boardsWithLocalProfile.add(board);
        }

        return boardsWithLocalProfile;
    }

    public static Board favoriteBoard() {
        ArrayList<Integer> boardsFromSessions = new ArrayList<>();
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

    @Nullable
    public String getName() {
        if (TextUtils.isEmpty(name)) name = null;
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(@Nullable byte[] image) {
        this.image = image;
    }

    @Nullable
    public String getDescription() {
        if (TextUtils.isEmpty(description)) description = null;
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nullable
    public String getState() {
        if (TextUtils.isEmpty(state)) state = null;
        return state;
    }

    public void setState(@Nullable String state) {
        this.state = state;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Nullable
    public String getCountry() {
        if (TextUtils.isEmpty(country)) country = null;
        return country;
    }

    public void setCountry(@Nullable String country) {
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

    public void addXp(int level_xp) {
        this.level_xp += level_xp;
        if (getLevel_xp() >= 100) {
            this.level_xp = 1;
            setLevel(getLevel()+1);
        }
        if (getLevel_xp() < 0) this.level_xp = 1;
    }

    public ArrayList<Integer> getFriend_ids() {
        return friend_ids;
    }

    public void setFriend_ids(ArrayList<Integer> friend_ids) {
        this.friend_ids = friend_ids;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
