package com.theredspy15.thanelocker;

import android.media.Image;

import com.google.android.material.chip.Chip;

import java.io.Serializable;
import java.util.LinkedList;

public class Board implements Serializable {
    private short id; // (incrementing) number
    private String name; //default: board + id
    private LinkedList<Chip> tags;
    private Image image;
    private String description;
    private String trucks;
    private byte rearAngle=50;
    private byte frontAngle=50;
    private String rd_bushing="Stock";
    private String bd_bushings="Stock";
    private String wheels;
    private String bearings;
    private double riserHt=0.0;
    private String gripTp="Standard";
    private LinkedList<Session> sessions;

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<Chip> getTags() {
        return tags;
    }

    public void setTags(LinkedList<Chip> tags) {
        this.tags = tags;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
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

    public double getRiserHt() {
        return riserHt;
    }

    public void setRiserHt(double riserHt) {
        this.riserHt = riserHt;
    }

    public String getGripTp() {
        return gripTp;
    }

    public void setGripTp(String gripTp) {
        this.gripTp = gripTp;
    }

    public LinkedList<Session> getSessions() {
        return sessions;
    }

    public void setSessions(LinkedList<Session> sessions) {
        this.sessions = sessions;
    }
}
