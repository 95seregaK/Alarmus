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
    private TextView textSunrise, textSunriseNext, textSunset, textSunsetNext,
            textNoon, textNoonNext, textDayDuration, textDayDurationNext;
    private TextView subDescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setPositionBtn = (FloatingActionButton) findViewById(R.id.set_position);
        setPositionBtn.setOnClickListener(this);
        infoWindow = (FloatingView) findViewById(R.id.info_view);
        textSunrise = (TextView) findViewById(R.id.text_sunrise);
        textSunriseNext = (TextView) findViewById(R.id.text_sunrise_next);
        textSunset = (TextView) findViewById(R.id.text_sunset);
        textSunsetNext = (TextView) findViewById(R.id.text_sunset_next);
        textNoon = (TextView) findViewById(R.id.text_noon);
        textNoonNext = (TextView) findViewById(R.id.text_noon_next);
        textDayDuration = (TextView) findViewById(R.id.text_day_duration);
        textDayDurationNext = (TextView) findViewById(R.id.text_day_duration_next);

        infoWindow.setOnClickListener(this);
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        //map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        //We can move the map on a default view point. For this, we need access to the map controller:

        IMapController mapController = map.getController();
        mapController.setZoom(9.5);
        GeoPoint startPoint = new GeoPoint(54.8583, 28.2944);
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
        setInfo((SunInfo) marker.getRelatedObject());
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

    public void setInfo(SunInfo info) {
        textSunrise.setText(SunInfo.timeToString(info.getSunriseLocalTime(), SunInfo.HH_MM));
        textSunset.setText(SunInfo.timeToString(info.getSunsetLocalTime(), SunInfo.HH_MM));
        textNoon.setText(SunInfo.timeToString(info.getNoonLocalTime(), SunInfo.HH_MM));
        textDayDuration.setText(SunInfo.timeToString(info.getDayDuration(), SunInfo.HH_MM));

        SunInfo infoNext = SunInfo.nextDaySunInfo(info);
        textSunriseNext.setText(SunInfo.timeToString(infoNext.getSunriseLocalTime(), SunInfo.HH_MM));
        textSunsetNext.setText(SunInfo.timeToString(infoNext.getSunsetLocalTime(), SunInfo.HH_MM));
        textNoonNext.setText(SunInfo.timeToString(infoNext.getNoonLocalTime(), SunInfo.HH_MM));
        textDayDurationNext.setText(SunInfo.timeToString(infoNext.getDayDuration(), SunInfo.HH_MM));
        if (!SunInfo.afterNow(info.getSunriseLocalTime())){
            textSunrise.setTextColor(getResources().getColor(R.color.info_color_passive));}
        if (!SunInfo.afterNow(info.getNoonLocalTime())){
            textNoon.setTextColor(getResources().getColor(R.color.info_color_passive));}
        if (!SunInfo.afterNow(info.getSunsetLocalTime())){
            textSunset.setTextColor(getResources().getColor(R.color.info_color_passive));}
    }
}
