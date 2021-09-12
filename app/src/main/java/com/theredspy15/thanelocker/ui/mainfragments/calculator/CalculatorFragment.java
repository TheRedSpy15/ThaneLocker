package com.theredspy15.thanelocker.ui.mainfragments.calculator;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.thanelocker.databinding.FragmentCalculatorBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Optional;

public class CalculatorFragment extends Fragment { // TODO: determine if this should be fragment, and what package to move to

    private CalculatorViewModel calculatorViewModel;
    private FragmentCalculatorBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        calculatorViewModel =
                new ViewModelProvider(this).get(CalculatorViewModel.class);

        binding = FragmentCalculatorBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.calculateButton.setOnClickListener(this::calculate);

        return root;
    }

    public void calculate(View view) {
        if (!TextUtils.isEmpty(binding.editTextWeight.getText().toString())) { // TODO: reset color
            int weight = (int) (Integer.parseInt(binding.editTextWeight.getText().toString())/2.2046226218);

            String duro = weightToDuro(weight,true);
            binding.recommendedTextView.setText(duro);
            binding.recommendedTextView.setVisibility(View.VISIBLE);
        } else {
            binding.editTextWeight.getBackground().mutate().setColorFilter(requireActivity().getColor(android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);

            Snackbar.make(view, "Please input a weight", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private String weightToDuro(int weight, boolean boardside) {
        LinkedList<Integer> bushings = new LinkedList<>();
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

        return String.valueOf(duro.get() + "a");
    }
}
