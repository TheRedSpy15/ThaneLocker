package com.theredspy15.longboardlife.models;

import java.util.ArrayList;
import java.util.List;

public class Meetup {
    String title;
    String description;
    List<Integer> profileIds = new ArrayList<>(); // people attending
    double latitude;
    double longitude;
}
