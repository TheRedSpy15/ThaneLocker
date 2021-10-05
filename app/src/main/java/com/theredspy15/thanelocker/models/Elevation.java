package com.theredspy15.thanelocker.models;

import com.google.gson.Gson;
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

  /**
   * TODO: verify we have connection to api
   * gets elevations for a session AFTER it has been created to load all the
   * elevations at once. The list of returned elevations should be saved inside
   * the session, to prevent constantly having to wait for server to respond
   * @param locations locations from sesssion
   * @return elevations for said locations
   */
  public static ArrayList<Float>
  getElevations(List<SessionLocationPoint> locations) throws IOException {
    // create requests from locations
    List<ElevationRequest> requests = new ArrayList<>();
    for (SessionLocationPoint point : locations) {
      requests.add(
          new ElevationRequest(point.getLatitude(), point.getLongitude()));
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
    String data =
        "{\"locations\":" + json + "}"; // TODO: do this this more properly
    byte[] out = data.getBytes(StandardCharsets.UTF_8);
    OutputStream stream = http.getOutputStream();
    stream.write(out);

    // getting response
    BufferedReader reader =
        new BufferedReader(new InputStreamReader((http.getInputStream())));
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
    while (elevationsFinder.find())
      elevations.append(elevationsFinder.group());

    // get just the integers
    Matcher valueFinder =
        Pattern.compile("[0-9]+").matcher(elevations.toString());
    while (valueFinder.find())
      list.add(Float.valueOf(valueFinder.group(0)));

    http.disconnect();

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
