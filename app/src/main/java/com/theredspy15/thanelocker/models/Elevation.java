package com.theredspy15.thanelocker.models;

import android.content.Context;
import android.net.ConnectivityManager;

import com.google.gson.Gson;

import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Elevation {

    public boolean isApiAvailable(final Context context) { // TODO: check specifically is api is available
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    /**
     * gets elevations for a session AFTER it has been created to load all the elevations at once.
     * The list of returned elevations should be saved inside the session, to prevent constantly
     * having to wait for server to respond
     * @param locations locations from sesssion
     * @return elevations for said locations
     */
    public static ArrayList<Float> getElevations(List<SessionLocationPoint> locations) throws IOException {
        // create requests from locations
        List<ElevationRequest> requests = new ArrayList<>();
        for (SessionLocationPoint point : locations) {
            requests.add(new ElevationRequest(point.getLatitude(), point.getLongitude()));
        }

        Gson gson = new Gson();
        String json = gson.toJson(requests);

        ArrayList<Float> list = new ArrayList<>();

        // creating connection
        URL url = new URL("https://api.open-elevation.com/api/v1/lookup");
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/json");

        // creating message to send
        String data = "{\"locations\":" + json + "}"; // TODO: do this this more properly
        byte[] out = data.getBytes(StandardCharsets.UTF_8);
        OutputStream stream = http.getOutputStream();
        stream.write(out);

        // getting response
        BufferedReader reader = new BufferedReader(new InputStreamReader((http.getInputStream())));
        StringBuilder stringBuilder = new StringBuilder();
        String output;
        while ((output = reader.readLine()) != null) {
            stringBuilder.append(output);
        }

        // regex to find elevations
        // separate all elevations from the data we don't need
        StringBuilder elevations = new StringBuilder();
        Matcher elevationsFinder = Pattern.compile("elevation\":(.*?)\\}")
                .matcher(stringBuilder.toString());
        while (elevationsFinder.find()) elevations.append(elevationsFinder.group());

        // get just the integers
        Matcher valueFinder = Pattern.compile("[0-9]+")
                .matcher(elevations.toString());
        while (valueFinder.find()) list.add(Float.valueOf(valueFinder.group(0)));

        http.disconnect();

        return list;
    }

    /**
     * use in finding the elevation of said route
     * @param start
     * @param end
     * @return
     * @throws IOException
     */
    public static List<SessionLocationPoint> getLocationsFromRoute(GeoPoint start, GeoPoint end) throws IOException {
        // create requests from locations
        String locations = start.getLongitude()+","+start.getLatitude()+";"+end.getLongitude()+","+end.getLatitude();

        List<SessionLocationPoint> list = new ArrayList<>();

        // creating connection
        URL url = new URL("https://routing.openstreetmap.de/routed-car/route/v1/driving/"+locations+"?alternatives=false&overview=full&steps=true");
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestMethod("GET");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/json");

        // getting response
        BufferedReader reader = new BufferedReader(new InputStreamReader((http.getInputStream())));
        StringBuilder stringBuilder = new StringBuilder();
        String output;
        while ((output = reader.readLine()) != null) {
            stringBuilder.append(output);
        }

        // regex to find elevations
        // separate all elevations from the data we don't need
        StringBuilder elevations = new StringBuilder();
        Matcher elevationsFinder = Pattern.compile("location\\\":(.*?)\\]")
                .matcher(stringBuilder.toString());
        while (elevationsFinder.find()) elevations.append(elevationsFinder.group());

        // get just the integers
        Matcher valueFinder = Pattern.compile("[-?0.0,-9.9]+")
                .matcher(elevations.toString());
        while (valueFinder.find()) {
            SessionLocationPoint point = new SessionLocationPoint();
            point.setLongitude(Double.parseDouble(valueFinder.group(0).split(",")[0]));
            point.setLatitude(Double.parseDouble(valueFinder.group(0).split(",")[1]));
            list.add(point);
        }

        http.disconnect();

        return list;
    }

    public static ArrayList<Float> getElevationsFromRoute(GeoPoint start, GeoPoint end) throws IOException {
        ArrayList<Float> list = getElevations(getLocationsFromRoute(start, end));
        list.remove(1);
        list.remove(2);
        list.remove(list.size()-1);

        return list;
    }

    static class ElevationRequest {
        double latitude;
        double longitude;

        ElevationRequest(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}