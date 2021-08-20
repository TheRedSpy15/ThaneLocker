package com.theredspy15.thanelocker.ui.activitycontrollers;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.thanelocker.R;
import com.example.thanelocker.databinding.ActivityBoardBinding;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.theredspy15.thanelocker.models.Board;

public class BoardActivity extends AppCompatActivity {

    private ActivityBoardBinding binding;

    Board board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        board = (Board) getIntent().getSerializableExtra("board");
        populateViews(board);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle(getTitle());

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }

    void populateViews(Board board) {
        binding.imageView.setImageBitmap(board.getImage());
        TextView textView = binding.getRoot().findViewById(R.id.thisdingus);
        textView.setText(board.getDescription());
    }
}