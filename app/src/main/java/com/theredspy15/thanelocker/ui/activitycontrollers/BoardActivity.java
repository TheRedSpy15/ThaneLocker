package com.theredspy15.thanelocker.ui.activitycontrollers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.longboardlife.BuildConfig;
import com.example.longboardlife.R;
import com.example.longboardlife.databinding.ActivityBoardBinding;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.models.Session;
import com.theredspy15.thanelocker.utils.App;

import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;

public class BoardActivity extends AppCompatActivity {

    private ActivityBoardBinding binding;

    Board board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseCrashlytics.getInstance().log("opening board activity");

        board = Board.savedBoards.get(getIntent().getIntExtra("board_id", 0));
        if (board != null) populateViews(board);

        binding.boardContent.deleteBoardButton.setOnClickListener(this::deleteBoard);
        binding.boardContent.editButton.setOnClickListener(this::editBoard);

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle(board.getName());
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (!MainActivity.preferences.getBoolean("subscribe",false)) loadAdData();
    }

    @Override
    public void onStop() {
        super.onStop();

        Session.save();
        Board.save();
    }

    private void loadAdData() {
        FirebaseCrashlytics.getInstance().log("Loading advertisement");

        String unitId;
        if (BuildConfig.DEBUG) {
            unitId = "ca-app-pub-3940256099942544/6300978111";
        } else unitId = "ca-app-pub-5128547878021429/7644000468"; // production only!

        MobileAds.initialize(this, initializationStatus -> { });
        AdRequest adRequest = new AdRequest.Builder().build();
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(unitId);
        binding.boardContent.layoutforcontent.addView(adView,2);
        adView.loadAd(adRequest);
    }

    void populateViews(Board board) {
        if (board.getImage() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(board.getImage(), 0, board.getImage().length);
            Glide.with(this).load(bitmap).centerCrop().into(binding.imageView);
        } else {
            binding.imageView.setVisibility(View.GONE);
        }

        if (! board.isAdvanceMode()) binding.boardContent.advanceTable.setVisibility(View.GONE);

        binding.boardContent.descriptionView.setText(board.getDescription());
        binding.boardContent.deckView.setText(board.getDeck());
        binding.boardContent.gripView.setText(board.getGripTp());
        binding.boardContent.trucksView.setText(board.getTrucks());
        binding.boardContent.angleFView.setText(board.getFrontAngle()+"\u00B0");
        binding.boardContent.angleRView.setText(board.getRearAngle()+"\u00B0");
        binding.boardContent.bushingsBdView.setText(board.getBd_bushings());
        binding.boardContent.bushingsRdView.setText(board.getRd_bushing());
        binding.boardContent.pivotView.setText(board.getPivot());
        binding.boardContent.wheelsView.setText(board.getWheels());
        binding.boardContent.bearingsView.setText(board.getBearings());

        if (!Session.sessionsWithBoard(board.getId()).isEmpty()) {
            binding.boardContent.totalDistanceView.setText(App.getDistanceFormatted(board.totalDistance(),getResources()));
            binding.boardContent.avgDistanceView.setText(App.getDistanceFormatted(board.avgDistance(),getResources()));
            binding.boardContent.longestDistanceView.setText(App.getDistanceFormatted(board.furthestDistance(),getResources()));
            binding.boardContent.topSpeedView.setText(App.getSpeedFormatted(board.fastestSpeed(),getResources()));
            binding.boardContent.avgSpeedView.setText(App.getSpeedFormatted(board.avgSpeed(),getResources()));
        }

        loadSessions();
    }

    void editBoard(View view) {
        Intent myIntent = new Intent(this, NewBoardActivity.class);
        myIntent.putExtra("board", board);
        startActivity(myIntent);
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
                drawable.setAlpha(30);

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
            textView.setText(R.string.no_sessions);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextSize(18);
            binding.boardContent.sessionsWithBoardLayout.addView(textView,layout);
        }
    }

    public void deleteBoard(View view) { // TODO: stop duplicating this with deletion in board fragment
        MaterialDialog mDialog = new MaterialDialog.Builder(this)
                .setTitle(getString(R.string.delete_board))
                .setAnimation("58413-delete-icon-animation.json")
                .setMessage(getString(R.string.are_you_sure))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.delete), (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                    Board.savedBoards.remove(board.getId());
                    Board.savedBoardIds.remove((Integer) board.getId());

                    for (Session session : Session.sessionsWithBoard(board.getId()))
                        Session.savedSessions.get(session.getId()).getBoard_ids().remove(session.getBoard_ids().indexOf(board.getId()));

                    Intent myIntent = new Intent(this, MainActivity.class);
                    startActivity(myIntent);
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> dialogInterface.dismiss())
                .build();
        mDialog.getAnimationView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mDialog.show();
    }
}