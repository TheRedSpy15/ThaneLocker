package com.theredspy15.thanelocker.models;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.example.longboardlife.R;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class Session implements Serializable { // TODO: parcelable in the future
    private static final long serialVersionUID = 1234568L;
    public static final double xpValue = 1.1;

    @SerializedName("boards") private ArrayList<Integer> board_ids = new ArrayList<>();
    @SerializedName("id") private int id = 0;
    @SerializedName("user") private int user_id = Profile.localProfile.getId();
    @SerializedName("notes") @Nullable private String notes = ""; // TODO: implement this!
    @SerializedName("start") private String time_start = "";
    @SerializedName("end") private String time_end = "";
    @SerializedName("startunix") private long start_millis = 0;
    @SerializedName("endunix") private long end_millis = 0;
    @SerializedName("date") private String date = "";
    @SerializedName("name") private String name = ""; // location + date
    @SerializedName("locations") private final ArrayList<SessionLocationPoint> locations = new ArrayList<>();
    @SerializedName("tags") private ArrayList<String> tags = new ArrayList<>();
    @SerializedName("elevations") private ArrayList<Float> elevationPoints = new ArrayList<>();
    @SerializedName("published") private boolean isPublished = false;

    public static HashMap<Integer,Session> savedSessions = new HashMap<>();
    public static ArrayList<Integer> savedSessionIds = new ArrayList<>();

    public Session() {
        int randId = ThreadLocalRandom.current().nextInt(); // TODO: change to result of hash after finished being created
        if (!savedSessionIds.contains(randId)) setId(randId);
        else while (savedSessionIds.contains(randId)) randId = ThreadLocalRandom.current().nextInt();

        setUser_id(Profile.localProfile.getId());
    }

    public static void load(Activity activity) {
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        Gson gson = new Gson();

        try {
            String json = MainActivity.preferences.getString("savedSessions", null);
            if (json != null) savedSessions = gson.fromJson(json, new TypeToken<HashMap<Integer,Session>>() {}.getType());
            else savedSessions = new HashMap<>();

            json = MainActivity.preferences.getString("savedSessionIds", null);
            if (json != null) savedSessionIds = gson.fromJson(json, new TypeToken<ArrayList<Integer>>() {}.getType());
            else savedSessionIds = new ArrayList<>();

            // extra crashlytics keys
            crashlytics.setCustomKey("session_count",savedSessionIds.size());
        } catch (JsonSyntaxException | IllegalStateException e) {
            MotionToast.Companion.createColorToast(
                    activity,
                    activity.getString(R.string.failed_to_load_sessions),
                    activity.getString(R.string.failed_load_sessions_sum),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(activity, R.font.roboto)
            );
            savedSessions = new HashMap<>();
            savedSessionIds = new ArrayList<>();
        }
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

    public static String convertTime(Session session, long millis) {
        long start = session.getLocations().get(0).getTimeStamp();
        long diff = millis - start;

        @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(diff) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff)),
                TimeUnit.MILLISECONDS.toSeconds(diff) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff)));
        return time;
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

        String totalString = String.format("%.1f", total*0.000621371192); // converts to miles (from METERS)
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

    public String getCityName(Context context) {
        String cityName;
        SessionLocationPoint point = getLocations().get(0);

        List<Address> addresses;
        try {
            Geocoder geocoder = new Geocoder(context, context.getResources().getConfiguration().getLocales().get(0));
            addresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
        } catch (Exception e) {
            e.printStackTrace();
            return context.getString(R.string.failed_city_name);
        }
        cityName = addresses.get(0).getLocality();

        if (cityName != null) return cityName.trim();
        else return context.getString(R.string.failed_city_name);
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

    @Nullable
    public String getNotes() {
        return notes;
    }

    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public ArrayList<Float> getElevationPoints() {
        return elevationPoints;
    }

    public void setElevationPoints(ArrayList<Float> elevationPoints) {
        this.elevationPoints = elevationPoints;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }
}
