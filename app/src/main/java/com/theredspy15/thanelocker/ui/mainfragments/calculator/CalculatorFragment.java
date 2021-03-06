package com.theredspy15.thanelocker.ui.mainfragments.calculator;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.FragmentCalculatorBinding;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class CalculatorFragment extends Fragment { // TODO: determine if this should be fragment, and what package to move to

    private FragmentCalculatorBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCalculatorBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseCrashlytics.getInstance().log("displaying calculator fragment");

        binding.calculateButton.setOnClickListener(this::calculate);

        return root;
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

            if (!recommendedShape.equals("")) binding.shapeTextView.setVisibility(View.VISIBLE);
            binding.shapeTextView.setText(getString(R.string.recommended_shape)+recommendedShape);
        } else {
            binding.editTextWeight.getBackground().mutate().setColorFilter(requireActivity().getColor(android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
            MotionToast.Companion.createColorToast(
                    requireActivity(),
                    getString(R.string.hold_up),
                    getString(R.string.need_weight),
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(requireContext(), R.font.roboto)
            );
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
