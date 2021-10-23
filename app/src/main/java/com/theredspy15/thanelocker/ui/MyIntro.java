package com.theredspy15.thanelocker.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.longboardlife.R;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.AppIntroPageTransformerType;
import com.github.appintro.SlideBackgroundColorHolder;

public class MyIntro extends AppIntro implements SlideBackgroundColorHolder {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.thank_you_title),
                getString(R.string.thank_you_content),
                R.drawable.heart,
                getColor(R.color.purple_500)
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.page_two_title),
                getString(R.string.page_two_sum),
                R.drawable.board,
                getColor(R.color.purple_200)
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.page_three_title),
                getString(R.string.page_three_sum),
                R.drawable.profile,
                getColor(R.color.rare)
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.page_four_title),
                getString(R.string.page_four_sum),
                R.drawable.mountain,
                getColor(R.color.purple_500)
        ));

        setTransformer(new AppIntroPageTransformerType.Parallax(-1.0,-1.0,2.0));
        setColorTransitionsEnabled(true);
        setImmersive(true);
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

    @Override
    public int getDefaultBackgroundColor() {
        return 0;
    }

    @Override
    public void setBackgroundColor(int i) {

    }
}