package com.siarhei.alarmus.activities;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
            if (sunAlarm.isUpdate())
                SetLocationActivity.defineCurrentLocation(this, (code, location) -> {
                    if (code == SetLocationActivity.CODE_SUCCESS) updateLocation(location);
                    else
                        Toast.makeText(this, R.string.message_location_cannot, Toast.LENGTH_SHORT).show();
                });
            updateLocationViews();
            updateDelayView();
        } else {
            timePicker.setCurrentHour(currentAlarm.getHour());
            timePicker.setCurrentMinute(currentAlarm.getMinute());
        }
        labelEdit.setText(currentAlarm.getLabel());
    }

    private void updateLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        cityName = defineCityName(latitude, longitude);
        updateAlarm();
        updateLocationViews();
        updateTimeView();
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
                updateRadioViews();
                updateTimeView();
            });
        }
        updateCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ((SunAlarm) currentAlarm).setUpdate(isChecked);
            if (isChecked) {
                SetLocationActivity.defineCurrentLocation(this, (code, location) -> {
                    if (code == SetLocationActivity.CODE_SUCCESS) updateLocation(location);
                    else
                        Toast.makeText(this, R.string.message_location_cannot, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.repeatCheck) {
            findViewById(R.id.view_week).setVisibility(b ? View.VISIBLE : View.GONE);
            updateAlarm();
            updateRadioViews();
            updateTimeView();
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
        Toast.makeText(this, this.getResources().getString(R.string.message_alarm_set)
                + " " + currentAlarm.toString(), Toast.LENGTH_SHORT).show();
    }

    private void chooseLocation() {
        Intent mapIntent = new Intent("android.intent.action.SET_LOCATION");
        mapIntent.putExtra(LATITUDE, ((SunAlarm) currentAlarm).getLatitude());
        mapIntent.putExtra(LONGITUDE, ((SunAlarm) currentAlarm).getLongitude());
        mapIntent.putExtra(SetLocationActivity.MODE_MAP, 1);
        this.startActivityForResult(mapIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_LOCATION_CHOSEN) {
            latitude = data.getDoubleExtra(LATITUDE, 0);
            longitude = data.getDoubleExtra(LONGITUDE, 0);
            //cityName = defineCityName(latitude, longitude);
            updateAlarm();
            updateLocationViews();
            updateTimeView();
            //Toast.makeText(getApplicationContext(), lat + " " + lon, Toast.LENGTH_LONG).show();
        }
    }

    private void updateRadioViews() {

        SunAlarm sunAlarm = new SunAlarm(0);
        sunAlarm.setPosition(latitude, longitude);
        boolean[] days = new boolean[7];
        sunAlarm.setRepeat(repeatCheck.isChecked());
        sunAlarm.setDelay(0);
        if (repeatCheck.isChecked()) {
            for (int i = 0; i < 7; i++) {
                days[i] = checkDays[i].isChecked();
            }
            sunAlarm.setDays(days);
        }
        sunAlarm.setTimeNext(true);
        radioSunrise.setText(sunAlarm.toTime());
        radioSunrise.setSubText(sunAlarm.toDate());
        sunAlarm.setSunMode(SunAlarm.MODE_NOON);
        sunAlarm.setTimeNext(true);
        radioNoon.setText(sunAlarm.toTime());
        radioNoon.setSubText(sunAlarm.toDate());
        sunAlarm.setSunMode(SunAlarm.MODE_SUNSET);
        sunAlarm.setTimeNext(true);
        radioSunset.setText(sunAlarm.toTime());
        radioSunset.setSubText(sunAlarm.toDate());
        Log.d("updateRadioViews", sunAlarm.toDate());
    }

    private void updateLocationViews() {
        if (alarmType == SUN_TYPE) {
            SunAlarm sunAlarm = (SunAlarm) currentAlarm;
            //cityName = defineCityName(latitude, longitude);
            if (cityName == null || cityName == "")
                locationView.setText("Location: " + SunInfo.toLocationString(latitude, longitude, 5));
            else
                locationView.setText("Location: " + cityName);
            updateRadioViews();
            delayBar.setValue(sunAlarm.getDelay());
            //updateDelayView();
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
        delayView.setText(delayString + (delay < 0 ? "" : "+") + delay + " "
                + getResources().getString(R.string.minutes));
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
            //e.printStackTrace();
        }
        return "";
    }
}