package com.theredspy15.thanelocker.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.longboardlife.R;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.AppIntroPageTransformerType;

public class MyIntro extends AppIntro {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance("Welcome!",
                "This is a demo example in java of AppIntro library, with a custom background on each slide!",
                R.drawable.ic_baseline_friends_24));

        addSlide(AppIntroFragment.newInstance(
                "Clean App Intros",
                "This library offers developers the ability to add clean app intros at the start of their apps.",
                R.drawable.ic_baseline_article_24
        ));

        addSlide(AppIntroFragment.newInstance(
                "Simple, yet Customizable",
                "The library offers a lot of customization, while keeping it simple for those that like simple.",
                R.drawable.ic_baseline_code_24
        ));

        addSlide(AppIntroFragment.newInstance(
                "Explore",
                "Feel free to explore the rest of the library demo!",
                R.drawable.ic_baseline_trophy_24
        ));
    }

    @Override
    protected void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }

    @Override
    protected void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }
}