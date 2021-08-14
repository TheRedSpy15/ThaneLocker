package com.theredspy15.thanelocker.ui.boards;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.thanelocker.databinding.FragmentBoardsBinding;
import com.theredspy15.thanelocker.Board;
import com.theredspy15.thanelocker.MainActivity;
import com.theredspy15.thanelocker.NewBoardActivity;

public class BoardsFragment extends Fragment {

    private BoardsViewModel boardsViewModel;
    private FragmentBoardsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        boardsViewModel =
                new ViewModelProvider(this).get(BoardsViewModel.class);

        binding = FragmentBoardsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadBoards();

        final TextView textView = binding.textBoards;
        boardsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }
    
    public void loadBoards() {
        if (MainActivity.savedBoards != null) {
            for (Board board: MainActivity.savedBoards) {
                Button button = new Button(getContext());
                button.setText(board.getName());
                binding.boardLayout.addView(button);
            }
        }

        // create new button
        Button button = new Button(getContext());
        button.setText("Add Board");
        button.setOnClickListener(this::loadCreateBoard);
        binding.boardLayout.addView(button);
    }

    public void loadCreateBoard(View view) {
        Intent myIntent = new Intent(getContext(), NewBoardActivity.class);
        startActivity(myIntent);
    }
            
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}