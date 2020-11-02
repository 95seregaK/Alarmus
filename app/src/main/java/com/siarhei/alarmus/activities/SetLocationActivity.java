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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.siarhei.alarmus.R;
import com.siarhei.alarmus.map.SunInfoMarker;
import com.siarhei.alarmus.sun.SunInfo;
import com.siarhei.alarmus.sun.SunMath;
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
import java.time.Month;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SetLocationActivity extends Activity implements Marker.OnMarkerClickListener, View.OnClickListener, LocationListener, MapEventsReceiver {


    private SunInfoMarker defaultMarker;
    private RadiusMarkerClusterer markerClusterer;
    private FusedLocationProviderClient fusedLocationClient;
    private FloatingActionButton setPositionBtn;
    private FloatingView infoWindow;
    private MapView map;
    Button dateButton;
    private SunInfoScrollView sunInfoView;
    private SunInfo currentSunInfo;
    public static final double DEFAULT_LATITUDE = 54.0;
    public static final double DEFAULT_LONGITUDE = 28.0;
    private TextView locationView;
    private TextView timeZoneView;
    private IMapController mapController;
    private View currentLocationButton;


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
        locationView = findViewById(R.id.location);
        timeZoneView = findViewById(R.id.time_zone);
        dateButton = findViewById(R.id.date_button);
        dateButton.setOnClickListener(this);
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

        mapController = map.getController();
        mapController.setZoom(9.5);
        double lat = getIntent().getDoubleExtra(EditAlarmActivity.LATITUDE, DEFAULT_LATITUDE);
        double lon = getIntent().getDoubleExtra(EditAlarmActivity.LONGITUDE, DEFAULT_LONGITUDE);
        GeoPoint startPoint = new GeoPoint(lat, lon);
        mapController.setCenter(startPoint);
        markerClusterer = new RadiusMarkerClusterer(this);
        markerClusterer.setRadius(20);

        defaultMarker = new SunInfoMarker(map, this);
        Drawable icon = getResources().getDrawable(R.drawable.ic_default_marker);
        defaultMarker.setIcon(icon);
        defaultMarker.setOnMarkerClickListener(this);

        defaultMarker.setPosition(startPoint);
        currentSunInfo = new SunInfo(Calendar.getInstance(), lat, lon);
        updateSunInfoLocation();
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        map.getOverlays().add(mapEventsOverlay);
        map.getOverlays().add(defaultMarker);
        map.getOverlays().add(markerClusterer);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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
            defineCurrentLocation();
        } else if (view.getId() == R.id.info_view) {
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

    public void defineCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Location cannot be determined! Please set location manually", Toast.LENGTH_LONG);
            return;
        }
        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            Location location = task.getResult();
            if (location != null) {
                GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapController.animateTo(startPoint);
                defaultMarker.setPosition(startPoint);
                //Log.d("Location1", "Location" + location.getLatitude() + " " + location.getLatitude());
                updateSunInfoLocation();
            } else {
                Toast.makeText(this, "Location cannot be determined! Please set location manually", Toast.LENGTH_LONG);
            }
        });
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
        int offset = currentSunInfo.getTimeZoneOffset();
        timeZoneView.setText("Time zone: " + currentSunInfo.getTimeZone() + (offset > 0 ? " +" : " ") + offset);
        sunInfoView.setSunInfo(currentSunInfo);
    }

    private String defineCityName(double lat, double lon) {
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(lat, lon, 1);
            if (addresses.size() > 0) {
                return addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
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
        Marker marker = new SunInfoMarker(map, SetLocationActivity.this);

        marker.setPosition(p);
        marker.setOnMarkerClickListener(SetLocationActivity.this);
        markerClusterer.add(marker);

        markerClusterer.invalidate();
        map.invalidate();

        onMarkerClick(marker, map);
        return false;
    }

}
