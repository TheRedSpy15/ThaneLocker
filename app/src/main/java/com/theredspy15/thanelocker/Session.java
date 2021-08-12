package com.theredspy15.thanelocker;

import com.google.android.material.chip.Chip;

import java.io.Serializable;
import java.util.LinkedList;

public class Session implements Serializable {
    short board_id;
    String description;
    String time_start;
    String time_end;
    String duration;
    LinkedList<Chip> tags;
}
