package com.theredspy15.longboardlife.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.longboardlife.R;
import com.theredspy15.longboardlife.models.Board;
import com.theredspy15.longboardlife.models.Session;
import com.theredspy15.longboardlife.ui.activitycontrollers.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext; // TODO: find an alternative to this. doesn't seem to cause leak using profiler however

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context mContext) {
        App.mContext = mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setContext(this);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static <T> T mostCommon(List<T> list) {
        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Map.Entry<T, Integer> max = null;

        for (Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return max.getKey();
    }

    public static void cleanBoards() { // TODO: clean hashmap, duplicates
        ArrayList<Integer> list = new ArrayList<>(); // ids to null objects
        for (int id : Board.savedBoardIds) {
            if (Board.savedBoards.get(id) == null) {
                list.add(id);
            }
        }
        for (int id : list) Board.savedBoardIds.remove((Integer) id);
    }

    public static void cleanSessions() { // TODO: clean hashmap, duplicates
        ArrayList<Integer> list = new ArrayList<>(); // ids to null objects
        for (int id : Session.savedSessionIds) {
            if (Session.savedSessions.get(id) == null) {
                list.add(id);
            }
        }
        for (int id : list) Session.savedSessionIds.remove((Integer) id);
    }

    public static void updateTheme() {
        String selectedTheme = MainActivity.preferences.getString("theme","Auto");
        switch (selectedTheme) {
            case "Dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "Light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }

    }

    /**
     * returns a speed as a string for display in activity with correct unit of measurement
     * @param speed should already be converted from m/s to mph
     * @return
     */
    public static String getSpeedFormatted(float speed, Resources resources) {
        String speedString = "";

        if (MainActivity.preferences.getBoolean("kilometers",false)) {
            speed*=1.609344;
            speedString += speed;
            speedString += " " + resources.getString(R.string.kph);
        } else {
            speedString += speed;
            speedString += " " + resources.getString(R.string.mph);
        }

        return speedString;
    }

    /**
     * returns a distance as a string for display in activity with correct unit of measurement
     * @param distance should already be in miles
     * @return
     */
    public static String getDistanceFormatted(float distance, Resources resources) {
        String distanceString = "";

        if (MainActivity.preferences.getBoolean("kilometers",false)) {
            distance*=1.609344;
            distanceString += distance;
            distanceString += " " + resources.getString(R.string.kilometer_short);
        } else {
            distanceString += distance;
            distanceString += " " + resources.getString(R.string.miles);
        }

        return distanceString;
    }

    // for charts
    public static int getThemeTextColor(Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.colorOnSecondary, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }

}