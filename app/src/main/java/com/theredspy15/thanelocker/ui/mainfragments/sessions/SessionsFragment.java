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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.FragmentSessionsBinding;
import com.theredspy15.thanelocker.models.Profile;
import com.theredspy15.thanelocker.models.Session;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;
import com.theredspy15.thanelocker.ui.activitycontrollers.SessionActivity;
import com.theredspy15.thanelocker.utils.App;
import com.theredspy15.thanelocker.utils.LocationService;
import com.theredspy15.thanelocker.utils.PermissionChecker;
import com.theredspy15.thanelocker.utils.Purchasing;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class SessionsFragment extends Fragment {

    private FragmentSessionsBinding binding;

    public static Session newSession = new Session();

    private static boolean isRecording = false;
    Thread sessionThread;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSessionsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        App.cleanSessions();

        binding.newSessionButton.setOnClickListener(this::loadStartSession);

        sessionThread = new Thread(this::loadSessions);
        sessionThread.start();
        loadRecordingButtonIndicator();

        if (!MainActivity.preferences.getBoolean("subscribe",false)) enableLimit();

        return root;
    }

    private void enableLimit() {
        if (Session.savedSessions.size() >= 10) {
            binding.newSessionButton.setText(R.string.free_limit_reached);
            binding.newSessionButton.setOnClickListener(v->{
                Intent myIntent = new Intent(requireContext(), MainActivity.class);
                startActivity(myIntent);
                new Purchasing(requireContext()).subscribe(requireContext(),requireActivity());
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void loadSessions() {
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
                    drawable.setAlpha(30);

                    button.setOnClickListener(v -> {
                        Intent myIntent = new Intent(getContext(), SessionActivity.class);
                        myIntent.putExtra("session_id", session.getId());
                        startActivity(myIntent);
                    });

                    button.setOnLongClickListener(v->{
                        MaterialDialog mDialog = new MaterialDialog.Builder(requireActivity())
                                .setTitle(getString(R.string.delete_session))
                                .setAnimation("58413-delete-icon-animation.json")
                                .setMessage(getString(R.string.are_you_sure))
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.delete), (dialogInterface, which) -> {
                                    dialogInterface.dismiss();
                                    Session.savedSessions.remove(session.getId());
                                    Session.savedSessionIds.remove((Integer) session.getId());
                                    sessionThread = new Thread(this::loadSessions);
                                    sessionThread.start();
                                })
                                .setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> dialogInterface.dismiss())
                                .build();
                        mDialog.getAnimationView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        mDialog.show();
                        return false;
                    });

                    requireActivity().runOnUiThread(()->binding.sessionsLayout.addView(button, layout));
                }
            }
        }
        Collections.reverse(Session.savedSessionIds);

        if (Session.savedSessions != null && Session.savedSessions.isEmpty()) { // no sessions
            TextView textView = new TextView(requireContext());
            textView.setText(R.string.no_sessions);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextSize(18);
            requireActivity().runOnUiThread(() -> binding.sessionsLayout.addView(textView, layout));
        }
    }

    private boolean checkPermission() { // TODO: check that gps is even on
        if (!PermissionChecker.checkPermissionLocation(requireContext())) PermissionChecker.requestPermissionLocation(requireActivity());

        return PermissionChecker.checkPermissionLocation(requireContext());
    }

    public void loadStartSession(View view) {
        if (checkPermission()) {
            MaterialDialog mDialog = new MaterialDialog.Builder(requireActivity())
                    .setTitle(getString(R.string.record_dialog_title))
                    .setAnimation("49696-skateboarding-boy.json")
                    .setMessage(getString(R.string.recording_disclaimer))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.start), (dialogInterface, which) -> {
                        PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                "ThaneLocker::WakelockTag");
                        wakeLock.acquire(60*60*1000L /*60 minutes*/);
                        startService();
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> dialogInterface.dismiss())
                    .build();
            mDialog.getAnimationView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mDialog.show();
        }
    }

    public void stopSession(View view) {
        stopService();
        loadRecordingButtonIndicator();
        if (!MainActivity.preferences.getBoolean("subscribe",false)) enableLimit();
    }

    /** sets record button text and color depending on whether or not a session is being recorded actively **/
    void loadRecordingButtonIndicator() {
        if (isRecording) {
            binding.newSessionButton.setBackgroundColor(requireContext().getColor(R.color.recording_session));
            binding.newSessionButton.setText(R.string.click_to_stop);
            binding.newSessionButton.setOnClickListener(this::stopSession);
        } else {
            binding.newSessionButton.setBackgroundColor(requireContext().getColor(R.color.purple_500));
            binding.newSessionButton.setText(R.string.record_session);
            binding.newSessionButton.setOnClickListener(this::loadStartSession);
        }
    }

    public void startService() {
        isRecording = true;
        loadRecordingButtonIndicator();
        prepareSession();
        Intent serviceIntent = new Intent(requireContext(), LocationService.class);
        serviceIntent.putExtra("description", getString(R.string.notification_description)); // maybe send list this way?
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

            Profile.localProfile.addXp((int) (Session.xpValue*newSession.getLocations().size()));
        } else
            MotionToast.Companion.createColorToast(
                    requireActivity(),
                    getString(R.string.no_save_session),
                    getString(R.string.not_enough_data),
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(requireContext(), R.font.roboto)
            );
    }

    void prepareSession() {
        newSession = new Session();

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