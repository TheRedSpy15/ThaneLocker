package com.theredspy15.thanelocker.ui.activitycontrollers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.thanelocker.R;
import com.example.thanelocker.databinding.ActivityBoardBinding;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.models.Session;

import java.util.Objects;

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

        binding.boardContent.deleteBoardButton.setOnClickListener(this::deleteBoard);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle(board.getName());
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    void populateViews(Board board) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(board.getImage(), 0, board.getImage().length);
        binding.imageView.setImageBitmap(bitmap);

        loadSessions();
    }

    void loadSessions() {
        binding.boardContent.sessionsWithBoardLayout.removeAllViews();
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setMargins(0, 20, 0, 20);

        byte sessionCount = 0;
        if (!Session.sessionsWithBoard(board.getId()).isEmpty()) { // TODO: remove redundant code from SessionsFragment. maybe just simplify button creation?
            for (Session session : Session.sessionsWithBoard(board.getId())) {
                sessionCount++;
                Button button = new Button(this);
                button.setText(session.getName());
                button.setTextSize(18);
                button.setBackgroundColor(this.getColor(R.color.grey));
                button.setPadding(50, 50, 50, 50);
                button.setAllCaps(false);
                button.setBackgroundResource(R.drawable.rounded_corners);
                GradientDrawable drawable = (GradientDrawable) button.getBackground();
                drawable.setColor(this.getColor(R.color.grey));
                drawable.setAlpha(64);

                button.setOnClickListener(v -> {
                    Intent myIntent = new Intent(this, SessionActivity.class);
                    myIntent.putExtra("session_id", session.getId());
                    startActivity(myIntent);
                });

                binding.boardContent.sessionsWithBoardLayout.addView(button, layout);

                if (sessionCount == 3) break;
            }
        }
        else { // no sessions
            TextView textView = new TextView(this);
            textView.setText("No Sessions");
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextSize(18);
            binding.boardContent.sessionsWithBoardLayout.addView(textView,layout);
        }
    }

    public void deleteBoard(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Delete Session");
        alertDialog.setMessage("Are you sure?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
                (dialog, which) -> {
                    dialog.dismiss();
                    Board.savedBoards.remove(board.getId());
                    Board.savedBoardIds.remove(Board.savedBoardIds.indexOf(board.getId())); // removing by object doesn't work
                    Board.save();

                    for (Session session : Session.sessionsWithBoard(board.getId()))
                        Session.savedSessions.get(session.getId()).getBoard_ids().remove(session.getBoard_ids().indexOf(board.getId())); // removing by object doesn't work
                    Session.save();

                    Intent myIntent = new Intent(this, MainActivity.class);
                    startActivity(myIntent);
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }
}