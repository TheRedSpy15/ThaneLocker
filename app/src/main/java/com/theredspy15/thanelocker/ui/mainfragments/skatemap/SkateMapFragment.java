package com.theredspy15.thanelocker.ui.mainfragments.skatemap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.longboardlife.R;
import com.example.longboardlife.databinding.FragmentSkatemapBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.theredspy15.thanelocker.models.Board;
import com.theredspy15.thanelocker.models.Elevation;
import com.theredspy15.thanelocker.models.SessionLocationPoint;
import com.theredspy15.thanelocker.ui.activitycontrollers.EditProfileActivity;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;
import com.theredspy15.thanelocker.utils.App;
import com.theredspy15.thanelocker.utils.MapThemes;
import com.theredspy15.thanelocker.utils.Purchasing;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;
import org.osmdroid.tileprovider.util.StorageUtils;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;

public class SkateMapFragment extends Fragment {

    private FragmentSkatemapBinding binding;

    private MapView map = null;
    private IMapController mapController;
    private GeoPoint point1 = null;
    private GeoPoint point2 = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSkatemapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = requireContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        // setup map
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()));
        map = binding.map;
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(20.712807,-156.251335);
        mapController.setCenter(startPoint);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        // get where user clicks on map
        MapEventsOverlay OverlayEvents = new MapEventsOverlay(mReceive);
        map.getOverlays().add(OverlayEvents);

        if (MainActivity.preferences.getBoolean("satellite",false) && MainActivity.preferences.getBoolean("subscribe",false)) {
            BingMapTileSource.retrieveBingKey(requireContext());
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

        // offline notice
        if (!new App().isNetworkAvailable(requireContext())) offlineNotice();
        else if (!MainActivity.preferences.getBoolean("subscribe",false)) premiumNotice();

        return root;
    }

    MapEventsReceiver mReceive = new MapEventsReceiver() {
        @Override
        public boolean singleTapConfirmedHelper(GeoPoint p) {
            if (point1 == null) {
                point1 = p;
                addMarker(point1,false);
            } else if (point2 == null) {
                point2 = p;
                addMarker(point2,true);
            } else {
                point2 = null; // reset for new route
                map.getOverlays().clear();
                MapEventsOverlay OverlayEvents = new MapEventsOverlay(mReceive);
                map.getOverlays().add(OverlayEvents);

                point1 = p;
                addMarker(point1,false);
            }

            return false;
        }

        @Override
        public boolean longPressHelper(GeoPoint p) { return false; }
    };

    /*
    // Calculates and returns grade
    function calcGrade(rise,run) {
        return (rise/run*100).toFixed(2);
    }
    */

    private void offlineNotice() {
        MaterialDialog mDialog = new MaterialDialog.Builder(requireActivity())
                .setTitle(getString(R.string.no_connection_title))
                .setMessage(getString(R.string.no_connection_sum))
                .setAnimation("12701-no-internet-connection.json")
                .setCancelable(false)
                .setPositiveButton(getString(R.string.delete), (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                    Intent myIntent = new Intent(requireContext(), MainActivity.class);
                    startActivity(myIntent);
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                    Intent myIntent = new Intent(requireContext(), MainActivity.class);
                    startActivity(myIntent);
                })
                .build();
        mDialog.getAnimationView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mDialog.show();
    }

    private void premiumNotice() {
        MaterialDialog mDialog = new MaterialDialog.Builder(requireActivity())
                .setTitle(getString(R.string.premium_required))
                .setMessage(getString(R.string.premium_sum))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.delete), (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                    Intent myIntent = new Intent(requireContext(), MainActivity.class);
                    startActivity(myIntent);
                    new Purchasing(requireContext()).subscribe(requireContext(),requireActivity());
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                    Intent myIntent = new Intent(requireContext(), MainActivity.class);
                    startActivity(myIntent);
                })
                .build();
        mDialog.getAnimationView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mDialog.show();
    }

    private void addMarker(GeoPoint point, boolean getElevation) {
        // add marker
        Drawable icon = AppCompatResources.getDrawable(requireContext(),R.drawable.ic_baseline_location_on_24);
        Marker nodeMarker = new Marker(map);
        nodeMarker.setPosition(new GeoPoint(point.getLatitude(),point.getLongitude()));
        nodeMarker.setIcon(icon);
        map.getOverlays().add(nodeMarker);

        if (getElevation) {
            if (point1 != null && point2 != null) {
                Thread elevationThread = new Thread(this::getElevation);
                elevationThread.start();
            }
        }
    }

    private void getElevation() {
        // display route
        RoadManager roadManager = new OSRMRoadManager(requireContext(), "MY_USER_AGENT");
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(point1);
        waypoints.add(point2);
        Road road = roadManager.getRoad(waypoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        roadOverlay.getOutlinePaint().setColor(Color.YELLOW);

        // get elevations
        ArrayList<Float> elePoints = new ArrayList<>();
        try {
            elePoints = new ArrayList<>(Elevation.getElevationsGeoPoint(road.mRouteHigh));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // update graph
        LineChart chart = binding.elevationsChart;

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < elePoints.size(); i++) {
            values.add(new Entry(i, elePoints.get(i), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_location_on_24,null)));
        }

        // create a data object with the data sets
        LineData data = App.createLineSet(values,getString(R.string.elevation),requireContext());

        // set data
        chart.setData(data);

        // coloring chart
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);
        int color = App.getThemeTextColor(requireContext());
        chart.getData().setValueTextColor(color);
        chart.getData().setValueTextColor(color);
        chart.getXAxis().setTextColor(color);
        chart.getAxisLeft().setTextColor(color);
        chart.getAxisRight().setTextColor(color);
        chart.getLegend().setTextColor(color);

        requireActivity().runOnUiThread(()->{
            // ui text
            binding.tipView.setVisibility(View.GONE);
            binding.elevationsChart.setVisibility(View.VISIBLE);
            binding.distanceView.setVisibility(View.VISIBLE);

            // route
            map.getOverlays().add(roadOverlay);
            map.invalidate();

            // chart
            chart.animateX(3000);

            // distance text
            binding.distanceView.setText("Distance: "+road.mLength*0.6213711922);
        });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}