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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.thanelocker.R;
import com.example.thanelocker.databinding.FragmentSessionsBinding;
import com.theredspy15.thanelocker.models.Session;
import com.theredspy15.thanelocker.ui.activitycontrollers.SessionActivity;
import com.theredspy15.thanelocker.utils.App;
import com.theredspy15.thanelocker.utils.LocationService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;

public class SessionsFragment extends Fragment {

    private SessionsViewModel sessionsViewModel;
    private FragmentSessionsBinding binding;

    public static Session newSession = new Session();

    private static boolean isRecording = false;
    Thread sessionThread;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        sessionsViewModel =
                new ViewModelProvider(this).get(SessionsViewModel.class);

        binding = FragmentSessionsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.newSessionButton.setOnClickListener(this::loadStartSession);

        sessionThread = new Thread(this::loadSessions);
        sessionThread.start();
        loadRecordingButtonIndicator();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void loadSessions() {
        App.cleanSessions();

        requireActivity().runOnUiThread(()->binding.sessionsLayout.removeAllViews());
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setMargins(0, 20, 0, 20);

        Collections.reverse(Session.savedSessionIds);
        if (Session.savedSessions != null) {
            for (int session_id : Session.savedSessionIds) {
                Session session = Session.savedSessions.get(session_id);
                if (session != null) {
                    Button button = new Button(getContext());
                    button.setText(session.getName());
                    button.setTextSize(18);
                    button.setPadding(50, 50, 50, 50);
                    button.setAllCaps(false);
                    button.setBackgroundResource(R.drawable.rounded_corners);
                    GradientDrawable drawable = (GradientDrawable) button.getBackground();
                    drawable.setColor(requireContext().getColor(R.color.grey));
                    drawable.setAlpha(64);

                    button.setOnClickListener(v -> {
                        Intent myIntent = new Intent(getContext(), SessionActivity.class);
                        myIntent.putExtra("session_id", session.getId());
                        startActivity(myIntent);
                    });

                    button.setOnLongClickListener(v->{
                        AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).create();
                        alertDialog.setTitle("Delete Session");
                        alertDialog.setMessage("Are you sure?");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
                                (dialog, which) -> {
                                    dialog.dismiss();
                                    Session.savedSessions.remove(session.getId());
                                    Session.savedSessionIds.remove(Session.savedSessionIds.indexOf(session.getId())); // removing by object doesn't work
                                    Session.save();
                                    sessionThread = new Thread(this::loadSessions);
                                    sessionThread.start();
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                                (dialog, which) -> dialog.dismiss());
                        alertDialog.show();
                        return false;
                    });

                    requireActivity().runOnUiThread(()->binding.sessionsLayout.addView(button, layout));
                }
            }
        }
        Collections.reverse(Session.savedSessionIds);

        if (Session.savedSessions != null && Session.savedSessions.isEmpty()) { // no sessions
            TextView textView = new TextView(requireContext());
            textView.setText("No Session Recorded");
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextSize(18);
            requireActivity().runOnUiThread(() -> binding.sessionsLayout.addView(textView, layout));
        }
    }

    public void loadStartSession(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).create();
        alertDialog.setTitle("Start Session?");
        alertDialog.setMessage("You're GPS data will be used");
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                (dialog, which) -> {
                    dialog.dismiss();
                    stopService();
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "START",
                (dialog, which) -> {
                    PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                            "MyApp::MyWakelockTag");
                    wakeLock.acquire(60*60*1000L /*60 minutes*/);

                    startService();
                    dialog.dismiss();
                });
        alertDialog.show();
    }

    public void stopSession(View view) {
        stopService();
        loadRecordingButtonIndicator();
    }

    /** sets record button text and color depending on whether or not a session is being recorded actively **/
    void loadRecordingButtonIndicator() {
        if (isRecording) {
            binding.newSessionButton.setBackgroundColor(requireContext().getColor(R.color.recording_session));
            binding.newSessionButton.setText("Click here to stop recording");
            binding.newSessionButton.setOnClickListener(this::stopSession);
        } else {
            binding.newSessionButton.setBackgroundColor(requireContext().getColor(R.color.purple_500));
            binding.newSessionButton.setText("Record Session");
            binding.newSessionButton.setOnClickListener(this::loadStartSession);
        }
    }

    public void startService() {
        isRecording = true;
        loadRecordingButtonIndicator();
        prepareSession();
        Intent serviceIntent = new Intent(requireContext(), LocationService.class);
        serviceIntent.putExtra("description", "Recording your data for you to later view"); // maybe send list this way?
        ContextCompat.startForegroundService(requireContext(), serviceIntent);
    }

    public void stopService() {
        isRecording = false;
        loadRecordingButtonIndicator();

        DateFormat df = new SimpleDateFormat("hh:mm a");
        String date = df.format(Calendar.getInstance().getTime());
        newSession.setTime_end(date);
        newSession.setEnd_millis(Calendar.getInstance().getTimeInMillis());

        Intent serviceIntent = new Intent(requireContext(), LocationService.class);
        requireContext().stopService(serviceIntent);

        if (newSession.getLocations().size() > 0) {
            Session.savedSessions.put(newSession.getId(),newSession);
            Session.savedSessionIds.add(newSession.getId());
            sessionThread = new Thread(this::loadSessions);
            sessionThread.start();
            Session.save();
        } else
            Toast.makeText(requireContext(), "Not enough data recorded to save", Toast.LENGTH_LONG).show();
    }

    void prepareSession() {
        DateFormat df = new SimpleDateFormat("EEE, MMM d (hh:mm a)");
        String date = df.format(Calendar.getInstance().getTime());
        newSession.setName(date);

        df = new SimpleDateFormat("hh:mm a");
        date = df.format(Calendar.getInstance().getTime());
        newSession.setTime_start(date);
        newSession.setStart_millis(Calendar.getInstance().getTimeInMillis());

        df = new SimpleDateFormat("MMM d");
        date = df.format(Calendar.getInstance().getTime());
        newSession.setDate(date);
    }

}