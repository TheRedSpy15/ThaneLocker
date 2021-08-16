package com.theredspy15.thanelocker;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.thanelocker.R;
import com.example.thanelocker.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    public static LinkedList<Board> savedBoards;
    public static LinkedList<Session> savedSessions;
    public static LinkedList<String> savedTrucks;
    public static LinkedList<String> savedBushings;
    public static LinkedList<String> savedWheels;
    public static LinkedList<String> savedBearings;
    public static LinkedList<String> savedGriptape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermissions();
        savedBoards = new LinkedList<>();
        //loadSavedData(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_boards, R.id.navigation_sessions, R.id.navigation_news, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    public static void loadSavedData(Context context) {
        savedBoards = SerializableManager.readSerializable(context,"savedBoards");
        savedSessions = SerializableManager.readSerializable(context,"savedSessions");
        savedTrucks = SerializableManager.readSerializable(context,"savedTrucks");
        savedBushings = SerializableManager.readSerializable(context,"savedBushings");
        savedWheels = SerializableManager.readSerializable(context,"savedWheels");
        savedBearings = SerializableManager.readSerializable(context,"savedBearings");
        savedGriptape = SerializableManager.readSerializable(context,"savedGriptape");
    }

    public static void saveData(Context context) {
        SerializableManager.saveSerializable(context,savedBoards,"savedBoards");
        SerializableManager.saveSerializable(context,savedSessions,"savedSessions");
        SerializableManager.saveSerializable(context,savedTrucks,"savedTrucks");
        SerializableManager.saveSerializable(context,savedBushings,"savedBushings");
        SerializableManager.saveSerializable(context,savedWheels,"savedWheels");
        SerializableManager.saveSerializable(context,savedBearings,"savedBearings");
        SerializableManager.saveSerializable(context,savedGriptape,"savedGriptape");
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

}