package com.theredspy15.thanelocker.ui.activitycontrollers;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import com.example.longboardlife.BuildConfig;
import com.example.longboardlife.R;
import com.example.longboardlife.databinding.ActivitySessionBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;
import com.theredspy15.thanelocker.customviews.PriorityMapView;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.models.Elevation;
import com.theredspy15.thanelocker.models.Session;
import com.theredspy15.thanelocker.models.SessionLocationPoint;
import com.theredspy15.thanelocker.utils.App;
import com.theredspy15.thanelocker.utils.MapThemes;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

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

        if (MainActivity.preferences.getBoolean("satellite",false) && MainActivity.preferences.getBoolean("subscribe",false)) {
            BingMapTileSource.retrieveBingKey(this);
            String m_locale = Locale.getDefault().getDisplayName();
            BingMapTileSource bing = new BingMapTileSource(m_locale);
            bing.setStyle(BingMapTileSource.IMAGERYSET_AERIAL);
            map.setTileSource(bing);
        } else {
            // determine theme for map
            int nightModeFlags = this.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES)
                map.getOverlayManager().getTilesOverlay().setColorFilter(MapThemes.darkFilter());
        }

        loadPoints();
        loadData();
        if (!MainActivity.preferences.getBoolean("subscribe",false)) loadAdData();
    }

    @Override
    public void onStop() {
        super.onStop();

        Session.save();
    }

    private void loadAdData() {
        String unitId;
        if (BuildConfig.DEBUG) {
            unitId = "ca-app-pub-3940256099942544/6300978111";
        } else unitId = "ca-app-pub-5128547878021429/7473799446"; // production only!

        MobileAds.initialize(this, initializationStatus -> { });
        AdRequest adRequest = new AdRequest.Builder().build();
        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(unitId);
        LinearLayout layout = binding.sessionLayout.findViewById(R.id.session_content_layout);
        layout.addView(adView,2);
        adView.loadAd(adRequest);
    }

    void loadData() {
        // stats
        TickerView avgSpeedView = binding.sessionLayout.findViewById(R.id.textViewAvgSpeed);
        avgSpeedView.setCharacterLists(TickerUtils.provideNumberList());
        avgSpeedView.setText(session.getAvgSpeed());

        TickerView totalDistanceView = binding.sessionLayout.findViewById(R.id.textViewTotalDistance);
        totalDistanceView.setCharacterLists(TickerUtils.provideNumberList());
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
        loadSpeedChart();

        if (MainActivity.preferences.getBoolean("downhillrider",false)) loadElevationData();
        else binding.sessionLayout.findViewById(R.id.elevationChart).setVisibility(View.GONE);
    }

    private void loadElevationData() {
        if (new Elevation().isApiAvailable(this) && session.getElevationPoints().isEmpty()) { // getting data if not already present
            Thread thread = new Thread(()-> {
                try {
                    session.setElevationPoints(Elevation.getElevations(session.getLocations()));
                } catch (IOException e) {
                    e.printStackTrace();
                    MotionToast.Companion.createColorToast(
                            this,
                            getString(R.string.no_elevation_failed),
                            getString(R.string.failed_elevation_server),
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this, R.font.roboto)
                    );
                    session.getElevationPoints().clear();
                }

                if (!session.getElevationPoints().isEmpty()) {
                    runOnUiThread(this::loadElevationChart);
                }
            });
            thread.start();
        } else loadElevationChart();
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
            });
            group.addView(chip);
        }

        Chip addChip = new Chip(this);
        addChip.setText("+");
        addChip.setOnClickListener(this::addTag);
        group.addView(addChip);
    }

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

        for (int board_id : session.getBoard_ids()) {
            Board board = Board.savedBoards.get(board_id);
            if (board != null) {
                Button button = new Button(this);
                button.setText(board.getName());
                button.setTextSize(18);
                button.setBackgroundColor(this.getColor(R.color.grey));
                button.getBackground().setAlpha(30);
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
            } else {
                session.getBoard_ids().remove((Integer) board_id);
            }
        }
    }

    public boolean removeUsedBoard(View view) {
        Button button = (Button) view;

        MaterialDialog mDialog = new MaterialDialog.Builder(this)
                .setTitle(getString(R.string.remove_board_from_session))
                .setAnimation("58413-delete-icon-animation.json")
                .setMessage(getString(R.string.are_you_sure))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.delete), (dialogInterface, which) -> {
                    dialogInterface.dismiss();//TODO use board's position in a listview, in a session.boards.get(postion) to remove it
                    session.getBoard_ids().remove((Integer) Board.BoardNameToId((String) button.getText()));
                    loadBoardsUsed();
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> dialogInterface.dismiss())
                .build();
        mDialog.getAnimationView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mDialog.show();

        return true;
    }

    public void addUsedBoard(View view) {
        String[] boards = new String[Board.savedBoards.size()];
        int i = 0;
        for (int board_id : Board.savedBoardIds) {
            Board board = Board.savedBoards.get(board_id); // TODO: don't do this!
            if (board != null) {
                boards[i] = board.getName();
            }
            i+=1;
        }

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(getString(R.string.add_board));
        b.setItems(boards, (dialog, which) -> {
            dialog.dismiss();
            session.getBoard_ids().add(Board.BoardNameToId(boards[which]));

            loadBoardsUsed();
        });
        b.show();
    }

    void loadPoints() {
        Polyline line = new Polyline(map);
        line.getOutlinePaint().setStrokeWidth(20f);
        line.getOutlinePaint().setColor(this.getColor(R.color.purple_500));
        List<GeoPoint> pts = new ArrayList<>();

        for (SessionLocationPoint point : session.getLocations()) {
            pts.add(new GeoPoint(point.getLatitude(), point.getLongitude()));
        }

        map.getOverlays().add(line);
        line.setPoints(pts);
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
    }

    public void deleteSession(View view) {
        MaterialDialog mDialog = new MaterialDialog.Builder(this)
                .setTitle(getString(R.string.delete_session))
                .setAnimation("58413-delete-icon-animation.json")
                .setMessage(getString(R.string.are_you_sure))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.delete), (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                    Session.savedSessions.remove(session.getId());
                    Session.savedSessionIds.remove((Integer) session.getId());
                    Intent myIntent = new Intent(this, MainActivity.class);
                    startActivity(myIntent);
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> dialogInterface.dismiss())
                .build();
        mDialog.getAnimationView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mDialog.show();
    }

    public void addTag(View view) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.add_tag);
        String[] types = getResources().getStringArray(R.array.tags);
        b.setItems(types, (dialog, which) -> {
            dialog.dismiss();
            session.getTags().add(types[which]);
            Session.savedSessions.get(session.getId()).setTags(session.getTags());

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

    private void loadSpeedChart() {
        LineChart chart = binding.sessionLayout.findViewById(R.id.speedChart);
        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < session.getLocations().size(); i++) {
            values.add(new Entry(i, session.getLocations().get(i).getSpeed(), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24,null)));
        }

        // create a data object with the data sets
        LineData data = App.createLineSet(values,getString(R.string.speed),true,this);

        XAxis xAxis = chart.getXAxis();
        xAxis.setLabelRotationAngle(30f);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                long start = session.getLocations().get(0).getTimeStamp();
                long current = session.getLocations().get((int) value).getTimeStamp();
                long diff = current - start;

                @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(diff) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff)),
                        TimeUnit.MILLISECONDS.toSeconds(diff) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff)));
                return time;
            }
        });

        // set data
        chart.setData(data);
        chart.animateX(3000);

        // coloring chart
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

    private void loadElevationChart() {
        LineChart chart = binding.sessionLayout.findViewById(R.id.elevationChart);
        chart.setVisibility(View.VISIBLE);
        binding.sessionLayout.findViewById(R.id.elevationText).setVisibility(View.VISIBLE);

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < session.getLocations().size(); i++) {
            values.add(new Entry(i, session.getElevationPoints().get(i), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24,null)));
        }

        // create a data object with the data sets
        LineData data = App.createLineSet(values,getString(R.string.elevation),true,this);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                long start = session.getLocations().get(0).getTimeStamp();
                long current = session.getLocations().get((int) value).getTimeStamp();
                long diff = current - start;

                @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(diff) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(diff)),
                        TimeUnit.MILLISECONDS.toSeconds(diff) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff)));
                return time;
            }
        });

        // set data
        chart.setData(data);
        chart.animateX(3000);

        // coloring chart
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
