package com.theredspy15.thanelocker.ui.mainfragments.skatemap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.example.longboardlife.R;
import com.example.longboardlife.databinding.FragmentSkatemapBinding;
import com.theredspy15.thanelocker.models.Meetup;
import com.theredspy15.thanelocker.models.Profile;
import com.theredspy15.thanelocker.utils.MapThemes;
import java.util.ArrayList;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

public class SkateMapFragment extends Fragment {

  private FragmentSkatemapBinding binding;

  private MapView map = null;
  IMapController mapController;
  Meetup newMeetup = null;

  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {

    binding = FragmentSkatemapBinding.inflate(inflater, container, false);
    binding.button.setOnClickListener(this::createMeetup);
    View root = binding.getRoot();

    // load/initialize the osmdroid configuration, this can be done
    Context ctx = requireContext();
    Configuration.getInstance().load(
        ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
    // setting this before the layout is inflated is a good idea
    // it 'should' ensure that the map has a writable location for the map
    // cache, even without permissions if no tiles are displayed, you can try
    // overriding the cache path using Configuration.getInstance().setCachePath
    // see also StorageUtils
    // note, the load method also sets the HTTP User Agent to your application's
    // package name, abusing osm's tile servers will get you banned based on this
    // string

    // setup map
    Configuration.getInstance().load(
        requireContext(),
        PreferenceManager.getDefaultSharedPreferences(requireContext()));
    map = binding.map;
    map.setTileSource(TileSourceFactory.MAPNIK);
    map.setMultiTouchControls(true);
    mapController = map.getController();
    mapController.setZoom(5.0);
    GeoPoint startPoint = new GeoPoint(40.722429, -99.366040);
    mapController.setCenter(startPoint);
    map.getZoomController().setVisibility(
        CustomZoomButtonsController.Visibility.NEVER);

    // determine theme for map
    int nightModeFlags = this.getResources().getConfiguration().uiMode &
                         android.content.res.Configuration.UI_MODE_NIGHT_MASK;
    if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES)
      map.getOverlayManager().getTilesOverlay().setColorFilter(
          MapThemes.darkFilter());

    // WIP disclaimer dialog
    AlertDialog alertDialog =
        new AlertDialog.Builder(requireContext()).create();
    alertDialog.setTitle(getString(R.string.not_added_title));
    alertDialog.setMessage(getString(R.string.not_added_disclaimer));
    alertDialog.setCancelable(false);
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                          (dialog, which) -> dialog.dismiss());
    alertDialog.show();

    loadMeets();

    return root;
  }

  private void resetCreatingMeetup() {
    binding.button.setText("Create Meet");
    binding.button.setBackgroundColor(
        requireContext().getColor(R.color.purple_500));
    binding.button.setOnClickListener(this::createMeetup);
    map.getOverlays().remove(touchOverlay);
  }

  public final void createMeetup(View view) {
    binding.button.setText("Select a location");
    binding.button.setBackgroundColor(
        getContext().getColor(R.color.recording_session));
    map.getOverlays().add(touchOverlay);
  }

  public View.OnClickListener confirmedLocation() {
    final EditText inputTitle = new EditText(requireContext());
    inputTitle.setHint("Title");
    final EditText inputDescription = new EditText(requireContext());
    inputDescription.setHint("Description");
    DatePicker datePicker = new DatePicker(requireContext());

    LinearLayout layout = new LinearLayout(requireContext());
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.addView(inputTitle);
    layout.addView(inputDescription);

    new AlertDialog.Builder(requireContext())
        .setTitle("Create Meetup")
        .setMessage(
            "Please provide a name and description so people know what the meetup is going to be like")
        .setView(layout)
        .setPositiveButton(
            "Next",
            (dialog, whichButton) -> {
              newMeetup.setTitle(inputTitle.getText().toString());
              newMeetup.setDescription(inputDescription.getText().toString());
              newMeetup.getAttending_users().add(Profile.localProfile.getId());
              newMeetup.setMeet_id(
                  (int)(newMeetup.getLatitude() + newMeetup.getLongitude()));

              new AlertDialog.Builder(requireContext())
                  .setTitle("Select Date")
                  .setView(datePicker)
                  .setPositiveButton("Create",
                                     (dialog2, whichButton2) -> {
                                       newMeetup.setDate(
                                           datePicker.getMonth() + "/" +
                                           datePicker.getDayOfMonth());
                                       Meetup.uploadMeetup(newMeetup);
                                       dialog.dismiss();
                                     })
                  .setNegativeButton(
                      R.string.cancel,
                      (dialog2, whichButton2) -> resetCreatingMeetup())
                  .show();
            })
        .setNegativeButton(R.string.cancel,
                           (dialog, whichButton) -> resetCreatingMeetup())
        .show();
    return null;
  }

  private void loadMeets() {
    for (Meetup meet : Meetup.meetups) {
      GeoPoint startPoint =
          new GeoPoint(meet.getLatitude(), meet.getLongitude());
      Marker marker = new Marker(map);
      marker.setPosition(startPoint);
      marker.setTitle(meet.getTitle() + " " + meet.getDate());
      marker.setSubDescription(meet.getDescription());
      marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
      map.getOverlays().add(marker);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    // this will refresh the osmdroid configuration on resuming.
    // if you make changes to the configuration, use
    // SharedPreferences prefs =
    // PreferenceManager.getDefaultSharedPreferences(this);
    // Configuration.getInstance().load(this,
    // PreferenceManager.getDefaultSharedPreferences(this));
    map.onResume(); // needed for compass, my location overlays, v6.0.0 and up
  }

  @Override
  public void onPause() {
    super.onPause();

    Meetup.loadFromFirebase();

    // this will refresh the osmdroid configuration on resuming.
    // if you make changes to the configuration, use
    // SharedPreferences prefs =
    // PreferenceManager.getDefaultSharedPreferences(this);
    // Configuration.getInstance().save(this, prefs);
    map.onPause(); // needed for compass, my location overlays, v6.0.0 and up
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  // overlay for selecting location
  Overlay touchOverlay = new Overlay() {
    ItemizedIconOverlay<OverlayItem> anotherItemizedIconOverlay = null;
    @Override
    public void draw(Canvas arg0, MapView arg1, boolean arg2) {}
    @Override
    public boolean onSingleTapConfirmed(final MotionEvent e,
                                        final MapView mapView) {
      final Drawable marker =
          requireContext().getDrawable(R.drawable.ic_baseline_location_on_24);
      Projection proj = mapView.getProjection();
      GeoPoint loc = (GeoPoint)proj.fromPixels((int)e.getX(), (int)e.getY());
      ArrayList<OverlayItem> overlayArray = new ArrayList<>();
      OverlayItem mapItem = new OverlayItem(
          "", "", new GeoPoint(loc.getLatitude(), loc.getLongitude()));
      mapItem.setMarker(marker);
      overlayArray.add(mapItem);
      if (anotherItemizedIconOverlay == null) { // first location
        anotherItemizedIconOverlay =
            new ItemizedIconOverlay<>(requireContext(), overlayArray, null);
        mapView.getOverlays().add(anotherItemizedIconOverlay);
        mapView.invalidate();
      } else { // replacing/updating it
        mapView.getOverlays().remove(anotherItemizedIconOverlay);
        mapView.invalidate();
        anotherItemizedIconOverlay =
            new ItemizedIconOverlay<>(requireContext(), overlayArray, null);
        mapView.getOverlays().add(anotherItemizedIconOverlay);
      }

      return true;
    }
  };
}
