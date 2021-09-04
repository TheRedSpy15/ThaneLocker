package com.theredspy15.thanelocker.ui.activitycontrollers;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.example.thanelocker.R;
import com.example.thanelocker.databinding.ActivitySessionBinding;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.theredspy15.thanelocker.customviews.PriorityMapView;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.models.Session;
import com.theredspy15.thanelocker.models.SessionLocationPoint;
import com.theredspy15.thanelocker.utils.App;
import com.theredspy15.thanelocker.utils.MapThemes;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class SessionActivity extends AppCompatActivity {

    private ActivitySessionBinding binding;
    private PriorityMapView map = null;
    IMapController mapController;

    Session session = new Session();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        // doesn't count as serializable like Board objects do
        session = Session.savedSessions.get(getIntent().getShortExtra("session_id", (short) 0));

        binding = ActivitySessionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // setup toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle(session.getName().split(String.valueOf(Pattern.compile("\\((.*)")))[0]);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // set on clicks
        binding.sessionLayout.findViewById(R.id.deleteSessionButton).setOnClickListener(this::deleteSession);
        binding.sessionLayout.findViewById(R.id.addBoardUsedButton).setOnClickListener(this::addUsedBoard);
        binding.fullscreenButton.setOnClickListener(this::toggleFullscreen);

        // setup map
        Context ctx = this;
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map = binding.mapView;
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(17.0);
        GeoPoint startPoint = new GeoPoint(40.722429, -99.366040);
        mapController.setCenter(startPoint);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.getOverlayManager().getTilesOverlay().setColorFilter(MapThemes.darkFilter());

        loadPoints();
        try {
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"DefaultLocale"})
    void loadData() throws IOException {
        // stats
        TextView avgSpeedView = binding.sessionLayout.findViewById(R.id.textViewAvgSpeed);
        avgSpeedView.setText(session.getAvgSpeed());

        TextView totalDistanceView = binding.sessionLayout.findViewById(R.id.textViewTotalDistance);
        totalDistanceView.setText(session.getTotalDistance());

        TextView cityView = binding.sessionLayout.findViewById(R.id.cityView);
        cityView.setText(session.getCityName(this));

        TextView topSpeedView = binding.sessionLayout.findViewById(R.id.topSpeedView);
        topSpeedView.setText(session.getTopSpeed()+" MPH");

        TextView durationView = binding.sessionLayout.findViewById(R.id.durationView);
        durationView.setText(session.getDuration()+" Minutes");

        TextView timeStartView = binding.sessionLayout.findViewById(R.id.timeStartView);
        timeStartView.setText(session.getTime_start());

        TextView timeEndView = binding.sessionLayout.findViewById(R.id.timeEndView);
        timeEndView.setText(session.getTime_end());

        loadTags();
        loadBoardsUsed();
    }

    private void loadTags() {
        ChipGroup group = binding.sessionLayout.findViewById(R.id.tagGroup);
        group.removeAllViews();

        for (String tag : session.getTags()) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setOnClickListener(v -> {
                chip.setVisibility(View.GONE);
                session.getTags().remove(chip.getText());
                Objects.requireNonNull(Session.savedSessions.get(session.getId())).setTags(session.getTags());
                Session.save();
            });
            group.addView(chip);
        }

        Chip addChip = new Chip(this);
        addChip.setText("+");
        addChip.setOnClickListener(this::addTag);
        group.addView(addChip);
    }

    @SuppressLint("ResourceType")
    public void toggleFullscreen(View view) {
        view = binding.appBar;
        ValueAnimator slideAnimator;

        // default size
        if (App.convertPixelsToDp(view.getHeight(),this) == 350.0) {
            slideAnimator = ValueAnimator
                    .ofInt(view.getHeight(), LinearLayout.LayoutParams.MATCH_PARENT)
                    .setDuration(500);
        } else { // likely fullscreen
            slideAnimator = ValueAnimator
                    .ofInt(view.getHeight(), (int) App.convertDpToPixel(350,this))
                    .setDuration(500);
        }

        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */

        View finalView = view;
        slideAnimator.addUpdateListener(animation1 -> {
            finalView.getLayoutParams().height = (Integer) animation1.getAnimatedValue();
            finalView.requestLayout();
        });

        /*  We use an animationSet to play the animation  */

        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start();

        binding.fullscreenButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_fullscreen_exit_24, 0, 0, 0);
    }

    public void loadBoardsUsed() {
        LinearLayout linearLayout = binding.sessionLayout.findViewById(R.id.session_board_layout);
        linearLayout.removeAllViews();
        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setMargins(0,0,0,50);
        for (short board_id : session.getBoard_ids()) {
            Board board = Board.savedBoards.get(board_id);
            Button button = new Button(this);
            button.setText(board.getName());
            button.setTextSize(18);
            button.setBackgroundColor(getResources().getColor(R.color.grey));
            button.getBackground().setAlpha(64);
            button.setPadding(0,0,0,0);
            button.setAllCaps(false);
            button.setOnClickListener(v->{
                Intent myIntent = new Intent(this, BoardActivity.class);
                myIntent.putExtra("board", board);
                startActivity(myIntent);
            });
            button.setOnLongClickListener(this::removeUsedBoard);
            Bitmap bitmap = BitmapFactory.decodeByteArray(board.getImage(), 0, board.getImage().length);
            Drawable drawable = new BitmapDrawable(this.getResources(),Bitmap.createScaledBitmap(bitmap, 400, 400, false));
            button.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable,null,null,null);
            linearLayout.addView(button,layout);
        }
    }

    public boolean removeUsedBoard(View view) {
        Button button = (Button) view;

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Remove board from session");
        alertDialog.setMessage("Are you sure?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
                (dialog, which) -> {
                    dialog.dismiss();
                    session.getBoard_ids().remove(Board.BoardNameToId((String) button.getText())); // removing by object doesn't work
                    Session.save();
                    loadBoardsUsed();
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

        return true;
    }

    public void addUsedBoard(View view) {
        String[] boards = new String[Board.savedBoards.size()];
        int i = 0;
        for (short board_id : Board.savedBoardIds) {
            Board board = Board.savedBoards.get(board_id);
            boards[i] = board.getName();
            i+=1;
        }

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Add Board");
        b.setItems(boards, (dialog, which) -> {
            dialog.dismiss();
            session.getBoard_ids().add(Board.BoardNameToId(boards[which]));
            Session.save();

            loadBoardsUsed();
        });
        b.show();
    }

    void loadPoints() {
        Polyline line = new Polyline(map);
        line.setWidth(20f);
        line.setColor(getResources().getColor(R.color.purple_500));
        List<GeoPoint> pts = new ArrayList<>();

        for (SessionLocationPoint point : session.getLocations()) {
            pts.add(new GeoPoint(point.getLatitude(), point.getLongitude()));
        }

        map.getOverlays().add(line);
        line.setPoints(pts);
        line.setOnClickListener(null); // TODO: maybe to see speed at each point?
        mapController.setCenter(pts.get(0));

        // start marker
        Marker startMarker = new Marker(map);
        startMarker.setPosition(pts.get(0));
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTextIcon("Start");
        map.getOverlays().add(startMarker);

        // finish marker
        Marker finishMarker = new Marker(map);
        finishMarker.setPosition(pts.get(pts.size()-1));
        finishMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        finishMarker.setTextIcon("Finish");
        map.getOverlays().add(finishMarker);
    }

    public void deleteSession(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Delete Session");
        alertDialog.setMessage("Are you sure?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
                (dialog, which) -> {
                    dialog.dismiss();
                    Session.savedSessions.remove(session.getId());
                    Session.savedSessionIds.remove(session.getId()); // removing by object doesn't work
                    Session.save();
                    Intent myIntent = new Intent(this, MainActivity.class);
                    startActivity(myIntent);
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    public void addTag(View view) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Add Tag");
        String[] types = getResources().getStringArray(R.array.tags);
        b.setItems(types, (dialog, which) -> {
            dialog.dismiss();
            session.getTags().add(types[which]);
            Session.savedSessions.get(session.getId()).setTags(session.getTags());
            Session.save();

            loadTags();
        });
        b.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

}
