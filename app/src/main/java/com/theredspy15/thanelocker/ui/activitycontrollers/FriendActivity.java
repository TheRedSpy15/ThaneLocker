package com.theredspy15.thanelocker.ui.activitycontrollers;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import com.example.longboardlife.R;
import com.example.longboardlife.databinding.ActivityFriendBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.models.Image;
import com.theredspy15.thanelocker.models.Profile;
import com.theredspy15.thanelocker.models.Session;
import com.theredspy15.thanelocker.utils.App;
import java.util.ArrayList;

public class FriendActivity extends AppCompatActivity {

  ActivityFriendBinding binding;

  private final ArrayList<Session> sessions = new ArrayList<>();
  private ArrayList<Board> boards; // TODO: implement with selling update
  private Profile friend;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_friend);

    binding = ActivityFriendBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    friend = (Profile)getIntent().getSerializableExtra("friend");
    sessionsWithProfile(friend);
  }

  public void sessionsWithProfile(Profile profile) {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Create a reference to the cities collection
    CollectionReference sessionsRef = db.collection("sessions");

    // Create a query against the collection.
    Query query = sessionsRef.whereEqualTo("user_id", profile.getId());
    query.get().addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        for (QueryDocumentSnapshot document : task.getResult()) {
          Session session = document.toObject(Session.class);
          sessions.add(session);
        }
        loadAllData();
      }
    });
  }

  private void loadAllData() { // TODO: multi-thread this
    if (friend.getName() != null)
      binding.nameText.setText(friend.getName());
    if (friend.getDescription() != null)
      binding.descriptionView.setText(friend.getDescription());

    if (friend.getImage() != null) {
      Bitmap bitmap = BitmapFactory.decodeByteArray(
          Image.convertImageStringToBytes(
              Profile.localProfile.getImage().getData()),
          0,
          Image
              .convertImageStringToBytes(
                  Profile.localProfile.getImage().getData())
              .length);
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
    binding.xpView.setProgress((int)friend.getLevel_xp());
    binding.levelView.setText(getString(R.string.level) + " " +
                              friend.getLevel());
  }

  private void loadTextViewStats() {
    float tDistance = 0;
    float tAvgDistance = 0;
    float tAvgSpeed = 0;

    Resources resources = getResources();

    for (Session session : sessions) {
      // fastest speed
      float top = 0;
      if (Float.parseFloat(session.getTopSpeed()) > top)
        top = Float.parseFloat(session.getTopSpeed());
      binding.topSpeedView.setText(App.getSpeedFormatted(top, resources));

      // furthest distance
      top = 0;
      if (Float.parseFloat(session.getTotalDistance()) > top)
        top = Float.parseFloat(session.getTotalDistance());
      binding.furthestDistanceView.setText(
          App.getDistanceFormatted(top, resources));

      tDistance += Float.parseFloat(session.getTotalDistance());
      tAvgDistance += Float.parseFloat(session.getTotalDistance());
      tAvgSpeed += Float.parseFloat(session.getAvgSpeed());
    }
    tAvgSpeed = tAvgSpeed / sessions.size();
    tAvgDistance = tAvgDistance / sessions.size();

    binding.totalDistanceView.setText(
        App.getDistanceFormatted(tDistance, resources));
    binding.avgDistanceView.setText(
        App.getDistanceFormatted(tAvgDistance, resources));
    binding.avgSpeedView.setText(App.getSpeedFormatted(tAvgSpeed, resources));
  }

  private void loadFavoriteBoard() {
    LinearLayout linearLayout = binding.statisticsLayout;
    LinearLayout.LayoutParams layout =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                      ViewGroup.LayoutParams.MATCH_PARENT);
    layout.setMargins(80, 0, 80, 0);

    Board board = Profile.favoriteBoard();

    Button button = new Button(this);
    if (board != null) {
      button.setText(board.getName());
      button.setTextSize(18);
      button.setBackgroundColor(this.getColor(R.color.grey));
      button.getBackground().setAlpha(64);
      button.setPadding(0, 0, 0, 0);
      button.setAllCaps(false);
      button.setOnClickListener(v -> {
        Intent myIntent = new Intent(this, BoardActivity.class);
        myIntent.putExtra("board_id", board.getId());
        startActivity(myIntent);
      });

      if (board.getImage() != null) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(
            Image.convertImageStringToBytes(board.getImage().getData()), 0,
            Image.convertImageStringToBytes(board.getImage().getData()).length);
        Drawable drawable = new BitmapDrawable(
            this.getResources(),
            Bitmap.createScaledBitmap(bitmap, 400, 400, false));
        button.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null,
                                                               null, null);
      }

    } else {
      button.setText(R.string.no_favorite);
      button.setTextSize(18);
      button.setBackgroundColor(this.getColor(R.color.grey));
      button.getBackground().setAlpha(64);
      button.setPadding(0, 0, 0, 0);
      button.setAllCaps(false);
    }
    linearLayout.addView(button, 8, layout);
  }

  public void loadAchievements(View view) {
    Intent myIntent = new Intent(this, AchievementsActivity.class);
    myIntent.putExtra("achievements", friend.getAchievements());
    startActivity(myIntent);
  }

  private void loadSpeedsChart() {
    LineChart chart = binding.speedsChart;

    ArrayList<Entry> values = new ArrayList<>();

    for (int i = 0; i < sessions.size(); i++) {
      values.add(new Entry(
          i, Float.parseFloat(sessions.get(i).getAvgSpeed()),
          ResourcesCompat.getDrawable(
              getResources(), R.drawable.ic_baseline_location_on_24, null)));
    }

    // create a data object with the data sets
    LineData data = App.createLineSet(values, getString(R.string.speed), this);

    // set data
    chart.setData(data);
    chart.animateX(3000);

    // coloring chart
    Description description = new Description();
    description.setText("");
    chart.setDescription(description);
    int color = App.getThemeTextColor(this);
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
      values.add(new Entry(
          i, Float.parseFloat(sessions.get(i).getTopSpeed()),
          ResourcesCompat.getDrawable(
              getResources(), R.drawable.ic_baseline_location_on_24, null)));
    }

    // create a data object with the data sets
    LineData data = App.createLineSet(values, getString(R.string.speed), this);

    // set data
    chart.setData(data);
    chart.animateX(3000);

    // coloring chart
    Description description = new Description();
    description.setText("");
    chart.setDescription(description);
    int color = App.getThemeTextColor(this);
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
      values.add(new Entry(
          i, Float.parseFloat(sessions.get(i).getTotalDistance()),
          ResourcesCompat.getDrawable(
              getResources(), R.drawable.ic_baseline_location_on_24, null)));
    }

    // create a data object with the data sets
    LineData data =
        App.createLineSet(values, getString(R.string.distances), this);

    // set data
    chart.setData(data);
    chart.animateX(3000);

    // coloring chart
    Description description = new Description();
    description.setText("");
    chart.setDescription(description);
    int color = App.getThemeTextColor(this);
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
      values.add(new Entry(
          i, sessions.get(i).getDuration(),
          ResourcesCompat.getDrawable(
              getResources(), R.drawable.ic_baseline_location_on_24, null)));
    }

    // create a data object with the data sets
    LineData data =
        App.createLineSet(values, getString(R.string.durations), this);

    // set data
    chart.setData(data);
    chart.animateX(3000);

    // coloring chart
    Description description = new Description();
    description.setText("");
    chart.setDescription(description);
    int color = App.getThemeTextColor(this);
    chart.getData().setValueTextColor(color);
    chart.getData().setValueTextColor(color);
    chart.getXAxis().setTextColor(color);
    chart.getAxisLeft().setTextColor(color);
    chart.getAxisRight().setTextColor(color);
    chart.getLegend().setTextColor(color);
  }
}