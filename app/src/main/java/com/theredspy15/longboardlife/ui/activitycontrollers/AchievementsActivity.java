package com.theredspy15.longboardlife.ui.activitycontrollers;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.ActivityAchievementsBinding;
import com.theredspy15.longboardlife.models.Achievement;
import com.theredspy15.longboardlife.utils.App;

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
                    text.setTextSize(18);
                    text.setPadding(50, 50, 50, 50);
                    text.setAllCaps(false);
                    text.setBackgroundResource(R.drawable.rounded_corners);
                    text.setBackgroundColor(this.getColor(R.color.grey));
                    text.getBackground().setAlpha(64);
                    text.setLineSpacing(App.convertDpToPixel(5,this),1);

                    text.setBackgroundResource(R.drawable.rounded_corners);
                    GradientDrawable drawable = (GradientDrawable) text.getBackground();
                    drawable.setColor(this.getColor(R.color.grey));
                    drawable.setAlpha(64);

                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    int color;
                    switch (achievement.getRarity()){
                        case LEGENDARY: color = this.getColor(R.color.legendary); break;
                        case RARE: color = this.getColor(R.color.rare); break;
                        default: color = App.getThemeTextColor(this);
                    }

                    builder.append(achievement.getDescription()).append("\n");

                    SpannableString str2= new SpannableString(""+achievement.getRarity());
                    str2.setSpan(new ForegroundColorSpan(color), 0, str2.length(), 0);
                    builder.append(str2);

                    builder.append(" (XP:").append(String.valueOf(achievement.getXp())).append(")");

                    text.setText(builder, TextView.BufferType.SPANNABLE);

                    this.runOnUiThread(()-> {
                        binding.achievementLayout.addView(text, layout);

                        //ObjectAnimator animation = ObjectAnimator.ofFloat(text, View.X, View.Y);
                        //animation.setDuration(2000);
                        //animation.start();
                    });
                }
            }
            Collections.reverse(achievements);

            if (achievements.isEmpty()) { // no achievements
                TextView textView = new TextView(this);
                textView.setText(R.string.no_achievement);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setTextSize(18);
                this.runOnUiThread(() -> binding.achievementLayout.addView(textView, layout));
            }
        }
    }
}