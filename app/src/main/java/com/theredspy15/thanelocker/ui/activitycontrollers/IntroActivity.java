package com.theredspy15.thanelocker.ui.activitycontrollers;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.ActivityIntroBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.models.Profile;
import com.theredspy15.thanelocker.models.Session;
import com.theredspy15.thanelocker.utils.App;

public class IntroActivity extends AppCompatActivity {

    ActivityIntroBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MainActivity.preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        App.updateTheme();

        signIntoFirestore();
    }

    private void signIntoFirestore() {
        MainActivity.mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = MainActivity.mAuth.getCurrentUser();
        if (currentUser == null) {
            MainActivity.mAuth.signInAnonymously()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            System.out.println("loading profile");
                            loadProfile();
                        }
                    });
        } else loadProfile();
    }

    private TextView createDoneText(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null, ContextCompat.getDrawable(this,R.drawable.ic_baseline_check_24),null);
        return textView;
    }

    public void loadProfile() {
        Profile.localProfile.setId(MainActivity.mAuth.getCurrentUser().getUid());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        System.out.println("loading profile3");

        // Create a reference to the cities collection
        CollectionReference profileRef = db.collection("profiles");

        // Create a query against the collection.
        Query query = profileRef.whereEqualTo("id", Profile.localProfile.getId());
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Profile.localProfile = document.toObject(Profile.class);
                            Profile.save();
                        }
                        binding.layout.addView(createDoneText("Loaded Profile"), 1);
                        loadSessions();
                    }
                });
    }

    public void loadSessions() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a reference to the cities collection
        CollectionReference sessionsRef = db.collection("sessions");

        // Create a query against the collection.
        Query query = sessionsRef.whereEqualTo("user_id",  Profile.localProfile.getId());
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Session session = document.toObject(Session.class);
                            Session.savedSessions.put(session.getId(), session);
                            Session.savedSessionIds.add((Integer) session.getId());
                        }
                        binding.layout.addView(createDoneText("Loaded Sessions"), 2);
                        loadBoard();
                    }
                });
    }

    public void loadBoard() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a reference to the cities collection
        CollectionReference boardsRef = db.collection("boards");

        // Create a query against the collection.
        Query query = boardsRef.whereEqualTo("user_id",  Profile.localProfile.getId());
        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Board board = document.toObject(Board.class);
                            Board.savedBoards.put(board.getId(), board);
                            Board.savedBoardIds.add((Integer) board.getId());
                        }
                        binding.layout.addView(createDoneText("Loaded Boards"), 3);
                        Intent myIntent = new Intent(this, MainActivity.class);
                        startActivity(myIntent);
                    }
                });
    }
}