package com.theredspy15.longboardlife.ui.activitycontrollers;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.ActivityAchievementsBinding;
import com.theredspy15.longboardlife.models.Achievement;

import java.util.ArrayList;
import java.util.Collections;

public class AchievementsActivity extends AppCompatActivity {

    ActivityAchievementsBinding binding;

    ArrayList<Achievement> achievements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        binding = ActivityAchievementsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        achievements = (ArrayList<Achievement>) getIntent().getSerializableExtra("achievements");

        Thread achievementThread = new Thread(this::loadAchievements);
        achievementThread.start();
    }

    void loadAchievements() {
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setMargins(0, 20, 0, 20);

        if (achievements != null) {
            Collections.reverse(achievements);
            for (Achievement achievement : achievements) {
                if (achievement != null) {
                    TextView text = new TextView(this);
                    text.setText(achievement.getDescription()+" (XP: "+achievement.getXp()+")");
                    text.setTextSize(18);
                    text.setPadding(50, 50, 50, 50);
                    text.setAllCaps(false);
                    text.setBackgroundResource(R.drawable.rounded_corners);
                    text.setBackgroundColor(this.getColor(R.color.grey));
                    text.getBackground().setAlpha(64);

                    this.runOnUiThread(()->binding.achievementLayout.addView(text, layout));
                }
            }
            Collections.reverse(achievements);

            if (achievements.isEmpty()) { // no achievements
                TextView textView = new TextView(this);
                textView.setText("No Achievements");
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setTextSize(18);
                this.runOnUiThread(() -> binding.achievementLayout.addView(textView, layout));
            }
        }
    }
}