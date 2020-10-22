package com.siarhei.alarmus.activities;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import com.siarhei.alarmus.R;
import com.siarhei.alarmus.data.Alarm;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.data.SunAlarm;
import com.siarhei.alarmus.sun.SunInfo;
import com.siarhei.alarmus.sun.SunMath;
import com.siarhei.alarmus.views.CircleCheckBox;
import com.siarhei.alarmus.views.DelayPicker;

import java.util.Calendar;

public class EditAlarmActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    public static final String APP_PREFERENCES = "my_settings";
    public static final int RESULT_LOCATION_CHOSEN = 2;
    public static final int RESULT_NEW_ALARM = 1;
    public static final int RESULT_EDIT_ALARM = 4;
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String ALARM_NAME = "alarm";
    public static final String ID = "id";
    private TextView timeView;
    private TextView locationView;
    private Switch alarmSwitch;
    private Alarm currentAlarm;
    private TimePicker timePicker;
    private AlarmPreferences preferences;
    private ImageButton plusBtn;
    private ImageButton minusBtn;
    private ImageButton saveBtn;
    private CheckBox repeatChk;
    private RadioGroup radioSunMode;
    private RadioButton radioSunrise, radioSunset, radioNoon;
    private DelayPicker delayPicker;
    private int alarmType;
    private CircleCheckBox[] checkDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.add_alarm_toolbar);
        setSupportActionBar(toolbar);

        alarmSwitch = (Switch) findViewById(R.id.alarmSwitch);
        timeView = (TextView) findViewById(R.id.timeView);
        locationView = (TextView) findViewById(R.id.btn_location);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        repeatChk = (CheckBox) findViewById(R.id.repeatCheck);
        saveBtn = (ImageButton) findViewById(R.id.save_button);
        radioSunMode = (RadioGroup) findViewById(R.id.radioSunMode);
        radioSunrise = (RadioButton) findViewById(R.id.radioSunrise);
        radioNoon = (RadioButton) findViewById(R.id.radioNoon);
        radioSunset = (RadioButton) findViewById(R.id.radioSunset);
        delayPicker = (DelayPicker) findViewById(R.id.delay_picker);
        checkDays = new CircleCheckBox[]{findViewById(R.id.check_day1),
                findViewById(R.id.check_day2), findViewById(R.id.check_day3),
                findViewById(R.id.check_day4), findViewById(R.id.check_day5),
                findViewById(R.id.check_day6), findViewById(R.id.check_day7)};
        timePicker.setIs24HourView(true);
        //plusBtn.setOnClickListener(this);
        //minusBtn.setOnClickListener(this);
        repeatChk.setOnCheckedChangeListener(this);
        saveBtn.setOnClickListener(this);
        locationView.setOnClickListener(this);

        preferences = AlarmPreferences.getInstance(this);
        currentAlarm = getIntent().getParcelableExtra(MainActivity.ALARM_EDITING);
        alarmType = (currentAlarm instanceof SunAlarm) ?
                AlarmPreferences.SUN_TYPE : AlarmPreferences.SIMPLE_TYPE;
        alarmSwitch.setChecked(currentAlarm.isEnabled());
        repeatChk.setChecked(currentAlarm.isRepeat());
        setDays();
        findViewById(R.id.view_week).setVisibility(currentAlarm.isRepeat() ? View.VISIBLE : View.GONE);
        if (alarmType == AlarmPreferences.SUN_TYPE) {
            SunAlarm sunAlarm = (SunAlarm) currentAlarm;
            radioSunMode.setVisibility(View.VISIBLE);
            locationView.setVisibility(View.VISIBLE);
            radioSunrise.setChecked(sunAlarm.getSunMode() == SunAlarm.MODE_SUNRISE);
            radioNoon.setChecked(sunAlarm.getSunMode() == SunAlarm.MODE_NOON);
            radioSunset.setChecked(sunAlarm.getSunMode() == SunAlarm.MODE_SUNSET);
            timePicker.setVisibility(View.GONE);
            delayPicker.setVisibility(View.VISIBLE);
            updateSunView();
        } else {
            timePicker.setCurrentHour(currentAlarm.getHour());
            timePicker.setCurrentMinute(currentAlarm.getMinute());
            timeView.setText(currentAlarm.toString());
        }

        Log.d("WEEK", (10 ^ 3) + " ");
        //alarmSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.repeatCheck) {
            findViewById(R.id.view_week).setVisibility(b ? View.VISIBLE : View.GONE);
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
        } /*else if (view.getId() == R.id.timeMinus) {
            timePicker.setCurrentMinute(timePicker.getCurrentMinute() - 1);
            if (timePicker.getCurrentMinute() == 59) {
                timePicker.setCurrentHour(timePicker.getCurrentHour() - 1);
            }
        } else if (view.getId() == R.id.timePlus) {
            timePicker.setCurrentMinute(timePicker.getCurrentMinute() + 1);
            if (timePicker.getCurrentMinute() == 0) {
                timePicker.setCurrentHour(timePicker.getCurrentHour() + 1);
            }
        }*/ else if (view.getId() == saveBtn.getId()) {
            save();
            setResult(RESULT_NEW_ALARM);
            finish();
        } else if (view.getId() == R.id.btn_location) {
            chooseLocation();
        }
    }

    private void save() {
        currentAlarm.setEnable(alarmSwitch.isChecked());
        if (alarmType == AlarmPreferences.SUN_TYPE) {
            SunAlarm sunAlarm = (SunAlarm) currentAlarm;
            if (radioSunrise.isChecked()) sunAlarm.setSunMode(SunAlarm.MODE_SUNRISE);
            else if (radioNoon.isChecked()) sunAlarm.setSunMode(SunAlarm.MODE_NOON);
            else if (radioSunset.isChecked()) sunAlarm.setSunMode(SunAlarm.MODE_SUNSET);
        } else {
            currentAlarm.setTime(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        }
        currentAlarm.setTimeNext();
        currentAlarm.setRepeat(repeatChk.isChecked());
        boolean days[] = new boolean[7];
        for (int i = 0; i < 7; i++) {
            days[i] = checkDays[i].isChecked();
        }
        currentAlarm.setDays(days);
        currentAlarm.cancelAlarm(this);
        if (currentAlarm.isEnabled()) currentAlarm.setAlarm(this);
        preferences.writeAlarm(currentAlarm);
        //Toast.makeText(this, "Будильник!!!", Toast.LENGTH_SHORT).show();
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
            double lat = data.getDoubleExtra(LATITUDE, 0);
            double lon = data.getDoubleExtra(LONGITUDE, 0);
            ((SunAlarm) currentAlarm).setPosition(lat, lon);
            updateSunView();
            //Toast.makeText(getApplicationContext(), lat + " " + lon, Toast.LENGTH_LONG).show();
        } else {
        }
    }

    @Override
    public void onResume() {

        super.onResume();
        //updateView();
    }

    private void updateSunView() {
        SunAlarm sunAlarm = (SunAlarm) currentAlarm;
        double lat = sunAlarm.getLatitude();
        double lon = sunAlarm.getLongitude();
        locationView.setText("Location: " + SunMath.round(lat, 4) + " " + SunMath.round(lon, 4));
        SunInfo sunInfo = new SunInfo(currentAlarm.getTime(), lat, lon);
        radioSunrise.setText(getResources().getString(R.string.sunrise) + ": "
                + SunInfo.timeToString(sunInfo.getSunriseLocalTime(), SunInfo.HH_MM));
        radioNoon.setText(getResources().getString(R.string.noon) + ": "
                + SunInfo.timeToString(sunInfo.getNoonLocalTime(), SunInfo.HH_MM));
        radioSunset.setText(getResources().getString(R.string.sunset) + ": "
                + SunInfo.timeToString(sunInfo.getSunsetLocalTime(), SunInfo.HH_MM));

    }

    private void setDays() {
        for (int i = 0; i < 7; i++) {
            checkDays[i].setChecked(currentAlarm.getDays()[i]);
        }
    }

}