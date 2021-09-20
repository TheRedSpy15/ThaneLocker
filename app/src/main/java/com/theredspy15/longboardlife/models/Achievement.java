package com.theredspy15.longboardlife.models;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.longboardlife.R;
import com.theredspy15.longboardlife.ui.activitycontrollers.MainActivity;

public class Achievement {
    private static final String CHANNEL_ID = "thaneachievement";

    public enum rarity {
        LEGENDARY,
        RARE,
        COMMON
    }

    private String description = "";
    private int xp = 0;
    private rarity rarity;

    public static void notify(String msg, String title, Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            title = "Achievement:"+" "+title;

            Intent intent=new Intent(context,MainActivity.class);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,"name", NotificationManager.IMPORTANCE_HIGH);

            PendingIntent pendingIntent=PendingIntent.getActivity(context,1,intent,0);
            Notification notification=new Notification.Builder(context,CHANNEL_ID)
                    .setContentText(msg)
                    .setContentTitle(title)
                    .setContentIntent(pendingIntent)
                    .setChannelId(CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_baseline_trophy_24)
                    .build();

            NotificationManager notificationManager=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.notify(1,notification);
        }
    }

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
