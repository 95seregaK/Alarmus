package com.siarhei.alarmus.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.siarhei.alarmus.R;
import com.siarhei.alarmus.map.MyMarkerInfoWindow;
import com.siarhei.alarmus.map.SunInfoMarker;
import com.siarhei.alarmus.sun.SunInfo;
import com.siarhei.alarmus.views.FloatingView;
import com.siarhei.alarmus.views.SunInfoScrollView;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SetLocationActivity extends Activity implements Marker.OnMarkerClickListener, View.OnClickListener {


    private SunInfoMarker defaultMarker;
    private RadiusMarkerClusterer markerClusterer;
    private MarkerInfoWindow markerInfoWindow;
    private FusedLocationProviderClient fusedLocationClient;
    private FloatingActionButton setPositionBtn;
    private FloatingView infoWindow;
    private MapView map;
    private TextView subDescription;
    private SunInfoScrollView sunInfoView;
    public static final double DEFAULT_LATITUDE = 54.0;
    public static final double DEFAULT_LONGITUDE = 28.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setPositionBtn = findViewById(R.id.set_position);
        setPositionBtn.setOnClickListener(this);
        infoWindow = findViewById(R.id.info_view);
        sunInfoView = findViewById(R.id.sun_info_view);
        infoWindow.setOnClickListener(this);
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        //map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        //We can move the map on a default view point. For this, we need access to the map controller:

        IMapController mapController = map.getController();
        mapController.setZoom(9.5);
        double lat = getIntent().getDoubleExtra(EditAlarmActivity.LATITUDE, DEFAULT_LATITUDE);
        double lon = getIntent().getDoubleExtra(EditAlarmActivity.LONGITUDE, DEFAULT_LONGITUDE);
        GeoPoint startPoint = new GeoPoint(lat, lon);
        mapController.setCenter(startPoint);
        markerClusterer = new RadiusMarkerClusterer(this);
        markerClusterer.setRadius(20);

        defaultMarker = new SunInfoMarker(map, this);
        defaultMarker.setIcon(getResources().getDrawable(R.drawable.ic_current_marker));
        defaultMarker.setOnMarkerClickListener(this);
        defaultMarker.setPosition(startPoint);
        markerInfoWindow = new MyMarkerInfoWindow(R.layout.info_window, map, this);
        defaultMarker.setInfoWindow(markerInfoWindow);

        MapEventsReceiver mapEventsReceiver = new MyMapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                defaultMarker.setPosition(p);
                onMarkerClick(defaultMarker, map);
                map.invalidate();
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                Marker marker = new SunInfoMarker(map, SetLocationActivity.this);

                marker.setPosition(p);
                marker.setInfoWindow(markerInfoWindow);
                marker.setOnMarkerClickListener(SetLocationActivity.this);
                markerClusterer.add(marker);

                markerClusterer.invalidate();
                map.invalidate();

                onMarkerClick(marker, map);
                return false;
            }
        };
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);

        map.getOverlays().add(mapEventsOverlay);
        map.getOverlays().add(defaultMarker);
        map.getOverlays().add(markerClusterer);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(task -> {
                    try {
                        Location location = task.getResult();
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                                    location.getLongitude(), 1);
                            startPoint.setLatitude(addresses.get(0).getLatitude());
                            startPoint.setLongitude(addresses.get(0).getLongitude());
                            mapController.animateTo(startPoint);
                            defaultMarker.setPosition(startPoint);
                            map.invalidate();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.set_position) {
            Intent intent = new Intent();
            intent.putExtra(EditAlarmActivity.LATITUDE, defaultMarker.getPosition().getLatitude());
            intent.putExtra(EditAlarmActivity.LONGITUDE, defaultMarker.getPosition().getLongitude());
            setResult(EditAlarmActivity.RESULT_LOCATION_CHOSEN, intent);
            finish();
        }
        if (view.getId() == R.id.info_view) {
            infoWindow.hide();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        //mapView.getController().animateTo(marker.getPosition());
        //marker.showInfoWindow();
        sunInfoView.setSunInfo(((SunInfo) marker.getRelatedObject()));
        infoWindow.emerge();
        return true;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public class MyMapEventsReceiver implements MapEventsReceiver {
        @Override
        public boolean singleTapConfirmedHelper(GeoPoint p) {
            return false;
        }

        @Override
        public boolean longPressHelper(GeoPoint p) {
            return false;
        }
    }


}
