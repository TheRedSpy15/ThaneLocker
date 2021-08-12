package com.theredspy15.thanelocker;

import android.media.Image;

import com.google.android.material.chip.Chip;

import java.io.Serializable;
import java.util.LinkedList;

public class Board implements Serializable {
    short id; // (incrementing) number
    String name; //default: board + id
    LinkedList<Chip> tags;
    Image image;
    String description;
    String trucks;
    String rd_bushings;
    String bd_bushings;
    String wheels;
    LinkedList<Session> sessions;

}
