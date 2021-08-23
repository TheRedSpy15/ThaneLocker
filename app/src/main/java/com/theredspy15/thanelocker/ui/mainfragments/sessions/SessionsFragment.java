package com.theredspy15.thanelocker.ui.mainfragments.sessions;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.thanelocker.R;
import com.example.thanelocker.databinding.FragmentSessionsBinding;
import com.theredspy15.thanelocker.utils.LocationService;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;
import com.theredspy15.thanelocker.utils.SavedDataManager;
import com.theredspy15.thanelocker.models.Session;
import com.theredspy15.thanelocker.ui.activitycontrollers.SessionActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SessionsFragment extends Fragment {

    private SessionsViewModel sessionsViewModel;
    private FragmentSessionsBinding binding;

    public static Session newSession = new Session();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        sessionsViewModel =
                new ViewModelProvider(this).get(SessionsViewModel.class);

        binding = FragmentSessionsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadSessions();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void loadSessions() {
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setMargins(0, 20, 0, 20);

        if (SavedDataManager.savedSessions != null) {
            for (Session session : SavedDataManager.savedSessions) {
                Button button = new Button(getContext());
                button.setText(session.getName());
                button.setTextSize(18);
                button.setBackgroundColor(getResources().getColor(R.color.grey));
                button.setPadding(50, 50, 50, 50);
                button.setAllCaps(false);
                button.setOnClickListener(v -> {
                    Intent myIntent = new Intent(getContext(), SessionActivity.class);
                    myIntent.putExtra("session", SavedDataManager.savedSessions.indexOf(session));
                    startActivity(myIntent);
                });
                binding.sessionsLayout.addView(button, layout);
            }
        }
        // TODO: move to xml and see what it looks like if it doesn't stretch the entire screen
        Button button = new Button(getContext());
        button.setText("Record Session");
        button.setTextSize(18);
        button.setPadding(50, 50, 50, 50);
        button.setBackgroundColor(getResources().getColor(R.color.grey));
        button.setOnClickListener(this::loadStartSession);
        button.setBackgroundResource(R.drawable.rounded_corners);
        GradientDrawable drawable = (GradientDrawable) button.getBackground();
        drawable.setColor(getResources().getColor(R.color.design_default_color_primary));
        binding.sessionsLayout.addView(button, layout);
    }

    public void loadStartSession(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).create();
        alertDialog.setTitle("Start Session?");
        alertDialog.setMessage("You're GPS data will be used");
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                (dialog, which) -> {
                    stopService();
                    dialog.dismiss();
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "START",
                (dialog, which) -> {
                    PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                            "MyApp::MyWakelockTag");
                    wakeLock.acquire(60*60*1000L /*60 minutes*/);

                    startService();
                    loadStopSession();
                });
        alertDialog.show();
    }

    void loadStopSession() {
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).create();
        alertDialog.setTitle("Recording Session");
        alertDialog.setMessage("Session is now being recorded. Turn off you screen and ride! press stop when done");
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "STOP",
                (dialog, which) -> stopService());
        alertDialog.show();

        binding.sessionsLayout.removeAllViews();
        loadSessions();
    }

    public void startService() {
        prepareSession();
        Intent serviceIntent = new Intent(requireContext(), LocationService.class);
        serviceIntent.putExtra("description", "Recording your data for you to later view"); // maybe send list this way?
        ContextCompat.startForegroundService(requireContext(), serviceIntent);
    }
    public void stopService() {
        Intent serviceIntent = new Intent(requireContext(), LocationService.class);
        requireContext().stopService(serviceIntent);
        SavedDataManager.savedSessions.add(newSession);
        SavedDataManager.saveData();

        Intent myIntent = new Intent(requireContext(), MainActivity.class);
        startActivity(myIntent);
    }

    void prepareSession() {
        DateFormat df = new SimpleDateFormat("EEE, MMM d");
        String date = df.format(Calendar.getInstance().getTime());

        newSession.setCityName(date);
    }

}