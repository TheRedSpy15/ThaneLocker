package com.theredspy15.thanelocker.ui.mainfragments.skatemap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.theredspy15.thanelocker.models.Elevation;
import com.theredspy15.thanelocker.models.HillRoute;
import com.theredspy15.thanelocker.ui.activitycontrollers.MainActivity;
import com.theredspy15.thanelocker.utils.App;
import com.theredspy15.thanelocker.utils.MapThemes;
import com.theredspy15.thanelocker.utils.PermissionChecker;
import com.theredspy15.thanelocker.utils.Purchasing;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;
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
import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class SkateMapFragment extends Fragment {

    private FragmentSkatemapBinding binding;

    private MapView map = null;
    private IMapController mapController;
    private GeoPoint point1 = null;
    private GeoPoint point2 = null;
    private boolean usingSatellite = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSkatemapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        FirebaseCrashlytics.getInstance().log("displaying skatemap fragment");

        // fab options
        binding.clearOption.setOnClickListener(this::clear);
        binding.helpOption.setOnClickListener(this::help);
        binding.loadOption.setOnClickListener(this::loadRoute);
        binding.saveOption.setOnClickListener(this::saveRoute);
        binding.styleOption.setOnClickListener(this::toggleStyle);
        binding.myLocationButton.setOnClickListener(this::lastKnownLocation);

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
        GeoPoint startPoint = new GeoPoint(20.712807, -156.251335);
        mapController.setCenter(startPoint);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        // get where user clicks on map
        MapEventsOverlay OverlayEvents = new MapEventsOverlay(mReceive);
        map.getOverlays().add(OverlayEvents);

        // determine theme for map - satellite
        if (MainActivity.preferences.getBoolean("satellite", false) && MainActivity.preferences.getBoolean("subscribe", false)) {
            BingMapTileSource.retrieveBingKey(requireContext());
            String m_locale = Locale.getDefault().getDisplayName();
            BingMapTileSource bing = new BingMapTileSource(m_locale);
            bing.setStyle(BingMapTileSource.IMAGERYSET_AERIAL);
            map.setTileSource(bing);
            usingSatellite = true;
        } else { // simple
            int nightModeFlags = this.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES)
                map.getOverlayManager().getTilesOverlay().setColorFilter(MapThemes.darkFilter());
        }

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        // offline & premium notices
        if (!new App().isNetworkAvailable(requireContext())) offlineNotice();
        else if (!MainActivity.preferences.getBoolean("subscribe", false)) premiumNotice();
    }

    private boolean checkPermission() { // TODO: check that gps is even on
        if (!PermissionChecker.checkPermissionLocation(requireContext())) PermissionChecker.requestPermissionLocation(requireActivity());

        return PermissionChecker.checkPermissionLocation(requireContext());
    }

    public void lastKnownLocation(View view) {
        if (checkPermission()) {
            SingleShotLocationProvider.requestSingleUpdate(requireContext(),
                    location -> mapController.setCenter(new GeoPoint(location.latitude,location.longitude)));
        }
    }

    public void help(View view) {
        MaterialDialog mDialog = new MaterialDialog.Builder(requireActivity())
                .setTitle(getString(R.string.hill_finder))
                .setMessage(getString(R.string.hill_finder_howto))
                .setAnimation("78890-finding-route.json")
                .setCancelable(true)
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> dialogInterface.dismiss())
                .setPositiveButton(getString(R.string.ok), (dialogInterface, which) -> dialogInterface.dismiss())
                .build();
        mDialog.getAnimationView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mDialog.show();
    }

    public void clear(View view) {
        point1 = null;
        point2 = null;
        map.getOverlays().clear();
        MapEventsOverlay OverlayEvents = new MapEventsOverlay(mReceive);
        map.getOverlays().add(OverlayEvents);
        LineChart chart = binding.elevationsChart;
        chart.clear();
        chart.setVisibility(View.GONE);
        binding.distanceView.setText("");
        binding.distanceView.setVisibility(View.GONE);
        map.postInvalidate();
    }

    public void saveRoute(View view) {
        if (point1 != null && point2 != null) {
            ArrayList<GeoPoint> points = new ArrayList<>();
            points.add(point1);
            points.add(point2);

            HillRoute route = new HillRoute();
            EditText editText = new EditText(requireContext());

            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.save_route)
                    .setMessage(R.string.save_route_sum)
                    .setView(editText)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                        String input = editText.getText().toString();
                        route.setName(input);
                        route.setPoints(points);
                        HillRoute.savedHillIds.add(route.getId());
                        HillRoute.savedHills.put(route.getId(),route);
                        HillRoute.save();

                        MotionToast.Companion.createColorToast(
                                requireActivity(),
                                getString(R.string.saved_success),
                                getString(R.string.saved_as)+" "+route.getName(),
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(requireContext(), R.font.roboto)
                        );
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .create();
            dialog.show();
        } else {
            MotionToast.Companion.createColorToast(
                    requireActivity(),
                    getString(R.string.did_not_save),
                    getString(R.string.no_route),
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(requireContext(), R.font.roboto)
            );
        }
    }

    public void loadRoute(View view) {
        clear(null);
        HillRoute.load();

        Toast.makeText(requireContext(), ""+HillRoute.savedHillIds.size(), Toast.LENGTH_SHORT).show();
        String[] routeNames = new String[HillRoute.savedHills.size()];
        int i = 0;
        for (int hill_id : HillRoute.savedHillIds) {
            HillRoute route = HillRoute.savedHills.get(hill_id); // TODO: don't do this!
            if (route != null) {
                routeNames[i] = route.getName();
            }
            i+=1;
        }

        AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
        b.setTitle("Load Route");
        b.setItems(routeNames, (dialog, which) -> {
            dialog.dismiss();
            HillRoute route = HillRoute.savedHills.get(HillRoute.RouteNameToId(routeNames[which]));
            if (route != null) {
                point1 = route.getPoints().get(0);
                point2 = route.getPoints().get(1);
                mapController.setCenter(point1);

                addMarker(point1,false,true);
                addMarker(point2,true,false);
            } else {
                MotionToast.Companion.createColorToast(
                        requireActivity(),
                        getString(R.string.failed_route_load),
                        getString(R.string.unknown_error),
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.roboto)
                );
            }
        });
        b.show();
        map.postInvalidate();
    }

    public void toggleStyle(View view) {
        if (usingSatellite) { // switch to simple
            usingSatellite = false;
            map.setTileSource(TileSourceFactory.MAPNIK);
            int nightModeFlags = this.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES)
                map.getOverlayManager().getTilesOverlay().setColorFilter(MapThemes.darkFilter());
        } else { // switch to satellite
            usingSatellite = true;
            map.getOverlayManager().getTilesOverlay().setColorFilter(null);
            BingMapTileSource.retrieveBingKey(requireContext());
            String m_locale = Locale.getDefault().getDisplayName();
            BingMapTileSource bing = new BingMapTileSource(m_locale);
            bing.setStyle(BingMapTileSource.IMAGERYSET_AERIAL);
            map.setTileSource(bing);
        }
    }

    // this is the overlay for getting user touch, and setting geopoints for those locations
    MapEventsReceiver mReceive = new MapEventsReceiver() {
        @Override
        public boolean singleTapConfirmedHelper(GeoPoint p) {
            if (point1 == null) {
                point1 = p;
                addMarker(point1,false, true);
            } else if (point2 == null) {
                point2 = p;
                addMarker(point2,true, false);
            } else {
                point2 = null; // reset for new route
                map.getOverlays().clear();
                MapEventsOverlay OverlayEvents = new MapEventsOverlay(mReceive);
                map.getOverlays().add(OverlayEvents);

                point1 = p;
                addMarker(point1,false, false);
            }

            return false;
        }

        @Override
        public boolean longPressHelper(GeoPoint p) { return false; }
    };

    /* TODO: maybe use this to get average grade for entire route
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
                .setPositiveButton(getString(R.string.ok), (dialogInterface, which) -> {
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
                .setPositiveButton(getString(R.string.sign_up), (dialogInterface, which) -> {
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

    private void addMarker(GeoPoint point, boolean getElevation, boolean startMarker) {
        // add marker
        Drawable icon = AppCompatResources.getDrawable(requireContext(),R.drawable.ic_baseline_location_on_24);
        if (icon != null) {
            if (startMarker) icon.setTint(Color.GREEN);
            else icon.setTint(Color.RED);
        }

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

        map.postInvalidate();
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
        LineData data = App.createLineSet(values,getString(R.string.elevation),true,requireContext());
        data.setDrawValues(false);

        // set data
        chart.setData(data);

        // connecting chart values to map
        final Marker[] marker = {null};
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (marker[0] == null) {
                    marker[0] = new Marker(map);
                    GeoPoint selectedLocation = road.mRouteHigh.get((int) e.getX());
                    marker[0].setPosition(selectedLocation);
                    map.getOverlays().add(marker[0]);
                    mapController.setCenter(selectedLocation);
                } else {
                    GeoPoint selectedLocation = road.mRouteHigh.get((int) e.getX());
                    marker[0].setPosition(selectedLocation);
                    mapController.setCenter(selectedLocation);
                }
                map.postInvalidate();
            }

            @Override
            public void onNothingSelected() {

            }
        });

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
            binding.elevationsChart.setVisibility(View.VISIBLE);
            binding.distanceView.setVisibility(View.VISIBLE);

            // route
            map.getOverlays().add(roadOverlay);
            map.invalidate();

            // chart
            chart.animateX(3000);

            // distance text
            binding.distanceView.setText(App.getDistanceFormatted((float) road.mLength,getResources()));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

/**
 * Used to go to user location
 */
class SingleShotLocationProvider {

    public interface LocationCallback {
        void onNewLocationAvailable(GPSCoordinates location);
    }

    @SuppressLint("MissingPermission")
    public static void requestSingleUpdate(final Context context, final LocationCallback callback) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isNetworkEnabled) { // uses coarse first, fine as a fallback
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            locationManager.requestSingleUpdate(criteria, location -> callback.onNewLocationAvailable(new GPSCoordinates(location.getLatitude(), location.getLongitude())), null);
        } else {
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                locationManager.requestSingleUpdate(criteria, location -> callback.onNewLocationAvailable(new GPSCoordinates(location.getLatitude(), location.getLongitude())), null);
            }
        }
    }

    public static class GPSCoordinates {
        public float longitude;
        public float latitude;

        public GPSCoordinates(double theLatitude, double theLongitude) {
            longitude = (float) theLongitude;
            latitude = (float) theLatitude;
        }
    }
}