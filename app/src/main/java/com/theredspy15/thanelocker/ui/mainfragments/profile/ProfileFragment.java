package com.theredspy15.thanelocker.ui.mainfragments.profile;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.longboardlife.R;
import com.example.longboardlife.databinding.FragmentProfileBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.models.Profile;
import com.theredspy15.thanelocker.models.Session;
import com.theredspy15.thanelocker.ui.activitycontrollers.BoardActivity;
import com.theredspy15.thanelocker.ui.activitycontrollers.EditProfileActivity;
import com.theredspy15.thanelocker.utils.App;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    private final Profile profile = Profile.localProfile;

    private ArrayList<Session> sessions;
    private ArrayList<Board> boards;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseCrashlytics.getInstance().log("displaying profile fragment");

        binding.editProfileButton.setOnClickListener(this::loadEditProfile);
        //binding.viewAchievementsButton.setOnClickListener(this::loadAchievements);

        new Thread(this::loadAllData).start();

        return root;
    }

    private void loadAllData() {
        sessions = Profile.sessionsWithLocalProfile();
        boards = Profile.boardsWithLocalProfile();

        binding.editProfileButton.setOnClickListener(this::loadEditProfile);
        //binding.viewAchievementsButton.setOnClickListener(this::loadAchievements);
        if (profile.getName() != null) binding.nameText.setText(profile.getName());
        if (profile.getDescription() != null) binding.descriptionView.setText(profile.getDescription());

        if (profile.getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(profile.getImage(), 0, profile.getImage().length);
            requireActivity().runOnUiThread(()-> Glide.with(requireActivity()).load(bitmap).into(binding.profilePictureView));
        }

        loadFavoriteBoard();
        loadXpBar();

        if (sessions.isEmpty()) {
            requireActivity().runOnUiThread(()-> binding.chartsLayout.setVisibility(View.GONE));
        } else {
            loadSpeedsChart();
            loadTopSpeedsChart();
            loadDistancesChart();
            loadDurationsChart();
            loadTextViewStats();
        }
    }

    void loadXpBar() {
        requireActivity().runOnUiThread(()->{
            binding.xpView.setProgress(profile.getLevel_xp());
            binding.levelView.setText(getString(R.string.level)+" "+profile.getLevel());
        });
    }

    public void loadEditProfile(View view) {
        Intent myIntent = new Intent(requireContext(), EditProfileActivity.class);
        startActivity(myIntent);
    }

    private void loadTextViewStats() {
        float tDistance = 0;
        float tAvgDistance = 0;
        float tAvgSpeed = 0;
        float topSpeed = 0;
        float topDistance = 0;

        Resources resources = getResources();

        for (Session session : sessions) {
            // fastest speed
            if (Float.parseFloat(session.getTopSpeed()) > topSpeed) topSpeed = Float.parseFloat(session.getTopSpeed());

            float finalTopSpeed = topSpeed;
            requireActivity().runOnUiThread(()-> binding.topSpeedView.setText(App.getSpeedFormatted(finalTopSpeed,resources)));

            // furthest distance
            if (Float.parseFloat(session.getTotalDistance()) > topDistance) topDistance = Float.parseFloat(session.getTotalDistance());

            float finalTopDistance = topDistance;
            requireActivity().runOnUiThread(()-> binding.furthestDistanceView.setText(App.getDistanceFormatted(finalTopDistance,resources)));

           tDistance += Float.parseFloat(session.getTotalDistance());
           tAvgDistance += Float.parseFloat(session.getTotalDistance());
           tAvgSpeed += Float.parseFloat(session.getAvgSpeed());
        }
        tAvgSpeed = tAvgSpeed / sessions.size();
        tAvgDistance = tAvgDistance / sessions.size();

        float finalTDistance = tDistance;
        float finalTAvgDistance = tAvgDistance;
        float finalTAvgSpeed = tAvgSpeed;
        requireActivity().runOnUiThread(()->{
            binding.totalDistanceView.setText(App.getDistanceFormatted(finalTDistance,resources));
            binding.avgDistanceView.setText(App.getDistanceFormatted(finalTAvgDistance,resources));
            binding.avgSpeedView.setText(App.getSpeedFormatted(finalTAvgSpeed,resources));
        });
    }

    private void loadFavoriteBoard() {
        LinearLayout linearLayout = binding.statisticsLayout;
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setMargins(80,0,80,0);

        Board board = Profile.favoriteBoard();

        Button button = new Button(requireContext());
        if (board != null) {
            button.setText(board.getName());
            button.setTextSize(18);
            button.setBackgroundColor(requireContext().getColor(R.color.grey));
            button.getBackground().setAlpha(30);
            button.setPadding(0,0,0,0);
            button.setAllCaps(false);
            button.setOnClickListener(v->{
                Intent myIntent = new Intent(requireContext(), BoardActivity.class);
                myIntent.putExtra("board_id", board.getId());
                startActivity(myIntent);
            });

            if (board.getImage() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(board.getImage(), 0, board.getImage().length);
                Drawable drawable = new BitmapDrawable(this.getResources(),Bitmap.createScaledBitmap(bitmap, 400, 400, false));
                button.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable,null,null,null);
            }

        } else {
            button.setText(R.string.no_favorite);
            button.setTextSize(18);
            button.setBackgroundColor(requireContext().getColor(R.color.grey));
            button.getBackground().setAlpha(30);
            button.setPadding(0,0,0,0);
            button.setAllCaps(false);
        }

        requireActivity().runOnUiThread(()->linearLayout.addView(button,9,layout));
    }

    /*public void loadAchievements(View view) {
        Intent myIntent = new Intent(requireContext(), AchievementsActivity.class);
        myIntent.putExtra("achievements", profile.getAchievements());
        startActivity(myIntent);
    }*/

    private void loadSpeedsChart() {
        LineChart chart = binding.speedsChart;

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < sessions.size(); i++) {
            values.add(new Entry(i, Float.parseFloat(sessions.get(i).getAvgSpeed()), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24,null)));
        }

        // create a data object with the data sets
        LineData data = App.createLineSet(values,getString(R.string.speed),false,requireContext());

        XAxis xAxis = chart.getXAxis();
        xAxis.setLabelCount(Session.savedSessions.size());
        xAxis.setLabelRotationAngle(0f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if ((int) value < sessions.size()-1 && (int) value >= 0) {
                    return App.convertTimeToStringDate(sessions.get((int) value).getLocations().get(0).getTimeStamp());
                }
                else
                    return App.convertTimeToStringDate(sessions.get(0).getLocations().get(0).getTimeStamp());
            }
        });

        // set data
        chart.setData(data);
        requireActivity().runOnUiThread(()->chart.animateX(0));

        // coloring chart
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        int color = App.getThemeTextColor(requireContext());
        chart.getData().setValueTextColor(color);
        chart.getData().setValueTextColor(color);
        chart.getXAxis().setTextColor(color);
        chart.getAxisLeft().setTextColor(color);
        chart.getAxisRight().setTextColor(color);
        chart.getLegend().setTextColor(color);
    }

    private void loadTopSpeedsChart() {
        LineChart chart = binding.topSpeedsChart;

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < sessions.size(); i++) {
            values.add(new Entry(i, Float.parseFloat(sessions.get(i).getTopSpeed()), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24,null)));
        }

        // create a data object with the data sets
        LineData data = App.createLineSet(values,getString(R.string.speed),false,requireContext());

        XAxis xAxis = chart.getXAxis();
        xAxis.setLabelCount(Session.savedSessions.size());
        xAxis.setLabelRotationAngle(0f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if ((int) value < sessions.size()-1 && (int) value >= 0) {
                    return App.convertTimeToStringDate(sessions.get((int) value).getLocations().get(0).getTimeStamp());
                }
                else
                    return App.convertTimeToStringDate(sessions.get(0).getLocations().get(0).getTimeStamp());
            }
        });

        // set data
        chart.setData(data);
        requireActivity().runOnUiThread(()->chart.animateX(0));

        // coloring chart
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        int color = App.getThemeTextColor(requireContext());
        chart.getData().setValueTextColor(color);
        chart.getData().setValueTextColor(color);
        chart.getXAxis().setTextColor(color);
        chart.getAxisLeft().setTextColor(color);
        chart.getAxisRight().setTextColor(color);
        chart.getLegend().setTextColor(color);
    }

    private void loadDistancesChart() {
        LineChart chart = binding.distancesChart;

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < sessions.size(); i++) {
            values.add(new Entry(i, Float.parseFloat(sessions.get(i).getTotalDistance()), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24,null)));
        }

        // create a data object with the data sets
        LineData data = App.createLineSet(values,getString(R.string.distances),false,requireContext());

        XAxis xAxis = chart.getXAxis();
        xAxis.setLabelCount(Session.savedSessions.size());
        xAxis.setLabelRotationAngle(0f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if ((int) value < sessions.size()-1 && (int) value >= 0) {
                    return App.convertTimeToStringDate(sessions.get((int) value).getLocations().get(0).getTimeStamp());
                }
                else
                    return App.convertTimeToStringDate(sessions.get(0).getLocations().get(0).getTimeStamp());
            }
        });

        // set data
        chart.setData(data);
        requireActivity().runOnUiThread(()->chart.animateX(0));

        // coloring chart
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        int color = App.getThemeTextColor(requireContext());
        chart.getData().setValueTextColor(color);
        chart.getData().setValueTextColor(color);
        chart.getXAxis().setTextColor(color);
        chart.getAxisLeft().setTextColor(color);
        chart.getAxisRight().setTextColor(color);
        chart.getLegend().setTextColor(color);
    }

    private void loadDurationsChart() {
        LineChart chart = binding.durationsChart;

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < sessions.size(); i++) {
            values.add(new Entry(i, sessions.get(i).getDuration(), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24,null)));
        }

        // create a data object with the data sets
        LineData data = App.createLineSet(values,getString(R.string.durations),false,requireContext());

        XAxis xAxis = chart.getXAxis();
        xAxis.setLabelCount(Session.savedSessions.size());
        xAxis.setLabelRotationAngle(0f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if ((int) value < sessions.size()-1 && (int) value >= 0) {
                    return App.convertTimeToStringDate(sessions.get((int) value).getLocations().get(0).getTimeStamp());
                }
                else
                    return App.convertTimeToStringDate(sessions.get(0).getLocations().get(0).getTimeStamp());
            }
        });

        // set data
        chart.setData(data);
        requireActivity().runOnUiThread(()->chart.animateX(0));

        // coloring chart
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        int color = App.getThemeTextColor(requireContext());
        chart.getData().setValueTextColor(color);
        chart.getData().setValueTextColor(color);
        chart.getXAxis().setTextColor(color);
        chart.getAxisLeft().setTextColor(color);
        chart.getAxisRight().setTextColor(color);
        chart.getLegend().setTextColor(color);
    }
}
