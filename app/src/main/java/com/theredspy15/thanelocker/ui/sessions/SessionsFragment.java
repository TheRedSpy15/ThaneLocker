package com.theredspy15.thanelocker.ui.sessions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.thanelocker.R;
import com.example.thanelocker.databinding.FragmentSessionsBinding;
import com.theredspy15.thanelocker.SavedDataManager;
import com.theredspy15.thanelocker.Session;
import com.theredspy15.thanelocker.SessionActivity;

public class SessionsFragment extends Fragment {

    private SessionsViewModel sessionsViewModel;
    private FragmentSessionsBinding binding;

    static Session newSession = new Session();

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
                button.setPadding(50, 150, 50, 150);
                button.setAllCaps(false);
                button.setOnClickListener(v -> {
                    Intent myIntent = new Intent(getContext(), SessionActivity.class);
                    myIntent.putExtra("session", SavedDataManager.savedSessions.get(SavedDataManager.savedSessions.indexOf(session)));
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

    @SuppressLint("MissingPermission")
    public void loadStartSession(View view) {
        // maybe just a modal popup that shows duration and a stop button
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).create();
        alertDialog.setTitle("Recording Session");
        alertDialog.setMessage("Alert message to be shown");
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "STOP",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locationListener);
    }

    /*---------- Listener class to get coordinates ------------- */
    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            System.out.println("still running");
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