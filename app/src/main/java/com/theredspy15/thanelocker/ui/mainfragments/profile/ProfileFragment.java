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
import androidx.lifecycle.ViewModelProvider;

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.FragmentProfileBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.models.Profile;
import com.theredspy15.thanelocker.models.Session;
import com.theredspy15.thanelocker.ui.activitycontrollers.AchievementsActivity;
import com.theredspy15.thanelocker.ui.activitycontrollers.BoardActivity;
import com.theredspy15.thanelocker.ui.activitycontrollers.EditProfileActivity;
import com.theredspy15.thanelocker.utils.App;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FragmentProfileBinding binding;

    private final ArrayList<Session> sessions = Profile.sessionsWithLocalProfile();
    private final ArrayList<Board> boards = Profile.boardsWithLocalProfile();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.editProfileButton.setOnClickListener(this::loadEditProfile);
        binding.viewAchievementsButton.setOnClickListener(this::loadAchievements);

        loadAllData();

        return root;
    }

    private void loadAllData() { // TODO: multi-thread this
        if (Profile.localProfile.getName() != null) binding.nameText.setText(Profile.localProfile.getName());
        if (Profile.localProfile.getDescription() != null) binding.descriptionView.setText(Profile.localProfile.getDescription());

        if (Profile.localProfile.getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(App.toByteArray(Profile.localProfile.getImage()), 0, Profile.localProfile.getImage().size());
            binding.profilePictureView.setImageBitmap(bitmap);
        }

        loadFavoriteBoard();
        loadXpBar();

        if (sessions.isEmpty()) {
            binding.chartsLayout.setVisibility(View.GONE);
        } else {
            loadSpeedsChart();
            loadTopSpeedsChart();
            loadDistancesChart();
            loadDurationsChart();
            loadTextViewStats();
        }
    }

    void loadXpBar() {
        binding.xpView.setProgress((int) Profile.localProfile.getLevel_xp());
        binding.levelView.setText(getString(R.string.level)+" "+Profile.localProfile.getLevel());
    }

    public void loadEditProfile(View view) {
        Intent myIntent = new Intent(requireContext(), EditProfileActivity.class);
        startActivity(myIntent);
    }

    private void loadTextViewStats() {
        float tDistance = 0;
        float tAvgDistance = 0;
        float tAvgSpeed = 0;

        Resources resources = getResources();

        for (Session session : sessions) {
            // fastest speed
            float top = 0;
            if (Float.parseFloat(session.getTopSpeed()) > top) top = Float.parseFloat(session.getTopSpeed());
            binding.topSpeedView.setText(App.getSpeedFormatted(top,resources));

            // furthest distance
            top = 0;
            if (Float.parseFloat(session.getTotalDistance()) > top) top = Float.parseFloat(session.getTotalDistance());
            binding.furthestDistanceView.setText(App.getDistanceFormatted(top,resources));

           tDistance += Float.parseFloat(session.getTotalDistance());
           tAvgDistance += Float.parseFloat(session.getTotalDistance());
           tAvgSpeed += Float.parseFloat(session.getAvgSpeed());
        }
        tAvgSpeed = tAvgSpeed / sessions.size();
        tAvgDistance = tAvgDistance / sessions.size();

        binding.totalDistanceView.setText(App.getDistanceFormatted(tDistance,resources));
        binding.avgDistanceView.setText(App.getDistanceFormatted(tAvgDistance,resources));
        binding.avgSpeedView.setText(App.getSpeedFormatted(tAvgSpeed,resources));
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
            button.getBackground().setAlpha(64);
            button.setPadding(0,0,0,0);
            button.setAllCaps(false);
            button.setOnClickListener(v->{
                Intent myIntent = new Intent(requireContext(), BoardActivity.class);
                myIntent.putExtra("board_id", board.getId());
                startActivity(myIntent);
            });

            if (board.getImage() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(App.toByteArray(board.getImage()), 0, board.getImage().size());
                Drawable drawable = new BitmapDrawable(this.getResources(),Bitmap.createScaledBitmap(bitmap, 400, 400, false));
                button.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable,null,null,null);
            }

        } else {
            button.setText(R.string.no_favorite);
            button.setTextSize(18);
            button.setBackgroundColor(requireContext().getColor(R.color.grey));
            button.getBackground().setAlpha(64);
            button.setPadding(0,0,0,0);
            button.setAllCaps(false);
        }
        linearLayout.addView(button,9,layout);
    }

    public void loadAchievements(View view) {
        Intent myIntent = new Intent(requireContext(), AchievementsActivity.class);
        myIntent.putExtra("achievements", Profile.localProfile.getAchievements());
        startActivity(myIntent);
    }

    private void loadSpeedsChart() {
        LineChart chart = binding.speedsChart;

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < sessions.size(); i++) {
            values.add(new Entry(i, Float.parseFloat(sessions.get(i).getAvgSpeed()), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24,null)));
        }

        // create a data object with the data sets
        LineData data = App.createLineSet(values,getString(R.string.speed),requireContext());

        // set data
        chart.setData(data);
        chart.animateX(3000);

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
        LineData data = App.createLineSet(values,getString(R.string.speed),requireContext());

        // set data
        chart.setData(data);
        chart.animateX(3000);

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
        LineData data = App.createLineSet(values,getString(R.string.distances),requireContext());

        // set data
        chart.setData(data);
        chart.animateX(3000);

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
        LineData data = App.createLineSet(values,getString(R.string.durations),requireContext());

        // set data
        chart.setData(data);
        chart.animateX(3000);

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
