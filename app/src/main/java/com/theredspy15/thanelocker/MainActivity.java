package com.theredspy15.thanelocker;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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

        loadSavedData();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_boards, R.id.navigation_sessions)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    public void loadSavedData() {
        savedBoards = SerializableManager.readSerializable(this,"savedBoards");
        savedSessions = SerializableManager.readSerializable(this,"savedSessions");
        savedTrucks = SerializableManager.readSerializable(this,"savedTrucks");
        savedBushings = SerializableManager.readSerializable(this,"savedBushings");
        savedWheels = SerializableManager.readSerializable(this,"savedWheels");
        savedBearings = SerializableManager.readSerializable(this,"savedBearings");
        savedGriptape = SerializableManager.readSerializable(this,"savedGriptape");
    }

}