package com.theredspy15.longboardlife.ui.activitycontrollers;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.DashPathEffect;
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
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.ActivitySessionBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.theredspy15.longboardlife.customviews.PriorityMapView;
import com.theredspy15.longboardlife.models.Board;
import com.theredspy15.longboardlife.models.Session;
import com.theredspy15.longboardlife.models.SessionLocationPoint;
import com.theredspy15.longboardlife.utils.App;
import com.theredspy15.longboardlife.utils.MapThemes;

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
        session = Session.savedSessions.get(getIntent().getIntExtra("session_id", 0));

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
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map = binding.mapView;
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(17.0);
        GeoPoint startPoint = new GeoPoint(40.722429, -99.366040);
        mapController.setCenter(startPoint);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

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
        topSpeedView.setText(session.getTopSpeed()+getString(R.string.mph));

        TextView durationView = binding.sessionLayout.findViewById(R.id.durationView);
        durationView.setText(session.getDuration()+getString(R.string.minutes));

        TextView timeStartView = binding.sessionLayout.findViewById(R.id.timeStartView);
        timeStartView.setText(session.getTime_start());

        TextView timeEndView = binding.sessionLayout.findViewById(R.id.timeEndView);
        timeEndView.setText(session.getTime_end());

        loadTags();
        loadBoardsUsed();
        loadChart();
    }

    private void loadTags() {
        ChipGroup group = binding.sessionLayout.findViewById(R.id.tagGroup);
        group.removeAllViews();

        for (String tag : session.getTags()) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setOnClickListener(v -> {
                chip.setVisibility(View.GONE);
                session.getTags().remove(chip.getText()); // TODO: remove by id
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
        for (int board_id : session.getBoard_ids()) { // TODO: verify board exists, if not delete record of it. same with sessions!
            Board board = Board.savedBoards.get(board_id);
            if (board != null) {
                Button button = new Button(this);
                button.setText(board.getName());
                button.setTextSize(18);
                button.setBackgroundColor(this.getColor(R.color.grey));
                button.getBackground().setAlpha(64);
                button.setPadding(0,0,0,0);
                button.setAllCaps(false);
                button.setOnClickListener(v->{
                    Intent myIntent = new Intent(this, BoardActivity.class);
                    myIntent.putExtra("board_id", board.getId());
                    startActivity(myIntent);
                });
                button.setOnLongClickListener(this::removeUsedBoard);

                if (board.getImage() != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(board.getImage(), 0, board.getImage().length);
                    Drawable drawable = new BitmapDrawable(this.getResources(),Bitmap.createScaledBitmap(bitmap, 400, 400, false));
                    button.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable,null,null,null);
                }

                linearLayout.addView(button,layout);
            }
        }
    }

    public boolean removeUsedBoard(View view) {
        Button button = (Button) view;

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.remove_board_from_session));
        alertDialog.setMessage(getString(R.string.are_you_sure));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.delete),
                (dialog, which) -> {
                    dialog.dismiss();
                    session.getBoard_ids().remove(session.getBoard_ids().indexOf(Board.BoardNameToId((String) button.getText()))); // removing by object doesn't work
                    Session.save();
                    loadBoardsUsed();
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

        return true;
    }

    public void addUsedBoard(View view) {
        String[] boards = new String[Board.savedBoards.size()];
        int i = 0;
        for (int board_id : Board.savedBoardIds) {
            Board board = Board.savedBoards.get(board_id);
            boards[i] = board.getName();
            i+=1;
        }

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(getString(R.string.add_board));
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
        line.setColor(this.getColor(R.color.purple_500));
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
        startMarker.setTextIcon(getString(R.string.start));
        map.getOverlays().add(startMarker);

        // finish marker
        Marker finishMarker = new Marker(map);
        finishMarker.setPosition(pts.get(pts.size()-1));
        finishMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        finishMarker.setTextIcon(getString(R.string.finish));
        map.getOverlays().add(finishMarker);

        // determine theme TODO: day/night support
        map.getOverlayManager().getTilesOverlay().setColorFilter(MapThemes.darkFilter());
    }

    public void deleteSession(View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.delete_session));
        alertDialog.setMessage(getString(R.string.are_you_sure));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.delete),
                (dialog, which) -> {
                    dialog.dismiss();
                    Session.savedSessions.remove(session.getId());
                    Session.savedSessionIds.remove(Session.savedSessionIds.indexOf(session.getId())); // removing by object doesn't work
                    Session.save();
                    Intent myIntent = new Intent(this, MainActivity.class);
                    startActivity(myIntent);
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    public void addTag(View view) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.add_tag);
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

    private void loadChart() {
        LineChart chart = binding.sessionLayout.findViewById(R.id.speedChart);

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < session.getLocations().size(); i++) {
            values.add(new Entry(i, session.getLocations().get(i).getSpeed(), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24,null)));
        }

        LineDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set1.notifyDataSetChanged();
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, getString(R.string.speed));

            set1.setDrawIcons(false);

            // draw dashed line
            set1.enableDashedLine(10f, 5f, 0f);

            // black lines and points
            set1.setColor(this.getColor(R.color.purple_500));
            set1.setCircleColor(this.getColor(R.color.purple_500));

            // line thickness and point size
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);

            // draw points as solid circles
            set1.setDrawCircleHole(false);

            // customize legend entry
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            // text size of values
            set1.setValueTextSize(9f);

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f);

            // set the filled area
            set1.setDrawFilled(true);
            set1.setFillFormatter((dataSet, dataProvider) -> chart.getAxisLeft().getAxisMinimum());
            set1.setFillColor(this.getColor(R.color.purple_500));

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            chart.setData(data);
            chart.animateX(3000);

            Description description = new Description();
            description.setText("");
            chart.setDescription(description);

            int color = App.getThemeTextColor(this);
            chart.getData().setValueTextColor(color);
            chart.getData().setValueTextColor(color);
            chart.getXAxis().setTextColor(color);
            chart.getAxisLeft().setTextColor(color);
            chart.getAxisRight().setTextColor(color);
            chart.getLegend().setTextColor(color);
        }
    }

}
