package com.siarhei.alarmus.activities;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.siarhei.alarmus.R;
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
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapActivity extends Activity implements Marker.OnMarkerClickListener,
        View.OnClickListener, LocationListener, MapEventsReceiver {


    public static final int CODE_SUCCESS = 1;
    public static final int CODE_FAILURE = 2;
    public static final double DEFAULT_LATITUDE = 48.85;
    public static final double DEFAULT_LONGITUDE = 2.33;
    public static final String MODE_MAP = "mode";
    private SunInfoMarker defaultMarker;
    private RadiusMarkerClusterer markerClusterer;
    private FusedLocationProviderClient fusedLocationClient;
    private FloatingActionButton setPositionBtn;
    private FloatingView infoWindow;
    private MapView map;
    private SunInfoScrollView sunInfoView;
    private SunInfo currentSunInfo;
    private TextView locationView;
    private TextView timeZoneView;
    private IMapController mapController;
    private View currentLocationButton, dateButton, infoWindowBar;

    public static void defineCurrentLocation(Context context, OnLocationDefinedCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationDefined(CODE_FAILURE, null);
            Log.d("Location", "No permissions");
            return;
        }
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            Location location = task.getResult();
            if (location != null) {
                Log.d("Location", "location!");
                callback.onLocationDefined(CODE_SUCCESS, location);
            } else {
                Log.d("Location", "location=null");
                callback.onLocationDefined(CODE_FAILURE, null);
            }
        });
    }

    public static String defineCityName(Context baseContext, double lat, double lon) {
        Geocoder gcd = new Geocoder(baseContext, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(lat, lon, 1);
            if (addresses.size() > 0 && addresses.get(0).getLocality() != null)
                return addresses.get(0).getLocality();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return "";
    }

    public static Location getLastKnownLocation(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location lastLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location lastLocationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location location = null;
        Log.d("LocationManager", "location");
        if (lastLocationNet != null && (lastLocationGPS == null ||
                lastLocationGPS.getTime() < lastLocationNet.getTime())) {
            location = lastLocationNet;
            Log.d("LocationManager", "NETWORK_PROVIDER");
        } else if (lastLocationGPS != null && (lastLocationNet == null ||
                lastLocationGPS.getTime() > lastLocationNet.getTime())) {
            location = lastLocationGPS;
            Log.d("LocationManager", "GPS_PROVIDER");
        }
        return location;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setPositionBtn = findViewById(R.id.button_choose_position);
        setPositionBtn.setOnClickListener(this);
        currentLocationButton = findViewById(R.id.button_define_location);
        currentLocationButton.setOnClickListener(this);
        infoWindow = findViewById(R.id.info_view);
        sunInfoView = findViewById(R.id.sun_info_view);
        infoWindowBar = findViewById(R.id.info_window_bar);
        locationView = findViewById(R.id.location);
        timeZoneView = findViewById(R.id.time_zone);
        dateButton = findViewById(R.id.date_button);
        dateButton.setOnClickListener(this);
        infoWindowBar.setOnClickListener(this);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        map.setMultiTouchControls(true);
        //We can move the map on a default view point. For this, we need access to the map controller:

        mapController = map.getController();
        mapController.setZoom(9.5);

        double lat = getIntent().getDoubleExtra(EditAlarmActivity.LATITUDE, DEFAULT_LATITUDE);
        double lon = getIntent().getDoubleExtra(EditAlarmActivity.LONGITUDE, DEFAULT_LONGITUDE);
        int mode = getIntent().getIntExtra(MODE_MAP, 0);

        GeoPoint startPoint = new GeoPoint(lat, lon);
        defaultMarker = new SunInfoMarker(map, this);
        if (Build.VERSION.SDK_INT <= 28) {
            Drawable icon = getResources().getDrawable(R.drawable.ic_default_marker);
            //defaultMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            defaultMarker.setIcon(icon);
            //defaultMarker.setPanToView(true);
        }
        defaultMarker.setOnMarkerClickListener(this);
        defaultMarker.setPosition(startPoint);
        currentSunInfo = new SunInfo(Calendar.getInstance(), lat, lon);
        updateSunInfoLocation();
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        map.getOverlays().add(mapEventsOverlay);
        map.getOverlays().add(defaultMarker);

        if (mode == 0) {
            Location loc = getLastKnownLocation(this);
            if (loc != null) {
                defaultMarker.setPosition(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
            }
            setPositionBtn.setVisibility(View.GONE);
            defineCurrentLocation(this, (code, location) -> {
                if (code == CODE_SUCCESS)
                    onLocationDefined(location);
                else
                    Toast.makeText(this, R.string.message_location_cannot, Toast.LENGTH_SHORT).show();
            });
        }

        mapController.setCenter(startPoint);
        // markerClusterer = new RadiusMarkerClusterer(this);
        // markerClusterer.setRadius(20);

        // map.getOverlays().add(markerClusterer);

        //startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        //mapController.animateTo(startPoint);
        //defaultMarker.setPosition(startPoint);
        //Log.d("Location1", "LocationLast" + location.getLatitude() + " " + location.getLatitude());
        updateSunInfoLocation();
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_choose_position) {
            Intent intent = new Intent();
            intent.putExtra(EditAlarmActivity.LATITUDE, defaultMarker.getPosition().getLatitude());
            intent.putExtra(EditAlarmActivity.LONGITUDE, defaultMarker.getPosition().getLongitude());
            setResult(EditAlarmActivity.RESULT_LOCATION_CHOSEN, intent);
            finish();
        } else if (view.getId() == R.id.button_define_location) {
            defineCurrentLocation(this, (code, location) -> {
                if (code == CODE_SUCCESS)
                    onLocationDefined(location);
                else
                    Toast.makeText(this, R.string.message_location_cannot, Toast.LENGTH_SHORT).show();

            });
        } else if (view.getId() == R.id.info_window_bar) {
            if (infoWindow.isHiden()) infoWindow.emerge();
            else infoWindow.hide();
        } else if (view.getId() == R.id.date_button) {
            setDate();
        }
    }

    private void setDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (v, year, monthOfYear, dayOfMonth) -> {
                    currentSunInfo.setNewDate(dayOfMonth, monthOfYear + 1, year);
                    sunInfoView.setSunInfo(currentSunInfo);
                    sunInfoView.goToCentre();
                }, currentSunInfo.getYear(), currentSunInfo.getMonth() - 1, currentSunInfo.getDay());
        datePickerDialog.show();
    }

    private void onLocationDefined(Location location) {
        GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapController.animateTo(startPoint);
        defaultMarker.setPosition(startPoint);
        updateSunInfoLocation();
    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        mapView.getController().animateTo(marker.getPosition());
        updateSunInfoLocation();
        //infoWindow.emerge();
        return true;
    }

    private void updateSunInfoLocation() {
        double lat = defaultMarker.getPosition().getLatitude();
        double lon = defaultMarker.getPosition().getLongitude();
        String cityName = "";
        //cityName += defineCityName(lat, lon);
        currentSunInfo.setNewPosition(lat, lon);
        locationView.setText("Location: " + cityName + SunInfo.toLocationString(lat, lon, 5));
        float offset = currentSunInfo.getTimeZoneOffset();
        timeZoneView.setText("Time zone: " + currentSunInfo.getTimeZone() + (offset > 0 ? " +" : " ") + offset + " h");
        sunInfoView.setSunInfo(currentSunInfo);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onLocationChanged(Location loc) {
        String longitude = "Longitude: " + loc.getLongitude();
        Log.v("TAG", longitude);
        String latitude = "Latitude: " + loc.getLatitude();
        Log.v("TAG", latitude);
        /*------- To get city name from coordinates -------- */

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {

        defaultMarker.setPosition(p);
        //map.getController().animateTo(p);
        updateSunInfoLocation();
        map.invalidate();
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        /*Marker marker = new SunInfoMarker(map, SetLocationActivity.this);

        marker.setPosition(p);
        marker.setOnMarkerClickListener(SetLocationActivity.this);
        markerClusterer.add(marker);

        markerClusterer.invalidate();
        map.invalidate();

        onMarkerClick(marker, map);*/
        return false;
    }

    public interface OnLocationDefinedCallback {
        void onLocationDefined(int code, Location location);
    }

}
