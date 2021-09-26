package com.theredspy15.longboardlife.ui.activitycontrollers;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.theredspy15.longboardlife.models.Board;
import com.theredspy15.longboardlife.models.Meetup;
import com.theredspy15.longboardlife.models.Profile;
import com.theredspy15.longboardlife.models.Session;
import com.theredspy15.longboardlife.utils.App;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    public static SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        App.updateTheme();

        Profile.load();
        if (Board.savedBoards.isEmpty()) Board.load();
        if (Session.savedSessions.isEmpty()) Session.load();
        if (Meetup.meetups.isEmpty()) Meetup.loadFromFirebase();

        if (preferences.getBoolean("firstTime",true)) firstTime();

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
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.thank_you_title));
        alertDialog.setMessage(getString(R.string.thank_you_content));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                (dialog, which) -> {
                    dialog.dismiss();
                    SharedPreferences.Editor prefsEditor = MainActivity.preferences.edit();
                    prefsEditor.putBoolean("firstTime",false);
                    prefsEditor.apply();
                });
        alertDialog.show();
    }

}