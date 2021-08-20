package com.theredspy15.thanelocker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.thanelocker.R;
import com.theredspy15.thanelocker.ui.sessions.SessionsFragment;

public class LocationService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    LocationManager locationManager;
    LocationListener locationListener;
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("description");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Recording Session")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_baseline_location_on_24)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        startTracker();

        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        locationManager.removeUpdates(locationListener);
        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // TODO: add support for older versions
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @SuppressLint("MissingPermission")
    void startTracker() {
        locationManager = (LocationManager) App.getContext().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new SessionLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    }

    static class SessionLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            SessionsFragment.newSession.getLocations().add(loc);
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}