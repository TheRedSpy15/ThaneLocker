package com.theredspy15.thanelocker.ui.activitycontrollers;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thanelocker.R;
import com.theredspy15.thanelocker.models.Session;
import com.theredspy15.thanelocker.utils.SavedDataManager;

public class SessionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        // not sure why session doesn't count as serializable like Board objects do
        Session session = SavedDataManager.savedSessions.get(getIntent().getIntExtra("session",0));

        Toast.makeText(this, ""+session.getLocations().get(0).getLatitude(), Toast.LENGTH_SHORT).show();
    }
}