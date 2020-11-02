package com.siarhei.alarmus.activities;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.siarhei.alarmus.R;
import com.siarhei.alarmus.data.Alarm;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.data.SunAlarm;
import com.siarhei.alarmus.data.SunAlarmManager;
import com.siarhei.alarmus.sun.SunInfo;
import com.siarhei.alarmus.views.CircleCheckBox;
import com.siarhei.alarmus.views.DelayPicker;
import com.siarhei.alarmus.views.ImageRadioButton;
import com.siarhei.alarmus.views.ImageRadioGroup;
import com.siarhei.alarmus.views.SunInfoScrollView;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditAlarmActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    public static final int RESULT_LOCATION_CHOSEN = 2;
    public static final int RESULT_NEW_ALARM = 1;
    public static final int SUN_TYPE = AlarmPreferences.SUN_TYPE;
    public static final int SIMPLE_TYPE = AlarmPreferences.SIMPLE_TYPE;
    public static final int RESULT_EDIT_ALARM = 4;
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String ALARM_NAME = "alarm";
    public static final String ID = "id";

    private TextView timeView, dateView, locationView;
    private EditText labelEdit;
    private TimePicker timePicker;
    private ImageButton saveBtn;
    private CheckBox repeatCheck, updateCheck;
    private ImageRadioGroup radioGroupSunMode;
    private ImageRadioButton radioSunrise, radioSunset, radioNoon;
    private CircleCheckBox[] checkDays;
    private SunInfoScrollView infoView;
    private TextView delayView;
    private ViewGroup weekView, sunLayout, locationButton;
    private Alarm currentAlarm;
    private AlarmPreferences preferences;
    private int alarmType;
    private double latitude;
    private double longitude;
    private Toolbar toolbar;
    private AlertDialog editLabelDialog;
    private DelayPicker delayBar;
    private String cityName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);

        initContentView();
        preferences = AlarmPreferences.getInstance(this);
        currentAlarm = getIntent().getParcelableExtra(AlarmListActivity.ALARM_EDITING);
        alarmType = (currentAlarm instanceof SunAlarm) ? SUN_TYPE : SIMPLE_TYPE;
        setVisibility();
        updateContentView();
        setListeners();
        updateTimeView();
    }

    private void initContentView() {
        toolbar = findViewById(R.id.add_alarm_toolbar);
        setSupportActionBar(toolbar);
        saveBtn = findViewById(R.id.save_button);
        timeView = findViewById(R.id.timeView);
        dateView = findViewById(R.id.dateView);
        locationView = findViewById(R.id.view_location);
        locationButton = findViewById(R.id.button_location);
        timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        labelEdit = findViewById(R.id.label_edit);
        infoView = findViewById(R.id.sun_info_view);
        radioGroupSunMode = findViewById(R.id.radio_group_sun_mode);
        radioSunrise = findViewById(R.id.radioSunrise);
        radioNoon = findViewById(R.id.radioNoon);
        radioSunset = findViewById(R.id.radioSunset);
        delayView = findViewById(R.id.delay_view);
        repeatCheck = findViewById(R.id.repeatCheck);
        weekView = findViewById(R.id.view_week);
        sunLayout = findViewById(R.id.sun_layout);
        delayBar = findViewById(R.id.delayBar);
        updateCheck = findViewById(R.id.check_update);
        checkDays = new CircleCheckBox[]{findViewById(R.id.check_day1),
                findViewById(R.id.check_day2), findViewById(R.id.check_day3),
                findViewById(R.id.check_day4), findViewById(R.id.check_day5),
                findViewById(R.id.check_day6), findViewById(R.id.check_day7)};
    }

    private void setVisibility() {
        if (alarmType == SUN_TYPE) {
            sunLayout.setVisibility(View.VISIBLE);
            timePicker.setVisibility(View.GONE);
        } else {
            sunLayout.setVisibility(View.GONE);
            timePicker.setVisibility(View.VISIBLE);
        }
    }

    private void updateContentView() {
        repeatCheck.setChecked(currentAlarm.isRepeat());
        weekView.setVisibility(currentAlarm.isRepeat() ? View.VISIBLE : View.GONE);
        for (int i = 0; i < 7; i++) {
            checkDays[i].setChecked(currentAlarm.getDays()[i]);
        }
        if (alarmType == SUN_TYPE) {
            SunAlarm sunAlarm = (SunAlarm) currentAlarm;
            radioSunrise.setChecked(sunAlarm.getSunMode() == SunAlarm.MODE_SUNRISE);
            radioNoon.setChecked(sunAlarm.getSunMode() == SunAlarm.MODE_NOON);
            radioSunset.setChecked(sunAlarm.getSunMode() == SunAlarm.MODE_SUNSET);
            latitude = sunAlarm.getLatitude();
            longitude = sunAlarm.getLongitude();
            updateCheck.setChecked(sunAlarm.isUpdate());
            if (sunAlarm.isUpdate()) defineCurrentLocation();
            updateLocationViews();
        } else {
            timePicker.setCurrentHour(currentAlarm.getHour());
            timePicker.setCurrentMinute(currentAlarm.getMinute());
        }
        labelEdit.setText(currentAlarm.getLabel());
    }

    public AlertDialog createLabelEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_label, null);
        builder.setView(dialogView);
        EditText labelEditD = dialogView.findViewById(R.id.label_edit);
        labelEditD.setText(labelEdit.getText().toString());
        labelEditD.setSelection(labelEditD.getText().length());
        labelEditD.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == 6) {
                labelEdit.setText(labelEditD.getText().toString());
                editLabelDialog.cancel();
            }
            return false;
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(0, 0);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialogView.findViewById(R.id.button_cancel).setOnClickListener((v) -> {
            dialog.cancel();
        });
        dialogView.findViewById(R.id.button_ok).setOnClickListener((v) -> {
            labelEdit.setText(labelEditD.getText().toString());
            dialog.cancel();
        });
        return dialog;
    }

    private void setListeners() {
        saveBtn.setOnClickListener(this);
        repeatCheck.setOnCheckedChangeListener(this);
        locationButton.setOnClickListener(this);
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            updateAlarm();
            updateTimeView();
        });
        labelEdit.setOnClickListener(v -> {
            editLabelDialog = createLabelEditDialog();
            editLabelDialog.show();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        });
        radioGroupSunMode.setOnCheckedChangeListener((id) -> {
            //Log.d("update", "" + id);
            updateAlarm();
            updateTimeView();
        });

        delayBar.setOnSeekBarChangeListener(this);

        for (int i = 0; i < 7; i++) {
            checkDays[i].setOnCheckedChangeListener((view, checked) -> {
                updateAlarm();
                updateTimeView();
                updateLocationViews();
            });
        }
        updateCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ((SunAlarm) currentAlarm).setUpdate(isChecked);
            if (isChecked) {
                defineCurrentLocation();
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.repeatCheck) {
            findViewById(R.id.view_week).setVisibility(b ? View.VISIBLE : View.GONE);
            updateAlarm();
            updateTimeView();
            updateLocationViews();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.timeView) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view1, hourOfDay, minute) ->
                    {
                        currentAlarm.setTime(hourOfDay, minute);
                        timeView.setText(currentAlarm.toString());
                    },
                    currentAlarm.getTime().get(Calendar.HOUR_OF_DAY),
                    currentAlarm.getTime().get(Calendar.MINUTE), true);
            timePickerDialog.show();
        } else if (view.getId() == saveBtn.getId()) {
            save();
            setResult(RESULT_NEW_ALARM);
            finish();
        } else if (view.getId() == R.id.button_location) {
            chooseLocation();
        }
    }

    private void save() {
        updateAlarm();
        currentAlarm.setEnable(true);
        SunAlarmManager.getService(this).set(currentAlarm);
        //MainActivity.addAlarm(currentAlarm);
        preferences.writeAlarm(currentAlarm);
    }

    private void chooseLocation() {
        Intent mapIntent = new Intent("android.intent.action.SET_LOCATION");
        mapIntent.putExtra(LATITUDE, ((SunAlarm) currentAlarm).getLatitude());
        mapIntent.putExtra(LONGITUDE, ((SunAlarm) currentAlarm).getLongitude());
        this.startActivityForResult(mapIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_LOCATION_CHOSEN) {
            latitude = data.getDoubleExtra(LATITUDE, 0);
            longitude = data.getDoubleExtra(LONGITUDE, 0);
            cityName = defineCityName(latitude, longitude);
            updateAlarm();
            updateLocationViews();
            updateTimeView();
            //Toast.makeText(getApplicationContext(), lat + " " + lon, Toast.LENGTH_LONG).show();
        }
    }

    private void updateLocationViews() {
        if (alarmType == SUN_TYPE) {
            SunAlarm sunAlarm = (SunAlarm) currentAlarm;
            cityName = defineCityName(latitude, longitude);
            locationView.setText("Location: " + cityName + " "
                    + SunInfo.toLocationString(latitude, longitude, 5));
            SunInfo sunInfo = new SunInfo(currentAlarm.getTime(), latitude, longitude);
            sunInfo = SunInfo.nextDaySunInfo(sunInfo, -1);
            if (!SunInfo.afterNow(sunInfo, SunInfo.SUNSET_MODE))
                sunInfo = SunInfo.nextDaySunInfo(sunInfo, 1);
            radioSunset.setSubText(sunInfo.toDateString());
            radioSunset.setText(SunInfo.toTimeString(sunInfo.getSunsetLocalTime(), SunInfo.HH_MM));

            if (!SunInfo.afterNow(sunInfo, SunInfo.NOON_MODE))
                sunInfo = SunInfo.nextDaySunInfo(sunInfo, 1);
            radioNoon.setSubText(sunInfo.toDateString());
            radioNoon.setText(SunInfo.toTimeString(sunInfo.getNoonLocalTime(), SunInfo.HH_MM));

            if (!SunInfo.afterNow(sunInfo, SunInfo.SUNRISE_MODE))
                sunInfo = SunInfo.nextDaySunInfo(sunInfo, 1);
            radioSunrise.setText(SunInfo.toTimeString(sunInfo.getSunriseLocalTime(), SunInfo.HH_MM));
            radioSunrise.setSubText(sunInfo.toDateString());
            delayBar.setValue(sunAlarm.getDelay());
            updateDelayView();
        }
    }

    public void setLocation() {
        SunAlarm sunAlarm = (SunAlarm) currentAlarm;

    }

    private void updateAlarm() {
        if (alarmType == SUN_TYPE) {
            SunAlarm sunAlarm = (SunAlarm) currentAlarm;
            if (radioSunrise.isChecked()) sunAlarm.setSunMode(SunAlarm.MODE_SUNRISE);
            else if (radioNoon.isChecked()) sunAlarm.setSunMode(SunAlarm.MODE_NOON);
            else if (radioSunset.isChecked()) sunAlarm.setSunMode(SunAlarm.MODE_SUNSET);
            sunAlarm.setPosition(latitude, longitude);
            sunAlarm.setDelay(delayBar.getValue());
            sunAlarm.setUpdate(updateCheck.isChecked());
            sunAlarm.setCity(cityName);
        } else {
            currentAlarm.setTime(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        }
        currentAlarm.setRepeat(repeatCheck.isChecked());
        boolean[] days = new boolean[7];
        for (int i = 0; i < 7; i++) {
            days[i] = checkDays[i].isChecked();
        }
        currentAlarm.setDays(days);
        currentAlarm.setTimeNext(true);
        currentAlarm.setLabel(labelEdit.getText().toString());
        //Log.d("update", "update");
    }

    private void updateTimeView() {
        timeView.setText(currentAlarm.toTime());
        dateView.setText(currentAlarm.toDate() + ", " + Alarm.toDay(currentAlarm.getTime(), Alarm.FULL));
    }

    private void updateDelayView() {
        String delayString = getResources().getString(R.string.delay) + "   ";
        int delay = delayBar.getValue();
        delayView.setText(delayString + (delay < 0 ? "" : "+") + delay + "  minutes");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateDelayView();
        ((SunAlarm) currentAlarm).setDelay(delayBar.getValue());
        updateTimeView();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        updateAlarm();
        updateTimeView();
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 12);

            }
            Toast.makeText(this, "Location cannot be determined! Please set location manually", Toast.LENGTH_LONG);
            return;
        }
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            Location location = task.getResult();
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                cityName = defineCityName(latitude, longitude);
                updateAlarm();
                updateLocationViews();
                updateTimeView();
            } else {
                Toast.makeText(this, "Location cannot be determined! Please set location manually", Toast.LENGTH_LONG);
            }
        });
    }

    private String defineCityName(double lat, double lon) {
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(lat, lon, 1);
            if (addresses.size() > 0) {
                if (addresses.get(0).getLocality() != null) return addresses.get(0).getLocality();
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}