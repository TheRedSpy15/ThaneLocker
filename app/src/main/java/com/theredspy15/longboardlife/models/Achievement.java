package com.theredspy15.longboardlife.models;

public class Achievement {
    private String description = "";
    private int xp = 0;

    public String getDescription() {
        return description;
    }

    public Achievement setDescription(String description) {
        this.description = description;
        return this;
    }

    public int getXp() {
        return xp;
    }

    public Achievement setXp(int xp) {
        this.xp = xp;
        return this;
    }
}
