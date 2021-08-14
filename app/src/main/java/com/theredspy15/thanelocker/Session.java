package com.theredspy15.thanelocker;

import com.google.android.material.chip.Chip;

import java.io.Serializable;
import java.util.LinkedList;

public class Session implements Serializable {
    private short board_id;
    private String description;
    private String time_start;
    private String time_end;
    private String duration;
    private LinkedList<Chip> tags;

    public short getBoard_id() {
        return board_id;
    }

    public void setBoard_id(short board_id) {
        this.board_id = board_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public LinkedList<Chip> getTags() {
        return tags;
    }

    public void setTags(LinkedList<Chip> tags) {
        this.tags = tags;
    }
}
