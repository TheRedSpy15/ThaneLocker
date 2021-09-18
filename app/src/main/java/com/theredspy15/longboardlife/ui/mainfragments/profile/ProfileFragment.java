package com.theredspy15.longboardlife.ui.mainfragments.profile;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.DashPathEffect;
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
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.theredspy15.longboardlife.models.Board;
import com.theredspy15.longboardlife.models.Profile;
import com.theredspy15.longboardlife.models.Session;
import com.theredspy15.longboardlife.ui.activitycontrollers.AchievementsActivity;
import com.theredspy15.longboardlife.ui.activitycontrollers.BoardActivity;
import com.theredspy15.longboardlife.ui.activitycontrollers.EditProfileActivity;
import com.theredspy15.longboardlife.utils.App;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FragmentProfileBinding binding;

    private final Profile profile = Profile.localProfile;

    private final ArrayList<Session> sessions = Profile.sessionsWithLocalProfile();
    private final ArrayList<Board> boards = Profile.boardsWithLocalProfile();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadAllData();

        return root;
    }

    private void loadAllData() { // TODO: multi-thread this
        binding.editProfileButton.setOnClickListener(this::loadEditProfile);
        binding.viewAchievementsButton.setOnClickListener(this::loadAchievements);
        binding.nameText.setText(profile.getName());
        binding.descriptionView.setText(profile.getDescription());

        if (profile.getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(profile.getImage(), 0, profile.getImage().length);
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
        binding.xpView.setProgress((int) profile.getLevel_xp());
        binding.levelView.setText(getString(R.string.level)+" "+profile.getLevel());
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
                Bitmap bitmap = BitmapFactory.decodeByteArray(board.getImage(), 0, board.getImage().length);
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
        myIntent.putExtra("achievements", profile.getAchievements());
        startActivity(myIntent);
    }

    private void loadSpeedsChart() {
        LineChart chart = binding.speedsChart;

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < sessions.size(); i++) {
            values.add(new Entry(i, Float.parseFloat(sessions.get(i).getAvgSpeed()), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24,null)));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, getString(R.string.speed)); // TODO: shrink set1 from all 3 graph function into 1, and just change values
        set1.setDrawIcons(false);

        // draw dashed line
        set1.enableDashedLine(10f, 5f, 0f);

        // black lines and points
        set1.setColor(requireContext().getColor(R.color.purple_500));
        set1.setCircleColor(requireContext().getColor(R.color.purple_500));

        // line thickness and point size
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);

        // draw points as solid circles
        set1.setDrawCircleHole(false);

        // customize legend entry
        set1.setFormLineWidth(1f);
        set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        set1.setFormSize(15.f);

        // text size of values
        set1.setValueTextSize(9f);

        // draw selection line as dashed
        set1.enableDashedHighlightLine(10f, 5f, 0f);

        // set the filled area
        set1.setDrawFilled(true);
        set1.setFillFormatter((dataSet, dataProvider) -> chart.getAxisLeft().getAxisMinimum());
        set1.setFillColor(requireContext().getColor(R.color.purple_500));

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets

        // create a data object with the data sets
        LineData data = new LineData(dataSets);

        // set data
        chart.setData(data);

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

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, getString(R.string.speed));

        set1.setDrawIcons(false);

        // draw dashed line
        set1.enableDashedLine(10f, 5f, 0f);

        // black lines and points
        set1.setColor(requireContext().getColor(R.color.purple_500));
        set1.setCircleColor(requireContext().getColor(R.color.purple_500));

        // line thickness and point size
        set1.setLineWidth(1f);
        set1.setCircleRadius(3f);

        // draw points as solid circles
        set1.setDrawCircleHole(false);

        // customize legend entry
        set1.setFormLineWidth(1f);
        set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        set1.setFormSize(15.f);

        // text size of values
        set1.setValueTextSize(9f);

        // draw selection line as dashed
        set1.enableDashedHighlightLine(10f, 5f, 0f);

        // set the filled area
        set1.setDrawFilled(true);
        set1.setFillFormatter((dataSet, dataProvider) -> chart.getAxisLeft().getAxisMinimum());
        set1.setFillColor(requireContext().getColor(R.color.purple_500));

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets

        // create a data object with the data sets
        LineData data = new LineData(dataSets);

        // set data
        chart.setData(data);

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

        LineDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set1.notifyDataSetChanged();
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, getString(R.string.distance));

            set1.setDrawIcons(false);

            // draw dashed line
            set1.enableDashedLine(10f, 5f, 0f);

            // black lines and points
            set1.setColor(requireContext().getColor(R.color.purple_500));
            set1.setCircleColor(requireContext().getColor(R.color.purple_500));

            // line thickness and point size
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);

            // draw points as solid circles
            set1.setDrawCircleHole(false);

            // customize legend entry
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            // text size of values
            set1.setValueTextSize(9f);

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f);

            // set the filled area
            set1.setDrawFilled(true);
            set1.setFillFormatter((dataSet, dataProvider) -> chart.getAxisLeft().getAxisMinimum());
            set1.setFillColor(requireContext().getColor(R.color.purple_500));

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            chart.setData(data);

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

    private void loadDurationsChart() {
        LineChart chart = binding.durationsChart;

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < sessions.size(); i++) {
            values.add(new Entry(i, sessions.get(i).getDuration(), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24,null)));
        }

        LineDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set1.notifyDataSetChanged();
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, getString(R.string.duration));

            set1.setDrawIcons(false);

            // draw dashed line
            set1.enableDashedLine(10f, 5f, 0f);

            // black lines and points
            set1.setColor(requireContext().getColor(R.color.purple_500));
            set1.setCircleColor(requireContext().getColor(R.color.purple_500));

            // line thickness and point size
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);

            // draw points as solid circles
            set1.setDrawCircleHole(false);

            // customize legend entry
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            // text size of values
            set1.setValueTextSize(9f);

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f);

            // set the filled area
            set1.setDrawFilled(true);
            set1.setFillFormatter((dataSet, dataProvider) -> chart.getAxisLeft().getAxisMinimum());
            set1.setFillColor(requireContext().getColor(R.color.purple_500));

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            chart.setData(data);
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
}