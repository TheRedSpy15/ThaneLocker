package com.theredspy15.thanelocker.ui.mainfragments.profile;

import android.content.Intent;
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

import com.example.thanelocker.R;
import com.example.thanelocker.databinding.FragmentProfileBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.models.Profile;
import com.theredspy15.thanelocker.models.Session;
import com.theredspy15.thanelocker.ui.activitycontrollers.BoardActivity;
import com.theredspy15.thanelocker.ui.activitycontrollers.EditProfileActivity;
import com.theredspy15.thanelocker.utils.App;

import java.util.ArrayList;
import java.util.LinkedList;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FragmentProfileBinding binding;

    private final LinkedList<Session> sessions = Profile.sessionsWithLocalProfile();
    private final LinkedList<Board> boards = Profile.boardsWithLocalProfile();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadAllData();

        return root;
    }

    private synchronized void loadAllData() { // TODO: multi-thread this
        binding.editProfileButton.setOnClickListener(this::loadEditProfile);
        binding.nameText.setText(Profile.localProfile.getName());
        binding.descriptionView.setText(Profile.localProfile.getDescription());

        if (Profile.localProfile.getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(Profile.localProfile.getImage(), 0, Profile.localProfile.getImage().length);
            binding.profilePictureView.setImageBitmap(bitmap);
        }

        loadFavoriteBoard();

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

    public void loadEditProfile(View view) {
        Intent myIntent = new Intent(requireContext(), EditProfileActivity.class);
        startActivity(myIntent);
    }

    private synchronized void loadTextViewStats() {
        float tDistance = 0;
        float tAvgDistance = 0;
        float tAvgSpeed = 0;

        for (Session session : sessions) {
            // fastest speed
            float top = 0;
            if (Float.parseFloat(session.getTopSpeed()) > top) top = Float.parseFloat(session.getTopSpeed());
            binding.topSpeedView.setText(top+" MPH");

            // furthest distance
            top = 0;
            if (Float.parseFloat(session.getTotalDistance()) > top) top = Float.parseFloat(session.getTotalDistance());
            binding.furthestDistanceView.setText(top+" Miles");

           tDistance += Float.parseFloat(session.getTotalDistance());
           tAvgDistance += Float.parseFloat(session.getTotalDistance());
           tAvgSpeed += Float.parseFloat(session.getAvgSpeed());
        }
        tAvgSpeed = tAvgSpeed / sessions.size();
        tAvgDistance = tAvgDistance / sessions.size();

        binding.totalDistanceView.setText(tDistance+" Miles");
        binding.avgDistanceView.setText(tAvgDistance+" Miles");
        binding.avgSpeedView.setText(tAvgSpeed+" MPH");
    }

    private synchronized void loadFavoriteBoard() {
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
                myIntent.putExtra("board", board);
                startActivity(myIntent);
            });

            if (board.getImage() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(board.getImage(), 0, board.getImage().length);
                Drawable drawable = new BitmapDrawable(this.getResources(),Bitmap.createScaledBitmap(bitmap, 400, 400, false));
                button.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable,null,null,null);
            }

        } else {
            button.setText("No Favorite");
            button.setTextSize(18);
            button.setBackgroundColor(requireContext().getColor(R.color.grey));
            button.getBackground().setAlpha(64);
            button.setPadding(0,0,0,0);
            button.setAllCaps(false);
        }
        linearLayout.addView(button,6,layout);
    }

    private synchronized void loadSpeedsChart() {
        LineChart chart = binding.speedsChart;

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < sessions.size(); i++) {
            values.add(new Entry(i, Float.parseFloat(sessions.get(i).getAvgSpeed()), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24,null)));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, "Speed"); // TODO: shrink set1 from all 3 graph function into 1, and just change values
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

    private synchronized void loadTopSpeedsChart() {
        LineChart chart = binding.topSpeedsChart;

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < sessions.size(); i++) {
            values.add(new Entry(i, Float.parseFloat(sessions.get(i).getTopSpeed()), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24,null)));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, "Speed");

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

    private synchronized void loadDistancesChart() {
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
            set1 = new LineDataSet(values, "Distance");

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

    private synchronized void loadDurationsChart() {
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
            set1 = new LineDataSet(values, "Duration");

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
