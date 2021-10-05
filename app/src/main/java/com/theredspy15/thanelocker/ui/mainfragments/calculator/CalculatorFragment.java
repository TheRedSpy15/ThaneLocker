package com.theredspy15.thanelocker.ui.mainfragments.calculator;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.longboardlife.BuildConfig;
import com.example.longboardlife.R;
import com.example.longboardlife.databinding.FragmentCalculatorBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

public class CalculatorFragment extends Fragment { // TODO: determine if this should be fragment, and what package to move to

    private FragmentCalculatorBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCalculatorBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.calculateButton.setOnClickListener(this::calculate);

        loadAdData();

        return root;
    }

    private void loadAdData() {
        String unitId;
        if (BuildConfig.BUILD_TYPE.contentEquals("debug")) {
            unitId = "ca-app-pub-3940256099942544/6300978111";
        } else unitId = "ca-app-pub-5128547878021429/6255236482"; // production only!

        MobileAds.initialize(requireContext(), initializationStatus -> { });
        AdRequest adRequest = new AdRequest.Builder().build();
        AdView adView = new AdView(requireContext());
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(unitId);
        binding.calculatorLayout.addView(adView,9);
        adView.loadAd(adRequest);
    }

    public void calculate(View view) {
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setMargins(30, 20, 30, 20);

        if (!TextUtils.isEmpty(binding.editTextWeight.getText().toString())) { // TODO: reset color
            int weight = (int) (Integer.parseInt(binding.editTextWeight.getText().toString())/2.2046226218);

            String duro = weightToDuro(weight,false);
            binding.recommendedTextView.setText(getString(R.string.recommended_durometer)+duro);
            binding.recommendedTextView.setVisibility(View.VISIBLE);
            binding.basedOnTextView.setVisibility(View.VISIBLE);
            binding.tipTextView.setVisibility(View.VISIBLE);

            // radio buttons (shape)
            String recommendedShape = "";
            if (binding.radioButtonCruise.isChecked()) recommendedShape = getString(R.string.cruise_shape);
            else if (binding.radioButtonFreeride.isChecked()) recommendedShape = getString(R.string.freeride_shape);

            if (!recommendedShape.equals("")) { // TODO: fix not changing after already being calculated with different radio button
                binding.shapeTextView.setText(getString(R.string.recommended_shape)+recommendedShape);
                binding.shapeTextView.setVisibility(View.VISIBLE);
            }
        } else {
            binding.editTextWeight.getBackground().mutate().setColorFilter(requireActivity().getColor(android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);

            Snackbar.make(view, R.string.need_weight, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    // based on: https://github.com/Widdershin/BushingPicker/blob/master/bushings.py
    private String weightToDuro(int weight, boolean boardside) {
        ArrayList<Integer> bushings = new ArrayList<>();
        bushings.add(78);
        bushings.add(81);
        bushings.add(85);
        bushings.add(87);
        bushings.add(90);
        bushings.add(93);
        bushings.add(95);
        bushings.add(97);

        if (!boardside)
            weight -= 5;

        weight -= 40;
        if (weight < 0)
            weight = 0;
        double approx_duro = (Math.pow(weight,0.66)+79);

        Optional<Integer> duro = bushings.stream()
                .min(Comparator.comparingDouble(i -> Math.abs(i - approx_duro)));

        return duro.get() + "a";
    }
}
