package com.theredspy15.thanelocker.ui.activitycontrollers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.example.longboardlife.BuildConfig;
import com.example.longboardlife.R;
import com.example.longboardlife.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.installations.FirebaseInstallations;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.models.Profile;
import com.theredspy15.thanelocker.models.Session;
import com.theredspy15.thanelocker.ui.MyIntro;
import com.theredspy15.thanelocker.utils.App;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    public static SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        App.updateTheme();

        // crashlytics
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCustomKey("mode",BuildConfig.BUILD_TYPE);
        crashlytics.setCustomKey("version_code",BuildConfig.VERSION_CODE);
        crashlytics.setCustomKey("version_name",BuildConfig.VERSION_NAME);
        FirebaseInstallations.getInstance().getId()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseCrashlytics.getInstance().setUserId(task.getResult());
                    } else {
                        FirebaseCrashlytics.getInstance().log("Failed to access FID");
                    }
                });

        // loading data
        FirebaseCrashlytics.getInstance().log("beginning to load data");
        Profile.load();
        if (Board.savedBoards.isEmpty()) Board.load();
        if (Session.savedSessions.isEmpty()) Session.load(this);

        // removes need for subscribing while testing
        if (BuildConfig.DEBUG)
            preferences.edit().putBoolean("subscribe",true).apply();

        if (preferences.getBoolean("firsttime",true)) firstTime();

        App.setContext(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_profile, R.id.navigation_boards, R.id.navigation_sessions, R.id.navigation_news, R.id.navigation_calculator, R.id.navigation_settings, R.id.navigation_skatemap)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @Override
    public void onStop() {
        super.onStop();

        Session.save();
        Board.save();
        Profile.save();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    private void firstTime() {
        Intent myIntent = new Intent(this, MyIntro.class);
        startActivity(myIntent);

        preferences.edit().putBoolean("firsttime",false).apply();
    }
}