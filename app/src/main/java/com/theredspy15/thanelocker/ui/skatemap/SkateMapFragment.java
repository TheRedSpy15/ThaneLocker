package com.theredspy15.thanelocker.ui.skatemap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.thanelocker.databinding.FragmentSkatemapBinding;

public class SkateMapFragment extends Fragment {

    private SkateMapViewModel skatemapViewModel;
    private FragmentSkatemapBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        skatemapViewModel =
                new ViewModelProvider(this).get(SkateMapViewModel.class);

        binding = FragmentSkatemapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
