package com.siarhei.alarmus.activities;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.siarhei.alarmus.data.AlarmData;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.R;
import com.siarhei.alarmus.sun.SunInfo;

import org.osmdroid.util.GeoPoint;

import java.util.Calendar;

public class AddAlarmActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    public static final String APP_PREFERENCES = "my_settings";
    public static final int RESULT_LOCATION_CHOSEN = 2;
    public static final int RESULT_NEW_ALARM = 1;
    public static final int RESULT_EDIT_ALARM = 4;
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String ALARM_NAME = "alarm";
    public static final String ID = "id";
    private Switch sunModeSwitch;
    private TextView timeView;
    private TextView locationView;
    private Switch alarmSwitch;
    private AlarmData currentAlarm;
    private TimePicker timePicker;
    public AlarmPreferences preferences;
    private ImageButton plusBtn;
    private ImageButton minusBtn;
    private ImageButton saveBtn;
    private CheckBox onceChk;
    private RadioGroup radioSunMode;
    private RadioButton radioSunrise;
    private RadioButton radioSunset;
    private double lat;
    private double lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.add_alarm_toolbar);
        setSupportActionBar(toolbar);

        alarmSwitch = (Switch) findViewById(R.id.alarmSwitch);
        sunModeSwitch = (Switch) findViewById(R.id.sunModeSwitch);
        timeView = (TextView) findViewById(R.id.timeView);
        locationView = (TextView) findViewById(R.id.text_location);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        plusBtn = (ImageButton) findViewById(R.id.timePlus);
        minusBtn = (ImageButton) findViewById(R.id.timeMinus);
        onceChk = (CheckBox) findViewById(R.id.onceCheck);
        saveBtn = (ImageButton) findViewById(R.id.save_button);
        radioSunMode = (RadioGroup) findViewById(R.id.radioSunMode);
        radioSunrise = (RadioButton) findViewById(R.id.radioSunrise);
        radioSunset = (RadioButton) findViewById(R.id.radioSunset);

        timePicker.setIs24HourView(true);
        plusBtn.setOnClickListener(this);
        minusBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        locationView.setOnClickListener(this);

        preferences = AlarmPreferences.getInstance(this);
        currentAlarm = (AlarmData) getIntent().getParcelableExtra(MainActivity.ALARM_EDITING);
        Toast.makeText(getApplicationContext(), currentAlarm.getId() + "",
                Toast.LENGTH_SHORT).show();
      /*  if (currentAlarm == null) {
            currentAlarm = new AlarmData();
        }*/
        updateView();

        sunModeSwitch.setOnCheckedChangeListener(this);
        //alarmSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == sunModeSwitch.getId()) {
            currentAlarm.setSunMode(b ? AlarmData.MODE_SUNRISE : 0);
            if (b) {

            }
            //radioSunMode.setVisibility(currentAlarm.getSunMode() != 0 ? View.VISIBLE : View.GONE);
            radioSunrise.setEnabled(currentAlarm.getSunMode() != 0);
            radioSunset.setEnabled(currentAlarm.getSunMode() != 0);
            locationView.setEnabled(currentAlarm.getSunMode() != 0);
            timePicker.setVisibility((currentAlarm.getSunMode() == 0 ? View.VISIBLE : View.GONE));
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.timeView) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (TimePickerDialog.OnTimeSetListener) (view1, hourOfDay, minute) ->
                    {
                        currentAlarm.setTime(hourOfDay, minute);
                        timeView.setText(currentAlarm.toString());
                    },
                    currentAlarm.getTime().get(Calendar.HOUR_OF_DAY),
                    currentAlarm.getTime().get(Calendar.MINUTE), true);
            timePickerDialog.show();
        } else if (view.getId() == R.id.timeMinus) {
            timePicker.setCurrentMinute(timePicker.getCurrentMinute() - 1);
            if (timePicker.getCurrentMinute() == 59) {
                timePicker.setCurrentHour(timePicker.getCurrentHour() - 1);
            }
        } else if (view.getId() == R.id.timePlus) {
            timePicker.setCurrentMinute(timePicker.getCurrentMinute() + 1);
            if (timePicker.getCurrentMinute() == 0) {
                timePicker.setCurrentHour(timePicker.getCurrentHour() + 1);
            }
        } else if (view.getId() == saveBtn.getId()) {
            save();
            setResult(RESULT_NEW_ALARM);
            finish();
        } else if (view.getId() == R.id.text_location) {
            Intent mapIntent = new Intent("android.intent.action.SET_LOCATION");
            this.startActivityForResult(mapIntent, 1);
        }
    }

    private void save() {
        currentAlarm.setEnable(alarmSwitch.isChecked());
        currentAlarm.setTime(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        currentAlarm.setOnce(onceChk.isChecked());
        if (sunModeSwitch.isChecked()) {
            currentAlarm.setSunMode(
                    radioSunrise.isChecked() ? AlarmData.MODE_SUNRISE : AlarmData.MODE_SUNSET);
            currentAlarm.setPosition(lat, lon);
        } else currentAlarm.setSunMode(0);

        currentAlarm.cancelAlarm(this);
        if (currentAlarm.isEnabled()) currentAlarm.setAlarm(this);
        preferences.writeAlarm(currentAlarm);
        //Toast.makeText(this, "Будильник!!!", Toast.LENGTH_SHORT).show();
    }

    private GeoPoint chooseLocation() {
        Intent mapIntent = new Intent("android.intent.category.LOCATION_CHOOSE");
        this.startActivityForResult(mapIntent, 1);
        double longitude = mapIntent.getIntExtra("longitude", 0);
        double latitude = mapIntent.getIntExtra("latitude", 0);
        return new GeoPoint(latitude, longitude);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_LOCATION_CHOSEN) {
            //alarmSwitch.setChecked(false);
            lat = data.getDoubleExtra(LATITUDE, 0);
            lon = data.getDoubleExtra(LONGITUDE, 0);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            SunInfo sunInfo = new SunInfo(day, month, year, lat, lon);
            // timeView.setText(alarm.toString());
            timePicker.setCurrentHour((int) sunInfo.getSunriseLocalTime());
            timePicker.setCurrentMinute((int) (60 * (sunInfo.getSunriseLocalTime() % 1)));
            Toast.makeText(getApplicationContext(), lat + " " + lon, Toast.LENGTH_LONG).show();

        } else {
            //timeView.setText(resultCode+"");
        }

    }

    @Override
    public void onResume() {

        super.onResume();
        //updateView();
    }

    private void updateView() {
        radioSunrise.setEnabled(currentAlarm.getSunMode() != 0);
        radioSunset.setEnabled(currentAlarm.getSunMode() != 0);
        locationView.setEnabled(currentAlarm.getSunMode() != 0);
        radioSunrise.setChecked(currentAlarm.getSunMode() == AlarmData.MODE_SUNRISE);
        radioSunset.setChecked(currentAlarm.getSunMode() == AlarmData.MODE_SUNSET);
        sunModeSwitch.setChecked(currentAlarm.getSunMode() != 0);
        alarmSwitch.setChecked(currentAlarm.isEnabled());
        onceChk.setChecked(currentAlarm.isOnce());
        timePicker.setVisibility((currentAlarm.getSunMode() == 0 ? View.VISIBLE : View.GONE));
        timePicker.setCurrentHour(currentAlarm.getHour());
        timePicker.setCurrentMinute(currentAlarm.getMinute());
        timeView.setText(currentAlarm.toString());
    }

}