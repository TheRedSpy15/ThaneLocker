package com.theredspy15.longboardlife.models;

public class Achievement {
    public enum rarity {
        LEGENDARY,
        RARE,
        COMMON
    }

    private String description = "";
    private int xp = 0;
    private rarity rarity;

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

    public Achievement.rarity getRarity() {
        return rarity;
    }

    public Achievement setRarity(Achievement.rarity rarity) {
        this.rarity = rarity;
        return this;
    }
}
